package com.kokoromi.domain.usecase

import com.kokoromi.data.repository.ReflectionRepository
import com.kokoromi.domain.model.Reflection
import com.kokoromi.util.Constants
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

class SaveReflectionUseCaseTest {

    private lateinit var reflectionRepository: ReflectionRepository
    private lateinit var useCase: SaveReflectionUseCase

    private val experimentId = "exp-123"
    private val today = LocalDate.now()

    @Before
    fun setUp() {
        reflectionRepository = mock()
        useCase = SaveReflectionUseCase(reflectionRepository)
    }

    // region happy path

    @Test
    fun `returns success when only plus is provided`() = runTest {
        val result = useCase(experimentId, today, plus = "went well", minus = null, next = null)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `returns success when only minus is provided`() = runTest {
        val result = useCase(experimentId, today, plus = null, minus = "struggled", next = null)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `returns success when only next is provided`() = runTest {
        val result = useCase(experimentId, today, plus = null, minus = null, next = "try shorter sessions")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `returns success when all fields provided`() = runTest {
        val result = useCase(experimentId, today, plus = "good", minus = "tired", next = "adjust")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `saves reflection with correct fields`() = runTest {
        val captor = argumentCaptor<Reflection>()
        useCase(experimentId, today, plus = "good", minus = "tired", next = "adjust")
        verify(reflectionRepository).saveReflection(captor.capture())

        val saved = captor.firstValue
        assert(saved.experimentId == experimentId)
        assert(saved.reflectionDate == today)
        assert(saved.plus == "good")
        assert(saved.minus == "tired")
        assert(saved.next == "adjust")
    }

    @Test
    fun `trims whitespace from all fields`() = runTest {
        val captor = argumentCaptor<Reflection>()
        useCase(experimentId, today, plus = "  good  ", minus = "  tired  ", next = "  adjust  ")
        verify(reflectionRepository).saveReflection(captor.capture())

        val saved = captor.firstValue
        assert(saved.plus == "good")
        assert(saved.minus == "tired")
        assert(saved.next == "adjust")
    }

    @Test
    fun `stores null for blank fields`() = runTest {
        val captor = argumentCaptor<Reflection>()
        useCase(experimentId, today, plus = "good", minus = "   ", next = null)
        verify(reflectionRepository).saveReflection(captor.capture())

        val saved = captor.firstValue
        assert(saved.plus == "good")
        assert(saved.minus == null)
        assert(saved.next == null)
    }

    @Test
    fun `accepts field at exactly max length`() = runTest {
        val atLimit = "a".repeat(Constants.REFLECTION_MAX_CHARS)
        val result = useCase(experimentId, today, plus = atLimit, minus = null, next = null)
        assertTrue(result.isSuccess)
    }

    // endregion

    // region validation failures

    @Test
    fun `returns failure when all fields are null`() = runTest {
        val result = useCase(experimentId, today, plus = null, minus = null, next = null)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        verify(reflectionRepository, never()).saveReflection(any())
    }

    @Test
    fun `returns failure when all fields are blank`() = runTest {
        val result = useCase(experimentId, today, plus = "  ", minus = "  ", next = "  ")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        verify(reflectionRepository, never()).saveReflection(any())
    }

    @Test
    fun `returns failure when plus exceeds max length`() = runTest {
        val tooLong = "a".repeat(Constants.REFLECTION_MAX_CHARS + 1)
        val result = useCase(experimentId, today, plus = tooLong, minus = null, next = null)
        assertTrue(result.isFailure)
        verify(reflectionRepository, never()).saveReflection(any())
    }

    @Test
    fun `returns failure when minus exceeds max length`() = runTest {
        val tooLong = "a".repeat(Constants.REFLECTION_MAX_CHARS + 1)
        val result = useCase(experimentId, today, plus = null, minus = tooLong, next = null)
        assertTrue(result.isFailure)
        verify(reflectionRepository, never()).saveReflection(any())
    }

    @Test
    fun `returns failure when next exceeds max length`() = runTest {
        val tooLong = "a".repeat(Constants.REFLECTION_MAX_CHARS + 1)
        val result = useCase(experimentId, today, plus = null, minus = null, next = tooLong)
        assertTrue(result.isFailure)
        verify(reflectionRepository, never()).saveReflection(any())
    }

    // endregion

    // region repository failure

    @Test
    fun `returns failure when repository throws`() = runTest {
        whenever(reflectionRepository.saveReflection(any())).thenThrow(RuntimeException("db exploded"))
        val result = useCase(experimentId, today, plus = "good", minus = null, next = null)
        assertTrue(result.isFailure)
    }

    // endregion
}
