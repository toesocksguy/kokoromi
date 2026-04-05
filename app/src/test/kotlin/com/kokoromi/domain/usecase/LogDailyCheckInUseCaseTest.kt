package com.kokoromi.domain.usecase

import com.kokoromi.data.repository.DailyLogRepository
import com.kokoromi.data.repository.ExperimentRepository
import com.kokoromi.domain.model.DailyLog
import com.kokoromi.domain.model.Experiment
import com.kokoromi.domain.model.ExperimentStatus
import com.kokoromi.domain.model.Frequency
import com.kokoromi.util.Constants
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class LogDailyCheckInUseCaseTest {

    private lateinit var experimentRepository: ExperimentRepository
    private lateinit var dailyLogRepository: DailyLogRepository
    private lateinit var useCase: LogDailyCheckInUseCase

    private val today = LocalDate.now()
    private val experimentId = "exp-123"

    @Before
    fun setUp() {
        experimentRepository = mock()
        dailyLogRepository = mock()
        useCase = LogDailyCheckInUseCase(experimentRepository, dailyLogRepository)
    }

    private fun makeExperiment(
        id: String = experimentId,
        startDate: LocalDate = today,
        durationDays: Int = 14,
        status: ExperimentStatus = ExperimentStatus.ACTIVE,
    ) = Experiment(
        id = id,
        hypothesis = "If I do X, then Y will happen",
        action = "Do the thing",
        why = null,
        startDate = startDate,
        endDate = startDate.plusDays(durationDays.toLong() - 1),
        frequency = Frequency.DAILY,
        status = status,
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
    )

    // region happy path

    @Test
    fun `returns success on valid check-in`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(makeExperiment())
        whenever(dailyLogRepository.getLogForDate(experimentId, today)).thenReturn(null)

        val result = useCase(experimentId, today, completed = true)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `saves log with correct fields`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(makeExperiment())
        whenever(dailyLogRepository.getLogForDate(experimentId, today)).thenReturn(null)

        val captor = argumentCaptor<DailyLog>()
        useCase(experimentId, today, completed = true, moodBefore = 3, moodAfter = 4, notes = "felt good")
        verify(dailyLogRepository).upsertDailyLog(captor.capture())

        val log = captor.firstValue
        assertEquals(experimentId, log.experimentId)
        assertEquals(today, log.date)
        assertEquals(true, log.completed)
        assertEquals(3, log.moodBefore)
        assertEquals(4, log.moodAfter)
        assertEquals("felt good", log.notes)
    }

    @Test
    fun `reuses existing log id on update`() = runTest {
        val existingId = "existing-log-id"
        val existingLog = DailyLog(
            id = existingId,
            experimentId = experimentId,
            date = today,
            completed = false,
            moodBefore = null,
            moodAfter = null,
            notes = null,
            loggedAt = Instant.now(),
        )
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(makeExperiment())
        whenever(dailyLogRepository.getLogForDate(experimentId, today)).thenReturn(existingLog)

        val captor = argumentCaptor<DailyLog>()
        useCase(experimentId, today, completed = true)
        verify(dailyLogRepository).upsertDailyLog(captor.capture())

        assertEquals(existingId, captor.firstValue.id)
    }

    @Test
    fun `trims whitespace from notes`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(makeExperiment())
        whenever(dailyLogRepository.getLogForDate(experimentId, today)).thenReturn(null)

        val captor = argumentCaptor<DailyLog>()
        useCase(experimentId, today, completed = true, notes = "  some notes  ")
        verify(dailyLogRepository).upsertDailyLog(captor.capture())

        assertEquals("some notes", captor.firstValue.notes)
    }

    @Test
    fun `stores null notes when notes is blank`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(makeExperiment())
        whenever(dailyLogRepository.getLogForDate(experimentId, today)).thenReturn(null)

        val captor = argumentCaptor<DailyLog>()
        useCase(experimentId, today, completed = true, notes = "   ")
        verify(dailyLogRepository).upsertDailyLog(captor.capture())

        assertEquals(null, captor.firstValue.notes)
    }

    @Test
    fun `can log for first day of experiment`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(makeExperiment(startDate = today))
        whenever(dailyLogRepository.getLogForDate(experimentId, today)).thenReturn(null)

        val result = useCase(experimentId, today, completed = true)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `can back-fill a past date within window`() = runTest {
        val yesterday = today.minusDays(1)
        whenever(experimentRepository.getExperiment(experimentId))
            .thenReturn(makeExperiment(startDate = yesterday, durationDays = 14))
        whenever(dailyLogRepository.getLogForDate(experimentId, yesterday)).thenReturn(null)

        val result = useCase(experimentId, yesterday, completed = true)

        assertTrue(result.isSuccess)
    }

    // endregion

    // region experiment validation

    @Test
    fun `returns failure when experiment not found`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(null)

        val result = useCase(experimentId, today, completed = true)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        verify(dailyLogRepository, never()).upsertDailyLog(any())
    }

    @Test
    fun `returns failure when experiment is not active`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId))
            .thenReturn(makeExperiment(status = ExperimentStatus.COMPLETED))

        val result = useCase(experimentId, today, completed = true)

        assertTrue(result.isFailure)
        verify(dailyLogRepository, never()).upsertDailyLog(any())
    }

    @Test
    fun `returns failure when experiment is paused`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId))
            .thenReturn(makeExperiment(status = ExperimentStatus.PAUSED))

        val result = useCase(experimentId, today, completed = true)

        assertTrue(result.isFailure)
        verify(dailyLogRepository, never()).upsertDailyLog(any())
    }

    @Test
    fun `returns failure when experiment is archived`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId))
            .thenReturn(makeExperiment(status = ExperimentStatus.ARCHIVED))

        val result = useCase(experimentId, today, completed = true)

        assertTrue(result.isFailure)
        verify(dailyLogRepository, never()).upsertDailyLog(any())
    }

    // endregion

    // region date validation

    @Test
    fun `returns failure for date before experiment start`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId))
            .thenReturn(makeExperiment(startDate = today))

        val result = useCase(experimentId, today.minusDays(1), completed = true)

        assertTrue(result.isFailure)
        verify(dailyLogRepository, never()).upsertDailyLog(any())
    }

    @Test
    fun `returns failure for date after experiment end`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId))
            .thenReturn(makeExperiment(startDate = today.minusDays(6), durationDays = 7))

        val result = useCase(experimentId, today.plusDays(1), completed = true)

        assertTrue(result.isFailure)
        verify(dailyLogRepository, never()).upsertDailyLog(any())
    }

    @Test
    fun `returns failure for future date within experiment window`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId))
            .thenReturn(makeExperiment(startDate = today, durationDays = 14))

        val result = useCase(experimentId, today.plusDays(1), completed = true)

        assertTrue(result.isFailure)
        verify(dailyLogRepository, never()).upsertDailyLog(any())
    }

    // endregion

    // region mood validation

    @Test
    fun `returns failure when moodBefore is below minimum`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(makeExperiment())
        whenever(dailyLogRepository.getLogForDate(experimentId, today)).thenReturn(null)

        val result = useCase(experimentId, today, completed = true, moodBefore = Constants.MOOD_MIN - 1)

        assertTrue(result.isFailure)
        verify(dailyLogRepository, never()).upsertDailyLog(any())
    }

    @Test
    fun `returns failure when moodBefore is above maximum`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(makeExperiment())
        whenever(dailyLogRepository.getLogForDate(experimentId, today)).thenReturn(null)

        val result = useCase(experimentId, today, completed = true, moodBefore = Constants.MOOD_MAX + 1)

        assertTrue(result.isFailure)
        verify(dailyLogRepository, never()).upsertDailyLog(any())
    }

    @Test
    fun `returns failure when moodAfter is out of range`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(makeExperiment())
        whenever(dailyLogRepository.getLogForDate(experimentId, today)).thenReturn(null)

        val result = useCase(experimentId, today, completed = true, moodAfter = Constants.MOOD_MAX + 1)

        assertTrue(result.isFailure)
        verify(dailyLogRepository, never()).upsertDailyLog(any())
    }

    @Test
    fun `accepts mood values at boundary`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(makeExperiment())
        whenever(dailyLogRepository.getLogForDate(experimentId, today)).thenReturn(null)

        val result = useCase(
            experimentId, today, completed = true,
            moodBefore = Constants.MOOD_MIN,
            moodAfter = Constants.MOOD_MAX,
        )

        assertTrue(result.isSuccess)
    }

    @Test
    fun `accepts null mood values`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(makeExperiment())
        whenever(dailyLogRepository.getLogForDate(experimentId, today)).thenReturn(null)

        val result = useCase(experimentId, today, completed = true, moodBefore = null, moodAfter = null)

        assertTrue(result.isSuccess)
    }

    // endregion

    // region repository failure

    @Test
    fun `returns failure when repository throws`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(makeExperiment())
        whenever(dailyLogRepository.getLogForDate(experimentId, today)).thenReturn(null)
        whenever(dailyLogRepository.upsertDailyLog(any())).thenThrow(RuntimeException("db exploded"))

        val result = useCase(experimentId, today, completed = true)

        assertTrue(result.isFailure)
    }

    // endregion
}
