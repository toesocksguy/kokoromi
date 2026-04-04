package com.kokoromi.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kokoromi.domain.usecase.CreateExperimentUseCase
import com.kokoromi.domain.model.Frequency
import com.kokoromi.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateExperimentFormState(
    val hypothesis: String = "",
    val action: String = "",
    val durationDays: Int = Constants.DEFAULT_EXPERIMENT_DURATION_DAYS,
    val why: String = "",
)

sealed interface CreateExperimentUiState {
    data object Idle : CreateExperimentUiState
    data object Loading : CreateExperimentUiState
    data object Success : CreateExperimentUiState
    data class Error(val message: String) : CreateExperimentUiState
}

@HiltViewModel
class CreateExperimentViewModel @Inject constructor(
    private val createExperiment: CreateExperimentUseCase,
) : ViewModel() {

    private val _form = MutableStateFlow(CreateExperimentFormState())
    val form: StateFlow<CreateExperimentFormState> = _form.asStateFlow()

    private val _uiState = MutableStateFlow<CreateExperimentUiState>(CreateExperimentUiState.Idle)
    val uiState: StateFlow<CreateExperimentUiState> = _uiState.asStateFlow()

    fun onHypothesisChange(value: String) {
        _form.update { it.copy(hypothesis = value.take(Constants.HYPOTHESIS_MAX_CHARS)) }
    }

    fun onActionChange(value: String) {
        _form.update { it.copy(action = value.take(Constants.ACTION_MAX_CHARS)) }
    }

    fun onDurationChange(days: Int) {
        _form.update { it.copy(durationDays = days) }
    }

    fun onWhyChange(value: String) {
        _form.update { it.copy(why = value.take(Constants.WHY_MAX_CHARS)) }
    }

    fun onSubmit() {
        val f = _form.value
        _uiState.value = CreateExperimentUiState.Loading
        viewModelScope.launch {
            createExperiment(
                hypothesis = f.hypothesis,
                action = f.action,
                why = f.why.ifBlank { null },
                durationDays = f.durationDays,
                frequency = Frequency.DAILY,
            ).onSuccess {
                _uiState.value = CreateExperimentUiState.Success
            }.onFailure { error ->
                _uiState.value = CreateExperimentUiState.Error(
                    error.message ?: "Something went wrong"
                )
            }
        }
    }

    fun onErrorDismissed() {
        _uiState.value = CreateExperimentUiState.Idle
    }
}
