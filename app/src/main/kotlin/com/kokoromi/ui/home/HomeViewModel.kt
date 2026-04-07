package com.kokoromi.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kokoromi.data.repository.ExperimentRepository
import com.kokoromi.data.repository.PreferencesRepository
import com.kokoromi.domain.model.Experiment
import com.kokoromi.domain.model.ExperimentStatus
import com.kokoromi.domain.model.ExperimentWithLogs
import com.kokoromi.domain.usecase.CheckExperimentLifecycleUseCase
import com.kokoromi.domain.usecase.GetActiveExperimentsWithLogsUseCase
import com.kokoromi.domain.usecase.GetReflectionPromptStateUseCase
import com.kokoromi.domain.usecase.ReflectionPromptState
import com.kokoromi.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
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
        val reflectionPrompts: List<ReflectionPromptState>,
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val checkExperimentLifecycle: CheckExperimentLifecycleUseCase,
    private val getReflectionPromptState: GetReflectionPromptStateUseCase,
    getActiveExperimentsWithLogs: GetActiveExperimentsWithLogsUseCase,
    experimentRepository: ExperimentRepository,
    preferencesRepository: PreferencesRepository,
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
        preferencesRepository.getUserPreferences(),
    ) { active, completed, prefs ->
        val prompts = active.mapNotNull { ewl ->
            val state = getReflectionPromptState(
                experiment = ewl.experiment,
                reflectionDay = prefs.reflectionDay,
            )
            if (state.shouldShowPrompt) state else null
        }

        HomeUiState.Success(
            experiments = active,
            completedExperiments = completed,
            canCreateExperiment = active.count { it.experiment.status == ExperimentStatus.ACTIVE } < Constants.MAX_ACTIVE_EXPERIMENTS,
            reflectionPrompts = prompts,
        ) as HomeUiState
    }
        .catch { e -> emit(HomeUiState.Error(e.message ?: "Failed to load experiments")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading,
        )
}
