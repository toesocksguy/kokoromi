package com.kokoromi.ui

import com.kokoromi.data.repository.DailyLogRepository
import com.kokoromi.domain.model.DailyLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate

class FakeDailyLogRepository : DailyLogRepository {

    private val logs = MutableStateFlow<List<DailyLog>>(emptyList())

    fun seedLog(log: DailyLog) {
        logs.value = logs.value + log
    }

    override suspend fun upsertDailyLog(log: DailyLog) {
        val updated = logs.value.filter { it.date != log.date || it.experimentId != log.experimentId } + log
        logs.value = updated
    }

    override fun getLogsForExperiment(experimentId: String): Flow<List<DailyLog>> =
        MutableStateFlow(logs.value.filter { it.experimentId == experimentId })

    override suspend fun getLogsInRange(
        experimentId: String,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<DailyLog> = logs.value.filter {
        it.experimentId == experimentId && it.date >= startDate && it.date <= endDate
    }

    override suspend fun getLogForDate(experimentId: String, date: LocalDate): DailyLog? =
        logs.value.find { it.experimentId == experimentId && it.date == date }

    override suspend fun getCompletionRate(
        experimentId: String,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Float {
        val range = logs.value.filter {
            it.experimentId == experimentId && it.date >= startDate && it.date <= endDate
        }
        if (range.isEmpty()) return 0f
        return range.count { it.completed }.toFloat() / range.size
    }
}
