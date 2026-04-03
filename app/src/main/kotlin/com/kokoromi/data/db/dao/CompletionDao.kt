package com.kokoromi.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.kokoromi.data.db.entity.CompletionEntity

@Dao
interface CompletionDao {
    @Insert
    suspend fun insertCompletion(completion: CompletionEntity)

    @Query("SELECT * FROM completions WHERE experiment_id = :experimentId")
    suspend fun getCompletion(experimentId: String): CompletionEntity?
}
