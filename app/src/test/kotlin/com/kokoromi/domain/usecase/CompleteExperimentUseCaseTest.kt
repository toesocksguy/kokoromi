package com.kokoromi.domain.usecase

import com.kokoromi.data.repository.CompletionRepository
import com.kokoromi.data.repository.DailyLogRepository
import com.kokoromi.data.repository.ExperimentRepository
import com.kokoromi.domain.model.Completion
import com.kokoromi.domain.model.DecisionType
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

class CompleteExperimentUseCaseTest {

    private lateinit var experimentRepository: ExperimentRepository
    private lateinit var completionRepository: CompletionRepository
    private lateinit var dailyLogRepository: DailyLogRepository
    private lateinit var computeStats: ComputeCompletionStatsUseCase
    private lateinit var createExperiment: CreateExperimentUseCase
    private lateinit var useCase: CompleteExperimentUseCase

    private val experimentId = "exp-123"
    private val today = LocalDate.now()

    @Before
    fun setUp() {
        experimentRepository = mock()
        completionRepository = mock()
        dailyLogRepository = mock()
        computeStats = ComputeCompletionStatsUseCase(experimentRepository, dailyLogRepository)
        createExperiment = CreateExperimentUseCase(experimentRepository)
        useCase = CompleteExperimentUseCase(
            experimentRepository = experimentRepository,
            completionRepository = completionRepository,
            computeCompletionStats = computeStats,
            createExperiment = createExperiment,
        )
    }

    private fun makeExperiment(
        id: String = experimentId,
        durationDays: Int = 7,
    ) = Experiment(
        id = id,
        hypothesis = "hypothesis",
        action = "action",
        why = null,
        startDate = today.minusDays(durationDays.toLong() - 1),
        endDate = today,
        frequency = Frequency.DAILY,
        status = ExperimentStatus.COMPLETED,
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
    )

    // region persist

    @Test
    fun `persist returns failure when experiment not found`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(null)

        val result = useCase.persist(experimentId, learnings = null)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `persist returns failure when slot cap reached`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(makeExperiment())
        whenever(experimentRepository.getActiveExperimentCount()).thenReturn(Constants.MAX_ACTIVE_EXPERIMENTS)
        whenever(dailyLogRepository.getLogsInRange(any(), any(), any())).thenReturn(emptyList())

