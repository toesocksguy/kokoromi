package com.kokoromi.ui.reflection

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kokoromi.data.repository.ExperimentRepository
import com.kokoromi.data.repository.ReflectionRepository
import com.kokoromi.domain.usecase.SaveReflectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class ReflectionFormState(
    val plus: String = "",
    val minus: String = "",
    val next: String = "",
) {
    val isEmpty get() = plus.isBlank() && minus.isBlank() && next.isBlank()
}

sealed interface ReflectionUiState {
    data object Loading : ReflectionUiState
    data object Ready : ReflectionUiState
    data object Saved : ReflectionUiState
    data class Error(val message: String) : ReflectionUiState
}

@HiltViewModel
class ReflectionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val experimentRepository: ExperimentRepository,
    private val reflectionRepository: ReflectionRepository,
    private val saveReflection: SaveReflectionUseCase,
) : ViewModel() {

    private val experimentId: String = checkNotNull(savedStateHandle["experimentId"])

    private val _uiState = MutableStateFlow<ReflectionUiState>(ReflectionUiState.Loading)
    val uiState: StateFlow<ReflectionUiState> = _uiState.asStateFlow()

    private val _form = MutableStateFlow(ReflectionFormState())
    val form: StateFlow<ReflectionFormState> = _form.asStateFlow()

    private val _experimentName = MutableStateFlow("")
    val experimentName: StateFlow<String> = _experimentName.asStateFlow()

    val weekStart: LocalDate = LocalDate.now()
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val weekEnd: LocalDate = LocalDate.now()
        .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

    init {
        viewModelScope.launch {
            val experiment = experimentRepository.getExperiment(experimentId)
            if (experiment == null) {
                _uiState.value = ReflectionUiState.Error("Experiment not found")
                return@launch
            }
            _experimentName.value = experiment.hypothesis

            val existing = reflectionRepository.getReflectionInRange(
                experimentId = experimentId,
                startDate = weekStart,
                endDate = weekEnd,
            )
            if (existing != null) {
                _form.value = ReflectionFormState(
                    plus = existing.plus ?: "",
                    minus = existing.minus ?: "",
                    next = existing.next ?: "",
                )
            }

            _uiState.value = ReflectionUiState.Ready
        }
    }

    fun onPlusChange(value: String) = _form.update { it.copy(plus = value) }
    fun onMinusChange(value: String) = _form.update { it.copy(minus = value) }
    fun onNextChange(value: String) = _form.update { it.copy(next = value) }

    fun onSave() {
        val f = _form.value
        _uiState.value = ReflectionUiState.Loading
        viewModelScope.launch {
            saveReflection(
                experimentId = experimentId,
                reflectionDate = LocalDate.now(),
                plus = f.plus.ifBlank { null },
                minus = f.minus.ifBlank { null },
                next = f.next.ifBlank { null },
            )
                .onSuccess {
                    Timber.d("Reflection saved for experiment $experimentId")
                    _uiState.value = ReflectionUiState.Saved
                }
                .onFailure { e ->
                    Timber.e(e, "Failed to save reflection")
                    _uiState.value = ReflectionUiState.Error(e.message ?: "Failed to save reflection")
                }
        }
    }

    fun onErrorDismissed() {
        _uiState.value = ReflectionUiState.Ready
    }
}
