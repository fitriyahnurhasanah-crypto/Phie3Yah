package com.example.data.ai

import android.util.Log
import com.example.model.QuestionItem
import com.example.model.QuestionType
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "QuestionGenerator"
private const val AI_TIMEOUT_MS = 60_000L

data class GenerationRequest(
    val subject: String,
    val gradeLevel: String,
    val materialContext: String,
    val difficulty: String,
    val pgCount: Int,
    val pgKompleksCount: Int,
    val benarSalahCount: Int,
    val isianCount: Int,
    val esaiCount: Int,
    val attachmentBytes: ByteArray? = null,
    val attachmentMimeType: String? = null
) {
    val totalRequested: Int
        get() = pgCount + pgKompleksCount + benarSalahCount + isianCount + esaiCount
}

interface AiQuestionGenerator {
    suspend fun generateQuestions(request: GenerationRequest): Result<List<QuestionItem>>
}

class FirebaseAiQuestionGenerator : AiQuestionGenerator {

    private val questionSchema = Schema.obj(
        properties = mapOf(
            "type" to Schema.enumeration(
                values = listOf(
                    QuestionType.PILIHAN_GANDA.name,
                    QuestionType.PILIHAN_GANDA_KOMPLEKS.name,
                    QuestionType.BENAR_SALAH.name,
                    QuestionType.ISIAN.name,
                    QuestionType.ESAI.name
                ),
                description = "Jenis soal sesuai ketentuan"
            ),
            "prompt" to Schema.string(description = "Teks pertanyaan yang jelas dalam Bahasa Indonesia baku"),
            "options" to Schema.array(
                items = Schema.string(),
                description = "4 pilihan untuk PILIHAN_GANDA atau 4-5 pilihan untuk PILIHAN_GANDA_KOMPLEKS; [] untuk tipe lain"
            ),
            "correctIndices" to Schema.array(
                items = Schema.integer(),
                description = "Indeks berbasis 0 jawaban benar (1 item untuk PILIHAN_GANDA, 2-3 item untuk PILIHAN_GANDA_KOMPLEKS; [] untuk tipe lain)"
            ),
            "statements" to Schema.array(
                items = Schema.string(),
                description = "2 sampai 3 pernyataan untuk BENAR_SALAH; [] untuk tipe lain"
            ),
            "statementAnswers" to Schema.array(
                items = Schema.boolean(),
                description = "Boolean (true=Benar, false=Salah) per pernyataan untuk BENAR_SALAH; [] untuk tipe lain"
            ),
            "shortAnswerKey" to Schema.string(description = "Kunci jawaban singkat untuk ISIAN; kosong jika bukan ISIAN"),
            "essayRubric" to Schema.string(description = "Pedoman jawaban dan rubrik penskoran untuk ESAI; kosong jika bukan ESAI"),
            "explanation" to Schema.string(description = "Pembahasan singkat untuk guru/kunci jawaban"),
            "topicTag" to Schema.string(description = "Nama topik atau bab singkat"),
            "points" to Schema.integer(description = "Bobot poin soal (contoh: PG=2, PGK=3, BS=2, Isian=3, Esai=5)")
        )
    )

    private val responseSchema = Schema.obj(
        properties = mapOf(
            "questions" to Schema.array(
                items = questionSchema,
                description = "Daftar seluruh soal ujian yang dihasilkan"
            )
        )
    )

