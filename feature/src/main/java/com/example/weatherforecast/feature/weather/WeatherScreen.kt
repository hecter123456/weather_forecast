package com.example.weatherforecast.feature.weather

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeatherScreen(modifier: Modifier = Modifier, viewModel: WeatherViewModel = hiltViewModel()) {
    var tabIndex by remember { mutableStateOf(0) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cityId by viewModel.cityId.collectAsStateWithLifecycle()

    LaunchedEffect(tabIndex, cityId) {
        if (tabIndex == 0) viewModel.loadToday() else viewModel.loadWeek()
    }

    Column(modifier) {
        TabRow(selectedTabIndex = tabIndex) {
            Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("Today") })
            Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("This Week") })
        }

        when (uiState) {
            WeatherUiState.Idle, WeatherUiState.Loading -> Box(Modifier.fillMaxWidth().padding(24.dp)) { CircularProgressIndicator() }
            is WeatherUiState.Error -> Text("Error: ${(uiState as WeatherUiState.Error).message}", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            is WeatherUiState.Today -> TodayContent((uiState as WeatherUiState.Today))
            is WeatherUiState.Week -> WeekContent((uiState as WeatherUiState.Week))
        }
    }
}

@Composable
internal fun TodayContent(state: WeatherUiState.Today) {
    val data = state.data
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Current Day", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("7-Day Forecast", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        items(list) { day ->
            ElevatedCard(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(formatDate(day?.dateEpochSeconds?.toLong()?:0L), fontWeight = FontWeight.SemiBold)
                        Text(day?.condition?:"")
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

private fun formatDate(epochSec: Long): String = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(epochSec * 1000))
private fun formatDateTime(epochSec: Long): String = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(epochSec * 1000))
