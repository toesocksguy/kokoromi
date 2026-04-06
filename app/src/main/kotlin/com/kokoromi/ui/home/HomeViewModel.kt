package com.kokoromi.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kokoromi.domain.model.Experiment
import com.kokoromi.domain.model.ExperimentWithLogs
import com.kokoromi.domain.usecase.CheckExperimentLifecycleUseCase
import com.kokoromi.domain.usecase.GetActiveExperimentsWithLogsUseCase
import com.kokoromi.data.repository.ExperimentRepository
import com.kokoromi.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val experiments: List<ExperimentWithLogs>,
        val completedExperiments: List<Experiment>,
        val canCreateExperiment: Boolean,
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val checkExperimentLifecycle: CheckExperimentLifecycleUseCase,
    getActiveExperimentsWithLogs: GetActiveExperimentsWithLogsUseCase,
    experimentRepository: ExperimentRepository,
) : ViewModel() {

    init {
        viewModelScope.launch {
            runCatching { checkExperimentLifecycle() }
                .onFailure { Timber.e(it, "Lifecycle check failed") }
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        getActiveExperimentsWithLogs(),
        experimentRepository.getCompletedExperiments(),
    ) { active, completed ->
        HomeUiState.Success(
            experiments = active,
            completedExperiments = completed,
            canCreateExperiment = active.size < Constants.MAX_ACTIVE_EXPERIMENTS,
        ) as HomeUiState
    }
        .catch { e -> emit(HomeUiState.Error(e.message ?: "Failed to load experiments")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading,
        )
}
