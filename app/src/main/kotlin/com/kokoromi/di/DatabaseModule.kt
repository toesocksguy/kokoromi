package com.kokoromi.di

import android.content.Context
import androidx.room.Room
import com.kokoromi.data.db.KokoromiDatabase
import com.kokoromi.data.db.dao.CompletionDao
import com.kokoromi.data.db.dao.DailyLogDao
import com.kokoromi.data.db.dao.ExperimentDao
import com.kokoromi.data.db.dao.FieldNoteDao
import com.kokoromi.data.db.dao.ReflectionDao
import com.kokoromi.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KokoromiDatabase =
        Room.databaseBuilder(context, KokoromiDatabase::class.java, Constants.DATABASE_NAME)
            .build()

    @Provides
    fun provideExperimentDao(db: KokoromiDatabase): ExperimentDao = db.experimentDao()

    @Provides
    fun provideDailyLogDao(db: KokoromiDatabase): DailyLogDao = db.dailyLogDao()

    @Provides
    fun provideReflectionDao(db: KokoromiDatabase): ReflectionDao = db.reflectionDao()

    @Provides
    fun provideCompletionDao(db: KokoromiDatabase): CompletionDao = db.completionDao()

    @Provides
    fun provideFieldNoteDao(db: KokoromiDatabase): FieldNoteDao = db.fieldNoteDao()
}
