package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.ExamRepository
import com.example.model.ExamDocument
import com.example.model.QuestionItem
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExamWorkspaceViewModel(
    private val repository: ExamRepository,
    private val examId: String
) : ViewModel() {

    val examState: StateFlow<UiState<ExamDocument?>> = repository.observeExam(examId)
        .map<ExamDocument?, UiState<ExamDocument?>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing exam $examId", rootError)
            emit(UiState.Error(rootError.localizedMessage ?: "Gagal memuat detail naskah."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val questionsState: StateFlow<UiState<List<QuestionItem>>> = repository.observeQuestions(examId)
        .map<List<QuestionItem>, UiState<List<QuestionItem>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing questions for $examId", rootError)
            emit(UiState.Error(rootError.localizedMessage ?: "Gagal memuat daftar soal."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun updateExamHeader(updated: ExamDocument, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                repository.updateExam(updated)
                _isSaving.value = false
                _statusMessage.value = "Identitas naskah diperbarui."
                onDone()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _isSaving.value = false
                Log.e(TAG, "Failed to update exam header", e)
                _statusMessage.value = e.localizedMessage ?: "Gagal menyimpan identitas naskah."
            }
        }
    }

    fun saveQuestion(question: QuestionItem, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                repository.saveQuestion(examId, question)
                _isSaving.value = false
                _statusMessage.value = if (question.id.isBlank()) "Soal ditambahkan." else "Soal diperbarui."
                onSaved()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _isSaving.value = false
                Log.e(TAG, "Failed to save question", e)
                _statusMessage.value = e.localizedMessage ?: "Gagal menyimpan soal."
            }
        }
    }

    fun addGeneratedQuestions(questions: List<QuestionItem>, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                repository.addQuestionsBatch(examId, questions)
                _isSaving.value = false
                _statusMessage.value = "${questions.size} soal AI berhasil ditambahkan."
                onDone()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _isSaving.value = false
                Log.e(TAG, "Failed to add AI questions", e)
                _statusMessage.value = e.localizedMessage ?: "Gagal menambahkan soal AI."
            }
        }
    }

    fun deleteQuestion(questionId: String) {
        viewModelScope.launch {
            try {
                repository.deleteQuestion(examId, questionId)
                _statusMessage.value = "Soal dihapus."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete question", e)
                _statusMessage.value = e.localizedMessage ?: "Gagal menghapus soal."
            }
        }
    }

    fun moveQuestion(questions: List<QuestionItem>, fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            try {
                repository.moveQuestion(examId, questions, fromIndex, toIndex)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reorder questions", e)
                _statusMessage.value = e.localizedMessage ?: "Gagal mengurutkan soal."
            }
        }
    }

    private companion object {
        const val TAG = "ExamWorkspaceVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
