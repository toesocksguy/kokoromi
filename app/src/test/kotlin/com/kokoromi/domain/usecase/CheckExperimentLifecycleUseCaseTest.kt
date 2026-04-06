package com.kokoromi.domain.usecase

import com.kokoromi.data.repository.ExperimentRepository
import com.kokoromi.domain.model.Experiment
import com.kokoromi.domain.model.ExperimentStatus
import com.kokoromi.domain.model.Frequency
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.time.LocalDate

class CheckExperimentLifecycleUseCaseTest {

    private lateinit var repository: ExperimentRepository
    private lateinit var useCase: CheckExperimentLifecycleUseCase

    private val today = LocalDate.now()

    @Before
    fun setUp() {
        repository = mock()
        useCase = CheckExperimentLifecycleUseCase(repository)
    }

    private fun makeExperiment(
        id: String = "exp-1",
        endDate: LocalDate = today,
        status: ExperimentStatus = ExperimentStatus.ACTIVE,
    ) = Experiment(
        id = id,
        hypothesis = "hypothesis",
        action = "action",
        why = null,
        startDate = endDate.minusDays(6),
        endDate = endDate,
        frequency = Frequency.DAILY,
        status = status,
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
    )

    @Test
    fun `transitions expired experiment to COMPLETED`() = runTest {
        val expired = makeExperiment(id = "exp-1", endDate = today.minusDays(1))
        whenever(repository.getActiveExperiments()).thenReturn(flowOf(listOf(expired)))

        useCase()

        verify(repository).updateExperimentStatus("exp-1", ExperimentStatus.COMPLETED)
    }

    @Test
    fun `does not transition experiment ending today`() = runTest {
        val endsToday = makeExperiment(endDate = today)
        whenever(repository.getActiveExperiments()).thenReturn(flowOf(listOf(endsToday)))

        useCase()

        verify(repository, never()).updateExperimentStatus(any(), any())
    }

    @Test
    fun `does not transition experiment ending in the future`() = runTest {
        val future = makeExperiment(endDate = today.plusDays(3))
        whenever(repository.getActiveExperiments()).thenReturn(flowOf(listOf(future)))

        useCase()

        verify(repository, never()).updateExperimentStatus(any(), any())
    }

    @Test
    fun `transitions all expired experiments`() = runTest {
        val exp1 = makeExperiment(id = "exp-1", endDate = today.minusDays(1))
        val exp2 = makeExperiment(id = "exp-2", endDate = today.minusDays(7))
        whenever(repository.getActiveExperiments()).thenReturn(flowOf(listOf(exp1, exp2)))

        useCase()

        verify(repository).updateExperimentStatus("exp-1", ExperimentStatus.COMPLETED)
        verify(repository).updateExperimentStatus("exp-2", ExperimentStatus.COMPLETED)
    }

    @Test
    fun `skips non-expired experiments in a mixed list`() = runTest {
        val expired = makeExperiment(id = "exp-1", endDate = today.minusDays(1))
        val active = makeExperiment(id = "exp-2", endDate = today.plusDays(5))
        whenever(repository.getActiveExperiments()).thenReturn(flowOf(listOf(expired, active)))

        useCase()

        verify(repository, times(1)).updateExperimentStatus(any(), any())
        verify(repository).updateExperimentStatus("exp-1", ExperimentStatus.COMPLETED)
    }

    @Test
    fun `does nothing when no active experiments`() = runTest {
        whenever(repository.getActiveExperiments()).thenReturn(flowOf(emptyList()))

        useCase()

        verify(repository, never()).updateExperimentStatus(any(), any())
    }
}
