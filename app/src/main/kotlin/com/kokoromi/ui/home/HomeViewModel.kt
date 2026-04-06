package com.kokoromi.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kokoromi.domain.model.ExperimentWithLogs
import com.kokoromi.domain.usecase.GetActiveExperimentsWithLogsUseCase
import com.kokoromi.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val experiments: List<ExperimentWithLogs>,
        val canCreateExperiment: Boolean,
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    getActiveExperimentsWithLogs: GetActiveExperimentsWithLogsUseCase,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = getActiveExperimentsWithLogs()
        .map<List<ExperimentWithLogs>, HomeUiState> { experiments ->
            HomeUiState.Success(
                experiments = experiments,
                canCreateExperiment = experiments.size < Constants.MAX_ACTIVE_EXPERIMENTS,
            )
        }
        .catch { e -> emit(HomeUiState.Error(e.message ?: "Failed to load experiments")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading,
        )
}
