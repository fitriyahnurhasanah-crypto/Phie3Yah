package com.example.data.firebase

import android.content.Context
import com.example.R
import com.example.model.ExamDocument
import com.example.model.QuestionItem
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ExamRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Pengguna harus login dengan Google sebelum mengakses Firestore.")
    }

    private fun examsCollection(uid: String) =
        db.collection("users").document(uid).collection("exams")

    private fun questionsCollection(uid: String, examId: String) =
        examsCollection(uid).document(examId).collection("questions")

    fun observeExams(): Flow<List<ExamDocument>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/exams"
        emitAll(
            examsCollection(uid)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        ExamDocument::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path, auth)
                    }
                    throw error
                }
        )
    }

    fun observeExam(examId: String): Flow<ExamDocument?> = flow {
        val uid = requireUserId()
        val docRef = examsCollection(uid).document(examId)
        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            ExamDocument::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else null
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, docRef.path, auth)
                    }
                    throw error
                }
        )
    }

    fun observeQuestions(examId: String): Flow<List<QuestionItem>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/exams/$examId/questions"
        emitAll(
            questionsCollection(uid, examId)
                .orderBy("orderIndex", Query.Direction.ASCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        QuestionItem::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path, auth)
                    }
                    throw error
                }
        )
    }

    suspend fun getExamById(examId: String): ExamDocument? {
        val uid = requireUserId()
        val docRef = examsCollection(uid).document(examId)
        return try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                snap.toObject(
                    ExamDocument::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            } else null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path, auth)
            throw e
        }
    }

    suspend fun createExam(exam: ExamDocument): String {
        val uid = requireUserId()
        val docRef = examsCollection(uid).document()
        val payload = mapOf(
            "userId" to uid,
            "title" to exam.title.trim().take(200),
            "schoolName" to exam.schoolName.trim().take(200),
            "subject" to exam.subject.trim().take(120),
            "gradeLevel" to exam.gradeLevel.trim().take(60),
            "semester" to exam.semester.trim().take(60),
            "academicYear" to exam.academicYear.trim().take(40),
            "durationMinutes" to exam.durationMinutes.coerceIn(5L, 600L),
            "instructions" to exam.instructions.trim().take(2000),
            "questionCount" to exam.questionCount.coerceAtLeast(0L),
            "totalPoints" to exam.totalPoints.coerceAtLeast(0L),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path, auth)
            throw e
        }
    }

    suspend fun updateExam(exam: ExamDocument) {
        val uid = requireUserId()
        require(exam.id.isNotBlank()) { "Exam ID tidak boleh kosong saat memperbarui naskah." }
        requireNotNull(exam.createdAt) { "Exam createdAt tidak boleh null saat memperbarui naskah." }
        val docRef = examsCollection(uid).document(exam.id)
        try {
            docRef.set(exam.copy(userId = uid, updatedAt = null)).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
            throw e
        }
    }

    suspend fun deleteExam(examId: String) {
        val uid = requireUserId()
        val examRef = examsCollection(uid).document(examId)
        try {
            val questionDocs = questionsCollection(uid, examId).get().await()
            val batch = db.batch()
            questionDocs.documents.forEach { batch.delete(it.reference) }
            batch.delete(examRef)
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, examRef.path, auth)
            throw e
        }
    }

    suspend fun saveQuestion(examId: String, question: QuestionItem): String {
        val uid = requireUserId()
        return if (question.id.isBlank()) {
            val docRef = questionsCollection(uid, examId).document()
            val payload = buildQuestionCreateMap(uid, examId, question)
            try {
                docRef.set(payload).await()
                recalculateExamSummary(uid, examId)
                docRef.id
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, docRef.path, auth)
                throw e
            }
        } else {
            requireNotNull(question.createdAt) { "Question createdAt tidak boleh null saat update." }
            val docRef = questionsCollection(uid, examId).document(question.id)
            try {
                docRef.set(
                    question.copy(
                        userId = uid,
                        examId = examId,
                        updatedAt = null
                    )
                ).await()
                recalculateExamSummary(uid, examId)
                question.id
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
                throw e
            }
        }
    }

    suspend fun addQuestionsBatch(examId: String, questions: List<QuestionItem>) {
        if (questions.isEmpty()) return
        val uid = requireUserId()
        val colRef = questionsCollection(uid, examId)
        try {
            val existingSnap = colRef.get().await()
            val startOrder = (existingSnap.documents.mapNotNull { it.getLong("orderIndex") }.maxOrNull() ?: -1L) + 1L
            val batch = db.batch()
            questions.forEachIndexed { idx, q ->
                val docRef = colRef.document()
                val payload = buildQuestionCreateMap(
                    uid = uid,
                    examId = examId,
                    question = q.copy(orderIndex = startOrder + idx)
                )
                batch.set(docRef, payload)
            }
            batch.commit().await()
            recalculateExamSummary(uid, examId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, colRef.path, auth)
            throw e
        }
    }

    suspend fun deleteQuestion(examId: String, questionId: String) {
        val uid = requireUserId()
        val docRef = questionsCollection(uid, examId).document(questionId)
        try {
            docRef.delete().await()
            recalculateExamSummary(uid, examId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path, auth)
            throw e
        }
    }

    suspend fun moveQuestion(examId: String, questions: List<QuestionItem>, fromIndex: Int, toIndex: Int) {
        if (fromIndex !in questions.indices || toIndex !in questions.indices || fromIndex == toIndex) return
        val uid = requireUserId()
        val reordered = questions.toMutableList().apply {
            val item = removeAt(fromIndex)
            add(toIndex, item)
        }
        val colRef = questionsCollection(uid, examId)
        try {
            val batch = db.batch()
            reordered.forEachIndexed { index, item ->
                if (item.id.isNotBlank() && item.orderIndex != index.toLong()) {
                    val docRef = colRef.document(item.id)
                    batch.update(
                        docRef,
                        mapOf(
                            "orderIndex" to index.toLong(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
            }
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, colRef.path, auth)
            throw e
        }
    }

    private suspend fun recalculateExamSummary(uid: String, examId: String) {
        val examRef = examsCollection(uid).document(examId)
        val snapshot = questionsCollection(uid, examId).get().await()
        val count = snapshot.size().toLong()
        val totalPts = snapshot.documents.sumOf { doc -> doc.getLong("points") ?: 0L }
        examRef.update(
            mapOf(
                "questionCount" to count,
                "totalPoints" to totalPts,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    private fun buildQuestionCreateMap(
        uid: String,
        examId: String,
        question: QuestionItem
    ): Map<String, Any> {
        return mapOf(
            "userId" to uid,
            "examId" to examId,
            "orderIndex" to question.orderIndex.coerceIn(0L, 1000L),
            "type" to question.questionType.name,
            "prompt" to question.prompt.trim().take(2000),
            "options" to question.options.take(6),
            "correctIndices" to question.correctIndices.take(6),
            "statements" to question.statements.take(6),
            "statementAnswers" to question.statementAnswers.take(6),
            "shortAnswerKey" to question.shortAnswerKey.trim().take(500),
            "essayRubric" to question.essayRubric.trim().take(3000),
            "explanation" to question.explanation.trim().take(2000),
            "topicTag" to question.topicTag.trim().take(120),
            "points" to question.points.coerceIn(1L, 100L),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }
}
