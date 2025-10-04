package com.example.weatherforecast.feature.citylist

import com.example.weatherforecast.core.model.City

sealed interface CityListUiState {
    data object Loading : CityListUiState
    data class Data(val cities: List<City>) : CityListUiState
    data class Error(val message: String) : CityListUiState
}