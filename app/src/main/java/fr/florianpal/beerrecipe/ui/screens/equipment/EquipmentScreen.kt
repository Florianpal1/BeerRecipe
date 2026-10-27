package fr.florianpal.beerrecipe.ui.screens.equipment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.florianpal.beerrecipe.data.entity.Equipment
import fr.florianpal.beerrecipe.domain.calc.EquipmentSizing
import fr.florianpal.beerrecipe.domain.model.EquipmentType
import fr.florianpal.beerrecipe.domain.model.Format
import fr.florianpal.beerrecipe.ui.common.EmptyState
import fr.florianpal.beerrecipe.ui.common.EnumDropdown
import fr.florianpal.beerrecipe.ui.common.ExpandableLineCard
import fr.florianpal.beerrecipe.ui.common.IntField
import fr.florianpal.beerrecipe.ui.common.KeyValueRow
import fr.florianpal.beerrecipe.ui.common.LabeledTextField
import fr.florianpal.beerrecipe.ui.common.NumberField
import fr.florianpal.beerrecipe.ui.common.SectionCard
import fr.florianpal.beerrecipe.ui.common.appContainer

/**
 * Les environnements de brassage. Chaque installation est décrite une fois puis
 * réutilisée par les recettes, qui en héritent volumes, pertes et rendement.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentScreen() {
    val container = appContainer()
    val viewModel: EquipmentViewModel =
        viewModel(factory = EquipmentViewModel.factory(container.repository))
    val equipments by viewModel.equipments.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { TopAppBar(title = { Text("Environnements de brassage") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.create() },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nouvelle installation") },
            )
        },
    ) { padding ->
        if (equipments.isEmpty()) {
            EmptyState(
                "Aucune installation enregistrée.",
                Modifier.padding(padding),
                hint = "Décrivez vos cuves une fois pour toutes : volumes, pertes, évaporation.",
            )
            return@Scaffold
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(padding),
        ) {
            items(equipments, key = { it.id }) { eq ->
                var calibrating by remember { mutableStateOf(false) }
                if (calibrating) {
                    CalibrationDialog(
                        equipment = eq,
                        onApply = { viewModel.save(it); calibrating = false },
                        onDismiss = { calibrating = false },
                    )
                }
                ExpandableLineCard(
                    title = eq.name + if (eq.isDefault) "  ★" else "",
                    summary = "${eq.type.label} · ${Format.liters(eq.batchVolumeL)} · " +
                        "rendement ${Format.percent(eq.brewhouseEfficiencyPct)}",
                    onDelete = { viewModel.delete(eq) },
                ) {
                    LabeledTextField("Nom", eq.name) { v -> viewModel.save(eq.copy(name = v)) }
                    LabeledTextField("Description", eq.description, onValueChange = { v ->
                        viewModel.save(eq.copy(description = v))
                    }, singleLine = false)
                    EnumDropdown("Type d'installation", EquipmentType.entries, eq.type) { v ->
                        viewModel.save(eq.copy(type = v))
                    }

                    Text("Volumes", style = MaterialTheme.typography.labelLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = eq.usesFixedWater,
                            onCheckedChange = { fixed ->
                                viewModel.save(if (fixed) eq.withCurrentWaterFixed() else eq.copy(fixedMashWaterL = null))
                            },
                        )
                        Text(
                            "  Partir d'une quantité d'eau fixe",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    eq.fixedMashWaterL?.let { mashWater ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            NumberField(
                                "Eau d'empâtage", mashWater,
                                { v -> viewModel.save(eq.withFixedWater(mashWaterL = v)) },
                                suffix = "L", modifier = Modifier.weight(1f),
                            )
                            NumberField(
                                "Eau de rinçage", eq.spargeWaterL,
                                { v -> viewModel.save(eq.withFixedWater(mashWater, spargeWaterL = v)) },
                                suffix = "L", modifier = Modifier.weight(1f),
                            )
                        }
                        Text(
                            "Eau totale ${Format.liters(eq.fixedTotalWaterL ?: 0.0)} : le rinçage rejoint " +
                                "la cuve d'ébullition avec le moût.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    NumberField(
                        "Grain de référence", eq.referenceGrainKg,
                        { v -> viewModel.save(eq.copy(referenceGrainKg = v)) },
                        suffix = "kg", decimals = 2,
                    )
                    if (eq.usesFixedWater) {
                        Text(
                            "Le volume en fermenteur se déduit de cette eau. Le grain de référence sert " +
                                "seulement à l'estimer ici : chaque recette le recalcule avec son propre grain. " +
                                "La température de rinçage se règle dans l'onglet Procédé de la recette.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        NumberField(
                            if (eq.usesFixedWater) "Brassin (calculé)" else "Brassin", eq.batchVolumeL,
                            { v -> viewModel.save(eq.copy(batchVolumeL = v)) },
                            suffix = "L", modifier = Modifier.weight(1f),
                            enabled = !eq.usesFixedWater,
                        )
                        IntField(
                            "Ébullition", eq.boilTimeMin,
                            { v -> viewModel.save(eq.copy(boilTimeMin = v)) },
                            suffix = "min", modifier = Modifier.weight(1f),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        NumberField(
                            "Cuve d'empâtage", eq.mashTunVolumeL,
                            { v -> viewModel.save(eq.copy(mashTunVolumeL = v)) },
                            suffix = "L", modifier = Modifier.weight(1f),
                        )
                        NumberField(
                            "Cuve d'ébullition", eq.kettleVolumeL,
                            { v -> viewModel.save(eq.copy(kettleVolumeL = v)) },
                            suffix = "L", modifier = Modifier.weight(1f),
                        )
                    }
                    NumberField(
                        "Fermenteur", eq.fermenterVolumeL,
                        { v -> viewModel.save(eq.copy(fermenterVolumeL = v)) },
                        suffix = "L",
                    )
                    VesselCheck(eq) { viewModel.save(EquipmentSizing.applySuggested(eq, eq.referenceGrainKg)) }

                    Text("Pertes et évaporation", style = MaterialTheme.typography.labelLarge)
                    TextButton(onClick = { calibrating = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Étalonner avec un brassin réel")
                    }
                    if (eq.usesFixedWater) {
                        Text(
                            "Évaporation, volume mort et pertes suivent l'eau de départ au prorata " +
                                "quand vous la modifiez. Vous pouvez toujours les corriger à la main.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        NumberField(
                            "Évaporation", eq.boilOffRateLPerHour,
                            { v -> viewModel.save(eq.copy(boilOffRateLPerHour = v)) },
                            suffix = "L/h", decimals = 2, modifier = Modifier.weight(1f),
                        )
                        NumberField(
                            "Rétraction", eq.coolingShrinkagePct,
                            { v -> viewModel.save(eq.copy(coolingShrinkagePct = v)) },
                            suffix = "%", modifier = Modifier.weight(1f),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        NumberField(
                            "Absorption du grain", eq.grainAbsorptionLPerKg,
                            { v -> viewModel.save(eq.copy(grainAbsorptionLPerKg = v)) },
                            suffix = "L/kg", decimals = 2, modifier = Modifier.weight(1f),
                        )
                        NumberField(
                            "Volume mort", eq.mashTunDeadspaceL,
                            { v -> viewModel.save(eq.copy(mashTunDeadspaceL = v)) },
                            suffix = "L", decimals = 2, modifier = Modifier.weight(1f),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        NumberField(
                            "Pertes en cuve", eq.trubLossL,
                            { v -> viewModel.save(eq.copy(trubLossL = v)) },
                            suffix = "L", decimals = 2, modifier = Modifier.weight(1f),
                        )
                        NumberField(
                            "Pertes au fermenteur", eq.fermenterLossL,
                            { v -> viewModel.save(eq.copy(fermenterLossL = v)) },
                            suffix = "L", decimals = 2, modifier = Modifier.weight(1f),
                        )
                    }

                    Text("Réglages de calcul", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        NumberField(
                            "Rendement", eq.brewhouseEfficiencyPct,
                            { v -> viewModel.save(eq.copy(brewhouseEfficiencyPct = v)) },
                            suffix = "%", modifier = Modifier.weight(1f),
                        )
                        NumberField(
                            "Utilisation du houblon", eq.hopUtilizationPct,
                            { v -> viewModel.save(eq.copy(hopUtilizationPct = v)) },
                            suffix = "%", decimals = 0, modifier = Modifier.weight(1f),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        NumberField(
                            if (eq.usesFixedWater) "Épaisseur (calculée)" else "Épaisseur d'empâtage",
                            eq.mashThicknessLPerKg,
                            { v -> viewModel.save(eq.copy(mashThicknessLPerKg = v)) },
                            suffix = "L/kg", decimals = 2, modifier = Modifier.weight(1f),
                            enabled = !eq.usesFixedWater,
                        )
                        NumberField(
                            "Inertie de la cuve", eq.mashTunHeatCapacityLEq,
                            { v -> viewModel.save(eq.copy(mashTunHeatCapacityLEq = v)) },
                            suffix = "L éq.", decimals = 2, modifier = Modifier.weight(1f),
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = eq.isDefault,
                            onCheckedChange = { viewModel.save(eq.copy(isDefault = it)) },
                        )
                        Text(
                            "  Installation par défaut des nouvelles recettes",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    LabeledTextField("Notes", eq.notes, onValueChange = { v ->
                        viewModel.save(eq.copy(notes = v))
                    }, singleLine = false)

                    SectionCard(
                        "Volumes calculés",
                        subtitle = "Pour ${Format.kg(eq.referenceGrainKg)} de grain",
                    ) {
                        KeyValueRow("Eau de départ", Format.liters(eq.totalWaterL(eq.referenceGrainKg)))
                        KeyValueRow("Avant ébullition", Format.liters(eq.preBoilVolumeL))
                        KeyValueRow("Chaud en fin d'ébullition", Format.liters(eq.postBoilHotVolumeL))
                        KeyValueRow("En fermenteur", Format.liters(eq.batchVolumeL))
                    }
                }
            }
            item { Text("") }
        }
    }
}

/**
 * Contrôle des cuves pour le grain de référence : alerte quand une cuve est trop petite,
 * et propose des contenances qui gardent la marge de travail d'usage.
 */
@Composable
private fun VesselCheck(eq: Equipment, onApplySuggested: () -> Unit) {
    val vessels = EquipmentSizing.vessels(eq, eq.referenceGrainKg)
    vessels.filter { it.tooSmall }.forEach { v ->
        Text(
            "${v.label} trop petite : ${Format.liters(v.neededL)} à loger dans ${Format.liters(v.currentL)}.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
    SectionCard(
        "Cuves conseillées",
        subtitle = "Pour ${Format.kg(eq.referenceGrainKg)} de grain, marge de travail comprise",
    ) {
        vessels.forEach { v ->
            KeyValueRow(v.label, "${Format.liters(v.suggestedL)} (actuelle ${Format.liters(v.currentL)})")
        }
        TextButton(onClick = onApplySuggested, modifier = Modifier.fillMaxWidth()) {
            Text("Appliquer les tailles conseillées")
        }
    }
}