    private val model by lazy {
        val config = generationConfig {
            responseMimeType = "application/json"
            responseSchema = this@FirebaseAiQuestionGenerator.responseSchema
        }
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = config
        )
    }

    override suspend fun generateQuestions(request: GenerationRequest): Result<List<QuestionItem>> {
        if (request.totalRequested <= 0) {
            return Result.failure(IllegalArgumentException("Pilih minimal 1 soal untuk dibuat."))
        }
        if (request.attachmentBytes != null && request.attachmentBytes.size > 19 * 1024 * 1024) {
            return Result.failure(
                IllegalArgumentException("Ukuran lampiran melebihi batas 19 MB. Pilih file yang lebih ringkas.")
            )
        }

        val promptText = buildString {
            appendLine("Anda adalah guru dan penyusun naskah soal Ujian Akhir Semester (UAS/PAS) profesional di Indonesia.")
            appendLine("Buatlah naskah soal ujian yang akurat, edukatif, bebas ambigu, dan sesuai tingkat perkembangan siswa.")
            appendLine()
            appendLine("Identitas Ujian:")
            appendLine("- Mata Pelajaran: ${request.subject}")
            appendLine("- Kelas/Jenjang: ${request.gradeLevel}")
            appendLine("- Tingkat Kesulitan: ${request.difficulty}")
            appendLine()
            appendLine("Komposisi Jumlah Soal WAJIB tepat sesuai rincian berikut:")
            if (request.pgCount > 0) appendLine("- PILIHAN_GANDA: ${request.pgCount} soal (masing-masing 4 opsi jawaban A-D tanpa awalan huruf 'A. ', tepat 1 indeks benar di correctIndices)")
            if (request.pgKompleksCount > 0) appendLine("- PILIHAN_GANDA_KOMPLEKS: ${request.pgKompleksCount} soal (masing-masing 4-5 opsi tanpa awalan huruf, dan WAJIB memiliki minimal 2 indeks jawaban benar di correctIndices)")
            if (request.benarSalahCount > 0) appendLine("- BENAR_SALAH: ${request.benarSalahCount} soal (masing-masing memuat 2-3 pernyataan di 'statements' beserta array boolean 'statementAnswers' dengan panjang sama)")
            if (request.isianCount > 0) appendLine("- ISIAN: ${request.isianCount} soal (kalimat rumpang atau pertanyaan jawaban singkat dengan tanda '....' dan kunci tepat di 'shortAnswerKey')")
            if (request.esaiCount > 0) appendLine("- ESAI: ${request.esaiCount} soal (pertanyaan uraian/HOTS beserta pedoman jawaban & rubrik penskoran di 'essayRubric')")
            appendLine()
            if (request.materialContext.isNotBlank()) {
                appendLine("Materi / Kisi-Kisi Referensi:")
                appendLine(request.materialContext)
            }
            if (request.attachmentBytes != null) {
                appendLine("Gunakan isi dokumen/gambar terlampir sebagai sumber utama penyusunan soal.")
            }
            appendLine()
            appendLine("Pastikan setiap soal memiliki pembahasan singkat dan topicTag yang relevan. Jangan sertakan awalan nomor soal di field prompt.")
        }

        return try {
            val response = withTimeout(AI_TIMEOUT_MS) {
                val inputContent = content {
                    if (request.attachmentBytes != null && !request.attachmentMimeType.isNullOrBlank()) {
                        inlineData(request.attachmentBytes, request.attachmentMimeType)
                    }
                    text(promptText)
                }
                model.generateContent(inputContent)
            }

            val jsonText = response.text
                ?: return Result.failure(IllegalStateException("Model AI mengembalikan respons kosong."))

            val parsed = parseQuestionsJson(jsonText)
            if (parsed.isEmpty()) {
                Result.failure(IllegalStateException("Tidak ada soal yang berhasil diproses dari respons AI."))
            } else {
                Result.success(parsed)
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI generation timed out", e)
            Result.failure(
                IllegalStateException("Pembuatan soal melebihi batas waktu 60 detik. Coba kurangi jumlah soal per sesi.")
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI generation failed", e)
            Result.failure(
                IllegalStateException(e.localizedMessage ?: "Gagal menghasilkan soal dari AI.")
            )
        }
    }

    private fun parseQuestionsJson(jsonText: String): List<QuestionItem> {
        val root = JSONObject(jsonText)
        val array = root.optJSONArray("questions") ?: return emptyList()
        val result = mutableListOf<QuestionItem>()

        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            val type = QuestionType.fromRaw(obj.optString("type", QuestionType.PILIHAN_GANDA.name))
            val prompt = obj.optString("prompt").trim()
            if (prompt.isBlank()) continue

            val optionsJson = obj.optJSONArray("options")
            val options = if (optionsJson != null) {
                (0 until optionsJson.length()).mapNotNull { idx ->
                    optionsJson.optString(idx).trim().takeIf { it.isNotBlank() }
                }
            } else emptyList()

            val correctJson = obj.optJSONArray("correctIndices")
            val correctIndices = if (correctJson != null) {
                (0 until correctJson.length()).map { idx ->
                    correctJson.optLong(idx, 0L)
                }.filter { it in 0L until options.size.toLong().coerceAtLeast(1L) }.distinct()
            } else emptyList()

            val statementsJson = obj.optJSONArray("statements")
            val statements = if (statementsJson != null) {
                (0 until statementsJson.length()).mapNotNull { idx ->
                    statementsJson.optString(idx).trim().takeIf { it.isNotBlank() }
                }
            } else emptyList()

            val answersJson = obj.optJSONArray("statementAnswers")
            val statementAnswers = if (answersJson != null) {
                (0 until answersJson.length()).map { idx ->
                    answersJson.optBoolean(idx, true)
                }
            } else emptyList()

            val normalizedStatementAnswers = if (statements.isNotEmpty()) {
                List(statements.size) { idx -> statementAnswers.getOrElse(idx) { true } }
            } else emptyList()

            val normalizedCorrectIndices = when (type) {
                QuestionType.PILIHAN_GANDA -> {
                    if (options.isNotEmpty()) listOf(correctIndices.firstOrNull() ?: 0L) else emptyList()
                }
                QuestionType.PILIHAN_GANDA_KOMPLEKS -> {
                    if (correctIndices.isEmpty() && options.size >= 2) listOf(0L, 1L) else correctIndices
                }
                else -> emptyList()
            }

            result.add(
                QuestionItem(
                    orderIndex = i.toLong(),
                    type = type.name,
                    prompt = prompt,
                    options = if (type == QuestionType.PILIHAN_GANDA || type == QuestionType.PILIHAN_GANDA_KOMPLEKS) options else emptyList(),
                    correctIndices = normalizedCorrectIndices,
                    statements = if (type == QuestionType.BENAR_SALAH) statements else emptyList(),
                    statementAnswers = if (type == QuestionType.BENAR_SALAH) normalizedStatementAnswers else emptyList(),
                    shortAnswerKey = if (type == QuestionType.ISIAN) obj.optString("shortAnswerKey").trim() else "",
                    essayRubric = if (type == QuestionType.ESAI) obj.optString("essayRubric").trim() else "",
                    explanation = obj.optString("explanation").trim(),
                    topicTag = obj.optString("topicTag").trim(),
                    points = obj.optLong("points", type.defaultPoints).coerceIn(1L, 100L)
                )
            )
        }
        return result
    }
}
