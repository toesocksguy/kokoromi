package com.kokoromi.domain.usecase

import com.kokoromi.data.repository.ExperimentRepository
import com.kokoromi.domain.model.Experiment
import com.kokoromi.domain.model.ExperimentStatus
import com.kokoromi.domain.model.Frequency
import com.kokoromi.util.Constants
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.time.LocalDate

class UpdateExperimentStatusUseCaseTest {

    private lateinit var repository: ExperimentRepository
    private lateinit var useCase: UpdateExperimentStatusUseCase

    private val today = LocalDate.now()

    @Before
    fun setUp() {
        repository = mock()
        useCase = UpdateExperimentStatusUseCase(repository)
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

    // region pause

    @Test
    fun `pause returns success for ACTIVE experiment`() = runTest {
        whenever(repository.getExperiment("exp-1")).thenReturn(makeExperiment(status = ExperimentStatus.ACTIVE))
        val result = useCase.pause("exp-1")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `pause updates status to PAUSED`() = runTest {
        whenever(repository.getExperiment("exp-1")).thenReturn(makeExperiment(status = ExperimentStatus.ACTIVE))
        useCase.pause("exp-1")
        verify(repository).updateExperimentStatus("exp-1", ExperimentStatus.PAUSED)
    }

    @Test
    fun `pause returns failure when experiment not found`() = runTest {
        whenever(repository.getExperiment("exp-1")).thenReturn(null)
        val result = useCase.pause("exp-1")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `pause returns failure for non-ACTIVE experiment`() = runTest {
        whenever(repository.getExperiment("exp-1")).thenReturn(makeExperiment(status = ExperimentStatus.PAUSED))
        val result = useCase.pause("exp-1")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
        verify(repository, never()).updateExperimentStatus(any(), any())
    }

    // endregion

    // region archive

    @Test
    fun `archive returns success for PAUSED experiment`() = runTest {
        whenever(repository.getExperiment("exp-1")).thenReturn(makeExperiment(status = ExperimentStatus.PAUSED))
        val result = useCase.archive("exp-1")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `archive updates status to ARCHIVED`() = runTest {
        whenever(repository.getExperiment("exp-1")).thenReturn(makeExperiment(status = ExperimentStatus.PAUSED))
        useCase.archive("exp-1")
        verify(repository).updateExperimentStatus("exp-1", ExperimentStatus.ARCHIVED)
    }

    @Test
    fun `archive returns failure when experiment not found`() = runTest {
        whenever(repository.getExperiment("exp-1")).thenReturn(null)
        val result = useCase.archive("exp-1")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `archive returns failure for non-PAUSED experiment`() = runTest {
        whenever(repository.getExperiment("exp-1")).thenReturn(makeExperiment(status = ExperimentStatus.ACTIVE))
        val result = useCase.archive("exp-1")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
        verify(repository, never()).updateExperimentStatus(any(), any())
    }

    // endregion

    // region endEarly

    @Test
    fun `endEarly returns success for ACTIVE experiment`() = runTest {
        whenever(repository.getExperiment("exp-1")).thenReturn(makeExperiment(status = ExperimentStatus.ACTIVE))
        val result = useCase.endEarly("exp-1")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `endEarly updates status to COMPLETED`() = runTest {
        whenever(repository.getExperiment("exp-1")).thenReturn(makeExperiment(status = ExperimentStatus.ACTIVE))
        useCase.endEarly("exp-1")
        verify(repository).updateExperimentStatus("exp-1", ExperimentStatus.COMPLETED)
    }

    @Test
    fun `endEarly returns failure when experiment not found`() = runTest {
        whenever(repository.getExperiment("exp-1")).thenReturn(null)
        val result = useCase.endEarly("exp-1")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `endEarly returns failure for non-ACTIVE experiment`() = runTest {
        whenever(repository.getExperiment("exp-1")).thenReturn(makeExperiment(status = ExperimentStatus.PAUSED))
        val result = useCase.endEarly("exp-1")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
        verify(repository, never()).updateExperimentStatus(any(), any())
    }

    // endregion

    // region resume

    @Test
    fun `resume returns success for PAUSED experiment under the active limit`() = runTest {
        whenever(repository.getExperiment("exp-1")).thenReturn(makeExperiment(status = ExperimentStatus.PAUSED))
        whenever(repository.getActiveExperimentCount()).thenReturn(0)
        val result = useCase.resume("exp-1")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `resume updates status to ACTIVE`() = runTest {
        whenever(repository.getExperiment("exp-1")).thenReturn(makeExperiment(status = ExperimentStatus.PAUSED))
        whenever(repository.getActiveExperimentCount()).thenReturn(0)
        useCase.resume("exp-1")
        verify(repository).updateExperimentStatus("exp-1", ExperimentStatus.ACTIVE)
    }

    @Test
    fun `resume returns failure when experiment not found`() = runTest {
        whenever(repository.getExperiment("exp-1")).thenReturn(null)
        val result = useCase.resume("exp-1")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `resume returns failure for non-PAUSED experiment`() = runTest {
        whenever(repository.getExperiment("exp-1")).thenReturn(makeExperiment(status = ExperimentStatus.ACTIVE))
        val result = useCase.resume("exp-1")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
        verify(repository, never()).updateExperimentStatus(any(), any())
    }

    @Test
    fun `resume returns failure when active experiment limit is reached`() = runTest {
        whenever(repository.getExperiment("exp-1")).thenReturn(makeExperiment(status = ExperimentStatus.PAUSED))
        whenever(repository.getActiveExperimentCount()).thenReturn(Constants.MAX_ACTIVE_EXPERIMENTS)
        val result = useCase.resume("exp-1")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
        verify(repository, never()).updateExperimentStatus(any(), any())
    }

    @Test
    fun `resume succeeds when active count is exactly one below the limit`() = runTest {
        whenever(repository.getExperiment("exp-1")).thenReturn(makeExperiment(status = ExperimentStatus.PAUSED))
        whenever(repository.getActiveExperimentCount()).thenReturn(Constants.MAX_ACTIVE_EXPERIMENTS - 1)
        val result = useCase.resume("exp-1")
        assertTrue(result.isSuccess)
    }

    // endregion
}
