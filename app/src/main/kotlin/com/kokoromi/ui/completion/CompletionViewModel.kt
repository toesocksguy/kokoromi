package com.kokoromi.ui.completion

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kokoromi.data.repository.ExperimentRepository
import com.kokoromi.domain.model.CompletionStats
import com.kokoromi.domain.model.Experiment
import com.kokoromi.domain.usecase.CompleteExperimentUseCase
import com.kokoromi.domain.usecase.ComputeCompletionStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class CompletionFormState(
    val learnings: String = "",
    val externalSignals: String = "",
    val internalSignals: String = "",
)

data class CompletionDisplayState(
    val experiment: Experiment,
    val stats: CompletionStats,
)

sealed interface CompletionUiState {
    data object Loading : CompletionUiState
    data object Ready : CompletionUiState
    data class Persisted(val newExperimentId: String) : CompletionUiState
    data object Pivoted : CompletionUiState
    data object Paused : CompletionUiState
    data class Error(val message: String) : CompletionUiState
}

@HiltViewModel
class CompletionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val experimentRepository: ExperimentRepository,
    private val computeCompletionStats: ComputeCompletionStatsUseCase,
    private val completeExperiment: CompleteExperimentUseCase,
) : ViewModel() {

    private val experimentId: String = checkNotNull(savedStateHandle["experimentId"])

    private val _uiState = MutableStateFlow<CompletionUiState>(CompletionUiState.Loading)
    val uiState: StateFlow<CompletionUiState> = _uiState.asStateFlow()

    private val _displayState = MutableStateFlow<CompletionDisplayState?>(null)
    val displayState: StateFlow<CompletionDisplayState?> = _displayState.asStateFlow()

    private val _form = MutableStateFlow(CompletionFormState())
    val form: StateFlow<CompletionFormState> = _form.asStateFlow()

    init {
        viewModelScope.launch {
            val experiment = experimentRepository.getExperiment(experimentId)
            if (experiment == null) {
                _uiState.value = CompletionUiState.Error("Experiment not found")
                return@launch
            }
            val stats = computeCompletionStats(experimentId).getOrElse { e ->
                Timber.e(e, "Failed to compute completion stats")
                _uiState.value = CompletionUiState.Error(e.message ?: "Failed to load stats")
                return@launch
            }
            _displayState.value = CompletionDisplayState(experiment = experiment, stats = stats)
            _uiState.value = CompletionUiState.Ready
        }
    }

    fun onLearningsChange(value: String) = _form.update { it.copy(learnings = value) }
    fun onExternalSignalsChange(value: String) = _form.update { it.copy(externalSignals = value) }
    fun onInternalSignalsChange(value: String) = _form.update { it.copy(internalSignals = value) }

    fun onPersist() = submitDecision {
        val learnings = combinedLearnings()
        completeExperiment.persist(experimentId, learnings)
            .onSuccess { newId -> _uiState.value = CompletionUiState.Persisted(newId) }
            .onFailure { _uiState.value = CompletionUiState.Error(it.message ?: "Failed to persist") }
    }

    fun onPivot() = submitDecision {
        val learnings = combinedLearnings()
        completeExperiment.pivot(experimentId, learnings)
            .onSuccess { _uiState.value = CompletionUiState.Pivoted }
            .onFailure { _uiState.value = CompletionUiState.Error(it.message ?: "Failed to pivot") }
    }

    fun onPause() = submitDecision {
        val learnings = combinedLearnings()
        completeExperiment.pause(experimentId, learnings)
            .onSuccess { _uiState.value = CompletionUiState.Paused }
            .onFailure { _uiState.value = CompletionUiState.Error(it.message ?: "Failed to pause") }
    }

    fun onErrorDismissed() {
        _uiState.value = CompletionUiState.Ready
    }

    private fun submitDecision(block: suspend () -> Unit) {
        _uiState.value = CompletionUiState.Loading
        viewModelScope.launch { block() }
    }

    /** Combine the three text fields into a single learnings string for storage. */
    private fun combinedLearnings(): String? {
        val f = _form.value
        val parts = buildList {
            if (f.learnings.isNotBlank()) add(f.learnings.trim())
            if (f.externalSignals.isNotBlank()) add("External: ${f.externalSignals.trim()}")
            if (f.internalSignals.isNotBlank()) add("Internal: ${f.internalSignals.trim()}")
        }
        return parts.joinToString("\n\n").ifBlank { null }
    }
}
