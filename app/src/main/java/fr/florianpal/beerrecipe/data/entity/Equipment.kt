package fr.florianpal.beerrecipe.data.entity

import androidx.room.ColumnInfo
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

    /** Volume visé dans le fermenteur, en litres : la base des calculs, sauf en eau fixe. */
    val batchVolumeL: Double = 20.0,
    val boilTimeMin: Int = 60,

    /**
     * Eau d'empâtage fixe, en litres. Renseignée, l'eau commande : avec l'eau de rinçage,
     * elle forme l'eau totale, d'où se déduit le volume en fermenteur pour le grain de chaque recette.
     * La colonne garde son nom historique.
     */
    @ColumnInfo(name = "startWaterL")
    val fixedMashWaterL: Double? = null,
    /** Poids de grain servant à estimer, sur l'installation seule, le volume tiré de l'eau de départ. */
    @ColumnInfo(defaultValue = "5.0")
    val referenceGrainKg: Double = 5.0,
    /**
     * Eau de rinçage des drêches en eau fixe, en litres, versée en plus de l'eau d'empâtage.
     * Elle rejoint ensuite la cuve d'ébullition avec le moût.
     */
    @ColumnInfo(defaultValue = "0.0")
    val spargeWaterL: Double = 0.0,

    /** Volume utile des cuves, pour alerter en cas de débordement. */
    val mashTunVolumeL: Double = 30.0,
    val kettleVolumeL: Double = 35.0,
    val fermenterVolumeL: Double = 30.0,

    /** Évaporation pendant l'ébullition, en litres par heure. */
    val boilOffRateLPerHour: Double = 3.0,
    /** Rétraction du moût au refroidissement, en pourcentage du volume chaud. */
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

    /** Eau totale à prévoir pour un poids de grain donné : absorption et volume mort compris. */
    fun totalWaterL(grainKg: Double): Double =
        preBoilVolumeL + grainKg * grainAbsorptionLPerKg + mashTunDeadspaceL

    /**
     * Volume en fermenteur obtenu à partir d'une eau totale : le plan d'eau parcouru à
     * l'envers (absorption et volume mort, évaporation, rétraction, puis pertes de cuve).
     */
    fun batchVolumeFromWater(
        totalWaterL: Double,
        grainKg: Double,
        boilTimeMin: Int = this.boilTimeMin,
    ): Double {
        val preBoil = totalWaterL - grainKg * grainAbsorptionLPerKg - mashTunDeadspaceL
        val postBoilHot = preBoil - boilOffRateLPerHour * boilTimeMin / 60.0
        return (postBoilHot * (1.0 - coolingShrinkagePct / 100.0) - trubLossL).coerceAtLeast(0.0)
    }

    /** Vrai quand l'eau est fixée : le volume en fermenteur se déduit alors de l'eau. */
    val usesFixedWater: Boolean get() = fixedMashWaterL != null

    /** En eau fixe, toute l'eau du brassage : empâtage plus rinçage. */
    val fixedTotalWaterL: Double?
        get() = fixedMashWaterL?.let { it + spargeWaterL }

    /**
     * Épaisseur d'empâtage pour un poids de grain donné. En eau fixe, l'eau d'empâtage est
     * imposée : l'épaisseur suit donc le grain.
     */
    fun mashThicknessFor(grainKg: Double): Double =
        fixedMashWaterL?.takeIf { grainKg > 0 }?.let { it / grainKg } ?: mashThicknessLPerKg

    /**
     * Change l'eau d'empâtage ou de rinçage en emportant les pertes avec l'eau totale, au prorata :
     * une installation qui brasse plus d'eau est supposée évaporer et perdre davantage dans les
     * mêmes proportions.
     */
    fun withFixedWater(mashWaterL: Double, spargeWaterL: Double = this.spargeWaterL): Equipment {
        val oldTotal = fixedTotalWaterL
        val newTotal = mashWaterL + spargeWaterL
        val updated = copy(fixedMashWaterL = mashWaterL, spargeWaterL = spargeWaterL)
        if (oldTotal == null || oldTotal <= 0 || mashWaterL <= 0 || newTotal <= 0) return updated
        val factor = newTotal / oldTotal
        return updated.copy(
            boilOffRateLPerHour = boilOffRateLPerHour * factor,
            mashTunDeadspaceL = mashTunDeadspaceL * factor,
            trubLossL = trubLossL * factor,
            fermenterLossL = fermenterLossL * factor,
        )
    }

    /**
     * Passe en eau fixe sans rien changer au résultat : on reprend l'eau que demande le brassin
     * actuel pour le grain de référence, répartie entre empâtage (selon l'épaisseur) et rinçage.
     */
    fun withCurrentWaterFixed(): Equipment {
        val total = totalWaterL(referenceGrainKg)
        val mash = (referenceGrainKg * mashThicknessLPerKg).coerceIn(0.0, total)
        return copy(fixedMashWaterL = mash, spargeWaterL = total - mash)
    }

    /** Recale le brassin et l'épaisseur d'empâtage sur l'eau, quand celle-ci est fixée. */
    fun withDerivedValues(): Equipment {
        val water = fixedTotalWaterL ?: return this
        return copy(
            batchVolumeL = batchVolumeFromWater(water, referenceGrainKg),
            mashThicknessLPerKg = mashThicknessFor(referenceGrainKg),
        )
    }
}
