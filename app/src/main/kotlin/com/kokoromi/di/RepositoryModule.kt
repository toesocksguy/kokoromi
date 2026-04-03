package com.kokoromi.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// Repository bindings (interface → implementation) will be added here
// in Milestone 2 once the data layer exists.
//
// Example shape:
//
// @Binds @Singleton
// abstract fun bindExperimentRepository(
//     impl: DefaultExperimentRepository
// ): ExperimentRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule
