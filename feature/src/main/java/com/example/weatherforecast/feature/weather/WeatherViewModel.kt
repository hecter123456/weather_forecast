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
import kotlinx.coroutines.Job
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

    private val _tabIndex = MutableStateFlow(0)
    val tabIndex: StateFlow<Int> = _tabIndex
    val city: StateFlow<SearchCity> =
        observeSelectedCityUseCase()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = LocalData.DefaultCity
            )
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val uiState: StateFlow<WeatherUiState> = _uiState

    private var favoriteObserveJob: Job? = null

    fun startObservingFavoriteByIdentity() {
        favoriteObserveJob?.cancel()
        favoriteObserveJob = viewModelScope.launch {
            val identity = city.value ?: return@launch
            observeIsFavoriteByIdentity(identity)
                .collect { _isFavorite.value = it }
        }
    }

    fun setTabIndex(index: Int) {
        _tabIndex.value = index
    }

    fun loadToday() {
        _uiState.value = WeatherUiState.Loading
        viewModelScope.launch {
            runCatching {
                getToday(city.value)
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
                getWeek(city.value)
            }
                .onSuccess { _uiState.value = WeatherUiState.Week(it) }
                .onFailure { _uiState.value = WeatherUiState.Error(it.message ?: "Unknown error") }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val identity = city.value ?: return@launch
            if (isFavorite.value) {
                removeFavoriteByIdentity(identity)
            } else {
                addFavorite(identity) // 直接把 SearchCity 存進 favorites（包含 country/state）
            }
        }
    }
}
