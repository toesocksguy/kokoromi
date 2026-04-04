package com.kokoromi.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kokoromi.data.db.entity.ReflectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReflectionDao {
    // REPLACE handles conflicts on the unique index (experiment_id, reflection_date).
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReflection(reflection: ReflectionEntity)

    @Query("SELECT * FROM reflections WHERE experiment_id = :experimentId ORDER BY reflection_date ASC")
    fun getReflectionsForExperiment(experimentId: String): Flow<List<ReflectionEntity>>

    @Query("SELECT * FROM reflections WHERE experiment_id = :experimentId ORDER BY reflection_date DESC LIMIT 1")
    suspend fun getLatestReflection(experimentId: String): ReflectionEntity?
}
