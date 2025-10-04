package com.example.weatherforecast.feature.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.core.domain.usecase.GetCurrentForecastUseCase
import com.example.weatherforecast.core.domain.usecase.GetDailyForecastUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val getToday: GetCurrentForecastUseCase,
    private val getWeek: GetDailyForecastUseCase,
) : ViewModel() {

    private val _cityId = MutableStateFlow("tpe")
    val cityId: StateFlow<String> = _cityId

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val uiState: StateFlow<WeatherUiState> = _uiState

    fun setCity(cityId: String) { _cityId.value = cityId }

    fun loadToday() {
        _uiState.value = WeatherUiState.Loading
        viewModelScope.launch {
            runCatching { getToday(cityId.value) }
                .onSuccess { _uiState.value = WeatherUiState.Today(it) }
                .onFailure { _uiState.value = WeatherUiState.Error(it.message ?: "Unknown error") }
        }
    }
    fun loadWeek() {
        _uiState.value = WeatherUiState.Loading
        viewModelScope.launch {
            runCatching { getWeek(cityId.value) }
                .onSuccess { _uiState.value = WeatherUiState.Week(it) }
                .onFailure { _uiState.value = WeatherUiState.Error(it.message ?: "Unknown error") }
        }
    }
}

