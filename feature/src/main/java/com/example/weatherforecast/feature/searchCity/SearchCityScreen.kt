package com.example.weatherforecast.feature.searchCity

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.weatherforecast.core.model.SearchCity

@Composable
fun SearchCityScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchCityViewModel = hiltViewModel(),
    onCitySelected: (SearchCity) -> Unit,
) {
    val query by viewModel.query.collectAsState()
    val results by viewModel.results.collectAsState()

    Column(modifier.padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.onQueryChange(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search city") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        if (results.isEmpty() && query.isNotBlank()) {
            Text("No results", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(results) { item ->
                    ResultRow(item) {
                        viewModel.onCitySelected(item)
                        onCitySelected(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultRow(item: SearchCity, onClick: () -> Unit) {
    ElevatedCard(Modifier
        .fillMaxWidth()
        .clickable { onClick() }) {
        Column(Modifier.padding(16.dp)) {
            Text(
                item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            val subtitle = listOfNotNull(item.state, item.country).joinToString(", ")
            if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            Text("lat ${item.lat}, lon ${item.lon}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
