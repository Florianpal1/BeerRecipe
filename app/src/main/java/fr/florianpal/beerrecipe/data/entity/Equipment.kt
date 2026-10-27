package fr.florianpal.beerrecipe.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import fr.florianpal.beerrecipe.domain.model.EquipmentType

/**
 * Environnement de brassage : la composition et le comportement d'une installation.
 * Une installation est enregistrée une fois puis réutilisée par autant de recettes que voulu ;
 * ses volumes et ses pertes pilotent tous les calculs d'eau et de densité.
 */
@Entity(tableName = "equipment")
data class Equipment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: EquipmentType = EquipmentType.ALL_GRAIN,
    val description: String = "",

    /** Volume visé dans le fermenteur, en litres : c'est la base de tous les calculs. */
    val batchVolumeL: Double = 20.0,
    val boilTimeMin: Int = 60,

    /** Volume utile des cuves, pour alerter en cas de débordement. */
    val mashTunVolumeL: Double = 30.0,
    val kettleVolumeL: Double = 35.0,
    val fermenterVolumeL: Double = 30.0,

    /** Évaporation pendant l'ébullition, en litres par heure. */
    val boilOffRateLPerHour: Double = 3.0,
    /** Rétractation du moût au refroidissement, en pourcentage du volume chaud. */
    val coolingShrinkagePct: Double = 4.0,

    /** Eau retenue par la drêche, en litres par kilo de grain. */
    val grainAbsorptionLPerKg: Double = 1.0,
    /** Volume mort de la cuve d'empâtage, perdu à chaque brassin. */
    val mashTunDeadspaceL: Double = 0.5,
    /** Pertes au fond de la cuve d'ébullition : drêche de houblon et trouble à chaud. */
    val trubLossL: Double = 1.5,
    /** Pertes au transfert du fermenteur vers l'embouteillage. */
    val fermenterLossL: Double = 1.0,

    /** Rendement de la brasserie, du grain au fermenteur, en pourcentage. */
    val brewhouseEfficiencyPct: Double = 72.0,
    /** Correctif d'utilisation des houblons propre à l'installation (100 = neutre). */
    val hopUtilizationPct: Double = 100.0,

    /** Épaisseur d'empâtage par défaut, en litres d'eau par kilo de grain. */
    val mashThicknessLPerKg: Double = 3.0,
    /** Capacité thermique de la cuve, en équivalent litres d'eau (0 = cuve isolée parfaite). */
    val mashTunHeatCapacityLEq: Double = 1.5,

    val isDefault: Boolean = false,
    val notes: String = "",
) {
    val boilTimeHours: Double get() = boilTimeMin / 60.0

    /** Volume chaud en fin d'ébullition, avant refroidissement. */
    val postBoilHotVolumeL: Double
        get() = (batchVolumeL + trubLossL) / (1.0 - coolingShrinkagePct / 100.0)

    /** Volume à mettre en cuve avant d'allumer le feu. */
    val preBoilVolumeL: Double
        get() = postBoilHotVolumeL + boilOffRateLPerHour * boilTimeHours
}
