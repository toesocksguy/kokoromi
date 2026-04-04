package com.kokoromi.domain.usecase

import com.kokoromi.data.repository.ExperimentRepository
import com.kokoromi.domain.model.Experiment
import com.kokoromi.domain.model.ExperimentStatus
import com.kokoromi.domain.model.Frequency
import com.kokoromi.util.Constants
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CreateExperimentUseCaseTest {

    private lateinit var repository: ExperimentRepository
    private lateinit var useCase: CreateExperimentUseCase

    @Before
    fun setUp() {
        repository = mock()
        useCase = CreateExperimentUseCase(repository)
    }

    // region happy path

    @Test
    fun `returns success with experiment id on valid input`() = runTest {
        whenever(repository.getActiveExperimentCount()).thenReturn(0)
        whenever(repository.createExperiment(any())).thenReturn("abc-123")

        val result = useCase("Test hypothesis", "Do the thing", null, 7)

        assertTrue(result.isSuccess)
        assertEquals("abc-123", result.getOrNull())
    }

    @Test
    fun `creates experiment with correct status and frequency`() = runTest {
        whenever(repository.getActiveExperimentCount()).thenReturn(0)
        whenever(repository.createExperiment(any())).thenAnswer { it.getArgument<Experiment>(0).id }

        val captor = argumentCaptor<Experiment>()
        useCase("hypothesis", "action", null, 14, Frequency.DAILY)
        verify(repository).createExperiment(captor.capture())

        val experiment = captor.firstValue
        assertEquals(ExperimentStatus.ACTIVE, experiment.status)
        assertEquals(Frequency.DAILY, experiment.frequency)
        assertEquals(13, java.time.temporal.ChronoUnit.DAYS.between(experiment.startDate, experiment.endDate).toInt())
    }

    @Test
    fun `trims whitespace from hypothesis and action`() = runTest {
        whenever(repository.getActiveExperimentCount()).thenReturn(0)
        whenever(repository.createExperiment(any())).thenAnswer { it.getArgument<Experiment>(0).id }

        val captor = argumentCaptor<Experiment>()
        useCase("  hypothesis  ", "  action  ", "  why  ", 7)
        verify(repository).createExperiment(captor.capture())

        assertEquals("hypothesis", captor.firstValue.hypothesis)
        assertEquals("action", captor.firstValue.action)
        assertEquals("why", captor.firstValue.why)
    }

    @Test
    fun `stores null why when why is blank`() = runTest {
        whenever(repository.getActiveExperimentCount()).thenReturn(0)
        whenever(repository.createExperiment(any())).thenAnswer { it.getArgument<Experiment>(0).id }

        val captor = argumentCaptor<Experiment>()
        useCase("hypothesis", "action", "   ", 7)
        verify(repository).createExperiment(captor.capture())

        assertNull(captor.firstValue.why)
    }

    @Test
    fun `stores null why when why is null`() = runTest {
        whenever(repository.getActiveExperimentCount()).thenReturn(0)
        whenever(repository.createExperiment(any())).thenAnswer { it.getArgument<Experiment>(0).id }

        val captor = argumentCaptor<Experiment>()
        useCase("hypothesis", "action", null, 7)
        verify(repository).createExperiment(captor.capture())

        assertNull(captor.firstValue.why)
    }

    @Test
    fun `end date is start date plus duration minus one`() = runTest {
        whenever(repository.getActiveExperimentCount()).thenReturn(0)
        whenever(repository.createExperiment(any())).thenAnswer { it.getArgument<Experiment>(0).id }

        val captor = argumentCaptor<Experiment>()
        useCase("hypothesis", "action", null, 28)
        verify(repository).createExperiment(captor.capture())

        val experiment = captor.firstValue
        assertEquals(experiment.startDate.plusDays(27), experiment.endDate)
    }

    // endregion

    // region slot cap

    @Test
    fun `returns failure when slot cap is reached`() = runTest {
        whenever(repository.getActiveExperimentCount()).thenReturn(Constants.MAX_ACTIVE_EXPERIMENTS)

        val result = useCase("hypothesis", "action", null, 7)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
        verify(repository, never()).createExperiment(any())
    }

    @Test
    fun `allows creation when one slot is used`() = runTest {
        whenever(repository.getActiveExperimentCount()).thenReturn(1)
        whenever(repository.createExperiment(any())).thenReturn("new-id")

        val result = useCase("hypothesis", "action", null, 7)

        assertTrue(result.isSuccess)
    }

    // endregion

    // region validation — hypothesis

    @Test
    fun `returns failure for blank hypothesis`() = runTest {
        val result = useCase("   ", "action", null, 7)

        assertTrue(result.isFailure)
        verify(repository, never()).createExperiment(any())
    }

    @Test
    fun `returns failure for hypothesis exceeding max chars`() = runTest {
        val hypothesis = "a".repeat(Constants.HYPOTHESIS_MAX_CHARS + 1)

        val result = useCase(hypothesis, "action", null, 7)

        assertTrue(result.isFailure)
        verify(repository, never()).createExperiment(any())
    }

    @Test
    fun `accepts hypothesis at exactly max chars`() = runTest {
        whenever(repository.getActiveExperimentCount()).thenReturn(0)
        whenever(repository.createExperiment(any())).thenReturn("id")
        val hypothesis = "a".repeat(Constants.HYPOTHESIS_MAX_CHARS)

        val result = useCase(hypothesis, "action", null, 7)

        assertTrue(result.isSuccess)
    }

    // endregion

    // region validation — action

    @Test
    fun `returns failure for blank action`() = runTest {
        val result = useCase("hypothesis", "   ", null, 7)

        assertTrue(result.isFailure)
        verify(repository, never()).createExperiment(any())
    }

    @Test
    fun `returns failure for action exceeding max chars`() = runTest {
        val action = "a".repeat(Constants.ACTION_MAX_CHARS + 1)

        val result = useCase("hypothesis", action, null, 7)

        assertTrue(result.isFailure)
        verify(repository, never()).createExperiment(any())
    }

    // endregion

    // region validation — why

    @Test
    fun `returns failure for why exceeding max chars`() = runTest {
        val why = "a".repeat(Constants.WHY_MAX_CHARS + 1)

        val result = useCase("hypothesis", "action", why, 7)

        assertTrue(result.isFailure)
        verify(repository, never()).createExperiment(any())
    }

    @Test
    fun `accepts why at exactly max chars`() = runTest {
        whenever(repository.getActiveExperimentCount()).thenReturn(0)
        whenever(repository.createExperiment(any())).thenReturn("id")
        val why = "a".repeat(Constants.WHY_MAX_CHARS)

        val result = useCase("hypothesis", "action", why, 7)

        assertTrue(result.isSuccess)
    }

    // endregion

    // region validation — duration

    @Test
    fun `returns failure for duration below minimum`() = runTest {
        val result = useCase("hypothesis", "action", null, Constants.MIN_EXPERIMENT_DURATION_DAYS - 1)

        assertTrue(result.isFailure)
        verify(repository, never()).createExperiment(any())
    }

    @Test
    fun `returns failure for duration above maximum`() = runTest {
        val result = useCase("hypothesis", "action", null, Constants.MAX_EXPERIMENT_DURATION_DAYS + 1)

        assertTrue(result.isFailure)
        verify(repository, never()).createExperiment(any())
    }

    @Test
    fun `accepts duration at minimum`() = runTest {
        whenever(repository.getActiveExperimentCount()).thenReturn(0)
        whenever(repository.createExperiment(any())).thenReturn("id")

        val result = useCase("hypothesis", "action", null, Constants.MIN_EXPERIMENT_DURATION_DAYS)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `accepts duration at maximum`() = runTest {
        whenever(repository.getActiveExperimentCount()).thenReturn(0)
        whenever(repository.createExperiment(any())).thenReturn("id")

        val result = useCase("hypothesis", "action", null, Constants.MAX_EXPERIMENT_DURATION_DAYS)

        assertTrue(result.isSuccess)
    }

    // endregion

    // region repository failure

    @Test
    fun `returns failure when repository throws`() = runTest {
        whenever(repository.getActiveExperimentCount()).thenReturn(0)
        whenever(repository.createExperiment(any())).thenThrow(RuntimeException("db exploded"))

        val result = useCase("hypothesis", "action", null, 7)

        assertTrue(result.isFailure)
    }

    // endregion
}
