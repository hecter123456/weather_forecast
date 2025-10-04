package com.example.weatherforecast.core.di

import com.example.weatherforecast.core.domain.repository.WeatherRepository
import com.example.weatherforecast.core.domain.repository.WeatherRepositoryImpl
import com.example.weatherforecast.core.network.datasource.WeatherNetworkDataSource
import com.example.weatherforecast.core.network.datasource.WeatherNetworkDataSourceImpl
import com.example.weatherforecast.core.network.retrofit.OpenWeatherApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://api.openweathermap.org/"
    private const val API_KEY = "7ea9d9be41951166ceb28a81f215f882"

    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
        .build()

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides @Singleton fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideApi(retrofit: Retrofit): OpenWeatherApi = retrofit.create(OpenWeatherApi::class.java)

    @Provides
    @Singleton
    fun provideDataSource(api: OpenWeatherApi): WeatherNetworkDataSource = WeatherNetworkDataSourceImpl(api,
        API_KEY
    )

    @Provides
    @Singleton
    fun provideRepository(datasource: WeatherNetworkDataSource): WeatherRepository =
        WeatherRepositoryImpl(datasource)
}