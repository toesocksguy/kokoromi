package com.kokoromi.di

import android.content.Context
import androidx.room.Room
import com.kokoromi.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// This module will provide the Room database and DAOs once they are
// created in Milestone 2. The stubs are here so Hilt compiles cleanly.

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    // Database and DAO providers will be added here in Milestone 2.
    // Example shape:
    //
    // @Provides @Singleton
    // fun provideDatabase(@ApplicationContext context: Context): KokoromiDatabase =
    //     Room.databaseBuilder(context, KokoromiDatabase::class.java, Constants.DATABASE_NAME)
    //         .build()
    //
    // @Provides
    // fun provideExperimentDao(db: KokoromiDatabase): ExperimentDao = db.experimentDao()
}
