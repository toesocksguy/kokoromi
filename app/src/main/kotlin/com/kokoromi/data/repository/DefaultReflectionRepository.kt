package com.kokoromi.data.repository

import com.kokoromi.data.db.dao.ReflectionDao
import com.kokoromi.data.db.entity.ReflectionEntity
import com.kokoromi.domain.model.Reflection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultReflectionRepository @Inject constructor(
    private val reflectionDao: ReflectionDao
) : ReflectionRepository {

    override suspend fun saveReflection(reflection: Reflection) {
        reflectionDao.upsertReflection(reflection.toEntity())
    }

    override fun getReflectionsForExperiment(experimentId: String): Flow<List<Reflection>> {
        return reflectionDao.getReflectionsForExperiment(experimentId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getLatestReflection(experimentId: String): Reflection? {
        return reflectionDao.getLatestReflection(experimentId)?.toDomain()
    }

    // --- Mapping ---

    private fun Reflection.toEntity() = ReflectionEntity(
        id = id,
        experimentId = experimentId,
        reflectionDate = reflectionDate,
        plus = plus,
        minus = minus,
        next = next,
        createdAt = createdAt
    )

    private fun ReflectionEntity.toDomain() = Reflection(
        id = id,
        experimentId = experimentId,
        reflectionDate = reflectionDate,
        plus = plus,
        minus = minus,
        next = next,
        createdAt = createdAt
    )
}
