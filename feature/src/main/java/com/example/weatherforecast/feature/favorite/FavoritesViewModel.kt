package com.example.weatherforecast.feature.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.core.domain.usecase.ObserveFavoritesUseCase
import com.example.weatherforecast.core.domain.usecase.RemoveFavoriteUseCase
import com.example.weatherforecast.core.domain.usecase.UpdateFavoriteUseCase
import com.example.weatherforecast.core.model.FavoriteCity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    observeFavorites: ObserveFavoritesUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
    private val updateFavoriteUseCase: UpdateFavoriteUseCase
) : ViewModel() {

    val favorites: StateFlow<List<FavoriteCity>> = observeFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(id: Long) = viewModelScope.launch { removeFavoriteUseCase(id) }
    fun update(id: Long, alias: String?, note: String?) = viewModelScope.launch {
        updateFavoriteUseCase(id, alias, note)
    }
}
