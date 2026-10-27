package fr.florianpal.beerrecipe.ui.screens.recipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.florianpal.beerrecipe.ui.common.NumberField

/** Quantité proposée à l'ajout d'un ingrédient : valeur par défaut et unité affichée. */
data class PickAmount(
    val default: Double,
    val suffix: String,
    val decimals: Int = 1,
)

/**
 * Feuille de sélection d'un ingrédient du catalogue, avec recherche et possibilité
 * de créer une ligne vierge quand l'ingrédient n'existe pas encore en fiche.
 * Le choix d'un ingrédient ouvre une seconde étape où l'on fixe la quantité à ajouter.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> CatalogPickerSheet(
    title: String,
    items: List<T>,
    optionLabel: (T) -> String,
    optionSubtitle: (T) -> String,
    searchText: (T) -> String,
    amountOf: (T) -> PickAmount,
    onPick: (item: T, amount: Double) -> Unit,
    onDismiss: () -> Unit,
    onCreateBlank: (() -> Unit)? = null,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<T?>(null) }
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
            val picked = selected
            if (picked != null) {
                key(picked) {
                    QuantityStep(
                        label = optionLabel(picked),
                        subtitle = optionSubtitle(picked),
                        amount = amountOf(picked),
                        onBack = { selected = null },
                        onConfirm = { onPick(picked, it) },
                    )
                }
            } else {
                Text(title, style = MaterialTheme.typography.titleMedium)
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
                            modifier = Modifier.clickable { selected = item },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

/** Seconde étape de la feuille : la quantité de l'ingrédient retenu, avant ajout. */
@Composable
private fun QuantityStep(
    label: String,
    subtitle: String,
    amount: PickAmount,
    onBack: () -> Unit,
    onConfirm: (Double) -> Unit,
) {
    var value by remember { mutableStateOf(amount.default) }
    Text(label, style = MaterialTheme.typography.titleMedium)
    subtitle.takeIf { it.isNotBlank() }?.let {
        Text(
            it,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    NumberField(
        "Quantité", value,
        { v -> value = v },
        suffix = amount.suffix, decimals = amount.decimals,
    )
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
    ) {
        TextButton(onClick = onBack) { Text("Retour") }
        Button(onClick = { onConfirm(value) }, enabled = value > 0) { Text("Ajouter") }
    }
}
