package com.example.weatherforecast.feature.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.weatherforecast.feature.citylist.CityListScreen
import com.example.weatherforecast.feature.weather.WeatherScreen
import com.example.weatherforecast.feature.weather.WeatherViewModel

object Routes {
    const val Forecast = "forecast"
    const val Cities = "cities"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        val nav = rememberNavController()
        Scaffold(
            topBar = { TopAppBar(title = { Text("Weather Forecast") }) },
            bottomBar = { BottomBar(nav) }
        ) { padding ->
            NavHost(navController = nav, startDestination = Routes.Forecast) {
                composable(Routes.Forecast) {
                    WeatherScreen(Modifier.padding(padding))
                }
                composable(Routes.Cities) {
                    CityListScreen(Modifier.padding(padding)) { id ->
                        nav.navigate(Routes.Forecast) {
                            popUpTo(nav.graph.findStartDestination().id) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomBar(nav: NavHostController) {
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route ?: Routes.Forecast
    NavigationBar {
        NavigationBarItem(
            selected = current == Routes.Forecast,
            onClick = { nav.navigate(Routes.Forecast) { popUpTo(nav.graph.findStartDestination().id); launchSingleTop = true } },
            icon = { Icon(Icons.Default.Cloud, contentDescription = null) },
            label = { Text("Forecast") }
        )
        NavigationBarItem(
            selected = current == Routes.Cities,
            onClick = { nav.navigate(Routes.Cities) { popUpTo(nav.graph.findStartDestination().id); launchSingleTop = true } },
            icon = { Icon(Icons.Default.LocationCity, contentDescription = null) },
            label = { Text("Cities") }
        )
    }
}
