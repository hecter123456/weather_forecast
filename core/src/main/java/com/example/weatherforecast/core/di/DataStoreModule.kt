package com.example.weatherforecast.core.di

import android.content.Context
import com.example.weatherforecast.core.datastore.PrefsDataStore
import com.example.weatherforecast.core.datastore.datasource.PreferencesDataSource
import com.example.weatherforecast.core.datastore.datasource.PreferencesDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    @Singleton
    fun providePrefs(@ApplicationContext ctx: Context): PrefsDataStore =
        PrefsDataStore(ctx)

    @Provides
    @Singleton
    internal fun providePreferenceDataSource(
        datastore: PrefsDataStore,
        @ApplicationScope coroutineScope: CoroutineScope
    ): PreferencesDataSource = PreferencesDataSourceImpl(datastore, coroutineScope)
}