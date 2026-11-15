package com.example.data.firebase

import com.example.base.FirestoreEmulatorTestBase
import com.example.model.ExamDocument
import com.example.model.QuestionItem
import com.example.model.QuestionType
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExamRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createExamAndQuestions_validPayload_succeedsAndEmitsUpdates() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = ExamRepository(firestore, auth)

        val examId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createExam(
                ExamDocument(
                    title = "PAS Ganjil IPAS",
                    schoolName = "SDN 01 Nusantara",
                    subject = "IPAS",
                    gradeLevel = "Kelas 5 SD",
                    semester = "Semester 1 (Ganjil)",
                    academicYear = "2026/2027",
                    durationMinutes = 90L,
                    instructions = "Jawablah dengan teliti."
                )
            )
        }
        assertTrue(examId.isNotBlank())

        val sampleQuestions = listOf(
            QuestionItem(
                type = QuestionType.PILIHAN_GANDA.name,
                prompt = "Proses penguapan air laut oleh panas matahari disebut...",
                options = listOf("Kondensasi", "Evaporasi", "Presipitasi", "Infiltrasi"),
                correctIndices = listOf(1L),
                points = 2L
            ),
            QuestionItem(
                type = QuestionType.PILIHAN_GANDA_KOMPLEKS.name,
                prompt = "Pilih dua contoh komponen abiotik dalam ekosistem:",
                options = listOf("Cahaya matahari", "Jamur pengurai", "Air", "Belalang"),
                correctIndices = listOf(0L, 2L),
                points = 3L
            ),
            QuestionItem(
                type = QuestionType.BENAR_SALAH.name,
                prompt = "Tentukan benar atau salah pernyataan berikut:",
                statements = listOf(
                    "Bunyi dapat merambat melalui ruang hampa udara.",
                    "Cahaya merambat lurus."
                ),
                statementAnswers = listOf(false, true),
                points = 2L
            ),
            QuestionItem(
                type = QuestionType.ISIAN.name,
                prompt = "Semboyan Kerajaan Majapahit adalah ....",
                shortAnswerKey = "Bhinneka Tunggal Ika",
                points = 3L
            ),
            QuestionItem(
                type = QuestionType.ESAI.name,
                prompt = "Jelaskan perbedaan gaung dan gema!",
                essayRubric = "Gaung adalah bunyi pantul hampir bersamaan; gema terdengar jelas setelah bunyi asli.",
                points = 5L
            )
        )

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.addQuestionsBatch(examId, sampleQuestions)
        }

        val observedExams = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeExams().first { list -> list.any { it.id == examId && it.questionCount == 5L } }
        }
        val createdExam = observedExams.first { it.id == examId }
        assertEquals(5L, createdExam.questionCount)
        assertEquals(15L, createdExam.totalPoints)

        val observedQuestions = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeQuestions(examId).first { it.size == 5 }
        }
        assertEquals(5, observedQuestions.size)

        val fetchedExam = withTimeout(DEFAULT_TIMEOUT_MS) { repository.getExamById(examId) }
        assertNotNull(fetchedExam)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.updateExam(fetchedExam!!.copy(title = "PAS Ganjil IPAS Revisi"))
        }

        val updated = withTimeout(DEFAULT_TIMEOUT_MS) { repository.getExamById(examId) }
        assertEquals("PAS Ganjil IPAS Revisi", updated?.title)
    }

    @Test
    fun observeExams_unauthenticatedUser_failsFast() = runBlocking {
        auth.signOut()
        val repository = ExamRepository(firestore, auth)

        var caught: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) { repository.observeExams().first() }
        } catch (t: Throwable) {
            caught = t
        }
        assertNotNull(caught)
    }

    @Test
    fun crossUserRead_differentOwner_isDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = ExamRepository(firestore, auth)
        val examId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createExam(
                ExamDocument(
                    title = "Ujian Rahasia Alice",
                    subject = "Matematika",
                    durationMinutes = 60L
                )
            )
        }

        signInTestUser(BOB_EMAIL)
        var denied: FirebaseFirestoreException? = null
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document(aliceUid)
                    .collection("exams").document(examId)
                    .get().await()
            }
        } catch (t: Throwable) {
            denied = t.unwrapFirestoreException()
        }
        assertNotNull(denied)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, denied?.code)
    }

    private companion object {
        const val ALICE_EMAIL = "guru.alice@test.com"
        const val BOB_EMAIL = "guru.bob@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
