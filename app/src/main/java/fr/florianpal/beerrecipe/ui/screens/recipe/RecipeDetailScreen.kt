package fr.florianpal.beerrecipe.ui.screens.recipe

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.florianpal.beerrecipe.ui.common.NumberField
import fr.florianpal.beerrecipe.ui.common.appContainer

private val TABS = listOf(
    "Résumé", "Maque", "Houblons", "Levures", "Additifs", "Procédé", "Journal",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(recipeId: Long, onBack: () -> Unit) {
    val container = appContainer()
    val viewModel: RecipeDetailViewModel = viewModel(
        key = "recipe-$recipeId",
        factory = RecipeDetailViewModel.factory(container.repository, recipeId),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val catalog by viewModel.catalog.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var menuOpen by remember { mutableStateOf(false) }
    var showScaleDialog by remember { mutableStateOf(false) }

    val current = state
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(current?.context?.details?.recipe?.name ?: "Recette") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    val isFavourite = current?.context?.details?.recipe?.isFavourite == true
                    IconButton(onClick = { viewModel.toggleFavourite() }) {
                        Icon(
                            if (isFavourite) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Favori",
                            tint = if (isFavourite) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline,
                        )
                    }
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Actions")
                        }
                        DropdownMenu(menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("Mettre à l'échelle") },
                                leadingIcon = { Icon(Icons.Default.Straighten, null) },
                                onClick = { menuOpen = false; showScaleDialog = true },
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (current == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(Modifier.fillMaxSize().padding(padding)) {
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 8.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                TABS.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) },
                    )
                }
            }

            when (selectedTab) {
                0 -> RecipeSummaryTab(current, catalog, viewModel)
                1 -> RecipeMaltTab(current, catalog, viewModel)
                2 -> RecipeHopTab(current, catalog, viewModel)
                3 -> RecipeYeastTab(current, catalog, viewModel)
                4 -> RecipeAdditiveTab(current, catalog, viewModel)
                5 -> RecipeProcessTab(current, catalog, viewModel)
                6 -> RecipeLogTab(current, sessions, viewModel)
            }
        }
    }

    if (showScaleDialog && current != null) {
        var target by remember { mutableStateOf(current.context.details.recipe.batchVolumeL) }
        AlertDialog(
            onDismissRequest = { showScaleDialog = false },
            title = { Text("Mettre la recette à l'échelle") },
            text = {
                Column {
                    Text(
                        "Toutes les quantités seront multipliées par le rapport des volumes. " +
                            "Le rendement, lui, reste inchangé.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    NumberField(
                        label = "Nouveau volume",
                        value = target,
                        onValueChange = { target = it },
                        suffix = "L",
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.scaleTo(target)
                    showScaleDialog = false
                }) { Text("Appliquer") }
            },
            dismissButton = {
                TextButton(onClick = { showScaleDialog = false }) { Text("Annuler") }
            },
        )
    }
}
