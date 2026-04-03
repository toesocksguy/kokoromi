package com.kokoromi.data.repository

import com.kokoromi.data.db.dao.FieldNoteDao
import com.kokoromi.data.db.entity.FieldNoteEntity
import com.kokoromi.domain.model.FieldNote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultFieldNoteRepository @Inject constructor(
    private val fieldNoteDao: FieldNoteDao
) : FieldNoteRepository {

    override fun getAllNotes(): Flow<List<FieldNote>> {
        return fieldNoteDao.getAllNotes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getNote(id: String): FieldNote? {
        return fieldNoteDao.getNote(id)?.toDomain()
    }

    override suspend fun saveNote(note: FieldNote) {
        fieldNoteDao.upsertNote(note.toEntity())
    }

    override suspend fun updateNote(note: FieldNote) {
        fieldNoteDao.upsertNote(note.toEntity())
    }

    override suspend fun deleteNote(id: String) {
        fieldNoteDao.deleteNote(id)
    }

    // --- Mapping ---

    private fun FieldNote.toEntity() = FieldNoteEntity(
        id = id,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun FieldNoteEntity.toDomain() = FieldNote(
        id = id,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
