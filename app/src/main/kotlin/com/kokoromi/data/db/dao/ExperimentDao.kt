package com.kokoromi.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.kokoromi.data.db.entity.ExperimentEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface ExperimentDao {
    @Insert
    suspend fun insertExperiment(experiment: ExperimentEntity)

    @Query("SELECT * FROM experiments WHERE id = :id")
    suspend fun getExperiment(id: String): ExperimentEntity?

    @Query("SELECT * FROM experiments WHERE status = 'ACTIVE' ORDER BY created_at DESC")
    fun getActiveExperiments(): Flow<List<ExperimentEntity>>

    @Query("SELECT * FROM experiments ORDER BY created_at DESC")
    fun getAllExperiments(): Flow<List<ExperimentEntity>>

    @Query("SELECT * FROM experiments WHERE status = 'ARCHIVED' ORDER BY updated_at DESC")
    fun getArchivedExperiments(): Flow<List<ExperimentEntity>>

    @Query("SELECT COUNT(*) FROM experiments WHERE status = 'ACTIVE'")
    suspend fun countActiveExperiments(): Int

    @Query("UPDATE experiments SET status = :status, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, updatedAt: Instant)
}
