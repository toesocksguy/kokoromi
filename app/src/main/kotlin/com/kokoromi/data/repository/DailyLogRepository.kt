package com.kokoromi.data.repository

import com.kokoromi.domain.model.DailyLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface DailyLogRepository {
    suspend fun upsertDailyLog(log: DailyLog)
    fun getLogsForExperiment(experimentId: String): Flow<List<DailyLog>>
    suspend fun getLogsInRange(
        experimentId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<DailyLog>
    suspend fun getLogForDate(experimentId: String, date: LocalDate): DailyLog?
    suspend fun getCompletionRate(
        experimentId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Float
}
