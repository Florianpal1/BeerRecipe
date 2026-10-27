package fr.florianpal.beerrecipe.ui.screens.recipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Feuille de sélection d'un ingrédient du catalogue, avec recherche et possibilité
 * de créer une ligne vierge quand l'ingrédient n'existe pas encore en fiche.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> CatalogPickerSheet(
    title: String,
    items: List<T>,
    optionLabel: (T) -> String,
    optionSubtitle: (T) -> String,
    searchText: (T) -> String,
    onPick: (T) -> Unit,
    onDismiss: () -> Unit,
    onCreateBlank: (() -> Unit)? = null,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }
    val filtered = remember(items, query) {
        if (query.isBlank()) items
        else items.filter { searchText(it).contains(query, ignoreCase = true) }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Rechercher") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            onCreateBlank?.let {
                TextButton(onClick = it, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("  Créer une ligne personnalisée")
                }
                HorizontalDivider()
            }
            LazyColumn(Modifier.heightIn(max = 460.dp)) {
                items(filtered) { item ->
                    ListItem(
                        headlineContent = { Text(optionLabel(item)) },
                        supportingContent = {
                            optionSubtitle(item).takeIf { it.isNotBlank() }?.let { Text(it) }
                        },
                        modifier = Modifier.clickable { onPick(item) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
