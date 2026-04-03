package com.kokoromi.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.kokoromi.data.repository.DefaultPreferencesRepository
import com.kokoromi.data.repository.PreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kokoromi_prefs")

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesModule {

    @Binds @Singleton
    abstract fun bindPreferencesRepository(
        impl: DefaultPreferencesRepository
    ): PreferencesRepository

    companion object {
        @Provides
        @Singleton
        fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
            context.dataStore
    }
}
