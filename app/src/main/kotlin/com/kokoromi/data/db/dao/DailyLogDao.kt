package com.kokoromi.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.kokoromi.data.db.entity.DailyLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyLogDao {
    @Upsert
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
