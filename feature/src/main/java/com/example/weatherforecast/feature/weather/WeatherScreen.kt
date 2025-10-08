package com.example.weatherforecast.feature.weather

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
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
import com.example.weatherforecast.core.model.SearchCity
import com.example.weatherforecast.core.utils.formatDate
import com.example.weatherforecast.core.utils.formatDateTime
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(modifier: Modifier = Modifier, viewModel: WeatherViewModel = hiltViewModel()) {
    val tabIndex by viewModel.tabIndex.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val city by viewModel.city.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        combine(viewModel.city, viewModel.tabIndex) { c, t -> c to t }
            .collectLatest { (_, tab) ->
                if (tab == 0) viewModel.loadToday() else viewModel.loadWeek()
                viewModel.startObservingFavoriteByIdentity()
            }

    }

    WeatherScreen(
        tabIndex = tabIndex,
        uiState = uiState,
        city = city,
        isFavorite = isFavorite,
        setTabIndex = viewModel::setTabIndex,
        toggleFavorite = viewModel::toggleFavorite,
        modifier = modifier
    )
}

@Composable
internal fun WeatherScreen(
    tabIndex: Int,
    uiState: WeatherUiState,
    city: SearchCity,
    isFavorite: Boolean,
    setTabIndex: (Int) -> Unit,
    toggleFavorite: () -> Unit,
    modifier: Modifier
) {
    LazyColumn(modifier = modifier) {
        item {
            PrimaryTabRow(selectedTabIndex = tabIndex) {
                Tab(
                    selected = tabIndex == 0,
                    onClick = { setTabIndex(0) },
                    text = { Text("Today") })
                Tab(
                    selected = tabIndex == 1,
                    onClick = { setTabIndex(1) },
                    text = { Text("This Week") })
            }
        }
        item {
            ElevatedCard(Modifier
                .fillMaxWidth()
                .padding(12.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row {
                        Text("City: ${city.name}")
                        Spacer(Modifier.weight(1f))
                        FloatingActionButton(
                            onClick = { toggleFavorite() },
                            modifier = Modifier.size(24.dp, 24.dp),
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
                    Text("Country: ${city.country}")
                    Text("State: ${city.state}")
                    Text("Lat: ${city.lat}")
                    Text("Lon: ${city.lon}")
                }
            }
        }

        item {
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

                is WeatherUiState.Today -> TodayContent(
                    (uiState as WeatherUiState.Today),
                    toggleFavorite,
                    isFavorite
                )

                is WeatherUiState.Week -> WeekContent((uiState as WeatherUiState.Week))
            }
        }
    }

}

@Composable
internal fun TodayContent(
    state: WeatherUiState.Today,
    toggleFavorite: () -> Unit,
    isFavorite: Boolean
) {
    val data = state.data
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

//        Box(Modifier.fillMaxSize()) {
//
//        }
    }
}

@Composable
internal fun WeekContent(state: WeatherUiState.Week) {
    val list = state.data
    Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "7-Day Forecast",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        list.map { day ->
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
        Spacer(Modifier.padding(vertical = 2.dp))
    }
}
