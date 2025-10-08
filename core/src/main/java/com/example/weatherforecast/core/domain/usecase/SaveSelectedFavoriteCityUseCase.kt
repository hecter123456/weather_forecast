package com.example.weatherforecast.core.domain.usecase

import com.example.weatherforecast.core.domain.repository.WeatherRepository
import com.example.weatherforecast.core.model.FavoriteCity
import javax.inject.Inject

class SaveSelectedFavoriteCityUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(favoriteCity: FavoriteCity) {
        val searchCity = repository.favoriteCityToSearchCity(favoriteCity)
        repository.saveSelectedCity(searchCity)
    }
}