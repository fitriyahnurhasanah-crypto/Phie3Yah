package com.example

import com.example.model.CurriculumBank
import com.example.model.ExamDocument
import com.example.model.QuestionItem
import com.example.model.QuestionType
import com.example.util.ExamPrintAndExportHelper
import com.example.util.ExportMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExamModelsAndExportTest {

    @Test
    fun allFiveQuestionTypes_formatAnswerKeysCorrectly() {
        val pg = QuestionItem(
            type = QuestionType.PILIHAN_GANDA.name,
            prompt = "Ibu kota Indonesia adalah...",
            options = listOf("Bandung", "Jakarta", "Surabaya", "Medan"),
            correctIndices = listOf(1L),
            points = 2L
        )
        assertEquals("B. Jakarta", pg.formatAnswerKey())

        val pgk = QuestionItem(
            type = QuestionType.PILIHAN_GANDA_KOMPLEKS.name,
            prompt = "Pilih bilangan prima:",
            options = listOf("2", "4", "5", "9"),
            correctIndices = listOf(0L, 2L),
            points = 3L
        )
        assertEquals("A. 2 | C. 5", pgk.formatAnswerKey())

        val bs = QuestionItem(
            type = QuestionType.BENAR_SALAH.name,
            prompt = "Tentukan Benar/Salah:",
            statements = listOf("Air mendidih pada 100°C", "Matahari terbit dari barat"),
            statementAnswers = listOf(true, false),
            points = 2L
        )
        assertEquals("(1) Benar  •  (2) Salah", bs.formatAnswerKey())

        val isian = QuestionItem(
            type = QuestionType.ISIAN.name,
            prompt = "Proses pembuatan makanan pada tumbuhan disebut ....",
            shortAnswerKey = "fotosintesis",
            points = 3L
        )
        assertEquals("fotosintesis", isian.formatAnswerKey())

        val esai = QuestionItem(
            type = QuestionType.ESAI.name,
            prompt = "Jelaskan siklus air!",
            essayRubric = "Evaporasi, kondensasi, presipitasi, infiltrasi.",
            points = 5L
        )
        assertEquals("Evaporasi, kondensasi, presipitasi, infiltrasi.", esai.formatAnswerKey())
    }

    @Test
    fun curriculumBank_containsAllThreeGrade5Subjects() {
        assertEquals(listOf("Bahasa Indonesia", "IPAS", "Matematika"), CurriculumBank.subjects)
        assertEquals(23, CurriculumBank.chapters.size)
    }

    @Test
    fun shareableExport_separatesStudentAndKeyModes() {
        val exam = ExamDocument(
            title = "PAS Ganjil",
            schoolName = "SDN 01",
            subject = "IPAS",
            gradeLevel = "Kelas 5 SD",
            semester = "Semester 1",
            academicYear = "2026/2027",
            durationMinutes = 90L
        )
        val questions = listOf(
            QuestionItem(
                type = QuestionType.PILIHAN_GANDA.name,
                prompt = "Contoh produsen?",
                options = listOf("Padi", "Elang"),
                correctIndices = listOf(0L),
                points = 2L
            )
        )

        val studentOnly = ExamPrintAndExportHelper.formatExamAsShareableText(exam, questions, ExportMode.SOAL_ONLY)
        assertTrue(studentOnly.contains("I. PILIHAN GANDA"))
        assertFalse(studentOnly.contains("KUNCI JAWABAN"))

        val combined = ExamPrintAndExportHelper.formatExamAsShareableText(exam, questions, ExportMode.SOAL_DAN_KUNCI)
        assertTrue(combined.contains("I. PILIHAN GANDA"))
        assertTrue(combined.contains("KUNCI JAWABAN & PEDOMAN PENSKORAN"))
        assertTrue(combined.contains("A. Padi"))
    }
}
