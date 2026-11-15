package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.model.ExamDocument
import com.example.model.QuestionItem
import com.example.model.QuestionType
import com.example.ui.components.QuestionTypeBadge
import com.example.util.ExamPrintAndExportHelper
import com.example.util.ExportMode

@Composable
fun ExamPreviewAndKeyTab(
    exam: ExamDocument,
    questions: List<QuestionItem>,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var showAnswerKeys by remember { mutableStateOf(false) }
    var showPrintDialog by remember { mutableStateOf(false) }

    val romanNumerals = listOf("I", "II", "III", "IV", "V")
    val letters = listOf("A", "B", "C", "D", "E", "F")
    val grouped = remember(questions) { ExamPrintAndExportHelper.groupedSections(questions) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = !showAnswerKeys,
                    onClick = { showAnswerKeys = false },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    modifier = Modifier.testTag("preview_student_mode_button")
                ) {
                    Text("Naskah Siswa")
                }
                SegmentedButton(
                    selected = showAnswerKeys,
                    onClick = { showAnswerKeys = true },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    modifier = Modifier.testTag("preview_key_mode_button")
                ) {
                    Text("Kunci & Pembahasan")
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    enabled = questions.isNotEmpty(),
                    onClick = { showPrintDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("print_pdf_button")
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cetak / PDF")
                }

                OutlinedButton(
                    enabled = questions.isNotEmpty(),
                    onClick = {
                        val mode = if (showAnswerKeys) ExportMode.KUNCI_ONLY else ExportMode.SOAL_DAN_KUNCI
                        val text = ExamPrintAndExportHelper.formatExamAsShareableText(exam, questions, mode)
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "${exam.title} - ${exam.subject}")
                            putExtra(Intent.EXTRA_TEXT, text)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Bagikan Naskah Soal"))
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Bagikan", modifier = Modifier.size(18.dp))
                }

                OutlinedButton(
                    enabled = questions.isNotEmpty(),
                    onClick = {
                        val mode = if (showAnswerKeys) ExportMode.KUNCI_ONLY else ExportMode.SOAL_ONLY
                        val text = ExamPrintAndExportHelper.formatExamAsShareableText(exam, questions, mode)
                        clipboard.setText(AnnotatedString(text))
                        onShowMessage("Teks ${mode.label} disalin ke papan klip.")
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Salin", modifier = Modifier.size(18.dp))
                }
            }
        }

        if (questions.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Belum ada soal pada naskah ini. Tambahkan soal di tab Susun Soal terlebih dahulu.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxSize()
            ) {
                if (!showAnswerKeys) {
                    // Student Paper Kop Naskah
                    item {
                        Card(
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (exam.schoolName.isNotBlank()) {
                                    Text(
                                        text = exam.schoolName.uppercase(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                Text(
                                    text = exam.title.uppercase(),
                                    style = MaterialTheme.typography.titleLarge,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                HorizontalDivider(thickness = 1.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                                Text("Mata Pelajaran : ${exam.subject}", style = MaterialTheme.typography.bodySmall)
                                Text("Kelas / Sem.   : ${exam.gradeLevel} / ${exam.semester}", style = MaterialTheme.typography.bodySmall)
                                Text("Tahun Ajaran   : ${exam.academicYear}  •  Waktu: ${exam.durationMinutes} Menit", style = MaterialTheme.typography.bodySmall)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                Text(
                                    text = "Nama: _________________________   No. Absen: ______",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                                if (exam.instructions.isNotBlank()) {
                                    Text(
                                        text = "Petunjuk: ${exam.instructions}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    var runningNo = 1
                    grouped.forEachIndexed { secIdx, (type, itemsList) ->
                        val roman = romanNumerals.getOrElse(secIdx) { "${secIdx + 1}" }
                        val startNumber = runningNo
                        runningNo += itemsList.size

                        item(key = "sec_${type.name}") {
                            Card(
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "$roman. ${type.sectionTitle}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    HorizontalDivider()

                                    itemsList.forEachIndexed { idx, q ->
                                        val qNumber = startNumber + idx
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = "$qNumber. ${q.prompt}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                            when (q.questionType) {
                                                QuestionType.PILIHAN_GANDA -> {
                                                    q.options.forEachIndexed { optIdx, opt ->
                                                        val letter = letters.getOrElse(optIdx) { "${optIdx + 1}" }
                                                        Text(
                                                            text = "    $letter. $opt",
                                                            style = MaterialTheme.typography.bodySmall
                                                        )
                                                    }
                                                }
                                                QuestionType.PILIHAN_GANDA_KOMPLEKS -> {
                                                    q.options.forEachIndexed { optIdx, opt ->
                                                        val letter = letters.getOrElse(optIdx) { "${optIdx + 1}" }
                                                        Text(
                                                            text = "    ☐  $letter. $opt",
                                                            style = MaterialTheme.typography.bodySmall
                                                        )
                                                    }
                                                }
                                                QuestionType.BENAR_SALAH -> {
                                                    q.statements.forEachIndexed { stIdx, st ->
                                                        Text(
                                                            text = "    ${stIdx + 1}) $st   [ B / S ]",
                                                            style = MaterialTheme.typography.bodySmall
                                                        )
                                                    }
                                                }
                                                QuestionType.ISIAN -> {
                                                    Text(
                                                        text = "    Jawaban: .................................................",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                QuestionType.ESAI -> {
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                                    Spacer(modifier = Modifier.height(14.dp))
                                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Answer Key & Scoring Rubric View
                    item {
                        Card(
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Column {
                                    Text(
                                        text = "Lembar Kunci Jawaban & Pembahasan",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "Total ${questions.size} soal • Skor Maksimal ${questions.sumOf { it.points }} poin",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    var runningNo = 1
                    grouped.forEachIndexed { secIdx, (type, itemsList) ->
                        val roman = romanNumerals.getOrElse(secIdx) { "${secIdx + 1}" }
                        val startNumber = runningNo
                        runningNo += itemsList.size

                        item(key = "key_${type.name}") {
                            Card(
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "$roman. ${type.sectionTitle}",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        QuestionTypeBadge(type = type, useShortLabel = true)
                                    }
                                    HorizontalDivider()

                                    itemsList.forEachIndexed { idx, q ->
                                        val qNumber = startNumber + idx
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = "Nomor $qNumber",
                                                        style = MaterialTheme.typography.labelLarge,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Text(
                                                        text = "${q.points} poin",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = MaterialTheme.colorScheme.tertiary
                                                    )
                                                }
                                                Text(
                                                    text = "Kunci: ${q.formatAnswerKey()}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                if (q.explanation.isNotBlank()) {
                                                    Text(
                                                        text = "Pembahasan: ${q.explanation}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPrintDialog) {
        AlertDialog(
            onDismissRequest = { showPrintDialog = false },
            title = { Text("Pilih Format Cetak / PDF") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Pilih dokumen A4 yang ingin dicetak atau disimpan sebagai file PDF:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ExportMode.entries.forEach { mode ->
                        OutlinedButton(
                            onClick = {
                                showPrintDialog = false
                                ExamPrintAndExportHelper.printOrSavePdf(context, exam, questions, mode)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(mode.label)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPrintDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}
