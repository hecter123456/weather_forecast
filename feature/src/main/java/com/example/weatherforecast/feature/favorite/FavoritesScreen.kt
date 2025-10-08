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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.weatherforecast.core.model.FavoriteCity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun FavoritesScreen(
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel(),
    onNavigateToWeather: () -> Unit,
) {
    val favorites by viewModel.favorites.collectAsState()
    val scope = rememberCoroutineScope()

    var editing: FavoriteCity? by remember { mutableStateOf(null) }
    var alias by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    FavoritesScreen(
        favorites = favorites,
        scope = scope,
        editing = editing,
        alias = alias,
        note = note,
        onCitySelected = viewModel::saveSelectedCity,
        onEdit = { favorite: FavoriteCity? -> editing = favorite },
        onAliasChanged = { it: String -> alias = it },
        onNoteChanged = { it: String -> note = it },
        onUpdate = viewModel::update,
        onDelete = viewModel::delete,
        onNavigateToWeather = onNavigateToWeather,
        modifier = modifier
    )
}

@Composable
internal fun FavoritesScreen(
    favorites: List<FavoriteCity>,
    scope: CoroutineScope,
    editing: FavoriteCity?,
    alias: String,
    note: String,
    onCitySelected: (FavoriteCity) -> Unit,
    onEdit: (FavoriteCity?) -> Unit,
    onAliasChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onUpdate: (id: Long, alias: String?, note: String?) -> Unit,
    onDelete: (Long) -> Unit,
    onNavigateToWeather: () -> Unit,
    modifier: Modifier
) {
    if (editing != null) {
        AlertDialog(
            onDismissRequest = { onEdit(null) },
            title = { Text("Edit Favorite") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = alias,
                        onValueChange = { onAliasChanged(it) },
                        label = { Text("Alias") })
                    OutlinedTextField(
                        value = note,
                        onValueChange = { onNoteChanged(it) },
                        label = { Text("Note") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    editing?.let {
                        onUpdate(
                            it.id,
                            alias.ifBlank { null },
                            note.ifBlank { null })
                    }
                    onEdit(null)
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { onEdit(null) }) { Text("Cancel") } }
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
                            Column(
                                Modifier
                                    .weight(1f)
                                    .clickable {
                                        onCitySelected(item)
                                        onNavigateToWeather()
                                    }
                                    .testTag("favoriteRow_${item.id}")
                            ) {
                                Text(
                                    "${item.name} ${item.alias?.let { "($it)" } ?: ""}",
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
                                onAliasChanged(item.alias ?: "")
                                onNoteChanged(item.note ?: "")
                                onEdit(item)
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { scope.launch { onDelete(item.id) } }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}
