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
import fr.florianpal.beerrecipe.data.entity.Yeast
import fr.florianpal.beerrecipe.domain.model.Flocculation
import fr.florianpal.beerrecipe.domain.model.Format
import fr.florianpal.beerrecipe.domain.model.YeastForm
import fr.florianpal.beerrecipe.domain.model.YeastType
import fr.florianpal.beerrecipe.ui.common.CatalogData
import fr.florianpal.beerrecipe.ui.common.EnumDropdown
import fr.florianpal.beerrecipe.ui.common.ExpandableLineCard
import fr.florianpal.beerrecipe.ui.common.KeyValueRow
import fr.florianpal.beerrecipe.ui.common.LabeledTextField
import fr.florianpal.beerrecipe.ui.common.NumberField
import fr.florianpal.beerrecipe.ui.common.OptionalNumberField
import fr.florianpal.beerrecipe.ui.common.SectionCard
import fr.florianpal.beerrecipe.ui.common.TagRow
import fr.florianpal.beerrecipe.ui.common.TagsField

@Composable
fun RecipeYeastTab(
    state: RecipeDetailUiState,
    catalog: CatalogData,
    viewModel: RecipeDetailViewModel,
) {
    val yeasts = state.context.details.sortedYeasts
    val pitching = state.stats.pitching
    var showPicker by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SectionCard(
                "Ensemencement",
                subtitle = "Calculé pour une densité de ${Format.gravity(state.stats.og)}",
            ) {
                KeyValueRow(
                    "Cellules nécessaires",
                    "%.0f milliards".format(pitching.requiredBillionCells),
                )
                KeyValueRow(
                    "Cellules apportées",
                    "%.0f milliards".format(pitching.providedBillionCells),
                    emphasis = true,
                )
                KeyValueRow(
                    "Levure sèche conseillée",
                    Format.grams(pitching.recommendedDryYeastG),
                )
                KeyValueRow("Atténuation retenue", Format.percent(state.stats.effectiveAttenuationPct))
                KeyValueRow("Température d'empâtage prise en compte", Format.temp(state.stats.mashTempC))
            }
        }

        items(yeasts, key = { it.id }) { yeast ->
            ExpandableLineCard(
                title = yeast.name,
                summary = buildString {
                    if (yeast.laboratory.isNotBlank()) append(yeast.laboratory).append(" · ")
                    append(Format.grams(yeast.amountG))
                    append(" · ").append(Format.percent(yeast.attenuationPct)).append(" atténuation")
                    append(" · ").append("%.0f–%.0f °C".format(yeast.tempMinC, yeast.tempMaxC))
                },
                trailingLabel = yeast.flocculation.label,
                onDelete = { viewModel.deleteYeast(yeast) },
            ) {
                LabeledTextField("Nom", yeast.name) { v -> viewModel.saveYeast(yeast.copy(name = v)) }
                LabeledTextField("Laboratoire", yeast.laboratory) { v ->
                    viewModel.saveYeast(yeast.copy(laboratory = v))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    EnumDropdown(
                        "Type", YeastType.entries, yeast.type,
                        onSelect = { v -> viewModel.saveYeast(yeast.copy(type = v)) },
                        modifier = Modifier.weight(1f))
                    EnumDropdown(
                        "Forme", YeastForm.entries, yeast.form,
                        onSelect = { v -> viewModel.saveYeast(yeast.copy(form = v)) },
                        modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumberField(
                        "Quantité", yeast.amountG,
                        { v -> viewModel.saveYeast(yeast.copy(amountG = v)) },
                        suffix = if (yeast.form == YeastForm.DRY) "g" else "mL",
                        modifier = Modifier.weight(1f),
                    )
                    NumberField(
                        "Atténuation", yeast.attenuationPct,
                        { v -> viewModel.saveYeast(yeast.copy(attenuationPct = v)) },
                        suffix = "%", modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumberField(
                        "Température min.", yeast.tempMinC,
                        { v -> viewModel.saveYeast(yeast.copy(tempMinC = v)) },
                        suffix = "°C", modifier = Modifier.weight(1f),
                    )
                    NumberField(
                        "Température max.", yeast.tempMaxC,
                        { v -> viewModel.saveYeast(yeast.copy(tempMaxC = v)) },
                        suffix = "°C", modifier = Modifier.weight(1f),
                    )
                }
                EnumDropdown(
                    "Floculation", Flocculation.entries, yeast.flocculation,
                    onSelect = { v -> viewModel.saveYeast(yeast.copy(flocculation = v)) })
                TagsField("Profil aromatique", yeast.aromaProfile) { v ->
                    viewModel.saveYeast(yeast.copy(aromaProfile = v))
                }
                TagsField("Styles populaires", yeast.popularStyles) { v ->
                    viewModel.saveYeast(yeast.copy(popularStyles = v))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumberField(
                        "Cellules par unité", yeast.cellsPerGramBillions,
                        { v -> viewModel.saveYeast(yeast.copy(cellsPerGramBillions = v)) },
                        suffix = "Md", decimals = 2, modifier = Modifier.weight(1f),
                    )
                    OptionalNumberField(
                        "Prix", yeast.price,
                        { v -> viewModel.saveYeast(yeast.copy(price = v)) },
                        suffix = "€", modifier = Modifier.weight(1f),
                    )
                }
                TagRow(yeast.aromaProfile + yeast.popularStyles)
            }
        }

        item {
            Button(onClick = { showPicker = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("  Ajouter une levure")
            }
        }

        item {
            Text(
                "L'atténuation retenue par le calcul combine celle de la levure, la température " +
                    "d'empâtage et la part de sucres simples : elle peut donc différer de la " +
                    "valeur annoncée par le laboratoire.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (showPicker) {
        CatalogPickerSheet(
            title = "Choisir une levure",
            items = catalog.yeasts,
            optionLabel = { "${it.laboratory} ${it.name}".trim() },
            optionSubtitle = { y ->
                "${y.type.label} · ${y.tempRangeLabel} · ${Format.percent(y.attenuationAvgPct)} · " +
                    y.popularStyles.joinToString(", ")
            },
            searchText = {
                "${it.name} ${it.laboratory} ${it.productId} ${it.popularStyles.joinToString(" ")} " +
                    it.aromaProfile.joinToString(" ")
            },
            amountOf = { y ->
                PickAmount(default = y.packageSizeG, suffix = if (y.form == YeastForm.DRY) "g" else "mL")
            },
            onPick = { yeast, amount ->
                viewModel.addYeast(yeast, amount)
                showPicker = false
            },
            onDismiss = { showPicker = false },
        )
    }
}
