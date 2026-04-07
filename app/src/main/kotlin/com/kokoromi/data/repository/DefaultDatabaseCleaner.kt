package com.kokoromi.data.repository

import com.kokoromi.data.db.KokoromiDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultDatabaseCleaner @Inject constructor(
    private val database: KokoromiDatabase,
) : DatabaseCleaner {
    override suspend fun clearAll() = withContext(Dispatchers.IO) {
        database.clearAllTables()
    }
}
