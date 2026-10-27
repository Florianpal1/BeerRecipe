package fr.florianpal.beerrecipe.ui.screens.recipe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.florianpal.beerrecipe.data.entity.Additive
import fr.florianpal.beerrecipe.domain.model.AdditiveStage
import fr.florianpal.beerrecipe.domain.model.AdditiveType
import fr.florianpal.beerrecipe.domain.model.AmountUnit
import fr.florianpal.beerrecipe.domain.model.Format
import fr.florianpal.beerrecipe.ui.common.CatalogData
import fr.florianpal.beerrecipe.ui.common.EnumDropdown
import fr.florianpal.beerrecipe.ui.common.ExpandableLineCard
import fr.florianpal.beerrecipe.ui.common.IntField
import fr.florianpal.beerrecipe.ui.common.KeyValueRow
import fr.florianpal.beerrecipe.ui.common.LabeledTextField
import fr.florianpal.beerrecipe.ui.common.NumberField
import fr.florianpal.beerrecipe.ui.common.OptionalNumberField
import fr.florianpal.beerrecipe.ui.common.SectionCard

@Composable
fun RecipeAdditiveTab(
    state: RecipeDetailUiState,
    catalog: CatalogData,
    viewModel: RecipeDetailViewModel,
) {
    val additives = state.context.details.sortedAdditives
    var showPicker by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SectionCard(
                "Additifs, épices et traitements d'eau",
                subtitle = "${additives.size} ajout(s) programmé(s)",
            ) {
                if (additives.isEmpty()) {
                    Text(
                        "Sels de brassage, acides, clarifiants, épices, fruits ou bois : " +
                            "tout ce qui n'est ni malt, ni houblon, ni levure.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    AdditiveStage.entries.forEach { stage ->
                        val atStage = additives.filter { it.stage == stage }
                        if (atStage.isNotEmpty()) {
                            KeyValueRow(
                                stage.label,
                                atStage.joinToString(", ") { it.name },
                            )
                        }
                    }
                }
            }
        }

        item {
            val water = state.stats.finalWaterProfile
            SectionCard("Eau après traitement", subtitle = "Profil résultant, en ppm") {
                KeyValueRow("Calcium (Ca)", "%.0f ppm".format(water.calcium))
                KeyValueRow("Magnésium (Mg)", "%.0f ppm".format(water.magnesium))
                KeyValueRow("Sodium (Na)", "%.0f ppm".format(water.sodium))
                KeyValueRow("Sulfate (SO4)", "%.0f ppm".format(water.sulfate))
                KeyValueRow("Chlorure (Cl)", "%.0f ppm".format(water.chloride))
                KeyValueRow("Bicarbonate (HCO3)", "%.0f ppm".format(water.bicarbonate))
                KeyValueRow(
                    "Rapport sulfate / chlorure",
                    if (water.sulfateChlorideRatio.isFinite())
                        "%.2f".format(water.sulfateChlorideRatio) else "—",
                    emphasis = true,
                )
                KeyValueRow(
                    "Alcalinité résiduelle",
                    "%.0f ppm CaCO3".format(state.stats.mashPh.residualAlkalinity),
                )
            }
        }

        items(additives, key = { it.id }) { additive ->
            ExpandableLineCard(
                title = additive.name,
                summary = buildString {
                    append("%.1f %s".format(additive.amount, additive.unit.label))
                    append(" · ").append(additive.stage.label)
                    append(" · ").append(additive.type.label)
                },
                trailingLabel = additive.takeIf { it.stage == AdditiveStage.BOIL }
                    ?.let { "${it.timeMin} min" },
                onDelete = { viewModel.deleteAdditive(additive) },
            ) {
                LabeledTextField("Nom", additive.name) { v ->
                    viewModel.saveAdditive(additive.copy(name = v))
                }
                EnumDropdown(
                    "Catégorie", AdditiveType.entries, additive.type,
                    onSelect = { v -> viewModel.saveAdditive(additive.copy(type = v)) })
                EnumDropdown(
                    "Moment d'ajout", AdditiveStage.entries, additive.stage,
                    onSelect = { v -> viewModel.saveAdditive(additive.copy(stage = v)) })
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumberField(
                        "Quantité", additive.amount,
                        { v -> viewModel.saveAdditive(additive.copy(amount = v)) },
                        decimals = 2, modifier = Modifier.weight(1f),
                    )
                    EnumDropdown(
                        "Unité", AmountUnit.entries, additive.unit,
                        onSelect = { v -> viewModel.saveAdditive(additive.copy(unit = v)) },
                        modifier = Modifier.weight(1f))
                }
                if (additive.stage == AdditiveStage.BOIL || additive.stage == AdditiveStage.WHIRLPOOL) {
                    IntField(
                        "Temps avant la fin", additive.timeMin,
                        { v -> viewModel.saveAdditive(additive.copy(timeMin = v)) },
                        suffix = "min",
                    )
                }
                OptionalNumberField(
                    "Prix de la ligne", additive.price,
                    { v -> viewModel.saveAdditive(additive.copy(price = v)) },
                    suffix = "€",
                )

                if (additive.type == AdditiveType.ACID) {
                    OptionalNumberField(
                        "Acidité apportée", additive.acidMeqPerUnit,
                        { v -> viewModel.saveAdditive(additive.copy(acidMeqPerUnit = v)) },
                        suffix = "mEq / unité",
                    )
                    Text(
                        "Seuls les acides ajoutés à l'empâtage entrent dans le calcul de pH.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (additive.type == AdditiveType.WATER_AGENT) {
                    Text(
                        "Ions apportés, en ppm par gramme dissous dans un litre",
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        NumberField(
                            "Ca", additive.calciumPpmPerGPerL,
                            { v -> viewModel.saveAdditive(additive.copy(calciumPpmPerGPerL = v)) },
                            decimals = 1, modifier = Modifier.weight(1f),
                        )
                        NumberField(
                            "Mg", additive.magnesiumPpmPerGPerL,
                            { v -> viewModel.saveAdditive(additive.copy(magnesiumPpmPerGPerL = v)) },
                            decimals = 1, modifier = Modifier.weight(1f),
                        )
                        NumberField(
                            "Na", additive.sodiumPpmPerGPerL,
                            { v -> viewModel.saveAdditive(additive.copy(sodiumPpmPerGPerL = v)) },
                            decimals = 1, modifier = Modifier.weight(1f),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        NumberField(
                            "SO4", additive.sulfatePpmPerGPerL,
                            { v -> viewModel.saveAdditive(additive.copy(sulfatePpmPerGPerL = v)) },
                            decimals = 1, modifier = Modifier.weight(1f),
                        )
                        NumberField(
                            "Cl", additive.chloridePpmPerGPerL,
                            { v -> viewModel.saveAdditive(additive.copy(chloridePpmPerGPerL = v)) },
                            decimals = 1, modifier = Modifier.weight(1f),
                        )
                        NumberField(
                            "HCO3", additive.bicarbonatePpmPerGPerL,
                            { v -> viewModel.saveAdditive(additive.copy(bicarbonatePpmPerGPerL = v)) },
                            decimals = 1, modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        item {
            Button(onClick = { showPicker = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("  Ajouter un additif")
            }
        }
    }

    if (showPicker) {
        CatalogPickerSheet(
            title = "Choisir un additif",
            items = catalog.additives,
            optionLabel = Additive::name,
            optionSubtitle = { a ->
                "${a.type.label} · ${a.defaultStage.label} · " +
                    "%.1f %s".format(a.defaultAmount, a.defaultUnit.label)
            },
            searchText = { "${it.name} ${it.type.label} ${it.notes}" },
            onPick = {
                viewModel.addAdditive(it)
                showPicker = false
            },
            onDismiss = { showPicker = false },
            onCreateBlank = {
                viewModel.addBlankAdditive()
                showPicker = false
            },
        )
    }
}
