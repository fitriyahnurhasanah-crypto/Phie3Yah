package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class ExamDocument(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val schoolName: String = "",
    val subject: String = "",
    val gradeLevel: String = "",
    val semester: String = "",
    val academicYear: String = "",
    val durationMinutes: Long = 90L,
    val instructions: String = "",
    val questionCount: Long = 0L,
    val totalPoints: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class QuestionItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val examId: String = "",
    val orderIndex: Long = 0L,
    val type: String = QuestionType.PILIHAN_GANDA.name,
    val prompt: String = "",
    val options: List<String> = emptyList(),
    val correctIndices: List<Long> = emptyList(),
    val statements: List<String> = emptyList(),
    val statementAnswers: List<Boolean> = emptyList(),
    val shortAnswerKey: String = "",
    val essayRubric: String = "",
    val explanation: String = "",
    val topicTag: String = "",
    val points: Long = 2L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val questionType: QuestionType
        get() = QuestionType.fromRaw(type)

    fun formatAnswerKey(): String {
        val letters = listOf("A", "B", "C", "D", "E", "F")
        return when (questionType) {
            QuestionType.PILIHAN_GANDA -> {
                val idx = correctIndices.firstOrNull()?.toInt()
                if (idx != null && idx in options.indices) {
                    "${letters.getOrElse(idx) { "${idx + 1}" }}. ${options[idx]}"
                } else {
                    "Belum dipilih"
                }
            }
            QuestionType.PILIHAN_GANDA_KOMPLEKS -> {
                if (correctIndices.isEmpty()) {
                    "Belum dipilih"
                } else {
                    correctIndices.sorted().mapNotNull { rawIdx ->
                        val i = rawIdx.toInt()
                        if (i in options.indices) {
                            "${letters.getOrElse(i) { "${i + 1}" }}. ${options[i]}"
                        } else null
                    }.joinToString(" | ")
                }
            }
            QuestionType.BENAR_SALAH -> {
                if (statements.isEmpty()) {
                    if (statementAnswers.firstOrNull() == true) "Benar" else "Salah"
                } else {
                    statements.mapIndexed { idx, _ ->
                        val ans = if (statementAnswers.getOrNull(idx) == true) "Benar" else "Salah"
                        "(${idx + 1}) $ans"
                    }.joinToString("  •  ")
                }
            }
            QuestionType.ISIAN -> shortAnswerKey.ifBlank { "Belum diisi" }
            QuestionType.ESAI -> essayRubric.ifBlank { "Belum ada pedoman jawaban" }
        }
    }
}
