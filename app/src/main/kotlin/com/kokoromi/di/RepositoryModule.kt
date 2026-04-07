package com.kokoromi.di

import com.kokoromi.data.repository.CompletionRepository
import com.kokoromi.data.repository.DailyLogRepository
import com.kokoromi.data.repository.DatabaseCleaner
import com.kokoromi.data.repository.DefaultCompletionRepository
import com.kokoromi.data.repository.DefaultDailyLogRepository
import com.kokoromi.data.repository.DefaultDatabaseCleaner
import com.kokoromi.data.repository.DefaultExperimentRepository
import com.kokoromi.data.repository.DefaultFieldNoteRepository
import com.kokoromi.data.repository.DefaultReflectionRepository
import com.kokoromi.data.repository.ExperimentRepository
import com.kokoromi.data.repository.FieldNoteRepository
import com.kokoromi.data.repository.ReflectionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindExperimentRepository(
        impl: DefaultExperimentRepository
    ): ExperimentRepository

    @Binds @Singleton
    abstract fun bindDailyLogRepository(
        impl: DefaultDailyLogRepository
    ): DailyLogRepository

    @Binds @Singleton
    abstract fun bindReflectionRepository(
        impl: DefaultReflectionRepository
    ): ReflectionRepository

    @Binds @Singleton
    abstract fun bindCompletionRepository(
        impl: DefaultCompletionRepository
    ): CompletionRepository

    @Binds @Singleton
    abstract fun bindFieldNoteRepository(
        impl: DefaultFieldNoteRepository
    ): FieldNoteRepository

    @Binds @Singleton
    abstract fun bindDatabaseCleaner(
        impl: DefaultDatabaseCleaner
    ): DatabaseCleaner
}
