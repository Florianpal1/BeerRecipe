package fr.florianpal.beerrecipe.ui.screens.recipe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.florianpal.beerrecipe.data.entity.BeerStyle
import fr.florianpal.beerrecipe.data.entity.Equipment
import fr.florianpal.beerrecipe.data.entity.WaterProfile
import fr.florianpal.beerrecipe.domain.advisor.Suggestion
import fr.florianpal.beerrecipe.domain.model.ColorFormula
import fr.florianpal.beerrecipe.domain.model.Format
import fr.florianpal.beerrecipe.domain.model.IbuFormula
import fr.florianpal.beerrecipe.domain.model.Severity
import fr.florianpal.beerrecipe.ui.common.BeerColorSwatch
import fr.florianpal.beerrecipe.ui.common.CatalogData
import fr.florianpal.beerrecipe.ui.common.Dropdown
import fr.florianpal.beerrecipe.ui.common.EnumDropdown
import fr.florianpal.beerrecipe.ui.common.KeyValueRow
import fr.florianpal.beerrecipe.ui.common.LabeledTextField
import fr.florianpal.beerrecipe.ui.common.NumberField
import fr.florianpal.beerrecipe.ui.common.IntField
import fr.florianpal.beerrecipe.ui.common.SectionCard
import fr.florianpal.beerrecipe.ui.common.StatTile

