package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.print.pdf.PrintedPdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.example.model.ExamDocument
import com.example.model.QuestionItem
import com.example.model.QuestionType
import java.io.FileOutputStream

enum class ExportMode(val label: String) {
    SOAL_ONLY("Naskah Soal Siswa"),
    KUNCI_ONLY("Kunci Jawaban & Pembahasan"),
    SOAL_DAN_KUNCI("Gabungan Soal & Kunci")
}

object ExamPrintAndExportHelper {

    private val romanNumerals = listOf("I", "II", "III", "IV", "V")
    private val optionLetters = listOf("A", "B", "C", "D", "E", "F")

    fun groupedSections(questions: List<QuestionItem>): List<Pair<QuestionType, List<QuestionItem>>> {
        val orderedTypes = listOf(
            QuestionType.PILIHAN_GANDA,
            QuestionType.PILIHAN_GANDA_KOMPLEKS,
            QuestionType.BENAR_SALAH,
            QuestionType.ISIAN,
            QuestionType.ESAI
        )
        return orderedTypes.mapNotNull { qType ->
            val list = questions.filter { it.questionType == qType }
            if (list.isEmpty()) null else qType to list
        }
    }

    fun formatExamAsShareableText(
        exam: ExamDocument,
        questions: List<QuestionItem>,
        mode: ExportMode
    ): String = buildString {
        if (mode == ExportMode.SOAL_ONLY || mode == ExportMode.SOAL_DAN_KUNCI) {
            if (exam.schoolName.isNotBlank()) appendLine(exam.schoolName.uppercase())
            appendLine(exam.title.uppercase())
            appendLine("Mata Pelajaran : ${exam.subject}")
            appendLine("Kelas / Sem.   : ${exam.gradeLevel} / ${exam.semester}")
            appendLine("Tahun Ajaran   : ${exam.academicYear} | Waktu: ${exam.durationMinutes} Menit")
            appendLine("=".repeat(44))
            appendLine("Nama Siswa : _______________________  No. Absen : _____")
            if (exam.instructions.isNotBlank()) {
                appendLine()
                appendLine("PETUNJUK UMUM:")
                appendLine(exam.instructions)
            }
            appendLine()

            val sections = groupedSections(questions)
            var runningNumber = 1
            sections.forEachIndexed { secIdx, (type, items) ->
                val roman = romanNumerals.getOrElse(secIdx) { "${secIdx + 1}" }
                appendLine("$roman. ${type.sectionTitle}")
                appendLine("-".repeat(36))
                items.forEach { q ->
                    appendLine("$runningNumber. ${q.prompt} (${q.points} poin)")
                    when (q.questionType) {
                        QuestionType.PILIHAN_GANDA -> {
                            q.options.forEachIndexed { optIdx, opt ->
                                val letter = optionLetters.getOrElse(optIdx) { "${optIdx + 1}" }
                                appendLine("   $letter. $opt")
                            }
                        }
                        QuestionType.PILIHAN_GANDA_KOMPLEKS -> {
                            q.options.forEachIndexed { optIdx, opt ->
                                val letter = optionLetters.getOrElse(optIdx) { "${optIdx + 1}" }
                                appendLine("   [ ] $letter. $opt")
                            }
                        }
                        QuestionType.BENAR_SALAH -> {
                            q.statements.forEachIndexed { stIdx, st ->
                                appendLine("   ${stIdx + 1}) $st  [ B / S ]")
                            }
                        }
                        QuestionType.ISIAN -> {
                            appendLine("   Jawaban: .......................................")
                        }
                        QuestionType.ESAI -> {
                            appendLine("   Jawaban:")
                            appendLine("   _____________________________________________")
                            appendLine("   _____________________________________________")
                        }
                    }
                    appendLine()
                    runningNumber++
                }
            }
        }

        if (mode == ExportMode.KUNCI_ONLY || mode == ExportMode.SOAL_DAN_KUNCI) {
            if (mode == ExportMode.SOAL_DAN_KUNCI) {
                appendLine()
                appendLine("=".repeat(44))
            }
            appendLine("KUNCI JAWABAN & PEDOMAN PENSKORAN")
            appendLine("${exam.title} - ${exam.subject} (${exam.gradeLevel})")
            appendLine("Total Soal: ${questions.size} | Total Skor Maksimal: ${questions.sumOf { it.points }}")
            appendLine("-".repeat(44))

            val sections = groupedSections(questions)
            var runningNumber = 1
            sections.forEachIndexed { secIdx, (type, items) ->
                val roman = romanNumerals.getOrElse(secIdx) { "${secIdx + 1}" }
                appendLine()
                appendLine("$roman. ${type.sectionTitle}")
                items.forEach { q ->
                    appendLine("$runningNumber. [${q.points} poin] Kunci: ${q.formatAnswerKey()}")
                    if (q.explanation.isNotBlank()) {
                        appendLine("   Pembahasan: ${q.explanation}")
                    }
                    runningNumber++
                }
            }
        }
    }

