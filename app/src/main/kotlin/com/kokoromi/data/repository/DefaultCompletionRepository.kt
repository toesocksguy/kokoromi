package com.kokoromi.data.repository

import com.kokoromi.data.db.dao.CompletionDao
import com.kokoromi.data.db.entity.CompletionEntity
import com.kokoromi.domain.model.Completion
import com.kokoromi.domain.model.DecisionType
import javax.inject.Inject

class DefaultCompletionRepository @Inject constructor(
    private val completionDao: CompletionDao
) : CompletionRepository {

    override suspend fun saveCompletion(completion: Completion) {
        completionDao.insertCompletion(completion.toEntity())
    }

    override suspend fun getCompletion(experimentId: String): Completion? {
        return completionDao.getCompletion(experimentId)?.toDomain()
    }

    // --- Mapping ---

    private fun Completion.toEntity() = CompletionEntity(
        id = id,
        experimentId = experimentId,
        completionDate = completionDate,
        completionRate = completionRate,
        decision = decision.name,
        learnings = learnings,
        nextExperimentId = nextExperimentId,
        createdAt = createdAt
    )

    private fun CompletionEntity.toDomain() = Completion(
        id = id,
        experimentId = experimentId,
        completionDate = completionDate,
        completionRate = completionRate,
        decision = DecisionType.valueOf(decision),
        learnings = learnings,
        nextExperimentId = nextExperimentId,
        createdAt = createdAt
    )
}
