package com.kokoromi.data.repository

import com.kokoromi.domain.model.Reflection
import kotlinx.coroutines.flow.Flow

interface ReflectionRepository {
    suspend fun saveReflection(reflection: Reflection)
    fun getReflectionsForExperiment(experimentId: String): Flow<List<Reflection>>
    suspend fun getLatestReflection(experimentId: String): Reflection?
}
