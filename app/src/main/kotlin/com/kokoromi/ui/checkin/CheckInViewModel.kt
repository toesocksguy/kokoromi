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

data class CheckInDisplayState(
    val experimentAction: String,
    val isEditing: Boolean,
    val checkInDate: LocalDate,
)

sealed interface CheckInUiState {
    data object Loading : CheckInUiState
    data object Ready : CheckInUiState
    data object Saved : CheckInUiState
    data class Error(val message: String) : CheckInUiState
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
    private val checkInDate: LocalDate = savedStateHandle.get<String>("date")
        ?.let { LocalDate.parse(it) } ?: LocalDate.now()

    private val _uiState = MutableStateFlow<CheckInUiState>(CheckInUiState.Loading)
    val uiState: StateFlow<CheckInUiState> = _uiState.asStateFlow()

    private val _displayState = MutableStateFlow<CheckInDisplayState?>(null)
    val displayState: StateFlow<CheckInDisplayState?> = _displayState.asStateFlow()

    private val _form = MutableStateFlow(CheckInFormState())
    val form: StateFlow<CheckInFormState> = _form.asStateFlow()

    init {
        viewModelScope.launch {
            val experiment = experimentRepository.getExperiment(experimentId)
            if (experiment == null) {
                _uiState.value = CheckInUiState.Error("Experiment not found")
                return@launch
            }
            val existingLog = dailyLogRepository.getLogForDate(experimentId, checkInDate)
            _form.value = CheckInFormState(
                completed = existingLog?.completed ?: initialCompleted,
                mood = existingLog?.moodAfter,
                notes = existingLog?.notes ?: "",
            )
            _displayState.value = CheckInDisplayState(
                experimentAction = experiment.action.displayFormat(),
                isEditing = existingLog != null,
                checkInDate = checkInDate,
            )
            _uiState.value = CheckInUiState.Ready
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
        _uiState.value = CheckInUiState.Loading
        viewModelScope.launch {
            logDailyCheckIn(
                experimentId = experimentId,
                date = checkInDate,
                completed = f.completed,
                moodBefore = null,
                moodAfter = f.mood,
                notes = f.notes.ifBlank { null },
            ).onSuccess {
                _uiState.value = CheckInUiState.Saved
            }.onFailure { error ->
                _uiState.value = CheckInUiState.Error(error.message ?: "Something went wrong")
            }
        }
    }

    fun onErrorDismissed() {
        _uiState.value = CheckInUiState.Ready
    }

    private fun String.displayFormat(): String =
        trim().trimEnd('.', ',', '!', '?', ';', ':').replaceFirstChar { it.uppercase() }
}
