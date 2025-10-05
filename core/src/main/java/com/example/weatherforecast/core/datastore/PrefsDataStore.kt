package com.example.weatherforecast.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appDataStore by preferencesDataStore(name = "settings")

class PrefsDataStore(private val context: Context) {

    private val KEY_UNITS = stringPreferencesKey("units")
    private val KEY_LAST_LAT = doublePreferencesKey("last_lat")
    private val KEY_LAST_LON = doublePreferencesKey("last_lon")

    val units: Flow<String> = context.appDataStore.data.map { it[KEY_UNITS] ?: "metric" }
    val lastLat: Flow<Double?> = context.appDataStore.data.map { it[KEY_LAST_LAT] }
    val lastLon: Flow<Double?> = context.appDataStore.data.map { it[KEY_LAST_LON] }

    suspend fun setUnits(units: String) {
        context.appDataStore.edit { it[KEY_UNITS] = units }
    }

    suspend fun setLastCoordinates(lat: Double, lon: Double) {
        context.appDataStore.edit {
            it[KEY_LAST_LAT] = lat
            it[KEY_LAST_LON] = lon
        }
    }
}
