package com.example.weatherforecast.core.domain.repository

import com.example.weatherforecast.core.model.City
import com.example.weatherforecast.core.network.model.OneCallResponse


interface WeatherRepository {
    suspend fun getCities(): List<City>
    suspend fun getCurrentWeather(city: City): OneCallResponse
}
