package com.kokoromi.domain.usecase

import com.kokoromi.data.repository.FieldNoteRepository
import com.kokoromi.domain.model.FieldNote
import javax.inject.Inject

class GetFieldNoteUseCase @Inject constructor(
    private val fieldNoteRepository: FieldNoteRepository,
) {
    suspend operator fun invoke(noteId: String): FieldNote? =
        fieldNoteRepository.getNote(noteId)
}
