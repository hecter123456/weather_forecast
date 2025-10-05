package com.example.weatherforecast.core.domain.usecase

import com.example.weatherforecast.core.domain.repository.WeatherRepository
import com.example.weatherforecast.core.model.SearchCity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSelectedCityUseCase @Inject constructor(private val repository: WeatherRepository) {
    operator fun invoke(): Flow<SearchCity> = repository.observeSelectedCity()
}