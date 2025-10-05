package com.example.weatherforecast.feature.searchCity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.core.domain.usecase.SaveSelectedCityUseCase
import com.example.weatherforecast.core.domain.usecase.SearchCitiesUseCase
import com.example.weatherforecast.core.model.SearchCity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchCityViewModel @Inject constructor(
    private val searchCitiesUseCase: SearchCitiesUseCase,
    private val saveSelectedCityUseCase: SaveSelectedCityUseCase
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _results = MutableStateFlow<List<SearchCity>>(emptyList())
    val results: StateFlow<List<SearchCity>> = _results

    private var searchJob: Job? = null

    fun onQueryChange(q: String) {
        _query.value = q
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350)
            val text = _query.value.trim()
            if (text.length < 2) {
                _results.value = emptyList()
                return@launch
            }
            runCatching { searchCitiesUseCase(text, 10) }
                .onSuccess { _results.value = it }
                .onFailure { _results.value = emptyList() }
        }
    }

    fun onCitySelected(item: SearchCity) {
        viewModelScope.launch {
            saveSelectedCityUseCase(item)
        }
    }
}
