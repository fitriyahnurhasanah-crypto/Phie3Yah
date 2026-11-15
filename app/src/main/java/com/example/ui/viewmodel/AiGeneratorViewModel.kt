package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiQuestionGenerator
import com.example.data.ai.FirebaseAiQuestionGenerator
import com.example.data.ai.GenerationRequest
import com.example.model.QuestionItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AiGeneratorState {
    data object Idle : AiGeneratorState
    data object Loading : AiGeneratorState
    data class Generated(val questions: List<QuestionItem>) : AiGeneratorState
    data class Error(val message: String) : AiGeneratorState
}

class AiGeneratorViewModel(
    private val generator: AiQuestionGenerator = FirebaseAiQuestionGenerator()
) : ViewModel() {

    private val _state = MutableStateFlow<AiGeneratorState>(AiGeneratorState.Idle)
    val state: StateFlow<AiGeneratorState> = _state.asStateFlow()

    fun generate(request: GenerationRequest) {
        viewModelScope.launch {
            _state.value = AiGeneratorState.Loading
            try {
                val result = generator.generateQuestions(request)
                result.fold(
                    onSuccess = { list ->
                        _state.value = AiGeneratorState.Generated(list)
                    },
                    onFailure = { err ->
                        _state.value = AiGeneratorState.Error(
                            err.localizedMessage ?: "Gagal menghasilkan soal AI."
                        )
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = AiGeneratorState.Error(
                    e.localizedMessage ?: "Terjadi kesalahan saat memproses AI."
                )
            }
        }
    }

    fun reset() {
        _state.value = AiGeneratorState.Idle
    }
}
