package fr.florianpal.beerrecipe.domain.calc

import fr.florianpal.beerrecipe.data.entity.WaterProfile

/** Part d'un houblon dans l'amertume totale, pour l'affichage détaillé. */
data class HopContribution(
    val hopId: Long,
    val name: String,
    val ibu: Double,
    val utilizationPct: Double,
)

/** Part d'un malt dans la maische, pour l'affichage détaillé. */
data class MaltContribution(
    val maltId: Long,
    val name: String,
    val amountG: Double,
    val weightPct: Double,
    val gravityPoints: Double,
    val colorPoints: Double,
)

/** Volumes d'eau du brassin, du concassage à l'embouteillage. */
data class WaterPlan(
    val mashWaterL: Double,
    val spargeWaterL: Double,
    val totalWaterL: Double,
    val grainAbsorptionL: Double,
    val preBoilVolumeL: Double,
    val postBoilHotVolumeL: Double,
    val batchVolumeL: Double,
    val packagingVolumeL: Double,
    val strikeTempC: Double,
)

/** Détail du calcul de pH, utile pour comprendre d'où vient l'écart à la cible. */
data class MashPhBreakdown(
    val estimatedPh: Double,
    val distilledWaterPh: Double,
    val residualAlkalinity: Double,
    val alkalinityMeq: Double,
    val acidMeq: Double,
    val bufferCapacity: Double,
    val mashWaterProfile: WaterProfile,
)

/** Ensemencement : cellules nécessaires contre cellules réellement apportées. */
data class PitchingPlan(
    val requiredBillionCells: Double,
    val providedBillionCells: Double,
    val recommendedDryYeastG: Double,
    val actualYeastG: Double,
) {
    val ratio: Double get() = if (requiredBillionCells <= 0) 1.0 else providedBillionCells / requiredBillionCells
}

/** Tous les indicateurs calculés d'une recette. */
data class RecipeStats(
    val og: Double,
    val fg: Double,
    val abv: Double,
    val ibu: Double,
    val srm: Double,
    val ebc: Double,
    val colorHex: Long,

    val preBoilGravity: Double,
    val boilGravity: Double,
    val totalGravityPoints: Double,

    val apparentAttenuationPct: Double,
    val realAttenuationPct: Double,
    val effectiveAttenuationPct: Double,
    val mashTempC: Double,

    val buGuRatio: Double,
    val caloriesPer330ml: Double,
    val caloriesPer100ml: Double,

    val totalGrainKg: Double,
    val totalFermentableKg: Double,
    val totalHopsG: Double,
    val sugarFractionPct: Double,
    val caramelFractionPct: Double,
    val roastedFractionPct: Double,
    val weightedDiastaticPower: Double,

    val water: WaterPlan,
    val mashPh: MashPhBreakdown,
    val finalWaterProfile: WaterProfile,
    val pitching: PitchingPlan,

    val primingSucroseG: Double,
    val primingDextroseG: Double,

    val totalCost: Double,
    val costPerLiter: Double,
    val hasPartialCost: Boolean,

    val hopContributions: List<HopContribution>,
    val maltContributions: List<MaltContribution>,
)
