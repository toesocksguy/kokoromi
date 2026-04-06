package com.kokoromi.data.repository

import com.kokoromi.domain.model.DecisionType
import com.kokoromi.domain.model.Experiment
import com.kokoromi.domain.model.ExperimentStatus
import kotlinx.coroutines.flow.Flow

interface ExperimentRepository {
    suspend fun createExperiment(experiment: Experiment): String
    suspend fun getExperiment(id: String): Experiment?
    fun getActiveExperiments(): Flow<List<Experiment>>
    fun getAllExperiments(): Flow<List<Experiment>>
    fun getCompletedExperiments(): Flow<List<Experiment>>
    fun getArchivedExperiments(): Flow<List<Experiment>>
    suspend fun getActiveExperimentCount(): Int
    suspend fun updateExperimentStatus(id: String, status: ExperimentStatus)
    suspend fun completeExperiment(
        id: String,
        decision: DecisionType,
        nextExperimentId: String?
    )
}
