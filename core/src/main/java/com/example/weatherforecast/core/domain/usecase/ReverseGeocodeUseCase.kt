package com.example.weatherforecast.core.domain.usecase

import com.example.weatherforecast.core.domain.repository.WeatherRepository
import com.example.weatherforecast.core.model.SearchCity

class ReverseGeocodeUseCase(private val repository: WeatherRepository) {
    suspend operator fun invoke(lat: Double, lon: Double, limit: Int = 1): List<SearchCity> =
        repository.reverseGeocode(lat, lon, limit)
}
