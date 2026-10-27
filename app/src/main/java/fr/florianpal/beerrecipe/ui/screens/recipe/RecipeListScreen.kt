package fr.florianpal.beerrecipe.ui.screens.recipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.florianpal.beerrecipe.domain.model.Format
import fr.florianpal.beerrecipe.ui.common.BeerColorSwatch
import fr.florianpal.beerrecipe.ui.common.EmptyState
import fr.florianpal.beerrecipe.ui.common.appContainer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(onOpenRecipe: (Long) -> Unit) {
    val container = appContainer()
    val viewModel: RecipeListViewModel =
        viewModel(factory = RecipeListViewModel.factory(container.repository))
    val recipes by viewModel.recipes.collectAsStateWithLifecycle()
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var showCreateDialog by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<RecipeCardState?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { TopAppBar(title = { Text("Mes recettes") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nouvelle recette") },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::search,
                label = { Text("Rechercher") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            if (recipes.isEmpty()) {
                EmptyState(
                    message = if (query.isBlank()) "Aucune recette pour l'instant."
                    else "Aucune recette ne correspond à cette recherche.",
                    hint = if (query.isBlank())
                        "Commencez par créer une recette, puis composez sa maische et son houblonnage."
                    else null,
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(recipes, key = { it.context.details.recipe.id }) { card ->
                        RecipeCard(
                            card = card,
                            onClick = { onOpenRecipe(card.context.details.recipe.id) },
                            onToggleFavourite = { viewModel.toggleFavourite(card) },
                            onDuplicate = { viewModel.duplicate(card) },
                            onDelete = { pendingDelete = card },
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateRecipeDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name ->
                showCreateDialog = false
                scope.launch { onOpenRecipe(viewModel.createRecipe(name)) }
            },
        )
    }

    pendingDelete?.let { card ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Supprimer la recette ?") },
            text = {
                Text(
                    "« ${card.context.details.recipe.name} » et tous ses ingrédients seront " +
                        "définitivement supprimés."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(card)
                    pendingDelete = null
                }) { Text("Supprimer") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Annuler") }
            },
        )
    }
}

@Composable
private fun RecipeCard(
    card: RecipeCardState,
    onClick: () -> Unit,
    onToggleFavourite: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
) {
    val recipe = card.context.details.recipe
    val stats = card.stats
    var menuOpen by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BeerColorSwatch(stats.ebc, size = 40)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(recipe.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        card.context.style?.displayName ?: "Style libre",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onToggleFavourite) {
                    Icon(
                        if (recipe.isFavourite) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favori",
                        tint = if (recipe.isFavourite) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline,
                    )
                }
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Actions")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Dupliquer") },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, null) },
                            onClick = { menuOpen = false; onDuplicate() },
                        )
                        DropdownMenuItem(
                            text = { Text("Supprimer") },
                            leadingIcon = { Icon(Icons.Default.Delete, null) },
                            onClick = { menuOpen = false; onDelete() },
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MiniStat("OG", Format.gravity(stats.og))
                MiniStat("ABV", Format.abv(stats.abv))
                MiniStat("IBU", Format.ibu(stats.ibu))
                MiniStat("EBC", Format.ebc(stats.ebc))
                MiniStat("Volume", Format.liters(stats.water.batchVolumeL))
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun CreateRecipeDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvelle recette") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nom de la recette") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onConfirm(name.trim()) },
            ) { Text("Créer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } },
    )
}
