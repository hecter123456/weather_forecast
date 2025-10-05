package com.example.weatherforecast.core.datastore.datasource

import com.example.weatherforecast.core.datastore.PrefsDataStore
import com.example.weatherforecast.core.model.LocalData
import com.example.weatherforecast.core.model.SearchCity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class PreferencesDataSourceImpl @Inject constructor(
    private val datastore: PrefsDataStore,
    private val coroutineScope: CoroutineScope
) : PreferencesDataSource {
    private val cache = MutableStateFlow<SearchCity>(LocalData.DefaultCity)

    init {
        //  After init, overwrite the cache with the DataStore value to keep it consistent
        coroutineScope.launch {
            datastore.selectedCity.collect { fromStore ->
                cache.value = fromStore ?: LocalData.DefaultCity
            }
        }
    }

    override fun observeSelectedCity(): Flow<SearchCity> {
        return cache
    }

    override suspend fun saveSelectedCity(city: SearchCity) {
        cache.value = city
        return datastore.setSelectedCity(city)
    }
}



