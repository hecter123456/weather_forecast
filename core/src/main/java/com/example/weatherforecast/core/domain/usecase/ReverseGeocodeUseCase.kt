package com.example.weatherforecast.core.domain.usecase

import com.example.weatherforecast.core.domain.repository.WeatherRepository
import com.example.weatherforecast.core.model.SearchCity
import javax.inject.Inject

class ReverseGeocodeUseCase @Inject constructor(private val repository: WeatherRepository) {
    suspend operator fun invoke(lat: Double, lon: Double, limit: Int = 1): List<SearchCity> =
        repository.reverseGeocode(lat, lon, limit)
}
