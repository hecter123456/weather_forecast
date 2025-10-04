package com.example.weatherforecast.core.domain.usecase

import com.example.weatherforecast.core.domain.repository.WeatherRepository
import com.example.weatherforecast.core.network.model.OneCallResponse
import javax.inject.Inject

class GetCurrentForecastUseCase @Inject constructor(private val repo: WeatherRepository) {
    suspend operator fun invoke(cityId: String): OneCallResponse.Current {
        val cities = repo.getCities()
        return cities.firstOrNull { it.id == cityId }?.let {
            repo.getCurrentWeather(it).current
        }?:  error("Unknown cityId: $cityId")
    }
}