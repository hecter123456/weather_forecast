package com.example.weatherforecast.core.domain.usecase

import com.example.weatherforecast.core.domain.repository.WeatherRepository
import com.example.weatherforecast.core.model.City
import javax.inject.Inject

class GetCitiesUseCase@Inject constructor(private val repo: WeatherRepository) {
    suspend operator fun invoke(): List<City> {
        return repo.getCities()
    }
}