package com.example.weatherforecast.feature.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.core.domain.usecase.AddFavoriteUseCase
import com.example.weatherforecast.core.domain.usecase.GetCurrentForecastUseCase
import com.example.weatherforecast.core.domain.usecase.GetDailyForecastUseCase
import com.example.weatherforecast.core.domain.usecase.GetTodayForecastByCoordinatesUseCase
import com.example.weatherforecast.core.domain.usecase.GetWeekForecastByCoordinatesUseCase
import com.example.weatherforecast.core.domain.usecase.ObserveIsFavoriteByIdentityUseCase
import com.example.weatherforecast.core.domain.usecase.ObserveSelectedCityUseCase
import com.example.weatherforecast.core.domain.usecase.RemoveFavoriteByIdentityUseCase
import com.example.weatherforecast.core.domain.usecase.ReverseGeocodeUseCase
import com.example.weatherforecast.core.model.LocalData
import com.example.weatherforecast.core.model.SearchCity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val getToday: GetCurrentForecastUseCase,
    private val getWeek: GetDailyForecastUseCase,
    private val getTodayAt: GetTodayForecastByCoordinatesUseCase,
    private val getWeekAt: GetWeekForecastByCoordinatesUseCase,
    private val reverseGeocode: ReverseGeocodeUseCase,
    private val addFavorite: AddFavoriteUseCase,
    private val observeIsFavoriteByIdentity: ObserveIsFavoriteByIdentityUseCase,
    private val removeFavoriteByIdentity: RemoveFavoriteByIdentityUseCase,
    observeSelectedCityUseCase: ObserveSelectedCityUseCase
) : ViewModel() {

    val city: StateFlow<SearchCity> =
        observeSelectedCityUseCase()              // Flow<SearchCity?>
            .stateIn(                             // 再升級成 StateFlow
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = LocalData.DefaultCity
            )

    val isFavorite: StateFlow<Boolean> =
        observeIsFavoriteByIdentity(city.value).stateIn(                             // 再升級成 StateFlow
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )
    private val _coordinates = MutableStateFlow<Pair<Double, Double>?>(null)
    val coordinates: StateFlow<Pair<Double, Double>?> = _coordinates

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val uiState: StateFlow<WeatherUiState> = _uiState

    private fun updateTitleFromReverseGeocode(lat: Double, lon: Double) {
        viewModelScope.launch {
            val result = runCatching { reverseGeocode(lat, lon).firstOrNull()?.name }.getOrNull()
        }
    }

    fun loadToday() {
        _uiState.value = WeatherUiState.Loading
        viewModelScope.launch {
            runCatching {
                coordinates.value?.let { (lat, lon) -> getTodayAt(lat, lon) }
                    ?: getToday(city.value)
            }
                .onSuccess { today ->
                    _uiState.value = WeatherUiState.Today(today)
                }
                .onFailure { _uiState.value = WeatherUiState.Error(it.message ?: "Unknown error") }
        }
    }
    fun loadWeek() {
        _uiState.value = WeatherUiState.Loading
        viewModelScope.launch {
            runCatching {
                coordinates.value?.let { (lat, lon) -> getWeekAt(lat, lon) } ?: getWeek(city.value)
            }
                .onSuccess { _uiState.value = WeatherUiState.Week(it) }
                .onFailure { _uiState.value = WeatherUiState.Error(it.message ?: "Unknown error") }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val identity = city.value ?: return@launch
            if (isFavorite.value) {
                removeFavoriteByIdentity(city.value)
            } else {
                addFavorite(identity) // 直接把 SearchCity 存進 favorites（包含 country/state）
            }
        }
    }
}
