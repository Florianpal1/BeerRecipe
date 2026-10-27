package fr.florianpal.beerrecipe.ui.screens.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.florianpal.beerrecipe.data.entity.Additive
import fr.florianpal.beerrecipe.data.entity.Hop
import fr.florianpal.beerrecipe.data.entity.Malt
import fr.florianpal.beerrecipe.data.entity.WaterProfile
import fr.florianpal.beerrecipe.data.entity.Yeast
import fr.florianpal.beerrecipe.domain.model.AdditiveStage
import fr.florianpal.beerrecipe.domain.model.AdditiveType
import fr.florianpal.beerrecipe.domain.model.AmountUnit
import fr.florianpal.beerrecipe.domain.model.FermentableType
import fr.florianpal.beerrecipe.domain.model.Flocculation
import fr.florianpal.beerrecipe.domain.model.Format
import fr.florianpal.beerrecipe.domain.model.HopForm
import fr.florianpal.beerrecipe.domain.model.HopPurpose
import fr.florianpal.beerrecipe.domain.model.YeastForm
import fr.florianpal.beerrecipe.domain.model.YeastType
import fr.florianpal.beerrecipe.ui.common.BeerColorSwatch
import fr.florianpal.beerrecipe.ui.common.EnumDropdown
import fr.florianpal.beerrecipe.ui.common.ExpandableLineCard
import fr.florianpal.beerrecipe.ui.common.IntField
import fr.florianpal.beerrecipe.ui.common.KeyValueRow
import fr.florianpal.beerrecipe.ui.common.LabeledTextField
import fr.florianpal.beerrecipe.ui.common.NumberField
import fr.florianpal.beerrecipe.ui.common.OptionalNumberField
import fr.florianpal.beerrecipe.ui.common.TagsField
import fr.florianpal.beerrecipe.ui.common.appContainer

private val TABS = listOf("Malts", "Houblons", "Levures", "Additifs", "Eaux")

