package com.kokoromi.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.kokoromi.data.db.entity.FieldNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FieldNoteDao {
    @Query("SELECT * FROM field_notes ORDER BY created_at DESC")
    fun getAllNotes(): Flow<List<FieldNoteEntity>>

    @Query("SELECT * FROM field_notes WHERE id = :id")
    suspend fun getNote(id: String): FieldNoteEntity?

    @Upsert
    suspend fun upsertNote(note: FieldNoteEntity)

    @Query("DELETE FROM field_notes WHERE id = :id")
    suspend fun deleteNote(id: String)
}
