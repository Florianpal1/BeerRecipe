package fr.florianpal.beerrecipe.ui.screens.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.florianpal.beerrecipe.data.entity.Malt
import fr.florianpal.beerrecipe.domain.calc.ColorScale
import fr.florianpal.beerrecipe.domain.model.FermentableType
import fr.florianpal.beerrecipe.domain.model.Format
import fr.florianpal.beerrecipe.ui.common.BeerColorSwatch
import fr.florianpal.beerrecipe.ui.common.CatalogData
import fr.florianpal.beerrecipe.ui.common.EnumDropdown
import fr.florianpal.beerrecipe.ui.common.ExpandableLineCard
import fr.florianpal.beerrecipe.ui.common.IntField
import fr.florianpal.beerrecipe.ui.common.KeyValueRow
import fr.florianpal.beerrecipe.ui.common.LabeledTextField
import fr.florianpal.beerrecipe.ui.common.NumberField
import fr.florianpal.beerrecipe.ui.common.OptionalNumberField
import fr.florianpal.beerrecipe.ui.common.SectionCard
import fr.florianpal.beerrecipe.ui.common.TagsField

@Composable
fun RecipeMaltTab(
    state: RecipeDetailUiState,
    catalog: CatalogData,
    viewModel: RecipeDetailViewModel,
) {
    val malts = state.context.details.sortedMalts
    val stats = state.stats
    var showPicker by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SectionCard("Composition de la maque", subtitle = Format.kg(stats.totalFermentableKg) + " au total") {
                if (malts.isEmpty()) {
                    Text("Aucun fermentescible.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    GrainBillBar(state)
                    stats.maltContributions
                        .sortedByDescending { it.amountG }
                        .forEach {
                            KeyValueRow(
                                it.name,
                                "${Format.grams(it.amountG)} · ${Format.percent(it.weightPct)}",
                            )
                        }
                    KeyValueRow("Pouvoir diastasique moyen", "%.0f °Lintner".format(stats.weightedDiastaticPower))
                }
            }
        }

        items(malts, key = { it.id }) { malt ->
            ExpandableLineCard(
                title = malt.name,
                summary = buildString {
                    append(Format.grams(malt.amountG))
                    append(" · ").append(malt.type.label)
                    append(" · ").append(Format.ebc(malt.colorEbc))
                    if (malt.origin.isNotBlank()) append(" · ").append(malt.origin)
                    malt.year?.let { append(" · ").append(it) }
                },
                trailingLabel = stats.maltContributions
                    .firstOrNull { it.maltId == malt.id }
                    ?.let { Format.percent(it.weightPct, 0) },
                leading = { BeerColorSwatch(malt.colorEbc, size = 28) },
                onDelete = { viewModel.deleteMalt(malt) },
            ) {
                LabeledTextField("Nom", malt.name) { v -> viewModel.saveMalt(malt.copy(name = v)) }
                EnumDropdown(
                    "Type de malt", FermentableType.entries, malt.type,
                    onSelect = { v -> viewModel.saveMalt(malt.copy(type = v)) })
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumberField(
                        "Quantité", malt.amountG,
                        { v -> viewModel.saveMalt(malt.copy(amountG = v)) },
                        suffix = "g", decimals = 0, modifier = Modifier.weight(1f),
                    )
                    NumberField(
                        "Couleur", malt.colorEbc,
                        { v -> viewModel.saveMalt(malt.copy(colorEbc = v)) },
                        suffix = "EBC", modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LabeledTextField(
                        "Origine", malt.origin,
                        onValueChange = { v -> viewModel.saveMalt(malt.copy(origin = v)) },
                        modifier = Modifier.weight(1f))
                    IntField(
                        "Année", malt.year ?: 0,
                        { v -> viewModel.saveMalt(malt.copy(year = v.takeIf { y -> y > 1900 })) },
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumberField(
                        "Rendement en extrait", malt.extractYieldPct,
                        { v -> viewModel.saveMalt(malt.copy(extractYieldPct = v)) },
                        suffix = "%", modifier = Modifier.weight(1f),
                    )
                    NumberField(
                        "Part fermentescible", malt.fermentabilityPct,
                        { v -> viewModel.saveMalt(malt.copy(fermentabilityPct = v)) },
                        suffix = "%", decimals = 0, modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumberField(
                        "Pouvoir diastasique", malt.diastaticPowerLintner,
                        { v -> viewModel.saveMalt(malt.copy(diastaticPowerLintner = v)) },
                        suffix = "°L", decimals = 0, modifier = Modifier.weight(1f),
                    )
                    OptionalNumberField(
                        "Prix", malt.pricePerKg,
                        { v -> viewModel.saveMalt(malt.copy(pricePerKg = v)) },
                        suffix = "€/kg", modifier = Modifier.weight(1f),
                    )
                }
                TagsField("Styles associés", malt.associatedStyles) { v ->
                    viewModel.saveMalt(malt.copy(associatedStyles = v))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OptionalNumberField(
                        "pH en eau distillée", malt.distilledWaterPh,
                        { v -> viewModel.saveMalt(malt.copy(distilledWaterPh = v)) },
                        modifier = Modifier.weight(1f),
                    )
                    OptionalNumberField(
                        "Pouvoir tampon", malt.bufferCapacity,
                        { v -> viewModel.saveMalt(malt.copy(bufferCapacity = v)) },
                        suffix = "mEq/kg", decimals = 0, modifier = Modifier.weight(1f),
                    )
                }
                Text(
                    "Laissés vides, ces deux derniers champs sont estimés d'après le type et la couleur du malt.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            Button(onClick = { showPicker = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("  Ajouter un malt")
            }
        }
    }

    if (showPicker) {
        CatalogPickerSheet(
            title = "Choisir un fermentescible",
            items = catalog.malts,
            optionLabel = Malt::name,
            optionSubtitle = { m ->
                "${m.type.label} · ${Format.ebc(m.colorEbc)} · ${Format.percent(m.extractYieldPct)} " +
                    if (m.origin.isNotBlank()) "· ${m.origin}" else ""
            },
            searchText = { "${it.name} ${it.origin} ${it.type.label} ${it.associatedStyles.joinToString(" ")}" },
            onPick = {
                viewModel.addMalt(it)
                showPicker = false
            },
            onDismiss = { showPicker = false },
            onCreateBlank = {
                viewModel.addBlankMalt()
                showPicker = false
            },
        )
    }
}

/** Barre empilée montrant la part de chaque malt dans la maque. */
@Composable
private fun GrainBillBar(state: RecipeDetailUiState) {
    val contributions = state.stats.maltContributions.filter { it.amountG > 0 }
    if (contributions.isEmpty()) return
    Row(
        Modifier
            .fillMaxWidth()
            .height(14.dp)
            .clip(CircleShape),
    ) {
        contributions.forEach { c ->
            Box(
                Modifier
                    .weight(c.weightPct.toFloat().coerceAtLeast(0.01f))
                    .fillMaxHeight()
                    .background(Color(ColorScale.ebcToArgb(
                        state.context.details.malts.firstOrNull { it.id == c.maltId }?.colorEbc ?: 4.0
                    ))),
            )
        }
    }
}
