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
import fr.florianpal.beerrecipe.data.entity.Hop
import fr.florianpal.beerrecipe.domain.model.Format
import fr.florianpal.beerrecipe.domain.model.HopForm
import fr.florianpal.beerrecipe.domain.model.HopUse
import fr.florianpal.beerrecipe.ui.common.CatalogData
import fr.florianpal.beerrecipe.ui.common.EnumDropdown
import fr.florianpal.beerrecipe.ui.common.ExpandableLineCard
import fr.florianpal.beerrecipe.ui.common.IntField
import fr.florianpal.beerrecipe.ui.common.KeyValueRow
import fr.florianpal.beerrecipe.ui.common.LabeledTextField
import fr.florianpal.beerrecipe.ui.common.NumberField
import fr.florianpal.beerrecipe.ui.common.OptionalNumberField
import fr.florianpal.beerrecipe.ui.common.SectionCard
import fr.florianpal.beerrecipe.ui.common.TagRow
import fr.florianpal.beerrecipe.ui.common.TagsField

@Composable
fun RecipeHopTab(
    state: RecipeDetailUiState,
    catalog: CatalogData,
    viewModel: RecipeDetailViewModel,
) {
    val hops = state.context.details.sortedHops
    val stats = state.stats
    var showPicker by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SectionCard(
                "Houblonnage",
                subtitle = "${Format.ibu(stats.ibu)} IBU · ${Format.grams(stats.totalHopsG)} · " +
                    "formule ${state.context.details.recipe.ibuFormula.label}",
            ) {
                if (hops.isEmpty()) {
                    Text("Aucun houblon.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    stats.hopContributions
                        .filter { it.ibu > 0.05 }
                        .sortedByDescending { it.ibu }
                        .forEach {
                            KeyValueRow(
                                it.name,
                                "${Format.ibu(it.ibu)} IBU · utilisation ${Format.percent(it.utilizationPct)}",
                            )
                        }
                    HopUse.entries.forEach { use ->
                        val total = hops.filter { it.use == use }.sumOf { it.amountG }
                        if (total > 0) KeyValueRow(use.label, Format.grams(total))
                    }
                }
            }
        }

        items(hops, key = { it.id }) { hop ->
            val contribution = stats.hopContributions.firstOrNull { it.hopId == hop.id }
            ExpandableLineCard(
                title = hop.name,
                summary = buildString {
                    append(Format.grams(hop.amountG))
                    append(" · ").append(Format.percent(hop.alphaAcidPct)).append(" AA")
                    append(" · ").append(hop.use.label)
                    if (hop.use != HopUse.DRY_HOP && hop.use != HopUse.MASH) {
                        append(" ").append(hop.timeMin).append(" min")
                    } else if (hop.use == HopUse.DRY_HOP) {
                        append(" ").append(hop.timeMin).append(" j")
                    }
                    if (hop.origin.isNotBlank()) append(" · ").append(hop.origin)
                    hop.year?.let { append(" · ").append(it) }
                },
                trailingLabel = contribution?.takeIf { it.ibu > 0.05 }?.let { "${Format.ibu(it.ibu)} IBU" },
                onDelete = { viewModel.deleteHop(hop) },
            ) {
                LabeledTextField("Nom", hop.name) { v -> viewModel.saveHop(hop.copy(name = v)) }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumberField(
                        "Quantité", hop.amountG,
                        { v -> viewModel.saveHop(hop.copy(amountG = v)) },
                        suffix = "g", modifier = Modifier.weight(1f),
                    )
                    NumberField(
                        "Acides alpha", hop.alphaAcidPct,
                        { v -> viewModel.saveHop(hop.copy(alphaAcidPct = v)) },
                        suffix = "%", modifier = Modifier.weight(1f),
                    )
                }
                EnumDropdown(
                    "Moment d'ajout", HopUse.entries, hop.use,
                    onSelect = { v -> viewModel.saveHop(hop.copy(use = v)) })
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IntField(
                        if (hop.use == HopUse.DRY_HOP) "Durée" else "Temps d'infusion",
                        hop.timeMin,
                        { v -> viewModel.saveHop(hop.copy(timeMin = v)) },
                        suffix = if (hop.use == HopUse.DRY_HOP) "jours" else "min",
                        modifier = Modifier.weight(1f),
                    )
                    if (hop.use == HopUse.WHIRLPOOL) {
                        NumberField(
                            "Température", hop.temperatureC,
                            { v -> viewModel.saveHop(hop.copy(temperatureC = v)) },
                            suffix = "°C", decimals = 0, modifier = Modifier.weight(1f),
                        )
                    }
                }
                EnumDropdown(
                    "Forme", HopForm.entries, hop.form,
                    onSelect = { v -> viewModel.saveHop(hop.copy(form = v)) })
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LabeledTextField(
                        "Origine", hop.origin,
                        onValueChange = { v -> viewModel.saveHop(hop.copy(origin = v)) },
                        modifier = Modifier.weight(1f))
                    IntField(
                        "Année de récolte", hop.year ?: 0,
                        { v -> viewModel.saveHop(hop.copy(year = v.takeIf { y -> y > 1900 })) },
                        modifier = Modifier.weight(1f),
                    )
                }
                TagsField("Arômes", hop.aromas) { v -> viewModel.saveHop(hop.copy(aromas = v)) }
                OptionalNumberField(
                    "Prix", hop.pricePerKg,
                    { v -> viewModel.saveHop(hop.copy(pricePerKg = v)) },
                    suffix = "€/kg",
                )
                if (hop.use == HopUse.WHIRLPOOL) {
                    Text(
                        "À ${Format.temp(hop.temperatureC)}, l'isomérisation tourne autour de " +
                            "${Format.percent(fr.florianpal.beerrecipe.domain.calc.IbuCalculator.whirlpoolFactor(hop.temperatureC) * 100, 0)} " +
                            "de celle d'une ébullition franche.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TagRow(hop.aromas)
            }
        }

        item {
            Button(onClick = { showPicker = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("  Ajouter un houblon")
            }
        }
    }

    if (showPicker) {
        CatalogPickerSheet(
            title = "Choisir un houblon",
            items = catalog.hops,
            optionLabel = Hop::name,
            optionSubtitle = { h ->
                "${Format.percent(h.alphaAcidPct)} AA · ${h.origin} · ${h.aromas.joinToString(", ")}"
            },
            searchText = { "${it.name} ${it.origin} ${it.aromas.joinToString(" ")}" },
            amountOf = { PickAmount(default = 20.0, suffix = "g") },
            onPick = { hop, amount ->
                viewModel.addHop(hop, amount)
                showPicker = false
            },
            onDismiss = { showPicker = false },
            onCreateBlank = {
                viewModel.addBlankHop()
                showPicker = false
            },
        )
    }
}
