package com.kokoromi.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kokoromi.data.db.entity.DailyLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyLogDao {
    // REPLACE handles conflicts on the unique index (experiment_id, date),
    // not just primary key — so inserting a new log for an existing day replaces it.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(log: DailyLogEntity)

    @Query("SELECT * FROM daily_logs WHERE experiment_id = :experimentId ORDER BY date DESC")
    fun getLogsForExperiment(experimentId: String): Flow<List<DailyLogEntity>>

    @Query("""
        SELECT * FROM daily_logs
        WHERE experiment_id = :experimentId
        AND date BETWEEN :startDate AND :endDate
        ORDER BY date ASC
    """)
    suspend fun getLogsInRange(
        experimentId: String,
        startDate: String,
        endDate: String
    ): List<DailyLogEntity>

    @Query("SELECT * FROM daily_logs WHERE experiment_id = :experimentId AND date = :date")
    suspend fun getLogForDate(experimentId: String, date: String): DailyLogEntity?

    @Query("""
        SELECT COUNT(*) FROM daily_logs
        WHERE experiment_id = :experimentId
        AND date BETWEEN :startDate AND :endDate
        AND completed = 1
    """)
    suspend fun countCompleted(
        experimentId: String,
        startDate: String,
        endDate: String
    ): Int
}
