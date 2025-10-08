package com.example.weatherforecast.core.domain.repository

import com.example.weatherforecast.core.datastore.datasource.PreferencesDataSource
import com.example.weatherforecast.core.di.ApplicationScope
import com.example.weatherforecast.core.model.DailyForecast
import com.example.weatherforecast.core.model.FavoriteCity
import com.example.weatherforecast.core.model.SearchCity
import com.example.weatherforecast.core.model.TodayForecast
import com.example.weatherforecast.core.network.datasource.WeatherNetworkDataSource
import com.example.weatherforecast.core.network.request.OneCallRequest
import com.example.weatherforecast.core.network.response.GeoDirectItem
import com.example.weatherforecast.core.network.response.OneCallResponse
import com.example.weatherforecast.core.room.FavoriteCityDao
import com.example.weatherforecast.core.room.FavoriteCityEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val datasource: WeatherNetworkDataSource,
    private val favoriteCityDao: FavoriteCityDao,
    @ApplicationScope private val preferencesDataSource: PreferencesDataSource
) : WeatherRepository {

    override suspend fun getCurrentWeather(city: SearchCity): TodayForecast {
        return datasource.fetchOneCall(request = OneCallRequest(lat = city.lat, lon = city.lon))
            .toToday(city)
    }

    override suspend fun getDailyWeather(city: SearchCity): List<DailyForecast?> {
        return datasource.fetchOneCall(request = OneCallRequest(lat = city.lat, lon = city.lon))
            .toWeek()
    }

    // Geocoding
    override suspend fun searchCities(query: String, limit: Int): List<SearchCity> =
        datasource.geocodeDirect(query, limit).toDomain()

    override suspend fun reverseGeocode(lat: Double, lon: Double, limit: Int): List<SearchCity> =
        datasource.reverseGeocode(lat, lon, limit).toDomain()

    // Favorites
    override fun observeFavorites(): Flow<List<FavoriteCity>> =
        favoriteCityDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun addFavorite(item: SearchCity, alias: String?, note: String?): Long =
        favoriteCityDao.insert(item.toEntity(alias, note))

    override suspend fun removeFavorite(id: Long) =
        favoriteCityDao.deleteById(id)

    override suspend fun updateFavorite(id: Long, alias: String?, note: String?) =
        favoriteCityDao.updateFields(id, alias, note, System.currentTimeMillis())

    override fun observeIsFavorite(city: SearchCity): Flow<Boolean> {
        return favoriteCityDao.countByIdentity(city.name, city.country, city.state).map { it > 0 }
    }

    override suspend fun removeFavorite(city: SearchCity) {
        favoriteCityDao.deleteByIdentity(city.name, city.country, city.state)
    }

    override fun observeSelectedCity(): Flow<SearchCity> {
        return preferencesDataSource.observeSelectedCity()
    }

    override suspend fun saveSelectedCity(city: SearchCity) {
        return preferencesDataSource.saveSelectedCity(city)
    }

    override fun favoriteCityToSearchCity(favoriteCity: FavoriteCity): SearchCity {
        return favoriteCity.toSearchCity()
    }


    fun OneCallResponse.toToday(city: SearchCity) = TodayForecast(
        cityName = city.name,
        dateEpochSeconds = current.dt,
        temperatureC = current.temp,
        condition = current.weather.firstOrNull()?.main ?: "N/A",
        precipitationChance = 0,
        windKph = current.windSpeed * 3.6,
    )

    fun OneCallResponse.toWeek() = daily.take(7).map { d ->
        DailyForecast(
            dateEpochSeconds = d.dt,
            minTempC = d.temp.min,
            maxTempC = d.temp.max,
            condition = d.weather.firstOrNull()?.main ?: "N/A"
        )
    }

    fun List<GeoDirectItem>.toDomain(): List<SearchCity> =
        map {
            SearchCity(
                name = it.name,
                country = it.country,
                state = it.state,
                lat = it.lat,
                lon = it.lon
            )
        }

    fun FavoriteCityEntity.toDomain() = FavoriteCity(
        id = id,
        name = name,
        alias = alias,
        note = note,
        country = country,
        state = state,
        lat = lat,
        lon = lon
    )

    fun SearchCity.toEntity(alias: String? = null, note: String? = null) = FavoriteCityEntity(
        name = name,
        alias = alias,
        note = note,
        country = country,
        state = state,
        lat = lat,
        lon = lon
    )

    fun FavoriteCity.toSearchCity() = SearchCity(
        name = name,
        country = country,
        state = state,
        lat = lat,
        lon = lon
    )
}
