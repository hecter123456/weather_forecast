package com.example.weatherforecast.feature.citylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.core.domain.usecase.GetCitiesUseCase
import com.example.weatherforecast.core.model.City
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class CityListViewModel @Inject constructor(private val getCities: GetCitiesUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow<CityListUiState>(CityListUiState.Loading)
    val uiState: StateFlow<CityListUiState> = _uiState

    init { refresh() }

    fun refresh() {
        _uiState.value = CityListUiState.Loading
        viewModelScope.launch {
            runCatching { getCities() }
                .onSuccess { _uiState.value = CityListUiState.Data(it) }
                .onFailure { _uiState.value = CityListUiState.Error(it.message ?: "Unknown error") }
        }
    }
}
