package fr.florianpal.beerrecipe.ui.screens.equipment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.florianpal.beerrecipe.data.entity.Equipment
import fr.florianpal.beerrecipe.domain.calc.EquipmentCalibration
import fr.florianpal.beerrecipe.domain.model.Format
import fr.florianpal.beerrecipe.ui.common.IntField
import fr.florianpal.beerrecipe.ui.common.KeyValueRow
import fr.florianpal.beerrecipe.ui.common.NumberField
import fr.florianpal.beerrecipe.ui.common.OptionalNumberField
import fr.florianpal.beerrecipe.ui.common.formatNumber

/**
 * Saisie d'un brassin réel pour étalonner l'installation : les pertes recalculées sont
 * montrées à côté des anciennes avant d'être appliquées.
 */
@Composable
fun CalibrationDialog(
    equipment: Equipment,
    onApply: (Equipment) -> Unit,
    onDismiss: () -> Unit,
) {
    var totalWater by remember {
        mutableDoubleStateOf(equipment.fixedTotalWaterL ?: equipment.totalWaterL(equipment.referenceGrainKg))
    }
    var grainKg by remember { mutableDoubleStateOf(equipment.referenceGrainKg) }
    var boilTime by remember { mutableIntStateOf(equipment.boilTimeMin) }
    var preBoil by remember { mutableStateOf<Double?>(null) }
    var fermenter by remember { mutableStateOf<Double?>(null) }

    val calibrated = fermenter?.takeIf { it > 0 }?.let {
        EquipmentCalibration.calibrate(
            equipment,
            EquipmentCalibration.Measurement(totalWater, grainKg, boilTime, preBoil, it),
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Étalonner avec un brassin réel") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "Indiquez ce que vous avez réellement mis et obtenu : l'évaporation et ce que " +
                        "retient le grain sont recalculés pour retrouver ce résultat.",
                    style = MaterialTheme.typography.bodySmall,
                )
                NumberField("Eau totale versée", totalWater, { totalWater = it }, suffix = "L")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumberField(
                        "Grain", grainKg, { grainKg = it },
                        suffix = "kg", decimals = 2, modifier = Modifier.weight(1f),
                    )
                    IntField(
                        "Ébullition", boilTime, { boilTime = it },
                        suffix = "min", modifier = Modifier.weight(1f),
                    )
                }
                OptionalNumberField("Volume avant ébullition", preBoil, { preBoil = it }, suffix = "L")
                OptionalNumberField("Moût obtenu dans le fermenteur", fermenter, { fermenter = it }, suffix = "L")
                Text(
                    "Le volume avant ébullition est facultatif ; relevé, il distingue ce que retient " +
                        "le grain de ce qui s'évapore.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                calibrated?.let { c ->
                    Text("Nouveaux réglages", style = MaterialTheme.typography.labelLarge)
                    KeyValueRow(
                        "Évaporation",
                        "${formatNumber(equipment.boilOffRateLPerHour, 2)} → ${formatNumber(c.boilOffRateLPerHour, 2)} L/h",
                    )
                    KeyValueRow(
                        "Absorption du grain",
                        "${formatNumber(equipment.grainAbsorptionLPerKg, 2)} → " +
                            "${formatNumber(c.grainAbsorptionLPerKg, 2)} L/kg",
                    )
                    KeyValueRow(
                        "Volume mort",
                        "${Format.liters(equipment.mashTunDeadspaceL)} → ${Format.liters(c.mashTunDeadspaceL)}",
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { calibrated?.let(onApply) },
                enabled = calibrated != null,
            ) { Text("Appliquer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } },
    )
}
