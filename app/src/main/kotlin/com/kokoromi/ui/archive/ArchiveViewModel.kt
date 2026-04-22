package com.kokoromi.ui.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kokoromi.domain.model.Experiment
import com.kokoromi.domain.model.ExperimentStatus
import com.kokoromi.domain.usecase.GetNonActiveExperimentsUseCase
import com.kokoromi.domain.usecase.UpdateExperimentStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ArchiveUiState {
    data object Loading : ArchiveUiState
    data class Success(
        val completed: List<Experiment>,
        val paused: List<Experiment>,
        val archived: List<Experiment>,
    ) : ArchiveUiState
    data class Error(val message: String) : ArchiveUiState
}

sealed interface ArchiveEvent {
    data object Resumed : ArchiveEvent
    data class Error(val message: String) : ArchiveEvent
}

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    getNonActiveExperiments: GetNonActiveExperimentsUseCase,
    private val updateExperimentStatus: UpdateExperimentStatusUseCase,
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _events = Channel<ArchiveEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    val uiState: StateFlow<ArchiveUiState> = getNonActiveExperiments()
        .map<List<Experiment>, ArchiveUiState> { experiments ->
            ArchiveUiState.Success(
                completed = experiments.filter { it.status == ExperimentStatus.COMPLETED },
                paused    = experiments.filter { it.status == ExperimentStatus.PAUSED },
                archived  = experiments.filter { it.status == ExperimentStatus.ARCHIVED },
            )
        }
        .catch { e -> emit(ArchiveUiState.Error(e.message ?: "Failed to load archive")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ArchiveUiState.Loading,
        )

    fun onTabSelected(index: Int) {
        _selectedTab.value = index
    }

    fun onResume(experimentId: String) {
        viewModelScope.launch {
            updateExperimentStatus.resume(experimentId)
                .onSuccess { _events.send(ArchiveEvent.Resumed) }
                .onFailure { e -> _events.send(ArchiveEvent.Error(e.message ?: "Could not resume experiment")) }
        }
    }
}
