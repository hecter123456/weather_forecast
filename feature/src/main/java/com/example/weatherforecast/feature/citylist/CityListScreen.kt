package com.example.weatherforecast.feature.citylist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weatherforecast.core.model.City

@Composable
fun CityListScreen(modifier: Modifier = Modifier, viewModel: CityListViewModel = hiltViewModel(), onCitySelected: (String) -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Box(modifier) {
        when (uiState) {
            is CityListUiState.Loading -> Text("Loading...", modifier = Modifier.padding(16.dp))
            is CityListUiState.Error -> Text((uiState as CityListUiState.Error).message, modifier = Modifier.padding(16.dp))
            is CityListUiState.Data -> CityList((uiState as CityListUiState.Data).cities, onCitySelected)
        }
    }
}

@Composable
private fun CityList(cities: List<City>, onCitySelected: (String) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(8.dp)) {
        items(cities) { city ->
            ListItem(
                headlineContent = { Text(city.name) },
                supportingContent = { Text(city.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onCitySelected(city.id) }
            )
            Divider()
        }
    }
}
