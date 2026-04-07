package com.kokoromi.data.repository

interface DatabaseCleaner {
    suspend fun clearAll()
}
