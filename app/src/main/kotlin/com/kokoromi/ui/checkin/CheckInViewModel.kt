package com.kokoromi.ui.checkin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kokoromi.data.repository.DailyLogRepository
import com.kokoromi.data.repository.ExperimentRepository
import com.kokoromi.domain.usecase.LogDailyCheckInUseCase
import com.kokoromi.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class CheckInFormState(
    val completed: Boolean = true,
    val mood: Int? = null,      // moodAfter; 1–5 or null
    val notes: String = "",
)

sealed interface CheckInUiState {
    data object Loading : CheckInUiState
    data class Ready(val experimentAction: String) : CheckInUiState
    data object Saved : CheckInUiState
    data class Error(val message: String, val experimentAction: String) : CheckInUiState
}

@HiltViewModel
class CheckInViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val experimentRepository: ExperimentRepository,
    private val dailyLogRepository: DailyLogRepository,
    private val logDailyCheckIn: LogDailyCheckInUseCase,
) : ViewModel() {

    private val experimentId: String = checkNotNull(savedStateHandle["experimentId"])
    private val initialCompleted: Boolean = checkNotNull(savedStateHandle["initialCompleted"])

    private val _uiState = MutableStateFlow<CheckInUiState>(CheckInUiState.Loading)
    val uiState: StateFlow<CheckInUiState> = _uiState.asStateFlow()

    private val _form = MutableStateFlow(CheckInFormState())
    val form: StateFlow<CheckInFormState> = _form.asStateFlow()

    init {
        viewModelScope.launch {
            val experiment = experimentRepository.getExperiment(experimentId)
            if (experiment == null) {
                _uiState.value = CheckInUiState.Error(
                    message = "Experiment not found",
                    experimentAction = "",
                )
                return@launch
            }
            val existingLog = dailyLogRepository.getLogForDate(experimentId, LocalDate.now())
            _form.value = CheckInFormState(
                completed = existingLog?.completed ?: initialCompleted,
                mood = existingLog?.moodAfter,
                notes = existingLog?.notes ?: "",
            )
            _uiState.value = CheckInUiState.Ready(
                experimentAction = experiment.action.displayFormat(),
            )
        }
    }

    fun onCompletedChange(completed: Boolean) {
        _form.update { it.copy(completed = completed) }
    }

    fun onMoodChange(mood: Int?) {
        _form.update { it.copy(mood = mood) }
    }

    fun onNotesChange(notes: String) {
        _form.update { it.copy(notes = notes.take(Constants.NOTES_MAX_CHARS)) }
    }

    fun onSubmit() {
        val f = _form.value
        val currentAction = (_uiState.value as? CheckInUiState.Ready)?.experimentAction ?: ""
        _uiState.value = CheckInUiState.Loading
        viewModelScope.launch {
            logDailyCheckIn(
                experimentId = experimentId,
                date = LocalDate.now(),
                completed = f.completed,
                moodBefore = null,
                moodAfter = f.mood,
                notes = f.notes.ifBlank { null },
            ).onSuccess {
                _uiState.value = CheckInUiState.Saved
            }.onFailure { error ->
                _uiState.value = CheckInUiState.Error(
                    message = error.message ?: "Something went wrong",
                    experimentAction = currentAction,
                )
            }
        }
    }

    fun onErrorDismissed() {
        val action = (_uiState.value as? CheckInUiState.Error)?.experimentAction ?: ""
        _uiState.value = CheckInUiState.Ready(experimentAction = action)
    }

    private fun String.displayFormat(): String =
        trim().trimEnd('.', ',', '!', '?', ';', ':').replaceFirstChar { it.uppercase() }
}
