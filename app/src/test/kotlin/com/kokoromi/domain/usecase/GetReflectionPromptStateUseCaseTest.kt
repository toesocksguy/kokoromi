package com.kokoromi.domain.usecase

import com.kokoromi.data.repository.ReflectionRepository
import com.kokoromi.domain.model.Experiment
import com.kokoromi.domain.model.ExperimentStatus
import com.kokoromi.domain.model.Frequency
import com.kokoromi.domain.model.Reflection
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate

class GetReflectionPromptStateUseCaseTest {

    private lateinit var reflectionRepository: ReflectionRepository
    private lateinit var useCase: GetReflectionPromptStateUseCase

    // 2026-04-22 is a Wednesday
    private val wednesday = LocalDate.of(2026, 4, 22)
    private val expectedWeekStart = LocalDate.of(2026, 4, 20) // Monday
    private val expectedWeekEnd = LocalDate.of(2026, 4, 26)   // Sunday

    @Before
    fun setUp() {
        reflectionRepository = mock()
        useCase = GetReflectionPromptStateUseCase(reflectionRepository)
    }

    private fun makeExperiment(id: String = "exp-1") = Experiment(
        id = id,
        hypothesis = "I will meditate for 7 days",
        action = "meditate",
        why = null,
        startDate = wednesday.minusDays(6),
        endDate = wednesday.plusDays(1),
        frequency = Frequency.DAILY,
        status = ExperimentStatus.ACTIVE,
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
    )

    private fun makeReflection(experimentId: String = "exp-1") = Reflection(
        id = "ref-1",
        experimentId = experimentId,
        reflectionDate = wednesday,
        plus = "felt good",
        minus = null,
        next = null,
        createdAt = Instant.now(),
    )

    // region not reflection day

    @Test
    fun `shouldShowPrompt false when today is not the reflection day`() = runTest {
        val result = useCase(makeExperiment(), today = wednesday, reflectionDay = DayOfWeek.SUNDAY)
        assertFalse(result.shouldShowPrompt)
    }

    @Test
    fun `does not call repository when today is not the reflection day`() = runTest {
        useCase(makeExperiment(), today = wednesday, reflectionDay = DayOfWeek.SUNDAY)
        verify(reflectionRepository, never()).getReflectionInRange(any(), any(), any())
    }

    // endregion

    // region reflection day, no existing reflection

    @Test
    fun `shouldShowPrompt true when today is reflection day and no existing reflection`() = runTest {
        whenever(
            reflectionRepository.getReflectionInRange(
                experimentId = "exp-1",
                startDate = expectedWeekStart,
                endDate = expectedWeekEnd,
            )
        ).thenReturn(null)

        val result = useCase(makeExperiment(), today = wednesday, reflectionDay = DayOfWeek.WEDNESDAY)
        assertTrue(result.shouldShowPrompt)
    }

    // endregion

    // region reflection day, existing reflection

    @Test
    fun `shouldShowPrompt false when today is reflection day and reflection already exists`() = runTest {
        whenever(
            reflectionRepository.getReflectionInRange(
                experimentId = "exp-1",
                startDate = expectedWeekStart,
                endDate = expectedWeekEnd,
            )
        ).thenReturn(makeReflection())

        val result = useCase(makeExperiment(), today = wednesday, reflectionDay = DayOfWeek.WEDNESDAY)
        assertFalse(result.shouldShowPrompt)
    }

    // endregion

    // region week boundary calculation

    @Test
    fun `weekStart is Monday of the current week`() = runTest {
        whenever(reflectionRepository.getReflectionInRange(any(), any(), any())).thenReturn(null)
        val result = useCase(makeExperiment(), today = wednesday, reflectionDay = DayOfWeek.WEDNESDAY)
        assertEquals(expectedWeekStart, result.weekStart)
    }

    @Test
    fun `weekEnd is Sunday of the current week`() = runTest {
        whenever(reflectionRepository.getReflectionInRange(any(), any(), any())).thenReturn(null)
        val result = useCase(makeExperiment(), today = wednesday, reflectionDay = DayOfWeek.WEDNESDAY)
        assertEquals(expectedWeekEnd, result.weekEnd)
    }

    @Test
    fun `weekStart is Monday when today is Sunday`() = runTest {
        val sunday = LocalDate.of(2026, 4, 26)
        whenever(reflectionRepository.getReflectionInRange(any(), any(), any())).thenReturn(null)
        val result = useCase(makeExperiment(), today = sunday, reflectionDay = DayOfWeek.SUNDAY)
        assertEquals(LocalDate.of(2026, 4, 20), result.weekStart)
        assertEquals(sunday, result.weekEnd)
    }

    @Test
    fun `weekStart and weekEnd correct when today is Monday`() = runTest {
        val monday = LocalDate.of(2026, 4, 20)
        whenever(reflectionRepository.getReflectionInRange(any(), any(), any())).thenReturn(null)
        val result = useCase(makeExperiment(), today = monday, reflectionDay = DayOfWeek.MONDAY)
        assertEquals(monday, result.weekStart)
        assertEquals(LocalDate.of(2026, 4, 26), result.weekEnd)
    }

    // endregion

    // region result fields

    @Test
    fun `result contains correct experimentId and experimentName`() = runTest {
        val experiment = makeExperiment(id = "exp-42")
        whenever(reflectionRepository.getReflectionInRange(any(), any(), any())).thenReturn(null)
        val result = useCase(experiment, today = wednesday, reflectionDay = DayOfWeek.WEDNESDAY)
        assertEquals("exp-42", result.experimentId)
        assertEquals(experiment.hypothesis, result.experimentName)
    }

    // endregion
}
