package com.example.weatherforecast.core.di

import android.content.Context
import com.example.weatherforecast.core.datastore.PrefsDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    @Singleton
    fun providePrefs(@ApplicationContext ctx: Context): PrefsDataStore =
        PrefsDataStore(ctx)
}