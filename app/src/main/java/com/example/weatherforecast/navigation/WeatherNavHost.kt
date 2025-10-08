package com.example.weatherforecast.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.weatherforecast.feature.favorite.FavoritesScreen
import com.example.weatherforecast.feature.searchCity.SearchCityScreen
import com.example.weatherforecast.feature.weather.WeatherScreen


@Composable
fun WeatherNavHost(
    navController: NavHostController,
    padding: PaddingValues
) {
    NavHost(navController = navController, startDestination = Routes.Forecast) {
        composable(Routes.Forecast) {
            WeatherScreen(Modifier.padding(padding))
        }
        composable(Routes.Search) {
            SearchCityScreen(Modifier.padding(padding)) {
                navController.navigate(Routes.Forecast) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        inclusive = false
                    }
                }
            }
        }
        composable(Routes.Favorites) {
            FavoritesScreen(Modifier.padding(padding)) {
                navController.navigate(Routes.Forecast) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        inclusive = false
                    }
                }
            }
        }
    }
}