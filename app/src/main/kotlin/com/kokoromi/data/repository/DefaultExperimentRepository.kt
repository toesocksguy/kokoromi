package com.kokoromi.data.repository

import com.kokoromi.data.db.dao.CompletionDao
import com.kokoromi.data.db.dao.ExperimentDao
import com.kokoromi.data.db.entity.CompletionEntity
import com.kokoromi.data.db.entity.ExperimentEntity
import com.kokoromi.domain.model.DecisionType
import com.kokoromi.domain.model.Experiment
import com.kokoromi.domain.model.ExperimentStatus
import com.kokoromi.domain.model.Frequency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class DefaultExperimentRepository @Inject constructor(
    private val experimentDao: ExperimentDao,
    private val completionDao: CompletionDao
) : ExperimentRepository {

    override suspend fun createExperiment(experiment: Experiment): String {
        val entity = experiment.toEntity()
        experimentDao.insertExperiment(entity)
        Timber.d("Experiment created: ${experiment.id} — ${experiment.hypothesis}")
        return experiment.id
    }

    override suspend fun getExperiment(id: String): Experiment? {
        return experimentDao.getExperiment(id)?.toDomain()
    }

    override fun getActiveExperiments(): Flow<List<Experiment>> {
        return experimentDao.getActiveExperiments().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllExperiments(): Flow<List<Experiment>> {
        return experimentDao.getAllExperiments().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getArchivedExperiments(): Flow<List<Experiment>> {
        return experimentDao.getArchivedExperiments().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getActiveExperimentCount(): Int {
        return experimentDao.countActiveExperiments()
    }

    override suspend fun updateExperimentStatus(id: String, status: ExperimentStatus) {
        experimentDao.updateStatus(
            id = id,
            status = status.name,
            updatedAt = Instant.now()
        )
        Timber.d("Experiment $id → ${status.name}")
    }

    override suspend fun completeExperiment(
        id: String,
        decision: DecisionType,
        nextExperimentId: String?
    ) {
        experimentDao.updateStatus(
            id = id,
            status = ExperimentStatus.ARCHIVED.name,
            updatedAt = Instant.now()
        )
        // Completion record is saved separately via CompletionRepository.
        // nextExperimentId is stored there via Completion.nextExperimentId.
        Timber.d("Experiment $id completed with decision ${decision.name}")
    }

    // --- Mapping ---

    private fun Experiment.toEntity() = ExperimentEntity(
        id = id,
        hypothesis = hypothesis,
        action = action,
        why = why,
        startDate = startDate,
        endDate = endDate,
        frequency = frequency.name,
        frequencyCustom = null,
        status = status.name,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun ExperimentEntity.toDomain() = Experiment(
        id = id,
        hypothesis = hypothesis,
        action = action,
        why = why,
        startDate = startDate,
        endDate = endDate,
        frequency = Frequency.valueOf(frequency),
        status = ExperimentStatus.valueOf(status),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