/**
 * Le garde-manger : fiches d'ingrédients réutilisables d'une recette à l'autre,
 * avec leur stock et leur prix.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen() {
    val container = appContainer()
    val viewModel: CatalogViewModel =
        viewModel(factory = CatalogViewModel.factory(container.repository))
    val catalog by viewModel.catalog.collectAsStateWithLifecycle()

    var tab by remember { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("") }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { TopAppBar(title = { Text("Catalogue d'ingrédients") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    when (tab) {
                        0 -> viewModel.save(Malt(name = "Nouveau malt", isCustom = true))
                        1 -> viewModel.save(Hop(name = "Nouveau houblon", isCustom = true))
                        2 -> viewModel.save(Yeast(name = "Nouvelle levure", isCustom = true))
                        3 -> viewModel.save(Additive(name = "Nouvel additif", isCustom = true))
                        4 -> viewModel.save(WaterProfile(name = "Nouvelle eau"))
                    }
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Ajouter") },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            PrimaryScrollableTabRow(selectedTabIndex = tab, edgePadding = 8.dp) {
                TABS.forEachIndexed { index, title ->
                    Tab(
                        selected = tab == index,
                        onClick = { tab = index },
                        text = { Text(title) },
                    )
                }
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Rechercher") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            val listPadding = PaddingValues(16.dp, 0.dp, 16.dp, 96.dp)
            val spacing = Arrangement.spacedBy(12.dp)

            when (tab) {
                0 -> LazyColumn(contentPadding = listPadding, verticalArrangement = spacing) {
                    items(
                        catalog.malts.filter { it.matches(query) },
                        key = { it.id },
                    ) { MaltCard(it, viewModel) }
                }
                1 -> LazyColumn(contentPadding = listPadding, verticalArrangement = spacing) {
                    items(
                        catalog.hops.filter { it.matches(query) },
                        key = { it.id },
                    ) { HopCard(it, viewModel) }
                }
                2 -> LazyColumn(contentPadding = listPadding, verticalArrangement = spacing) {
                    items(
                        catalog.yeasts.filter { it.matches(query) },
                        key = { it.id },
                    ) { YeastCard(it, viewModel) }
                }
                3 -> LazyColumn(contentPadding = listPadding, verticalArrangement = spacing) {
                    items(
                        catalog.additives.filter { it.matches(query) },
                        key = { it.id },
                    ) { AdditiveCard(it, viewModel) }
                }
                4 -> LazyColumn(contentPadding = listPadding, verticalArrangement = spacing) {
                    items(
                        catalog.waterProfiles.filter { it.name.contains(query, true) },
                        key = { it.id },
                    ) { WaterCard(it, viewModel) }
                }
            }
        }
    }
}

private fun Malt.matches(q: String) = q.isBlank() ||
    "$name $origin $maltster ${type.label} ${associatedStyles.joinToString(" ")}".contains(q, true)

private fun Hop.matches(q: String) = q.isBlank() ||
    "$name $origin ${aromas.joinToString(" ")}".contains(q, true)

private fun Yeast.matches(q: String) = q.isBlank() ||
    "$name $laboratory $productId ${popularStyles.joinToString(" ")}".contains(q, true)

private fun Additive.matches(q: String) = q.isBlank() ||
    "$name ${type.label} $notes".contains(q, true)

@Composable
private fun MaltCard(malt: Malt, viewModel: CatalogViewModel) {
    ExpandableLineCard(
        title = malt.name,
        summary = "${malt.type.label} · ${Format.ebc(malt.colorEbc)} · " +
            "${Format.percent(malt.extractYieldPct)} rendement" +
            if (malt.origin.isNotBlank()) " · ${malt.origin}" else "",
        leading = { BeerColorSwatch(malt.colorEbc, size = 28) },
        trailingLabel = malt.pricePerKg?.let { Format.money(it) + "/kg" },
        onDelete = { viewModel.delete(malt) },
    ) {
        LabeledTextField("Nom", malt.name) { v -> viewModel.save(malt.copy(name = v)) }
        EnumDropdown("Type", FermentableType.entries, malt.type) { v ->
            viewModel.save(malt.copy(type = v))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LabeledTextField("Origine", malt.origin, onValueChange = { v ->
                viewModel.save(malt.copy(origin = v))
            }, modifier = Modifier.weight(1f))
            LabeledTextField("Malterie", malt.maltster, onValueChange = { v ->
                viewModel.save(malt.copy(maltster = v))
            }, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IntField("Année", malt.year ?: 0, { v ->
                viewModel.save(malt.copy(year = v.takeIf { y -> y > 1900 }))
            }, modifier = Modifier.weight(1f))
            NumberField("Couleur", malt.colorEbc, { v ->
                viewModel.save(malt.copy(colorEbc = v))
            }, suffix = "EBC", modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberField("Rendement", malt.extractYieldPct, { v ->
                viewModel.save(malt.copy(extractYieldPct = v))
            }, suffix = "%", modifier = Modifier.weight(1f))
            NumberField("Part fermentescible", malt.fermentabilityPct, { v ->
                viewModel.save(malt.copy(fermentabilityPct = v))
            }, suffix = "%", decimals = 0, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberField("Pouvoir diastasique", malt.diastaticPowerLintner, { v ->
                viewModel.save(malt.copy(diastaticPowerLintner = v))
            }, suffix = "°L", decimals = 0, modifier = Modifier.weight(1f))
            NumberField("Part maximale", malt.maxBatchPct, { v ->
                viewModel.save(malt.copy(maxBatchPct = v))
            }, suffix = "%", decimals = 0, modifier = Modifier.weight(1f))
        }
        TagsField("Styles associés", malt.associatedStyles) { v ->
            viewModel.save(malt.copy(associatedStyles = v))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OptionalNumberField("Prix", malt.pricePerKg, { v ->
                viewModel.save(malt.copy(pricePerKg = v))
            }, suffix = "€/kg", modifier = Modifier.weight(1f))
            NumberField("Stock", malt.stockG, { v ->
                viewModel.save(malt.copy(stockG = v))
            }, suffix = "g", decimals = 0, modifier = Modifier.weight(1f))
        }
        LabeledTextField("Notes", malt.notes, onValueChange = { v ->
            viewModel.save(malt.copy(notes = v))
        }, singleLine = false)
    }
}

@Composable
private fun HopCard(hop: Hop, viewModel: CatalogViewModel) {
    ExpandableLineCard(
        title = hop.name,
        summary = "${Format.percent(hop.alphaAcidPct)} AA · ${hop.purpose.label}" +
            if (hop.origin.isNotBlank()) " · ${hop.origin}" else "",
        trailingLabel = hop.pricePerKg?.let { Format.money(it) + "/kg" },
        onDelete = { viewModel.delete(hop) },
    ) {
        LabeledTextField("Nom", hop.name) { v -> viewModel.save(hop.copy(name = v)) }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LabeledTextField("Origine", hop.origin, onValueChange = { v ->
                viewModel.save(hop.copy(origin = v))
            }, modifier = Modifier.weight(1f))
            IntField("Année", hop.year ?: 0, { v ->
                viewModel.save(hop.copy(year = v.takeIf { y -> y > 1900 }))
            }, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberField("Acides alpha", hop.alphaAcidPct, { v ->
                viewModel.save(hop.copy(alphaAcidPct = v))
            }, suffix = "%", modifier = Modifier.weight(1f))
            NumberField("Acides bêta", hop.betaAcidPct, { v ->
                viewModel.save(hop.copy(betaAcidPct = v))
            }, suffix = "%", modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            EnumDropdown("Forme", HopForm.entries, hop.form, onSelect = { v ->
                viewModel.save(hop.copy(form = v))
            }, modifier = Modifier.weight(1f))
            EnumDropdown("Usage", HopPurpose.entries, hop.purpose, onSelect = { v ->
                viewModel.save(hop.copy(purpose = v))
            }, modifier = Modifier.weight(1f))
        }
        TagsField("Arômes", hop.aromas) { v -> viewModel.save(hop.copy(aromas = v)) }
        TagsField("Substituts", hop.substitutes) { v -> viewModel.save(hop.copy(substitutes = v)) }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OptionalNumberField("Prix", hop.pricePerKg, { v ->
                viewModel.save(hop.copy(pricePerKg = v))
            }, suffix = "€/kg", modifier = Modifier.weight(1f))
            NumberField("Stock", hop.stockG, { v ->
                viewModel.save(hop.copy(stockG = v))
            }, suffix = "g", decimals = 0, modifier = Modifier.weight(1f))
        }
        LabeledTextField("Notes", hop.notes, onValueChange = { v ->
            viewModel.save(hop.copy(notes = v))
        }, singleLine = false)
    }
}

@Composable
private fun YeastCard(yeast: Yeast, viewModel: CatalogViewModel) {
    ExpandableLineCard(
        title = "${yeast.laboratory} ${yeast.name}".trim(),
        summary = "${yeast.type.label} · ${yeast.tempRangeLabel} · " +
            "${Format.percent(yeast.attenuationAvgPct)} · ${yeast.flocculation.label}",
        trailingLabel = yeast.pricePerUnit?.let { Format.money(it) },
        onDelete = { viewModel.delete(yeast) },
    ) {
        LabeledTextField("Nom", yeast.name) { v -> viewModel.save(yeast.copy(name = v)) }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LabeledTextField("Laboratoire", yeast.laboratory, onValueChange = { v ->
                viewModel.save(yeast.copy(laboratory = v))
            }, modifier = Modifier.weight(1f))
            LabeledTextField("Référence", yeast.productId, onValueChange = { v ->
                viewModel.save(yeast.copy(productId = v))
            }, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            EnumDropdown("Type", YeastType.entries, yeast.type, onSelect = { v ->
                viewModel.save(yeast.copy(type = v))
            }, modifier = Modifier.weight(1f))
            EnumDropdown("Forme", YeastForm.entries, yeast.form, onSelect = { v ->
                viewModel.save(yeast.copy(form = v))
            }, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberField("Température min.", yeast.tempMinC, { v ->
                viewModel.save(yeast.copy(tempMinC = v))
            }, suffix = "°C", modifier = Modifier.weight(1f))
            NumberField("Température max.", yeast.tempMaxC, { v ->
                viewModel.save(yeast.copy(tempMaxC = v))
            }, suffix = "°C", modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberField("Atténuation min.", yeast.attenuationMinPct, { v ->
                viewModel.save(yeast.copy(attenuationMinPct = v))
            }, suffix = "%", modifier = Modifier.weight(1f))
            NumberField("Atténuation max.", yeast.attenuationMaxPct, { v ->
                viewModel.save(yeast.copy(attenuationMaxPct = v))
            }, suffix = "%", modifier = Modifier.weight(1f))
        }
        EnumDropdown("Floculation", Flocculation.entries, yeast.flocculation) { v ->
            viewModel.save(yeast.copy(flocculation = v))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberField("Tolérance à l'alcool", yeast.alcoholTolerancePct, { v ->
                viewModel.save(yeast.copy(alcoholTolerancePct = v))
            }, suffix = "%", modifier = Modifier.weight(1f))
            NumberField("Cellules par unité", yeast.cellsPerGramBillions, { v ->
                viewModel.save(yeast.copy(cellsPerGramBillions = v))
            }, suffix = "Md", decimals = 2, modifier = Modifier.weight(1f))
        }
        TagsField("Profil aromatique", yeast.aromaProfile) { v ->
            viewModel.save(yeast.copy(aromaProfile = v))
        }
        TagsField("Styles populaires", yeast.popularStyles) { v ->
            viewModel.save(yeast.copy(popularStyles = v))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OptionalNumberField("Prix", yeast.pricePerUnit, { v ->
                viewModel.save(yeast.copy(pricePerUnit = v))
            }, suffix = "€", modifier = Modifier.weight(1f))
            NumberField("Conditionnement", yeast.packageSizeG, { v ->
                viewModel.save(yeast.copy(packageSizeG = v))
            }, suffix = "g", modifier = Modifier.weight(1f))
        }
        LabeledTextField("Notes", yeast.notes, onValueChange = { v ->
            viewModel.save(yeast.copy(notes = v))
        }, singleLine = false)
    }
}

@Composable
private fun AdditiveCard(additive: Additive, viewModel: CatalogViewModel) {
    ExpandableLineCard(
        title = additive.name,
        summary = "${additive.type.label} · ${additive.defaultStage.label} · " +
            "%.1f %s".format(additive.defaultAmount, additive.defaultUnit.label),
        onDelete = { viewModel.delete(additive) },
    ) {
        LabeledTextField("Nom", additive.name) { v -> viewModel.save(additive.copy(name = v)) }
        EnumDropdown("Catégorie", AdditiveType.entries, additive.type) { v ->
            viewModel.save(additive.copy(type = v))
        }
        EnumDropdown("Moment d'ajout", AdditiveStage.entries, additive.defaultStage) { v ->
            viewModel.save(additive.copy(defaultStage = v))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberField("Quantité type", additive.defaultAmount, { v ->
                viewModel.save(additive.copy(defaultAmount = v))
            }, decimals = 2, modifier = Modifier.weight(1f))
            EnumDropdown("Unité", AmountUnit.entries, additive.defaultUnit, onSelect = { v ->
                viewModel.save(additive.copy(defaultUnit = v))
            }, modifier = Modifier.weight(1f))
        }
        if (additive.type == AdditiveType.ACID) {
            OptionalNumberField("Acidité", additive.acidMeqPerUnit, { v ->
                viewModel.save(additive.copy(acidMeqPerUnit = v))
            }, suffix = "mEq / unité")
        }
        if (additive.type == AdditiveType.WATER_AGENT) {
            Text("Ions apportés (ppm par g/L)", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberField("Ca", additive.calciumPpmPerGPerL, { v ->
                    viewModel.save(additive.copy(calciumPpmPerGPerL = v))
                }, decimals = 1, modifier = Modifier.weight(1f))
                NumberField("Mg", additive.magnesiumPpmPerGPerL, { v ->
                    viewModel.save(additive.copy(magnesiumPpmPerGPerL = v))
                }, decimals = 1, modifier = Modifier.weight(1f))
                NumberField("Na", additive.sodiumPpmPerGPerL, { v ->
                    viewModel.save(additive.copy(sodiumPpmPerGPerL = v))
                }, decimals = 1, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberField("SO4", additive.sulfatePpmPerGPerL, { v ->
                    viewModel.save(additive.copy(sulfatePpmPerGPerL = v))
                }, decimals = 1, modifier = Modifier.weight(1f))
                NumberField("Cl", additive.chloridePpmPerGPerL, { v ->
                    viewModel.save(additive.copy(chloridePpmPerGPerL = v))
                }, decimals = 1, modifier = Modifier.weight(1f))
                NumberField("HCO3", additive.bicarbonatePpmPerGPerL, { v ->
                    viewModel.save(additive.copy(bicarbonatePpmPerGPerL = v))
                }, decimals = 1, modifier = Modifier.weight(1f))
            }
        }
        OptionalNumberField("Prix", additive.pricePerUnit, { v ->
            viewModel.save(additive.copy(pricePerUnit = v))
        }, suffix = "€")
        LabeledTextField("Notes", additive.notes, onValueChange = { v ->
            viewModel.save(additive.copy(notes = v))
        }, singleLine = false)
    }
}

@Composable
private fun WaterCard(profile: WaterProfile, viewModel: CatalogViewModel) {
    ExpandableLineCard(
        title = profile.name,
        summary = "Ca %.0f · Mg %.0f · Na %.0f · SO4 %.0f · Cl %.0f · HCO3 %.0f".format(
            profile.calcium, profile.magnesium, profile.sodium,
            profile.sulfate, profile.chloride, profile.bicarbonate,
        ),
        onDelete = { viewModel.delete(profile) },
    ) {
        LabeledTextField("Nom", profile.name) { v -> viewModel.save(profile.copy(name = v)) }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberField("Calcium", profile.calcium, { v ->
                viewModel.save(profile.copy(calcium = v))
            }, suffix = "ppm", decimals = 0, modifier = Modifier.weight(1f))
            NumberField("Magnésium", profile.magnesium, { v ->
                viewModel.save(profile.copy(magnesium = v))
            }, suffix = "ppm", decimals = 0, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberField("Sodium", profile.sodium, { v ->
                viewModel.save(profile.copy(sodium = v))
            }, suffix = "ppm", decimals = 0, modifier = Modifier.weight(1f))
            NumberField("Sulfate", profile.sulfate, { v ->
                viewModel.save(profile.copy(sulfate = v))
            }, suffix = "ppm", decimals = 0, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberField("Chlorure", profile.chloride, { v ->
                viewModel.save(profile.copy(chloride = v))
            }, suffix = "ppm", decimals = 0, modifier = Modifier.weight(1f))
            NumberField("Bicarbonate", profile.bicarbonate, { v ->
                viewModel.save(profile.copy(bicarbonate = v))
            }, suffix = "ppm", decimals = 0, modifier = Modifier.weight(1f))
        }
        KeyValueRow("Alcalinité", "%.0f ppm CaCO3".format(profile.alkalinityAsCaCO3))
        KeyValueRow("Alcalinité résiduelle", "%.0f ppm CaCO3".format(profile.residualAlkalinity), emphasis = true)
        KeyValueRow(
            "Rapport sulfate / chlorure",
            if (profile.sulfateChlorideRatio.isFinite()) "%.2f".format(profile.sulfateChlorideRatio) else "—",
        )
        LabeledTextField("Notes", profile.notes, onValueChange = { v ->
            viewModel.save(profile.copy(notes = v))
        }, singleLine = false)
    }
}
