package com.kokoromi.domain.usecase

import com.kokoromi.data.repository.FieldNoteRepository
import javax.inject.Inject

class DeleteFieldNoteUseCase @Inject constructor(
    private val fieldNoteRepository: FieldNoteRepository,
) {
    suspend operator fun invoke(noteId: String): Result<Unit> =
        runCatching { fieldNoteRepository.deleteNote(noteId) }
}
