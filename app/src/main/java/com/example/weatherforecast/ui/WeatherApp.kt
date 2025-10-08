package com.example.weatherforecast.ui

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
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.weatherforecast.navigation.Routes
import com.example.weatherforecast.navigation.WeatherNavHost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherApp() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        val navController = rememberNavController()
        Scaffold(
            topBar = { TopAppBar(title = { Text("Weather Forecast") }) },
            bottomBar = { BottomBar(navController) }
        ) { padding ->
            WeatherNavHost(navController, padding)
        }
    }
}

@Composable
internal fun BottomBar(navController: NavHostController) {
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
            label = { Text("Forecast") },
            modifier = Modifier.testTag("Forecast")

        )
        NavigationBarItem(
            selected = current == Routes.Search,
            onClick = {
                navController.navigate(Routes.Search) {
                    popUpTo(navController.graph.findStartDestination().id)
                }
            },
            icon = { Icon(Icons.Filled.Search, contentDescription = null) },
            label = { Text("Search") },
            modifier = Modifier.testTag("Search")
        )
        NavigationBarItem(
            selected = current == Routes.Favorites,
            onClick = {
                navController.navigate(Routes.Favorites) {
                    popUpTo(navController.graph.findStartDestination().id)
                }
            },
            icon = { Icon(Icons.Filled.Favorite, contentDescription = null) },
            label = { Text("Favorites") },
            modifier = Modifier.testTag("Favorites")
        )
    }
}
