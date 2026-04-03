package com.kokoromi.data.repository

import com.kokoromi.domain.model.Completion

interface CompletionRepository {
    suspend fun saveCompletion(completion: Completion)
    suspend fun getCompletion(experimentId: String): Completion?
}
