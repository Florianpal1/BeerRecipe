package fr.florianpal.beerrecipe.ui.screens.recipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.florianpal.beerrecipe.data.seed.ReferenceRecipe
import fr.florianpal.beerrecipe.domain.model.Format

/**
 * Bibliothèque des recettes de référence : un appui en ajoute une copie aux recettes de
 * l'utilisateur, que l'on peut ensuite modifier librement.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferenceRecipeSheet(
    references: List<ReferenceRecipe>,
    onPick: (ReferenceRecipe) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Recettes de référence", style = MaterialTheme.typography.titleMedium)
            Text(
                "Pensées pour environ 5 L : les quantités sont adaptées au volume de votre " +
                    "installation par défaut. La recette ajoutée est une copie, modifiable à volonté.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LazyColumn(Modifier.heightIn(max = 520.dp)) {
                items(references, key = { it.recipe.name }) { ref ->
                    ListItem(
                        headlineContent = { Text(ref.recipe.name) },
                        overlineContent = {
                            Text("${ref.styleName} · ${Format.grams(ref.malts.sumOf { it.amountG })} de malt")
                        },
                        supportingContent = ref.description.takeIf { it.isNotBlank() }?.let { { Text(it) } },
                        modifier = Modifier.clickable { onPick(ref) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
