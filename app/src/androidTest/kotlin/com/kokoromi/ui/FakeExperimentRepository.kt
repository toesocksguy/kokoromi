package com.kokoromi.ui

import com.kokoromi.data.repository.ExperimentRepository
import com.kokoromi.domain.model.DecisionType
import com.kokoromi.domain.model.Experiment
import com.kokoromi.domain.model.ExperimentStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeExperimentRepository : ExperimentRepository {

    private val _activeExperiments = MutableStateFlow<List<Experiment>>(emptyList())
    var activeCount: Int = 0
    var nextCreateId: String = "fake-id"
    var createShouldFail: Boolean = false

    fun setActiveExperiments(experiments: List<Experiment>) {
        _activeExperiments.value = experiments
        activeCount = experiments.size
    }

    override fun getActiveExperiments(): Flow<List<Experiment>> = _activeExperiments
    override suspend fun getActiveExperimentCount(): Int = activeCount
    override suspend fun createExperiment(experiment: Experiment): String {
        if (createShouldFail) throw RuntimeException("Create failed")
        return nextCreateId
    }
    override suspend fun getExperiment(id: String): Experiment? =
        _activeExperiments.value.find { it.id == id }
    override fun getAllExperiments(): Flow<List<Experiment>> = _activeExperiments
    override fun getCompletedExperiments(): Flow<List<Experiment>> = MutableStateFlow(emptyList())
    override fun getArchivedExperiments(): Flow<List<Experiment>> = MutableStateFlow(emptyList())
    override suspend fun updateExperimentStatus(id: String, status: ExperimentStatus) {}
    override suspend fun completeExperiment(id: String, decision: DecisionType, nextExperimentId: String?) {}
}
