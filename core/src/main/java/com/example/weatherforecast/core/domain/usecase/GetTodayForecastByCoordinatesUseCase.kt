package com.example.weatherforecast.core.domain.usecase

import com.example.weatherforecast.core.domain.repository.WeatherRepository
import com.example.weatherforecast.core.model.SearchCity
import com.example.weatherforecast.core.model.TodayForecast
import javax.inject.Inject


class GetTodayForecastByCoordinatesUseCase @Inject constructor(private val repository: WeatherRepository) {
    suspend operator fun invoke(lat: Double, lon: Double): TodayForecast {
        val name = try {
            repository.reverseGeocode(lat, lon).firstOrNull()?.name
        } catch (_: Exception) {
            null
        }
        return repository.getCurrentWeather(
            SearchCity(
                name = name ?: "Unknown",
                lat = lat,
                lon = lon
            )
        )
    }
}
