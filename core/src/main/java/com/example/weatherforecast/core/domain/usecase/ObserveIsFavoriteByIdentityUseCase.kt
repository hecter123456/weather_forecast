package com.example.weatherforecast.core.domain.usecase

import com.example.weatherforecast.core.domain.repository.WeatherRepository
import com.example.weatherforecast.core.model.SearchCity
import javax.inject.Inject

class ObserveIsFavoriteByIdentityUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    operator fun invoke(city: SearchCity) = repository.observeIsFavorite(city)
}