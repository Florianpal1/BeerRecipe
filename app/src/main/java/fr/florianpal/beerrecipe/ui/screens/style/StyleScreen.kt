package fr.florianpal.beerrecipe.ui.screens.style

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.florianpal.beerrecipe.data.entity.BeerStyle
import fr.florianpal.beerrecipe.domain.model.Format
import fr.florianpal.beerrecipe.ui.common.BeerColorSwatch
import fr.florianpal.beerrecipe.ui.common.ExpandableLineCard
import fr.florianpal.beerrecipe.ui.common.KeyValueRow
import fr.florianpal.beerrecipe.ui.common.LabeledTextField
import fr.florianpal.beerrecipe.ui.common.NumberField
import fr.florianpal.beerrecipe.ui.common.appContainer
import fr.florianpal.beerrecipe.ui.screens.catalog.CatalogViewModel

/** Référentiel des styles : ce sont eux qui fournissent les cibles du moteur de suggestions. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyleScreen() {
    val container = appContainer()
    val viewModel: CatalogViewModel =
        viewModel(factory = CatalogViewModel.factory(container.repository))
    val catalog by viewModel.catalog.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }

    val styles = remember(catalog.styles, query) {
        if (query.isBlank()) catalog.styles
        else catalog.styles.filter {
            "${it.code} ${it.name} ${it.category}".contains(query, ignoreCase = true)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { TopAppBar(title = { Text("Styles de bière") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.save(
                        BeerStyle(
                            code = "", name = "Nouveau style", category = "Personnel",
                            ogMin = 1.040, ogMax = 1.060, fgMin = 1.008, fgMax = 1.016,
                            abvMin = 4.0, abvMax = 6.5, ibuMin = 20.0, ibuMax = 40.0,
                            ebcMin = 10.0, ebcMax = 30.0, isCustom = true,
                        )
                    )
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Style personnalisé") },
            )
        },
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(padding),
        ) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Rechercher un style") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                )
            }

            items(styles, key = { it.id }) { style ->
                ExpandableLineCard(
                    title = style.displayName,
                    summary = "${style.category} · ${Format.range(style.abvMin, style.abvMax)} % vol. · " +
                        "${Format.ibu(style.ibuMin)}–${Format.ibu(style.ibuMax)} IBU",
                    leading = { BeerColorSwatch((style.ebcMin + style.ebcMax) / 2, size = 28) },
                    onDelete = { viewModel.delete(style) },
                ) {
                    if (style.description.isNotBlank()) {
                        Text(style.description, style = MaterialTheme.typography.bodyMedium)
                    }
                    KeyValueRow(
                        "Densité initiale",
                        "${Format.gravity(style.ogMin)} – ${Format.gravity(style.ogMax)}",
                    )
                    KeyValueRow(
                        "Densité finale",
                        "${Format.gravity(style.fgMin)} – ${Format.gravity(style.fgMax)}",
                    )
                    KeyValueRow("Alcool", Format.range(style.abvMin, style.abvMax) + " % vol.")
                    KeyValueRow("Amertume", "${Format.ibu(style.ibuMin)} – ${Format.ibu(style.ibuMax)} IBU")
                    KeyValueRow("Couleur", "${Format.ebc(style.ebcMin)} – ${Format.ebc(style.ebcMax)}")
                    KeyValueRow("pH d'empâtage", Format.range(style.mashPhMin, style.mashPhMax, 2))
                    KeyValueRow(
                        "Carbonatation",
                        Format.range(style.carbonationMin, style.carbonationMax) + " volumes",
                    )

                    if (style.isCustom) {
                        LabeledTextField("Nom", style.name) { v -> viewModel.save(style.copy(name = v)) }
                        LabeledTextField("Code", style.code) { v -> viewModel.save(style.copy(code = v)) }
                        LabeledTextField("Catégorie", style.category) { v ->
                            viewModel.save(style.copy(category = v))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            NumberField("OG min.", style.ogMin, { v ->
                                viewModel.save(style.copy(ogMin = v))
                            }, decimals = 3, modifier = Modifier.weight(1f))
                            NumberField("OG max.", style.ogMax, { v ->
                                viewModel.save(style.copy(ogMax = v))
                            }, decimals = 3, modifier = Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            NumberField("FG min.", style.fgMin, { v ->
                                viewModel.save(style.copy(fgMin = v))
                            }, decimals = 3, modifier = Modifier.weight(1f))
                            NumberField("FG max.", style.fgMax, { v ->
                                viewModel.save(style.copy(fgMax = v))
                            }, decimals = 3, modifier = Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            NumberField("IBU min.", style.ibuMin, { v ->
                                viewModel.save(style.copy(ibuMin = v))
                            }, decimals = 0, modifier = Modifier.weight(1f))
                            NumberField("IBU max.", style.ibuMax, { v ->
                                viewModel.save(style.copy(ibuMax = v))
                            }, decimals = 0, modifier = Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            NumberField("EBC min.", style.ebcMin, { v ->
                                viewModel.save(style.copy(ebcMin = v))
                            }, decimals = 0, modifier = Modifier.weight(1f))
                            NumberField("EBC max.", style.ebcMax, { v ->
                                viewModel.save(style.copy(ebcMax = v))
                            }, decimals = 0, modifier = Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            NumberField("ABV min.", style.abvMin, { v ->
                                viewModel.save(style.copy(abvMin = v))
                            }, modifier = Modifier.weight(1f))
                            NumberField("ABV max.", style.abvMax, { v ->
                                viewModel.save(style.copy(abvMax = v))
                            }, modifier = Modifier.weight(1f))
                        }
                        LabeledTextField("Description", style.description, onValueChange = { v ->
                            viewModel.save(style.copy(description = v))
                        }, singleLine = false)
                    } else {
                        Text(
                            "Style du guide BJCP : dupliquez-le en style personnalisé pour " +
                                "en modifier les bornes.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
