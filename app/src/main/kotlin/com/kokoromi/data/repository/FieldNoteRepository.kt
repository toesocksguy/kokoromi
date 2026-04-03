package com.kokoromi.data.repository

import com.kokoromi.domain.model.FieldNote
import kotlinx.coroutines.flow.Flow

interface FieldNoteRepository {
    fun getAllNotes(): Flow<List<FieldNote>>
    suspend fun getNote(id: String): FieldNote?
    suspend fun saveNote(note: FieldNote)
    suspend fun updateNote(note: FieldNote)
    suspend fun deleteNote(id: String)
}
