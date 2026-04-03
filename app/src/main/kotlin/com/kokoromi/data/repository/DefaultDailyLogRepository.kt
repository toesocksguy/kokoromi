package com.kokoromi.data.repository

import com.kokoromi.data.db.dao.DailyLogDao
import com.kokoromi.data.db.entity.DailyLogEntity
import com.kokoromi.domain.model.DailyLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class DefaultDailyLogRepository @Inject constructor(
    private val dailyLogDao: DailyLogDao
) : DailyLogRepository {

    override suspend fun upsertDailyLog(log: DailyLog) {
        dailyLogDao.upsertLog(log.toEntity())
    }

    override fun getLogsForExperiment(experimentId: String): Flow<List<DailyLog>> {
        return dailyLogDao.getLogsForExperiment(experimentId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getLogsInRange(
        experimentId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<DailyLog> {
        return dailyLogDao.getLogsInRange(
            experimentId = experimentId,
            startDate = startDate.toString(),
            endDate = endDate.toString()
        ).map { it.toDomain() }
    }

    override suspend fun getLogForDate(experimentId: String, date: LocalDate): DailyLog? {
        return dailyLogDao.getLogForDate(
            experimentId = experimentId,
            date = date.toString()
        )?.toDomain()
    }

    override suspend fun getCompletionRate(
        experimentId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Float {
        val totalDays = startDate.until(endDate).days + 1
        if (totalDays <= 0) return 0f
        val completed = dailyLogDao.countCompleted(
            experimentId = experimentId,
            startDate = startDate.toString(),
            endDate = endDate.toString()
        )
        return completed.toFloat() / totalDays
    }

    // --- Mapping ---

    private fun DailyLog.toEntity() = DailyLogEntity(
        id = id,
        experimentId = experimentId,
        date = date,
        completed = completed,
        moodBefore = moodBefore,
        moodAfter = moodAfter,
        notes = notes,
        loggedAt = loggedAt
    )

    private fun DailyLogEntity.toDomain() = DailyLog(
        id = id,
        experimentId = experimentId,
        date = date,
        completed = completed,
        moodBefore = moodBefore,
        moodAfter = moodAfter,
        notes = notes,
        loggedAt = loggedAt
    )
}
