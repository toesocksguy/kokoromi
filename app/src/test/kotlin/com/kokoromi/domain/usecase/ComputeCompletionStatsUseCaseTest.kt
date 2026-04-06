package com.kokoromi.domain.usecase

import com.kokoromi.data.repository.DailyLogRepository
import com.kokoromi.data.repository.ExperimentRepository
import com.kokoromi.domain.model.DailyLog
import com.kokoromi.domain.model.Experiment
import com.kokoromi.domain.model.ExperimentStatus
import com.kokoromi.domain.model.Frequency
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant
import java.time.LocalDate

class ComputeCompletionStatsUseCaseTest {

    private lateinit var experimentRepository: ExperimentRepository
    private lateinit var dailyLogRepository: DailyLogRepository
    private lateinit var useCase: ComputeCompletionStatsUseCase

    private val experimentId = "exp-123"
    private val startDate = LocalDate.of(2026, 3, 1)
    private val endDate = LocalDate.of(2026, 3, 14) // 14-day experiment

    @Before
    fun setUp() {
        experimentRepository = mock()
        dailyLogRepository = mock()
        useCase = ComputeCompletionStatsUseCase(experimentRepository, dailyLogRepository)
    }

    private fun makeExperiment() = Experiment(
        id = experimentId,
        hypothesis = "hypothesis",
        action = "action",
        why = null,
        startDate = startDate,
        endDate = endDate,
        frequency = Frequency.DAILY,
        status = ExperimentStatus.COMPLETED,
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
    )

    private fun makeLog(
        date: LocalDate,
        completed: Boolean = true,
        moodAfter: Int? = null,
    ) = DailyLog(
        id = "log-${date}",
        experimentId = experimentId,
        date = date,
        completed = completed,
        moodBefore = null,
        moodAfter = moodAfter,
        notes = null,
        loggedAt = Instant.now(),
    )

    @Test
    fun `returns failure when experiment not found`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(null)

        val result = useCase(experimentId)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `totalDays is end minus start plus one`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(makeExperiment())
        whenever(dailyLogRepository.getLogsInRange(any(), any(), any())).thenReturn(emptyList())

        val stats = useCase(experimentId).getOrThrow()

        assertEquals(14, stats.totalDays)
    }

    @Test
    fun `daysLogged is number of log entries`() = runTest {
        val logs = listOf(
            makeLog(startDate, completed = true),
            makeLog(startDate.plusDays(1), completed = false),
            makeLog(startDate.plusDays(2), completed = true),
        )
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(makeExperiment())
        whenever(dailyLogRepository.getLogsInRange(any(), any(), any())).thenReturn(logs)

        val stats = useCase(experimentId).getOrThrow()

        assertEquals(3, stats.daysLogged)
    }

    @Test
    fun `daysCompleted counts only completed logs`() = runTest {
        val logs = listOf(
            makeLog(startDate, completed = true),
            makeLog(startDate.plusDays(1), completed = false),
            makeLog(startDate.plusDays(2), completed = true),
        )
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(makeExperiment())
        whenever(dailyLogRepository.getLogsInRange(any(), any(), any())).thenReturn(logs)

        val stats = useCase(experimentId).getOrThrow()

        assertEquals(2, stats.daysCompleted)
    }

    @Test
    fun `completionRate is daysCompleted over totalDays not daysLogged`() = runTest {
        // 2 completed out of 3 logged, but 14 total days → rate = 2/14
        val logs = listOf(
            makeLog(startDate, completed = true),
            makeLog(startDate.plusDays(1), completed = false),
            makeLog(startDate.plusDays(2), completed = true),
        )
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(makeExperiment())
        whenever(dailyLogRepository.getLogsInRange(any(), any(), any())).thenReturn(logs)

        val stats = useCase(experimentId).getOrThrow()

        assertEquals(2f / 14f, stats.completionRate, 0.001f)
    }

    @Test
    fun `avgMoodAfter is average of non-null mood values`() = runTest {
        val logs = listOf(
            makeLog(startDate, moodAfter = 4),
            makeLog(startDate.plusDays(1), moodAfter = 2),
            makeLog(startDate.plusDays(2), moodAfter = 3),
        )
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(makeExperiment())
        whenever(dailyLogRepository.getLogsInRange(any(), any(), any())).thenReturn(logs)

        val stats = useCase(experimentId).getOrThrow()

        assertEquals(3.0f, stats.avgMoodAfter!!, 0.001f)
    }

    @Test
    fun `avgMoodAfter is null when no mood data`() = runTest {
        val logs = listOf(
            makeLog(startDate, moodAfter = null),
            makeLog(startDate.plusDays(1), moodAfter = null),
        )
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(makeExperiment())
        whenever(dailyLogRepository.getLogsInRange(any(), any(), any())).thenReturn(logs)

        val stats = useCase(experimentId).getOrThrow()

        assertNull(stats.avgMoodAfter)
    }

    @Test
    fun `avgMoodAfter ignores null mood entries`() = runTest {
        val logs = listOf(
            makeLog(startDate, moodAfter = 4),
            makeLog(startDate.plusDays(1), moodAfter = null),
            makeLog(startDate.plusDays(2), moodAfter = 2),
        )
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(makeExperiment())
        whenever(dailyLogRepository.getLogsInRange(any(), any(), any())).thenReturn(logs)

        val stats = useCase(experimentId).getOrThrow()

        assertEquals(3.0f, stats.avgMoodAfter!!, 0.001f)
    }

    @Test
    fun `moodDelta is always null in v1`() = runTest {
        val logs = listOf(makeLog(startDate, moodAfter = 5))
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(makeExperiment())
        whenever(dailyLogRepository.getLogsInRange(any(), any(), any())).thenReturn(logs)

        val stats = useCase(experimentId).getOrThrow()

        assertNull(stats.moodDelta)
    }

    @Test
    fun `returns zeroed stats when no logs exist`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(makeExperiment())
        whenever(dailyLogRepository.getLogsInRange(any(), any(), any())).thenReturn(emptyList())

        val stats = useCase(experimentId).getOrThrow()

        assertEquals(0, stats.daysLogged)
        assertEquals(0, stats.daysCompleted)
        assertEquals(0f, stats.completionRate, 0.001f)
        assertNull(stats.avgMoodAfter)
    }
}
