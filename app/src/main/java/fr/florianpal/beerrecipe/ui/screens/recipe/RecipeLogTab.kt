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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.florianpal.beerrecipe.data.entity.BrewSession
import fr.florianpal.beerrecipe.domain.calc.BrewCalculator
import fr.florianpal.beerrecipe.domain.model.Format
import fr.florianpal.beerrecipe.ui.common.ExpandableLineCard
import fr.florianpal.beerrecipe.ui.common.IntField
import fr.florianpal.beerrecipe.ui.common.KeyValueRow
import fr.florianpal.beerrecipe.ui.common.LabeledTextField
import fr.florianpal.beerrecipe.ui.common.OptionalNumberField
import fr.florianpal.beerrecipe.ui.common.SectionCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val DATE_FORMAT = SimpleDateFormat("d MMMM yyyy", Locale.FRANCE)

@Composable
fun RecipeLogTab(
    state: RecipeDetailUiState,
    sessions: List<BrewSession>,
    viewModel: RecipeDetailViewModel,
) {
    val malts = state.context.details.malts

    LazyColumn(
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SectionCard(
                "Journal de brassage",
                subtitle = "Confrontez le mesuré au calculé pour recaler votre installation",
            ) {
                KeyValueRow("OG estimée", Format.gravity(state.stats.og))
                KeyValueRow("FG estimée", Format.gravity(state.stats.fg))
                KeyValueRow("Rendement saisi", Format.percent(state.context.details.recipe.efficiencyPct))
                if (sessions.isEmpty()) {
                    Text(
                        "Aucun brassin enregistré. Ajoutez-en un le jour J pour noter vos mesures.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        items(sessions, key = { it.id }) { session ->
            val efficiency = session.measuredOg?.let { og ->
                BrewCalculator.measuredEfficiency(
                    malts,
                    og,
                    session.measuredVolumeL ?: state.context.batchVolumeL,
                )
            }
            val abv = if (session.measuredOg != null && session.measuredFg != null) {
                BrewCalculator.alcoholByVolume(session.measuredOg, session.measuredFg)
            } else null

            ExpandableLineCard(
                title = DATE_FORMAT.format(Date(session.brewDate)),
                summary = buildString {
                    session.measuredOg?.let { append("OG ").append(Format.gravity(it)) }
                    session.measuredFg?.let { append(" · FG ").append(Format.gravity(it)) }
                    abv?.let { append(" · ").append(Format.abv(it)) }
                    if (isEmpty()) append("Mesures à compléter")
                },
                trailingLabel = efficiency?.let { Format.percent(it, 0) },
                onDelete = { viewModel.deleteSession(session) },
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OptionalNumberField(
                        "Densité pré-ébullition", session.measuredPreBoilGravity,
                        { v -> viewModel.saveSession(session.copy(measuredPreBoilGravity = v)) },
                        decimals = 3, modifier = Modifier.weight(1f),
                    )
                    OptionalNumberField(
                        "Volume pré-ébullition", session.measuredPreBoilVolumeL,
                        { v -> viewModel.saveSession(session.copy(measuredPreBoilVolumeL = v)) },
                        suffix = "L", modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OptionalNumberField(
                        "OG mesurée", session.measuredOg,
                        { v -> viewModel.saveSession(session.copy(measuredOg = v)) },
                        decimals = 3, modifier = Modifier.weight(1f),
                    )
                    OptionalNumberField(
                        "FG mesurée", session.measuredFg,
                        { v -> viewModel.saveSession(session.copy(measuredFg = v)) },
                        decimals = 3, modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OptionalNumberField(
                        "Volume obtenu", session.measuredVolumeL,
                        { v -> viewModel.saveSession(session.copy(measuredVolumeL = v)) },
                        suffix = "L", modifier = Modifier.weight(1f),
                    )
                    OptionalNumberField(
                        "pH d'empâtage mesuré", session.measuredMashPh,
                        { v -> viewModel.saveSession(session.copy(measuredMashPh = v)) },
                        decimals = 2, modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OptionalNumberField(
                        "Température d'empâtage", session.measuredMashTempC,
                        { v -> viewModel.saveSession(session.copy(measuredMashTempC = v)) },
                        suffix = "°C", decimals = 1, modifier = Modifier.weight(1f),
                    )
                    OptionalNumberField(
                        "Température de fermentation", session.fermentationTempC,
                        { v -> viewModel.saveSession(session.copy(fermentationTempC = v)) },
                        suffix = "°C", decimals = 1, modifier = Modifier.weight(1f),
                    )
                }
                IntField(
                    "Note sur 10", session.rating ?: 0,
                    { v -> viewModel.saveSession(session.copy(rating = v.coerceIn(0, 10))) },
                )
                LabeledTextField(
                    "Dégustation", session.tastingNotes,
                    onValueChange = { v -> viewModel.saveSession(session.copy(tastingNotes = v)) },
                    singleLine = false)
                LabeledTextField(
                    "Notes du jour", session.notes,
                    onValueChange = { v -> viewModel.saveSession(session.copy(notes = v)) },
                    singleLine = false)

                efficiency?.let {
                    KeyValueRow("Rendement réel du brassin", Format.percent(it), emphasis = true)
                    Text(
                        "Reportez cette valeur dans le rendement de la recette et de " +
                            "l'installation pour que les prochaines estimations tombent juste.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                abv?.let { KeyValueRow("Alcool réel", Format.abv(it), emphasis = true) }
            }
        }

        item {
            Button(onClick = { viewModel.addSession() }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("  Enregistrer un brassin")
            }
        }
    }
}
