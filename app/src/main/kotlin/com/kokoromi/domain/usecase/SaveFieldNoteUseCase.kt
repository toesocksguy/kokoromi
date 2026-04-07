package com.kokoromi.domain.usecase

import com.kokoromi.data.repository.FieldNoteRepository
import com.kokoromi.domain.model.FieldNote
import com.kokoromi.util.Constants
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class SaveFieldNoteUseCase @Inject constructor(
    private val fieldNoteRepository: FieldNoteRepository,
) {
    suspend operator fun invoke(
        content: String,
        existingNote: FieldNote? = null,
    ): Result<Unit> {
        val trimmed = content.trim()

        if (trimmed.isBlank()) {
            return Result.failure(IllegalArgumentException("Note content cannot be empty"))
        }

        if (trimmed.length > Constants.FIELD_NOTE_BODY_MAX_CHARS) {
            return Result.failure(IllegalArgumentException("Note exceeds ${Constants.FIELD_NOTE_BODY_MAX_CHARS} characters"))
        }

        val now = Instant.now()
        val note = if (existingNote != null) {
            existingNote.copy(content = trimmed, updatedAt = now)
        } else {
            FieldNote(
                id = UUID.randomUUID().toString(),
                content = trimmed,
                createdAt = now,
                updatedAt = now,
            )
        }

        return runCatching {
            if (existingNote != null) {
                fieldNoteRepository.updateNote(note)
            } else {
                fieldNoteRepository.saveNote(note)
            }
        }
    }
}