    fun printOrSavePdf(
        context: Context,
        exam: ExamDocument,
        questions: List<QuestionItem>,
        mode: ExportMode
    ) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val jobName = "${exam.title}_${exam.subject}_${mode.label}".replace(" ", "_")
        val adapter = ExamPrintAdapter(context, exam, questions, mode)
        val attributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .build()
        printManager.print(jobName, adapter, attributes)
    }

    private class ExamPrintAdapter(
        private val context: Context,
        private val exam: ExamDocument,
        private val questions: List<QuestionItem>,
        private val mode: ExportMode
    ) : PrintDocumentAdapter() {

        private var pdfDocument: PrintedPdfDocument? = null
        private val blocks = mutableListOf<PrintBlock>()
        private var pages = mutableListOf<List<PrintBlock>>()

        override fun onLayout(
            oldAttributes: PrintAttributes?,
            newAttributes: PrintAttributes,
            cancellationSignal: CancellationSignal?,
            callback: LayoutResultCallback,
            extras: Bundle?
        ) {
            pdfDocument = PrintedPdfDocument(context, newAttributes)
            if (cancellationSignal?.isCanceled == true) {
                callback.onLayoutCancelled()
                return
            }

            val pageWidth = 595 // A4 points
            val pageHeight = 842
            val marginX = 44f
            val marginTop = 44f
            val marginBottom = 46f
            val contentWidth = (pageWidth - marginX * 2).toInt()

            blocks.clear()
            buildBlocks(contentWidth)

            pages = paginateBlocks(blocks, pageHeight - marginTop - marginBottom)
            if (pages.isEmpty()) pages.add(emptyList())

            val info = PrintDocumentInfo.Builder("${exam.subject}_${mode.name}.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(pages.size)
                .build()
            callback.onLayoutFinished(info, true)
        }

        override fun onWrite(
            pageRanges: Array<out PageRange>,
            destination: ParcelFileDescriptor,
            cancellationSignal: CancellationSignal?,
            callback: WriteResultCallback
        ) {
            val doc = pdfDocument ?: return
            try {
                for (pageIdx in pages.indices) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback.onWriteCancelled()
                        doc.close()
                        pdfDocument = null
                        return
                    }
                    val page = doc.startPage(pageIdx)
                    val canvas = page.canvas
                    drawPage(canvas, pages[pageIdx], pageIdx + 1, pages.size)
                    doc.finishPage(page)
                }
                FileOutputStream(destination.fileDescriptor).use { out ->
                    doc.writeTo(out)
                }
                callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            } catch (e: Exception) {
                callback.onWriteFailed(e.localizedMessage)
            } finally {
                doc.close()
                pdfDocument = null
            }
        }

        private fun buildBlocks(contentWidth: Int) {
            val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val headerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.DKGRAY
                textSize = 10f
                typeface = Typeface.DEFAULT
            }
            val subtlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.GRAY
                textSize = 9.2f
                typeface = Typeface.DEFAULT
            }

            fun addTextBlock(text: String, paint: TextPaint, indent: Float = 0f, spaceAfter: Float = 6f) {
                val width = (contentWidth - indent.toInt()).coerceAtLeast(100)
                val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(2f, 1.15f)
                    .build()
                blocks.add(PrintBlock.Text(layout, indent, spaceAfter))
            }

            if (mode == ExportMode.SOAL_ONLY || mode == ExportMode.SOAL_DAN_KUNCI) {
                if (exam.schoolName.isNotBlank()) {
                    addTextBlock(exam.schoolName.uppercase(), titlePaint, spaceAfter = 2f)
                }
                addTextBlock(exam.title.uppercase(), titlePaint, spaceAfter = 4f)
                addTextBlock(
                    "Mata Pelajaran: ${exam.subject}   |   Kelas: ${exam.gradeLevel}   |   ${exam.semester}",
                    bodyPaint,
                    spaceAfter = 2f
                )
                addTextBlock(
                    "Tahun Ajaran: ${exam.academicYear}   |   Alokasi Waktu: ${exam.durationMinutes} Menit",
                    bodyPaint,
                    spaceAfter = 6f
                )
                blocks.add(PrintBlock.Divider(1.5f, 8f))
                addTextBlock("Nama Siswa: ....................................................    No. Absen: ............", bodyPaint, spaceAfter = 6f)
                if (exam.instructions.isNotBlank()) {
                    addTextBlock("Petunjuk Umum: ${exam.instructions}", subtlePaint, spaceAfter = 10f)
                }

                val sections = groupedSections(questions)
                var runningNo = 1
                sections.forEachIndexed { secIdx, (type, items) ->
                    val roman = romanNumerals.getOrElse(secIdx) { "${secIdx + 1}" }
                    addTextBlock("$roman. ${type.sectionTitle}", headerPaint, spaceAfter = 6f)
                    items.forEach { q ->
                        addTextBlock("$runningNo. ${q.prompt}  [${q.points} poin]", bodyPaint, spaceAfter = 4f)
                        when (q.questionType) {
                            QuestionType.PILIHAN_GANDA -> {
                                q.options.forEachIndexed { idx, opt ->
                                    val letter = optionLetters.getOrElse(idx) { "${idx + 1}" }
                                    addTextBlock("$letter. $opt", bodyPaint, indent = 18f, spaceAfter = 2f)
                                }
                            }
                            QuestionType.PILIHAN_GANDA_KOMPLEKS -> {
                                q.options.forEachIndexed { idx, opt ->
                                    val letter = optionLetters.getOrElse(idx) { "${idx + 1}" }
                                    addTextBlock("[   ] $letter. $opt", bodyPaint, indent = 18f, spaceAfter = 2f)
                                }
                            }
                            QuestionType.BENAR_SALAH -> {
                                q.statements.forEachIndexed { idx, st ->
                                    addTextBlock("${idx + 1}) $st   [ Benar / Salah ]", bodyPaint, indent = 18f, spaceAfter = 2f)
                                }
                            }
                            QuestionType.ISIAN -> {
                                addTextBlock("Jawaban: ........................................................................", bodyPaint, indent = 18f, spaceAfter = 4f)
                            }
                            QuestionType.ESAI -> {
                                addTextBlock("Jawab: ______________________________________________________________________", subtlePaint, indent = 18f, spaceAfter = 4f)
                                addTextBlock("       ______________________________________________________________________", subtlePaint, indent = 18f, spaceAfter = 4f)
                            }
                        }
                        blocks.add(PrintBlock.Spacer(6f))
                        runningNo++
                    }
                    blocks.add(PrintBlock.Spacer(8f))
                }
            }

            if (mode == ExportMode.KUNCI_ONLY || mode == ExportMode.SOAL_DAN_KUNCI) {
                if (mode == ExportMode.SOAL_DAN_KUNCI) {
                    blocks.add(PrintBlock.PageBreak)
                }
                addTextBlock("KUNCI JAWABAN & PEDOMAN PENSKORAN", titlePaint, spaceAfter = 4f)
                addTextBlock("${exam.title} — ${exam.subject} (${exam.gradeLevel})", bodyPaint, spaceAfter = 2f)
                addTextBlock(
                    "Jumlah Soal: ${questions.size}   |   Skor Maksimal: ${questions.sumOf { it.points }}",
                    bodyPaint,
                    spaceAfter = 8f
                )
                blocks.add(PrintBlock.Divider(1.5f, 10f))

                val sections = groupedSections(questions)
                var runningNo = 1
                sections.forEachIndexed { secIdx, (type, items) ->
                    val roman = romanNumerals.getOrElse(secIdx) { "${secIdx + 1}" }
                    addTextBlock("$roman. ${type.sectionTitle}", headerPaint, spaceAfter = 6f)
                    items.forEach { q ->
                        addTextBlock("$runningNo. [${q.points} poin] Kunci: ${q.formatAnswerKey()}", bodyPaint, spaceAfter = 2f)
                        if (q.explanation.isNotBlank()) {
                            addTextBlock("Pembahasan: ${q.explanation}", subtlePaint, indent = 14f, spaceAfter = 5f)
                        } else {
                            blocks.add(PrintBlock.Spacer(3f))
                        }
                        runningNo++
                    }
                    blocks.add(PrintBlock.Spacer(6f))
                }
            }
        }

        private fun paginateBlocks(allBlocks: List<PrintBlock>, maxHeight: Float): MutableList<List<PrintBlock>> {
            val result = mutableListOf<List<PrintBlock>>()
            var currentPage = mutableListOf<PrintBlock>()
            var currentY = 0f

            for (block in allBlocks) {
                if (block === PrintBlock.PageBreak) {
                    if (currentPage.isNotEmpty()) {
                        result.add(currentPage)
                        currentPage = mutableListOf()
                        currentY = 0f
                    }
                    continue
                }
                val h = block.height()
                if (currentY + h > maxHeight && currentPage.isNotEmpty()) {
                    result.add(currentPage)
                    currentPage = mutableListOf()
                    currentY = 0f
                }
                currentPage.add(block)
                currentY += h
            }
            if (currentPage.isNotEmpty()) {
                result.add(currentPage)
            }
            return result
        }

        private fun drawPage(canvas: Canvas, pageBlocks: List<PrintBlock>, pageNum: Int, totalPages: Int) {
            val marginX = 44f
            var y = 44f
            val linePaint = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 1f
            }
            val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.GRAY
                textSize = 8.5f
            }

            for (block in pageBlocks) {
                when (block) {
                    is PrintBlock.Text -> {
                        canvas.save()
                        canvas.translate(marginX + block.indent, y)
                        block.layout.draw(canvas)
                        canvas.restore()
                        y += block.height()
                    }
                    is PrintBlock.Divider -> {
                        linePaint.strokeWidth = block.strokeWidth
                        canvas.drawLine(marginX, y + 2f, 595f - marginX, y + 2f, linePaint)
                        y += block.height()
                    }
                    is PrintBlock.Spacer -> {
                        y += block.space
                    }
                    PrintBlock.PageBreak -> Unit
                }
            }

            canvas.drawText(
                "${exam.subject} - ${exam.title}   |   Halaman $pageNum dari $totalPages",
                marginX,
                820f,
                footerPaint
            )
        }
    }

    private sealed interface PrintBlock {
        fun height(): Float

        data class Text(
            val layout: StaticLayout,
            val indent: Float,
            val spaceAfter: Float
        ) : PrintBlock {
            override fun height(): Float = layout.height.toFloat() + spaceAfter
        }

        data class Divider(val strokeWidth: Float, val spaceAfter: Float) : PrintBlock {
            override fun height(): Float = strokeWidth + 4f + spaceAfter
        }

        data class Spacer(val space: Float) : PrintBlock {
            override fun height(): Float = space
        }

        data object PageBreak : PrintBlock {
            override fun height(): Float = 0f
        }
    }
}
