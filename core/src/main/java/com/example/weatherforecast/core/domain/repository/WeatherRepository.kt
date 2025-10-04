package com.example.weatherforecast.core.domain.repository

import com.example.weatherforecast.core.model.City
import com.example.weatherforecast.core.model.DailyForecast
import com.example.weatherforecast.core.model.TodayForecast


interface WeatherRepository {
    suspend fun getCities(): List<City>
    suspend fun getCurrentWeather(city: City): TodayForecast

    suspend fun getDailyWeather(city: City): List<DailyForecast?>

}
