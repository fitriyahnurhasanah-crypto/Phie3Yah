package com.example.model

enum class QuestionType(
    val displayName: String,
    val shortLabel: String,
    val sectionTitle: String,
    val defaultPoints: Long,
    val description: String
) {
    PILIHAN_GANDA(
        displayName = "Pilihan Ganda",
        shortLabel = "PG",
        sectionTitle = "PILIHAN GANDA",
        defaultPoints = 2L,
        description = "Memilih 1 jawaban paling tepat (A–D/E)"
    ),
    PILIHAN_GANDA_KOMPLEKS(
        displayName = "Pilihan Ganda Kompleks",
        shortLabel = "PG Kompleks",
        sectionTitle = "PILIHAN GANDA KOMPLEKS",
        defaultPoints = 3L,
        description = "Memilih lebih dari 1 jawaban benar (kotak centang)"
    ),
    BENAR_SALAH(
        displayName = "Benar / Salah",
        shortLabel = "Benar/Salah",
        sectionTitle = "BENAR ATAU SALAH",
        defaultPoints = 2L,
        description = "Menentukan kebenaran setiap pernyataan (B / S)"
    ),
    ISIAN(
        displayName = "Isian Singkat",
        shortLabel = "Isian",
        sectionTitle = "ISIAN SINGKAT",
        defaultPoints = 3L,
        description = "Melengkapi pernyataan dengan jawaban singkat"
    ),
    ESAI(
        displayName = "Esai / Uraian",
        shortLabel = "Esai",
        sectionTitle = "URAIAN / ESAI",
        defaultPoints = 5L,
        description = "Menjawab pertanyaan secara terperinci beserta pedoman skor"
    );

    companion object {
        fun fromRaw(raw: String): QuestionType {
            return entries.firstOrNull { it.name == raw } ?: PILIHAN_GANDA
        }
    }
}