        val result = useCase.persist(experimentId, learnings = null)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
        verify(completionRepository, never()).saveCompletion(any())
    }

    @Test
    fun `persist saves completion with PERSIST decision`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(makeExperiment())
        whenever(experimentRepository.getActiveExperimentCount()).thenReturn(0)
        whenever(experimentRepository.createExperiment(any())).thenReturn("new-exp-id")
        whenever(dailyLogRepository.getLogsInRange(any(), any(), any())).thenReturn(emptyList())

        useCase.persist(experimentId, learnings = "it worked")

        val captor = argumentCaptor<Completion>()
        verify(completionRepository).saveCompletion(captor.capture())
        assertEquals(DecisionType.PERSIST, captor.firstValue.decision)
        assertEquals(experimentId, captor.firstValue.experimentId)
        assertEquals("it worked", captor.firstValue.learnings)
        assertEquals("new-exp-id", captor.firstValue.nextExperimentId)
    }

    @Test
    fun `persist archives the old experiment`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(makeExperiment())
        whenever(experimentRepository.getActiveExperimentCount()).thenReturn(0)
        whenever(experimentRepository.createExperiment(any())).thenReturn("new-exp-id")
        whenever(dailyLogRepository.getLogsInRange(any(), any(), any())).thenReturn(emptyList())

        useCase.persist(experimentId, learnings = null)

        verify(experimentRepository).updateExperimentStatus(experimentId, ExperimentStatus.ARCHIVED)
    }

    @Test
    fun `persist returns new experiment id on success`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(makeExperiment())
        whenever(experimentRepository.getActiveExperimentCount()).thenReturn(0)
        whenever(experimentRepository.createExperiment(any())).thenReturn("new-exp-id")
        whenever(dailyLogRepository.getLogsInRange(any(), any(), any())).thenReturn(emptyList())

        val result = useCase.persist(experimentId, learnings = null)

        assertTrue(result.isSuccess)
        assertEquals("new-exp-id", result.getOrNull())
    }

    @Test
    fun `persist trims blank learnings to null`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(makeExperiment())
        whenever(experimentRepository.getActiveExperimentCount()).thenReturn(0)
        whenever(experimentRepository.createExperiment(any())).thenReturn("new-exp-id")
        whenever(dailyLogRepository.getLogsInRange(any(), any(), any())).thenReturn(emptyList())

        useCase.persist(experimentId, learnings = "   ")

        val captor = argumentCaptor<Completion>()
        verify(completionRepository).saveCompletion(captor.capture())
        assertEquals(null, captor.firstValue.learnings)
    }

    // endregion

    // region pivot

    @Test
    fun `pivot returns failure when experiment not found`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(null)

        val result = useCase.pivot(experimentId, learnings = null)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `pivot saves completion with PIVOT decision`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(makeExperiment())
        whenever(dailyLogRepository.getLogsInRange(any(), any(), any())).thenReturn(emptyList())

        useCase.pivot(experimentId, learnings = "changing direction")

        val captor = argumentCaptor<Completion>()
        verify(completionRepository).saveCompletion(captor.capture())
        assertEquals(DecisionType.PIVOT, captor.firstValue.decision)
        assertEquals("changing direction", captor.firstValue.learnings)
    }

    @Test
    fun `pivot saves null nextExperimentId`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(makeExperiment())
        whenever(dailyLogRepository.getLogsInRange(any(), any(), any())).thenReturn(emptyList())

        useCase.pivot(experimentId, learnings = null)

        val captor = argumentCaptor<Completion>()
        verify(completionRepository).saveCompletion(captor.capture())
        assertEquals(null, captor.firstValue.nextExperimentId)
    }

    @Test
    fun `pivot archives the experiment`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(makeExperiment())
        whenever(dailyLogRepository.getLogsInRange(any(), any(), any())).thenReturn(emptyList())

        useCase.pivot(experimentId, learnings = null)

        verify(experimentRepository).updateExperimentStatus(experimentId, ExperimentStatus.ARCHIVED)
    }

    @Test
    fun `pivot does not create a new experiment`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(makeExperiment())
        whenever(dailyLogRepository.getLogsInRange(any(), any(), any())).thenReturn(emptyList())

        useCase.pivot(experimentId, learnings = null)

        verify(experimentRepository, never()).createExperiment(any())
    }

    // endregion

    // region pause

    @Test
    fun `pause returns failure when experiment not found`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(null)

        val result = useCase.pause(experimentId, learnings = null)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `pause saves completion with PAUSE decision`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(makeExperiment())
        whenever(dailyLogRepository.getLogsInRange(any(), any(), any())).thenReturn(emptyList())

        useCase.pause(experimentId, learnings = "setting aside")

        val captor = argumentCaptor<Completion>()
        verify(completionRepository).saveCompletion(captor.capture())
        assertEquals(DecisionType.PAUSE, captor.firstValue.decision)
        assertEquals("setting aside", captor.firstValue.learnings)
    }

    @Test
    fun `pause archives the experiment`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(makeExperiment())
        whenever(dailyLogRepository.getLogsInRange(any(), any(), any())).thenReturn(emptyList())

        useCase.pause(experimentId, learnings = null)

        verify(experimentRepository).updateExperimentStatus(experimentId, ExperimentStatus.ARCHIVED)
    }

    @Test
    fun `pause does not create a new experiment`() = runTest {
        whenever(experimentRepository.getExperiment(experimentId)).thenReturn(makeExperiment())
        whenever(dailyLogRepository.getLogsInRange(any(), any(), any())).thenReturn(emptyList())

        useCase.pause(experimentId, learnings = null)

        verify(experimentRepository, never()).createExperiment(any())
    }

    // endregion
}
