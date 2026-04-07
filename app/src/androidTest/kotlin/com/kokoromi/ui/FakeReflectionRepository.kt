package com.kokoromi.ui

import com.kokoromi.data.repository.ReflectionRepository
import com.kokoromi.domain.model.Reflection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate

class FakeReflectionRepository : ReflectionRepository {
    private val reflections = mutableListOf<Reflection>()

    override suspend fun saveReflection(reflection: Reflection) {
        reflections.add(reflection)
    }

    override fun getReflectionsForExperiment(experimentId: String): Flow<List<Reflection>> =
        flowOf(reflections.filter { it.experimentId == experimentId })

    override suspend fun getLatestReflection(experimentId: String): Reflection? =
        reflections.filter { it.experimentId == experimentId }
            .maxByOrNull { it.reflectionDate }

    override suspend fun getReflectionInRange(
        experimentId: String,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Reflection? = reflections.firstOrNull {
        it.experimentId == experimentId &&
            !it.reflectionDate.isBefore(startDate) &&
            !it.reflectionDate.isAfter(endDate)
    }
}
