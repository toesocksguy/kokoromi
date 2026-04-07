package com.kokoromi.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kokoromi.data.repository.CompletionRepository
import com.kokoromi.data.repository.DailyLogRepository
import com.kokoromi.data.repository.ExperimentRepository
import com.kokoromi.data.repository.ReflectionRepository
import com.kokoromi.domain.model.Completion
import com.kokoromi.domain.model.CompletionStats
import com.kokoromi.domain.model.DailyLog
import com.kokoromi.domain.model.Experiment
import com.kokoromi.domain.model.ExperimentStatus
import com.kokoromi.domain.model.Reflection
import com.kokoromi.domain.usecase.UpdateExperimentStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.temporal.ChronoUnit
import javax.inject.Inject

sealed interface ExperimentDetailUiState {
    data object Loading : ExperimentDetailUiState
    data class Success(
        val experiment: Experiment,
        val logs: List<DailyLog>,
        val reflections: List<Reflection>,
        val completion: Completion?,
        val stats: CompletionStats,
        val showPauseDialog: Boolean = false,
    ) : ExperimentDetailUiState
    data class Error(val message: String) : ExperimentDetailUiState
}

@HiltViewModel
class ExperimentDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val experimentRepository: ExperimentRepository,
    private val dailyLogRepository: DailyLogRepository,
    private val reflectionRepository: ReflectionRepository,
    private val completionRepository: CompletionRepository,
    private val updateExperimentStatus: UpdateExperimentStatusUseCase,
) : ViewModel() {

    private val experimentId: String = checkNotNull(savedStateHandle["experimentId"])

    private val _completion = MutableStateFlow<Completion?>(null)
    private val _showPauseDialog = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            _completion.value = completionRepository.getCompletion(experimentId)
        }
    }

    val uiState: StateFlow<ExperimentDetailUiState> = combine(
        experimentRepository.getAllExperiments().map { list -> list.find { it.id == experimentId } },
        dailyLogRepository.getLogsForExperiment(experimentId),
        reflectionRepository.getReflectionsForExperiment(experimentId),
        _completion,
        _showPauseDialog,
    ) { experiment, logs, reflections, completion, showPauseDialog ->
        if (experiment == null) {
            ExperimentDetailUiState.Error("Experiment not found")
        } else {
            ExperimentDetailUiState.Success(
                experiment = experiment,
                logs = logs.sortedByDescending { it.date },
                reflections = reflections.sortedByDescending { it.reflectionDate },
                completion = completion,
                stats = computeStats(experiment, logs),
                showPauseDialog = showPauseDialog,
            )
        }
    }
        .catch { e -> emit(ExperimentDetailUiState.Error(e.message ?: "Failed to load experiment")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ExperimentDetailUiState.Loading,
        )

    fun onPauseRequested() = _showPauseDialog.update { true }

    fun onPauseDismissed() = _showPauseDialog.update { false }

    fun onPauseConfirmed() {
        _showPauseDialog.update { false }
        viewModelScope.launch {
            updateExperimentStatus.pause(experimentId)
        }
    }

    private fun computeStats(experiment: Experiment, logs: List<DailyLog>): CompletionStats {
        val totalDays = ChronoUnit.DAYS.between(experiment.startDate, experiment.endDate).toInt() + 1
        val daysCompleted = logs.count { it.completed }
        val moodReadings = logs.mapNotNull { it.moodAfter }
        return CompletionStats(
            totalDays = totalDays,
            daysLogged = logs.size,
            daysCompleted = daysCompleted,
            completionRate = daysCompleted.toFloat() / totalDays,
            avgMoodAfter = if (moodReadings.isEmpty()) null else moodReadings.average().toFloat(),
            moodDelta = null,
        )
    }
}
