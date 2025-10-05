package com.example.weatherforecast.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.weatherforecast.core.model.SearchCity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appDataStore by preferencesDataStore(name = "settings")

class PrefsDataStore(private val context: Context) {

    // existing keys...
    private val KEY_UNITS = stringPreferencesKey("units")
    private val KEY_LAST_LAT = doublePreferencesKey("last_lat")
    private val KEY_LAST_LON = doublePreferencesKey("last_lon")

    // ★ Selected City keys
    private val KEY_SELECTED_NAME = stringPreferencesKey("selected_name")
    private val KEY_SELECTED_COUNTRY = stringPreferencesKey("selected_country")
    private val KEY_SELECTED_STATE = stringPreferencesKey("selected_state")
    private val KEY_SELECTED_LAT = doublePreferencesKey("selected_lat")
    private val KEY_SELECTED_LON = doublePreferencesKey("selected_lon")

    val units: Flow<String> = context.appDataStore.data.map { it[KEY_UNITS] ?: "metric" }
    val lastLat: Flow<Double?> = context.appDataStore.data.map { it[KEY_LAST_LAT] }
    val lastLon: Flow<Double?> = context.appDataStore.data.map { it[KEY_LAST_LON] }

    // ★ Observe selected city
    val selectedCity: Flow<SearchCity?> = context.appDataStore.data.map { prefs ->
        val name = prefs[KEY_SELECTED_NAME] ?: return@map null
        val lat = prefs[KEY_SELECTED_LAT] ?: return@map null
        val lon = prefs[KEY_SELECTED_LON] ?: return@map null
        SearchCity(
            name = name,
            country = prefs[KEY_SELECTED_COUNTRY]?.ifBlank { null },
            state = prefs[KEY_SELECTED_STATE]?.ifBlank { null },
            lat = lat,
            lon = lon
        )
    }

    suspend fun setUnits(units: String) {
        context.appDataStore.edit { it[KEY_UNITS] = units }
    }

    suspend fun setLastCoord(lat: Double, lon: Double) {
        context.appDataStore.edit {
            it[KEY_LAST_LAT] = lat
            it[KEY_LAST_LON] = lon
        }
    }

    // ★ Save & clear selected city
    suspend fun setSelectedCity(city: SearchCity) {
        context.appDataStore.edit {
            it[KEY_SELECTED_NAME] = city.name
            it[KEY_SELECTED_COUNTRY] = city.country.orEmpty()
            it[KEY_SELECTED_STATE] = city.state.orEmpty()
            it[KEY_SELECTED_LAT] = city.lat
            it[KEY_SELECTED_LON] = city.lon
        }
    }

    suspend fun clearSelectedCity() {
        context.appDataStore.edit {
            it.remove(KEY_SELECTED_NAME)
            it.remove(KEY_SELECTED_COUNTRY)
            it.remove(KEY_SELECTED_STATE)
            it.remove(KEY_SELECTED_LAT)
            it.remove(KEY_SELECTED_LON)
        }
    }
}
