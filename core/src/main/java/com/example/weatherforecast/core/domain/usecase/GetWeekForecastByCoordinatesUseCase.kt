package com.example.weatherforecast.core.domain.usecase

import com.example.weatherforecast.core.domain.repository.WeatherRepository
import com.example.weatherforecast.core.model.City
import com.example.weatherforecast.core.model.DailyForecast


class GetWeekForecastByCoordinatesUseCase(private val repository: WeatherRepository) {
    suspend operator fun invoke(lat: Double, lon: Double): List<DailyForecast?> {
        val name = try {
            repository.reverseGeocode(lat, lon).firstOrNull()?.name
        } catch (_: Exception) {
            null
        }
        return repository.getDailyWeather(City(name = name ?: "Unknown", lat = lat, lon = lon))
    }

}
