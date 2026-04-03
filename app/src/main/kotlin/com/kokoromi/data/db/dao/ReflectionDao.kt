package com.kokoromi.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.kokoromi.data.db.entity.ReflectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReflectionDao {
    @Upsert
    suspend fun upsertReflection(reflection: ReflectionEntity)

    @Query("SELECT * FROM reflections WHERE experiment_id = :experimentId ORDER BY reflection_date ASC")
    fun getReflectionsForExperiment(experimentId: String): Flow<List<ReflectionEntity>>

    @Query("SELECT * FROM reflections WHERE experiment_id = :experimentId ORDER BY reflection_date DESC LIMIT 1")
    suspend fun getLatestReflection(experimentId: String): ReflectionEntity?
}
