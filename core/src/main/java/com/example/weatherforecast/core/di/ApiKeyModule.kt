package com.example.weatherforecast.core.di

import com.example.weatherforecast.core.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiKeyModule {

    @Provides
    @Singleton
    @Named("owmApiKey")
    fun provideOwmApiKey(): String = BuildConfig.OPENWEATHER_API_KEY
}
