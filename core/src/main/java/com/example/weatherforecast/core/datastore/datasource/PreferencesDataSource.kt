package com.example.weatherforecast.core.datastore.datasource

import com.example.weatherforecast.core.model.SearchCity
import kotlinx.coroutines.flow.Flow

interface PreferencesDataSource {
    fun observeSelectedCity(): Flow<SearchCity>
    suspend fun saveSelectedCity(city: SearchCity)
}