package com.kokoromi.domain.usecase

import com.kokoromi.data.repository.FieldNoteRepository
import com.kokoromi.domain.model.FieldNote
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFieldNotesUseCase @Inject constructor(
    private val fieldNoteRepository: FieldNoteRepository,
) {
    operator fun invoke(): Flow<List<FieldNote>> = fieldNoteRepository.getAllNotes()
}
