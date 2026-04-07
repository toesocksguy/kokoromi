package com.kokoromi.domain.usecase

import com.kokoromi.data.repository.DatabaseCleaner
import javax.inject.Inject

class ClearAllDataUseCase @Inject constructor(
    private val databaseCleaner: DatabaseCleaner,
) {
    suspend operator fun invoke(): Result<Unit> =
        runCatching { databaseCleaner.clearAll() }
}
