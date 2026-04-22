package com.kokoromi.domain.usecase

import com.kokoromi.data.repository.DailyLogRepository
import com.kokoromi.data.repository.ExperimentRepository
import com.kokoromi.domain.model.DailyLog
import com.kokoromi.domain.model.Experiment
import com.kokoromi.domain.model.ExperimentStatus
import com.kokoromi.domain.model.Frequency
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant
import java.time.LocalDate

class GetActiveExperimentsWithLogsUseCaseTest {

    private lateinit var experimentRepository: ExperimentRepository
    private lateinit var dailyLogRepository: DailyLogRepository
    private lateinit var useCase: GetActiveExperimentsWithLogsUseCase

    private val today = LocalDate.now()

    @Before
    fun setUp() {
        experimentRepository = mock()
        dailyLogRepository = mock()
        useCase = GetActiveExperimentsWithLogsUseCase(experimentRepository, dailyLogRepository)
    }

    private fun makeExperiment(
        id: String = "exp-1",
        status: ExperimentStatus = ExperimentStatus.ACTIVE,
    ) = Experiment(
        id = id,
        hypothesis = "hypothesis",
        action = "action",
        why = null,
        startDate = today.minusDays(6),
        endDate = today.plusDays(1),
        frequency = Frequency.DAILY,
        status = status,
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
    )

    private fun makeLog(
        id: String = "log-1",
        experimentId: String = "exp-1",
        date: LocalDate = today,
    ) = DailyLog(
        id = id,
        experimentId = experimentId,
        date = date,
        completed = true,
        moodBefore = null,
        moodAfter = null,
        notes = null,
        loggedAt = Instant.now(),
    )

    // region empty / filtered

    @Test
    fun `emits empty list when no experiments exist`() = runTest {
        whenever(experimentRepository.getAllExperiments()).thenReturn(flowOf(emptyList()))
        val result = useCase().first()
        assertEquals(emptyList<Any>(), result)
    }

    @Test
    fun `emits empty list when all experiments are non-ACTIVE`() = runTest {
        val experiments = listOf(
            makeExperiment(id = "exp-1", status = ExperimentStatus.PAUSED),
            makeExperiment(id = "exp-2", status = ExperimentStatus.COMPLETED),
            makeExperiment(id = "exp-3", status = ExperimentStatus.ARCHIVED),
        )
        whenever(experimentRepository.getAllExperiments()).thenReturn(flowOf(experiments))
        val result = useCase().first()
        assertEquals(emptyList<Any>(), result)
    }

    @Test
    fun `filters out non-ACTIVE experiments from mixed list`() = runTest {
        val active = makeExperiment(id = "exp-1", status = ExperimentStatus.ACTIVE)
        val paused = makeExperiment(id = "exp-2", status = ExperimentStatus.PAUSED)
        whenever(experimentRepository.getAllExperiments()).thenReturn(flowOf(listOf(active, paused)))
        whenever(dailyLogRepository.getLogsForExperiment("exp-1")).thenReturn(flowOf(emptyList()))

        val result = useCase().first()
        assertEquals(1, result.size)
        assertEquals("exp-1", result[0].experiment.id)
    }

    // endregion

    // region todayLog

    @Test
    fun `todayLog is set when today's log exists`() = runTest {
        val experiment = makeExperiment()
        whenever(experimentRepository.getAllExperiments()).thenReturn(flowOf(listOf(experiment)))
        whenever(dailyLogRepository.getLogsForExperiment("exp-1"))
            .thenReturn(flowOf(listOf(makeLog(date = today))))

        val result = useCase().first()
        assertNotNull(result[0].todayLog)
        assertEquals(today, result[0].todayLog!!.date)
    }

    @Test
    fun `todayLog is null when no log exists for today`() = runTest {
        val experiment = makeExperiment()
        whenever(experimentRepository.getAllExperiments()).thenReturn(flowOf(listOf(experiment)))
        whenever(dailyLogRepository.getLogsForExperiment("exp-1"))
            .thenReturn(flowOf(listOf(makeLog(date = today.minusDays(1)))))

        val result = useCase().first()
        assertNull(result[0].todayLog)
    }

    @Test
    fun `todayLog is null when experiment has no logs`() = runTest {
        val experiment = makeExperiment()
        whenever(experimentRepository.getAllExperiments()).thenReturn(flowOf(listOf(experiment)))
        whenever(dailyLogRepository.getLogsForExperiment("exp-1")).thenReturn(flowOf(emptyList()))

        val result = useCase().first()
        assertNull(result[0].todayLog)
    }

    // endregion

    // region logs list

    @Test
    fun `logs list contains all logs for the experiment`() = runTest {
        val experiment = makeExperiment()
        val logs = listOf(
            makeLog(id = "log-1", date = today),
            makeLog(id = "log-2", date = today.minusDays(1)),
            makeLog(id = "log-3", date = today.minusDays(2)),
        )
        whenever(experimentRepository.getAllExperiments()).thenReturn(flowOf(listOf(experiment)))
        whenever(dailyLogRepository.getLogsForExperiment("exp-1")).thenReturn(flowOf(logs))

        val result = useCase().first()
        assertEquals(3, result[0].logs.size)
    }

    // endregion

    // region multiple experiments

    @Test
    fun `emits one entry per active experiment`() = runTest {
        val exp1 = makeExperiment(id = "exp-1")
        val exp2 = makeExperiment(id = "exp-2")
        whenever(experimentRepository.getAllExperiments()).thenReturn(flowOf(listOf(exp1, exp2)))
        whenever(dailyLogRepository.getLogsForExperiment("exp-1")).thenReturn(flowOf(emptyList()))
        whenever(dailyLogRepository.getLogsForExperiment("exp-2")).thenReturn(flowOf(emptyList()))

        val result = useCase().first()
        assertEquals(2, result.size)
    }

    @Test
    fun `each experiment in result has its own logs`() = runTest {
        val exp1 = makeExperiment(id = "exp-1")
        val exp2 = makeExperiment(id = "exp-2")
        val log1 = makeLog(id = "log-1", experimentId = "exp-1", date = today)
        val log2 = makeLog(id = "log-2", experimentId = "exp-2", date = today.minusDays(1))
        whenever(experimentRepository.getAllExperiments()).thenReturn(flowOf(listOf(exp1, exp2)))
        whenever(dailyLogRepository.getLogsForExperiment("exp-1")).thenReturn(flowOf(listOf(log1)))
        whenever(dailyLogRepository.getLogsForExperiment("exp-2")).thenReturn(flowOf(listOf(log2)))

        val result = useCase().first()
        val r1 = result.first { it.experiment.id == "exp-1" }
        val r2 = result.first { it.experiment.id == "exp-2" }
        assertNotNull(r1.todayLog)
        assertNull(r2.todayLog)
    }

    // endregion
}
