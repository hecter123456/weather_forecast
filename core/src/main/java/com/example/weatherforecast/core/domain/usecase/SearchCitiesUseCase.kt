package com.example.weatherforecast.core.domain.usecase

import com.example.weatherforecast.core.domain.repository.WeatherRepository
import com.example.weatherforecast.core.model.SearchCity

class SearchCitiesUseCase(private val repository: WeatherRepository) {
    suspend operator fun invoke(query: String, limit: Int = 10): List<SearchCity> =
        repository.searchCities(query, limit)
}
