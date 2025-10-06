package com.example.weatherforecast.feature.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.weatherforecast.feature.favorite.FavoritesScreen
import com.example.weatherforecast.feature.searchCity.SearchCityScreen
import com.example.weatherforecast.feature.weather.WeatherScreen

object Routes {
    const val Forecast = "forecast"
    const val Search = "search"
    const val Favorites = "favorites"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        val navController = rememberNavController()
        Scaffold(
            topBar = { TopAppBar(title = { Text("Weather Forecast") }) },
            bottomBar = { BottomBar(navController) }
        ) { padding ->
            NavHost(navController = navController, startDestination = Routes.Forecast) {
                composable(Routes.Forecast) {
                    WeatherScreen(Modifier.padding(padding))
                }
                composable(Routes.Search) {
                    SearchCityScreen(Modifier.padding(padding)) { selected ->
                        navController.navigate(Routes.Forecast) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                            }
                        }
                    }
                }
                composable(Routes.Favorites) {
                    FavoritesScreen(Modifier.padding(padding)) { favorite ->
                        navController.navigate(Routes.Forecast) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomBar(navController: NavHostController) {
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination?.route ?: Routes.Forecast
    NavigationBar {
        NavigationBarItem(
            selected = current == Routes.Forecast,
            onClick = {
                navController.navigate(Routes.Forecast) {
                    popUpTo(navController.graph.findStartDestination().id)
                }
            },
            icon = { Icon(Icons.Default.Cloud, contentDescription = null) },
            label = { Text("Forecast") }
        )
        NavigationBarItem(
            selected = current == Routes.Search,
            onClick = {
                navController.navigate(Routes.Search) {
                    popUpTo(navController.graph.findStartDestination().id)
                }
            },
            icon = { Icon(Icons.Filled.Search, contentDescription = null) },
            label = { Text("Search") }
        )
        NavigationBarItem(
            selected = current == Routes.Favorites,
            onClick = {
                navController.navigate(Routes.Favorites) {
                    popUpTo(navController.graph.findStartDestination().id)
                }
            },
            icon = { Icon(Icons.Filled.Favorite, contentDescription = null) },
            label = { Text("Favorites") }
        )
    }
}
