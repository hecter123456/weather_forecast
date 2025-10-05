package com.example.weatherforecast.feature.favorite

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.weatherforecast.core.model.FavoriteCity
import kotlinx.coroutines.launch

@Composable
fun FavoritesScreen(
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel(),
    onCitySelected: (FavoriteCity) -> Unit,
) {
    val favorites by viewModel.favorites.collectAsState()
    val scope = rememberCoroutineScope()

    var editing: FavoriteCity? by remember { mutableStateOf(null) }
    var alias by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    if (editing != null) {
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text("Edit Favorite") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = alias,
                        onValueChange = { alias = it },
                        label = { Text("Alias") })
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Note") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    editing?.let {
                        viewModel.update(
                            it.id,
                            alias.ifBlank { null },
                            note.ifBlank { null })
                    }
                    editing = null
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text("Cancel") } }
        )
    }

    Column(modifier.padding(16.dp)) {
        if (favorites.isEmpty()) {
            Text("No favorites yet. Save a location from the Forecast screen.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(favorites, key = { it.id }) { item ->
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp)) {
                            Column(Modifier
                                .weight(1f)
                                .clickable { onCitySelected(item) }) {
                                Text(
                                    item.alias ?: item.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                val subtitle = buildString {
                                    val parts = listOfNotNull(item.state, item.country)
                                    if (parts.isNotEmpty()) append(parts.joinToString(", "))
                                    if (!item.note.isNullOrBlank()) append("  ·  ${item.note}")
                                }
                                if (subtitle.isNotBlank()) Text(
                                    subtitle,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "lat ${item.lat}, lon ${item.lon}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            IconButton(onClick = {
                                alias = item.alias ?: ""
                                note = item.note ?: ""
                                editing = item
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { scope.launch { viewModel.delete(item.id) } }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}
