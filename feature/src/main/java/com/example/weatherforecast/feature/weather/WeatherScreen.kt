package com.example.weatherforecast.feature.weather

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(modifier: Modifier = Modifier, viewModel: WeatherViewModel = hiltViewModel()) {
    val tabIndex by viewModel.tabIndex.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val city by viewModel.city.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()

    LaunchedEffect(city, tabIndex) {
        viewModel.city.collect {
            if (tabIndex == 0) viewModel.loadToday() else viewModel.loadWeek()
            viewModel.startObservingFavoriteByIdentity()
        }

    }

    Column(modifier) {
        TabRow(selectedTabIndex = tabIndex) {
            Tab(
                selected = tabIndex == 0,
                onClick = { viewModel.setTabIndex(0) },
                text = { Text("Today") })
            Tab(
                selected = tabIndex == 1,
                onClick = { viewModel.setTabIndex(1) },
                text = { Text("This Week") })
        }

        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("City: ${city.name}")
                    Text("Country: ${city.country}")
                    Text("State: ${city.state}")
                    Text("Lat: ${city.lat}")
                    Text("Lon: ${city.lon}")
                    Text("isFavorite: $isFavorite")
                }
            }
        }

        when (uiState) {
            WeatherUiState.Idle, WeatherUiState.Loading -> Box(
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) { CircularProgressIndicator() }

            is WeatherUiState.Error -> Text(
                "Error: ${(uiState as WeatherUiState.Error).message}",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )

            is WeatherUiState.Today -> TodayContent((uiState as WeatherUiState.Today))
            is WeatherUiState.Week -> WeekContent((uiState as WeatherUiState.Week))
        }

        Box(Modifier.fillMaxSize()) {
            FloatingActionButton(
                onClick = { viewModel.toggleFavorite() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = if (isFavorite)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isFavorite)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites"
                )
            }
        }
    }


}

@Composable
internal fun TodayContent(state: WeatherUiState.Today) {
    val data = state.data
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Current Day",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("City: ${data.cityName.uppercase()}")
                Text("Temp: ${data.temperatureC}°C", style = MaterialTheme.typography.headlineSmall)
                Text("Condition: ${data.condition}")
                Text("Wind: ${data.windKph} kph")
                Text("Updated: ${formatDateTime(data.dateEpochSeconds.toLong())}")
            }
        }
    }
}

@Composable
internal fun WeekContent(state: WeatherUiState.Week) {
    val list = state.data
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "7-Day Forecast",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        items(list) { day ->
            ElevatedCard(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            formatDate(day?.dateEpochSeconds?.toLong() ?: 0L),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(day?.condition ?: "")
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Max ${day?.maxTempC}°C")
                        Text("Min ${day?.minTempC}°C")
                    }
                }
            }
        }
    }
}

private fun formatDate(epochSec: Long): String =
    SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(epochSec * 1000))

private fun formatDateTime(epochSec: Long): String =
    SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(epochSec * 1000))
