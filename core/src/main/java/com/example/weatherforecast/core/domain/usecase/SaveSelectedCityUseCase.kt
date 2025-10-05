package com.example.weatherforecast.core.domain.usecase

import com.example.weatherforecast.core.domain.repository.WeatherRepository
import com.example.weatherforecast.core.model.SearchCity
import javax.inject.Inject


class SaveSelectedCityUseCase @Inject constructor(private val repository: WeatherRepository) {
    suspend operator fun invoke(city: SearchCity) = repository.saveSelectedCity(city)
}