@Composable
fun RecipeSummaryTab(
    state: RecipeDetailUiState,
    catalog: CatalogData,
    viewModel: RecipeDetailViewModel,
) {
    val recipe = state.context.details.recipe
    val stats = state.stats
    val style = state.context.style

    LazyColumn(
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionCard("Identité", subtitle = "Ce que vous cherchez à brasser") {
                LabeledTextField("Nom", recipe.name) { v -> viewModel.editRecipe { it.copy(name = v) } }
                LabeledTextField("Brasseur", recipe.author) { v -> viewModel.editRecipe { it.copy(author = v) } }
                Dropdown(
                    label = "Style visé",
                    options = catalog.styles,
                    selected = style,
                    onSelect = { s -> viewModel.editRecipe { it.copy(styleId = s.id) } },
                    optionLabel = BeerStyle::displayName,
                    placeholder = "— style libre —",
                )
                style?.description?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            SectionCard(
                "Environnement de brassage",
                subtitle = "Installation enregistrée, réutilisable d'une recette à l'autre",
            ) {
                Dropdown(
                    label = "Installation",
                    options = catalog.equipments,
                    selected = state.context.equipment,
                    onSelect = { viewModel.applyEquipment(it.id) },
                    optionLabel = Equipment::name,
                    placeholder = "— aucune —",
                )
                state.context.equipment?.let { eq ->
                    Text(
                        "${eq.type.label} · cuve d'empâtage ${Format.liters(eq.mashTunVolumeL)} · " +
                            "évaporation ${Format.liters(eq.boilOffRateLPerHour)}/h",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumberField(
                        "Volume en fermenteur", state.context.batchVolumeL,
                        { v -> viewModel.editRecipe { it.copy(batchVolumeL = v) } },
                        suffix = "L", modifier = Modifier.weight(1f),
                        enabled = !state.context.usesFixedWater,
                    )
                    IntField(
                        "Ébullition", recipe.boilTimeMin,
                        { v -> viewModel.editRecipe { it.copy(boilTimeMin = v) } },
                        suffix = "min", modifier = Modifier.weight(1f),
                    )
                }
                NumberField(
                    "Rendement de brasserie", recipe.efficiencyPct,
                    { v -> viewModel.editRecipe { it.copy(efficiencyPct = v) } },
                    suffix = "%",
                )
                if (state.context.usesFixedWater) {
                    Text(
                        "Volume calculé depuis l'eau totale de l'installation " +
                            "(${Format.liters(state.context.equipment?.fixedTotalWaterL ?: 0.0)}, empâtage et rinçage compris) et le grain " +
                            "de la recette. Pour le changer, modifiez l'eau dans l'installation.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Dropdown(
                    label = "Eau de brassage",
                    options = catalog.waterProfiles,
                    selected = state.context.water,
                    onSelect = { w -> viewModel.editRecipe { it.copy(sourceWaterId = w.id) } },
                    optionLabel = WaterProfile::name,
                    placeholder = "— non renseignée —",
                )
            }
        }

        item { StatsGrid(state) }

        item {
            SectionCard("Suggestions", subtitle = "Classées de l'écart le plus marqué au simple repère") {
                if (state.suggestions.isEmpty()) {
                    Text(
                        "Rien à signaler : la recette tient dans les clous du style visé.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        items(state.suggestions) { suggestion -> SuggestionCard(suggestion) }

        item {
            SectionCard("Chiffres du brassin") {
                KeyValueRow("Densité pré-ébullition", Format.gravity(stats.preBoilGravity))
                KeyValueRow("Densité moyenne d'ébullition", Format.gravity(stats.boilGravity))
                KeyValueRow("Atténuation apparente", Format.percent(stats.apparentAttenuationPct))
                KeyValueRow("Atténuation réelle", Format.percent(stats.realAttenuationPct))
                KeyValueRow("Rapport BU:GU", "%.2f".format(stats.buGuRatio))
                KeyValueRow("Calories", "%.0f kcal / 33 cL".format(stats.caloriesPer330ml))
                KeyValueRow("Grain total", Format.kg(stats.totalFermentableKg))
                KeyValueRow("Houblon total", Format.grams(stats.totalHopsG))
                if (stats.totalCost > 0) {
                    KeyValueRow(
                        "Coût des ingrédients",
                        Format.money(stats.totalCost) + " · " + Format.money(stats.costPerLiter) + "/L",
                        emphasis = true,
                    )
                    if (stats.hasPartialCost) {
                        Text(
                            "Certains ingrédients n'ont pas de prix : le total est partiel.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        item {
            SectionCard("Méthodes de calcul", subtitle = "Changez de formule pour comparer les estimations") {
                EnumDropdown(
                    "Formule d'amertume", IbuFormula.entries, recipe.ibuFormula,
                    onSelect = { v -> viewModel.editRecipe { it.copy(ibuFormula = v) } })
                EnumDropdown(
                    "Formule de couleur", ColorFormula.entries, recipe.colorFormula,
                    onSelect = { v -> viewModel.editRecipe { it.copy(colorFormula = v) } })
            }
        }

        item {
            SectionCard("Notes") {
                LabeledTextField(
                    "Notes de brassage", recipe.notes,
                    onValueChange = { v -> viewModel.editRecipe { it.copy(notes = v) } },
                    singleLine = false)
                LabeledTextField(
                    "Notes de dégustation", recipe.tastingNotes,
                    onValueChange = { v -> viewModel.editRecipe { it.copy(tastingNotes = v) } },
                    singleLine = false)
            }
        }
    }
}

/** Les six indicateurs demandés, chacun situé dans l'intervalle du style. */
@Composable
private fun StatsGrid(state: RecipeDetailUiState) {
    val stats = state.stats
    val style = state.context.style

    SectionCard(
        "Indicateurs",
        subtitle = style?.let { "Cible : ${it.displayName}" } ?: "Aucun style visé : pas de cible",
        trailing = { BeerColorSwatch(stats.ebc, size = 44) },
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                "Densité initiale", Format.gravity(stats.og), Modifier.weight(1f),
                range = style?.ogRange, rawValue = stats.og,
                rangeLabel = style?.let { "${Format.gravity(it.ogMin)} – ${Format.gravity(it.ogMax)}" },
            )
            StatTile(
                "Densité finale", Format.gravity(stats.fg), Modifier.weight(1f),
                range = style?.fgRange, rawValue = stats.fg,
                rangeLabel = style?.let { "${Format.gravity(it.fgMin)} – ${Format.gravity(it.fgMax)}" },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                "Alcool", Format.abv(stats.abv), Modifier.weight(1f),
                range = style?.abvRange, rawValue = stats.abv,
                rangeLabel = style?.let { Format.range(it.abvMin, it.abvMax) + " % vol." },
            )
            StatTile(
                "Amertume", "${Format.ibu(stats.ibu)} IBU", Modifier.weight(1f),
                range = style?.ibuRange, rawValue = stats.ibu,
                rangeLabel = style?.let { "${Format.ibu(it.ibuMin)} – ${Format.ibu(it.ibuMax)} IBU" },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                "Couleur", "${Format.ebc(stats.ebc)} · ${Format.srm(stats.srm)}", Modifier.weight(1f),
                range = style?.ebcRange, rawValue = stats.ebc,
                rangeLabel = style?.let { "${Format.ebc(it.ebcMin)} – ${Format.ebc(it.ebcMax)}" },
            )
            StatTile(
                "pH d'empâtage", Format.ph(stats.mashPh.estimatedPh), Modifier.weight(1f),
                range = style?.mashPhRange, rawValue = stats.mashPh.estimatedPh,
                rangeLabel = style?.let { Format.range(it.mashPhMin, it.mashPhMax, 2) },
            )
        }
    }
}

@Composable
private fun SuggestionCard(suggestion: Suggestion) {
    val (icon, tint) = when (suggestion.severity) {
        Severity.PROBLEM -> Icons.Default.ReportProblem to MaterialTheme.colorScheme.error
        Severity.WARNING -> Icons.Default.WarningAmber to MaterialTheme.colorScheme.tertiary
        Severity.INFO -> Icons.Default.Lightbulb to MaterialTheme.colorScheme.secondary
    }
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, tint.copy(alpha = 0.35f)),
    ) {
        Row(Modifier.padding(14.dp)) {
            Icon(icon, contentDescription = null, tint = tint)
            Spacer(Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        suggestion.parameter.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = tint,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(suggestion.title, style = MaterialTheme.typography.titleSmall)
                Text(
                    suggestion.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                suggestion.action?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
