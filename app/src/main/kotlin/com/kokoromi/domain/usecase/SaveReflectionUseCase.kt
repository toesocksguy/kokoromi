package com.kokoromi.domain.usecase

import com.kokoromi.data.repository.ReflectionRepository
import com.kokoromi.domain.model.Reflection
import com.kokoromi.util.Constants
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class SaveReflectionUseCase @Inject constructor(
    private val reflectionRepository: ReflectionRepository,
) {
    suspend operator fun invoke(
        experimentId: String,
        reflectionDate: LocalDate,
        plus: String?,
        minus: String?,
        next: String?,
    ): Result<Unit> {
        val plusTrimmed = plus?.trim()?.ifBlank { null }
        val minusTrimmed = minus?.trim()?.ifBlank { null }
        val nextTrimmed = next?.trim()?.ifBlank { null }

        if (plusTrimmed == null && minusTrimmed == null && nextTrimmed == null) {
            return Result.failure(IllegalArgumentException("At least one field must have content"))
        }

        if (plusTrimmed != null && plusTrimmed.length > Constants.REFLECTION_MAX_CHARS) {
            return Result.failure(IllegalArgumentException("Plus field exceeds ${Constants.REFLECTION_MAX_CHARS} characters"))
        }
        if (minusTrimmed != null && minusTrimmed.length > Constants.REFLECTION_MAX_CHARS) {
            return Result.failure(IllegalArgumentException("Minus field exceeds ${Constants.REFLECTION_MAX_CHARS} characters"))
        }
        if (nextTrimmed != null && nextTrimmed.length > Constants.REFLECTION_MAX_CHARS) {
            return Result.failure(IllegalArgumentException("Next field exceeds ${Constants.REFLECTION_MAX_CHARS} characters"))
        }

        val reflection = Reflection(
            id = UUID.randomUUID().toString(),
            experimentId = experimentId,
            reflectionDate = reflectionDate,
            plus = plusTrimmed,
            minus = minusTrimmed,
            next = nextTrimmed,
            createdAt = Instant.now(),
        )

        return runCatching { reflectionRepository.saveReflection(reflection) }
    }
}
