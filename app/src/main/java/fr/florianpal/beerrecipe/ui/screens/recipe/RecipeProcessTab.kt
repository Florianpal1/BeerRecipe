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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.florianpal.beerrecipe.domain.calc.BrewCalculator
import fr.florianpal.beerrecipe.domain.model.Format
import fr.florianpal.beerrecipe.domain.model.MashStepType
import fr.florianpal.beerrecipe.ui.common.CatalogData
import fr.florianpal.beerrecipe.ui.common.EnumDropdown
import fr.florianpal.beerrecipe.ui.common.ExpandableLineCard
import fr.florianpal.beerrecipe.ui.common.IntField
import fr.florianpal.beerrecipe.ui.common.KeyValueRow
import fr.florianpal.beerrecipe.ui.common.LabeledTextField
import fr.florianpal.beerrecipe.ui.common.NumberField
import fr.florianpal.beerrecipe.ui.common.SectionCard

@Composable
fun RecipeProcessTab(
    state: RecipeDetailUiState,
    catalog: CatalogData,
    viewModel: RecipeDetailViewModel,
) {
    val recipe = state.context.details.recipe
    val stats = state.stats
    val water = stats.water

    LazyColumn(
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SectionCard("Plan d'eau", subtitle = "Calculé depuis l'installation et la maische") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumberField(
                        "Épaisseur d'empâtage", state.context.mashThicknessLPerKg,
                        { v -> viewModel.editRecipe { it.copy(mashThicknessLPerKg = v) } },
                        suffix = "L/kg", decimals = 2, modifier = Modifier.weight(1f),
                        enabled = !state.context.usesFixedWater,
                    )
                    NumberField(
                        "Température du grain", recipe.grainTempC,
                        { v -> viewModel.editRecipe { it.copy(grainTempC = v) } },
                        suffix = "°C", modifier = Modifier.weight(1f),
                    )
                }
                NumberField(
                    "Température de rinçage", recipe.spargeTempC,
                    { v -> viewModel.editRecipe { it.copy(spargeTempC = v) } },
                    suffix = "°C",
                )
                KeyValueRow("Eau d'empâtage", Format.liters(water.mashWaterL), emphasis = true)
                KeyValueRow("Eau de rinçage", Format.liters(water.spargeWaterL), emphasis = true)
                KeyValueRow("Eau totale", Format.liters(water.totalWaterL))
                KeyValueRow("Absorbée par la drêche", Format.liters(water.grainAbsorptionL))
                KeyValueRow("Volume avant ébullition", Format.liters(water.preBoilVolumeL))
                KeyValueRow("Volume chaud en fin d'ébullition", Format.liters(water.postBoilHotVolumeL))
                KeyValueRow("Volume en fermenteur", Format.liters(water.batchVolumeL))
                KeyValueRow("Volume à l'embouteillage", Format.liters(water.packagingVolumeL))
                KeyValueRow("Température d'eau de trempe", Format.temp(water.strikeTempC), emphasis = true)
            }
        }

        item {
            SectionCard("Paliers d'empâtage") {
                Text(
                    "Température retenue pour le calcul d'atténuation : " +
                        "${Format.temp(stats.mashTempC)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        items(state.context.details.sortedMashSteps, key = { "mash-${it.id}" }) { step ->
            ExpandableLineCard(
                title = step.name,
                summary = "${Format.temp(step.temperatureC)} pendant ${step.timeMin} min · ${step.type.label}",
                onDelete = { viewModel.deleteMashStep(step) },
            ) {
                LabeledTextField("Nom du palier", step.name) { v ->
                    viewModel.saveMashStep(step.copy(name = v))
                }
                EnumDropdown(
                    "Type", MashStepType.entries, step.type,
                    onSelect = { v -> viewModel.saveMashStep(step.copy(type = v)) })
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumberField(
                        "Température", step.temperatureC,
                        { v -> viewModel.saveMashStep(step.copy(temperatureC = v)) },
                        suffix = "°C", modifier = Modifier.weight(1f),
                    )
                    IntField(
                        "Durée", step.timeMin,
                        { v -> viewModel.saveMashStep(step.copy(timeMin = v)) },
                        suffix = "min", modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        item {
            OutlinedButton(
                onClick = { viewModel.addMashStep() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("  Ajouter un palier")
            }
        }

        item {
            SectionCard(
                "pH d'empâtage",
                subtitle = "Estimation par bilan de charges entre les malts et l'eau",
            ) {
                KeyValueRow("pH estimé", Format.ph(stats.mashPh.estimatedPh), emphasis = true)
                KeyValueRow("pH de la maische en eau distillée", Format.ph(stats.mashPh.distilledWaterPh))
                KeyValueRow(
                    "Alcalinité résiduelle de l'eau",
                    "%.0f ppm CaCO3".format(stats.mashPh.residualAlkalinity),
                )
                KeyValueRow("Apport alcalin", "%.1f mEq".format(stats.mashPh.alkalinityMeq))
                KeyValueRow("Acide ajouté", "%.1f mEq".format(stats.mashPh.acidMeq))
                KeyValueRow("Pouvoir tampon de la maische", "%.0f mEq/pH".format(stats.mashPh.bufferCapacity))
                Text(
                    "Une estimation reste une estimation : mesurez le pH sur un échantillon " +
                        "refroidi à 20 °C, dix à quinze minutes après le début de l'empâtage.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item { SectionCard("Fermentation") { Text(
            "Les paliers servent à vérifier que la levure travaille dans sa plage.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        ) } }

        items(state.context.details.sortedFermentationSteps, key = { "fermentation-${it.id}" }) { step ->
            ExpandableLineCard(
                title = step.name,
                summary = "${Format.temp(step.temperatureC)} pendant ${step.days} jour(s)",
                onDelete = { viewModel.deleteFermentationStep(step) },
            ) {
                LabeledTextField("Nom", step.name) { v ->
                    viewModel.saveFermentationStep(step.copy(name = v))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumberField(
                        "Température", step.temperatureC,
                        { v -> viewModel.saveFermentationStep(step.copy(temperatureC = v)) },
                        suffix = "°C", modifier = Modifier.weight(1f),
                    )
                    IntField(
                        "Durée", step.days,
                        { v -> viewModel.saveFermentationStep(step.copy(days = v)) },
                        suffix = "j", modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        item {
            OutlinedButton(
                onClick = { viewModel.addFermentationStep() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("  Ajouter un palier de fermentation")
            }
        }

        item {
            SectionCard("Refermentation en bouteille") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumberField(
                        "Carbonatation visée", recipe.carbonationVolumes,
                        { v -> viewModel.editRecipe { it.copy(carbonationVolumes = v) } },
                        suffix = "vol. CO2", decimals = 2, modifier = Modifier.weight(1f),
                    )
                    NumberField(
                        "Température en fin de garde", recipe.bottlingTempC,
                        { v -> viewModel.editRecipe { it.copy(bottlingTempC = v) } },
                        suffix = "°C", modifier = Modifier.weight(1f),
                    )
                }
                KeyValueRow(
                    "CO2 déjà dissous",
                    "%.2f volumes".format(BrewCalculator.residualCo2Volumes(recipe.bottlingTempC)),
                )
                KeyValueRow("Saccharose à ajouter", Format.grams(stats.primingSucroseG), emphasis = true)
                KeyValueRow("ou dextrose monohydraté", Format.grams(stats.primingDextroseG))
                state.context.style?.let {
                    KeyValueRow(
                        "Fourchette du style",
                        "${Format.range(it.carbonationMin, it.carbonationMax, 1)} volumes",
                    )
                }
            }
        }
    }
}
