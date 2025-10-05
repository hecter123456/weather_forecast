package com.example.weatherforecast.core.domain.repository

import com.example.weatherforecast.core.model.City
import com.example.weatherforecast.core.model.DailyForecast
import com.example.weatherforecast.core.model.FavoriteCity
import com.example.weatherforecast.core.model.SearchCity
import com.example.weatherforecast.core.model.TodayForecast
import kotlinx.coroutines.flow.Flow


interface WeatherRepository {
    suspend fun getCities(): List<City>
    suspend fun getCurrentWeather(city: SearchCity): TodayForecast

    suspend fun getDailyWeather(city: SearchCity): List<DailyForecast?>

    // Geocoding
    suspend fun searchCities(query: String, limit: Int = 10): List<SearchCity>
    suspend fun reverseGeocode(lat: Double, lon: Double, limit: Int = 1): List<SearchCity>

    // Favorites
    fun observeFavorites(): Flow<List<FavoriteCity>>
    suspend fun addFavorite(item: SearchCity, alias: String? = null, note: String? = null): Long
    suspend fun removeFavorite(id: Long)
    suspend fun updateFavorite(id: Long, alias: String?, note: String?)

    // Selected City via DataStore
    fun observeSelectedCity(): Flow<SearchCity>
    suspend fun saveSelectedCity(city: SearchCity)

}
