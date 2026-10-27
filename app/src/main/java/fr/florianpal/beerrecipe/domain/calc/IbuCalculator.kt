package fr.florianpal.beerrecipe.domain.calc

import fr.florianpal.beerrecipe.data.entity.RecipeHop
import fr.florianpal.beerrecipe.domain.model.HopUse
import fr.florianpal.beerrecipe.domain.model.IbuFormula
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.tanh

/**
 * Estimation de l'amertume. Trois écoles coexistent ; Tinseth est celle qui colle le mieux
 * aux brassins tout grain modernes, Rager donne des valeurs plus élevées et reste la référence
 * de nombreux logiciels anciens, Garetz corrige en plus par le taux de houblonnage.
 */
object IbuCalculator {

    /** Paramètres de l'installation et du brassin qui influent sur l'isomérisation. */
    data class Context(
        val batchVolumeL: Double,
        val boilGravity: Double,
        val boilTimeMin: Int,
        val equipmentUtilizationPct: Double = 100.0,
        val formula: IbuFormula = IbuFormula.TINSETH,
    )

    /** Amertume apportée par un houblon, et l'utilisation correspondante en pourcentage. */
    fun contribution(hop: RecipeHop, ctx: Context): Pair<Double, Double> {
        if (ctx.batchVolumeL <= 0.0 || hop.amountG <= 0.0) return 0.0 to 0.0
        val minutes = effectiveMinutes(hop, ctx.boilTimeMin)
        if (minutes <= 0.0) return 0.0 to 0.0

        val baseUtil = when (ctx.formula) {
            IbuFormula.TINSETH -> tinsethUtilization(ctx.boilGravity, minutes)
            IbuFormula.RAGER -> ragerUtilization(ctx.boilGravity, minutes)
            IbuFormula.GARETZ -> garetzUtilization(minutes)
        }

        val util = baseUtil *
            hop.form.utilizationFactor *
            useFactor(hop) *
            (ctx.equipmentUtilizationPct / 100.0)

        val mgPerLiter = hop.alphaAcidPct / 100.0 * hop.amountG * 1000.0 / ctx.batchVolumeL
        val ibu = util * mgPerLiter
        return ibu to util * 100.0
    }

    fun total(hops: List<RecipeHop>, ctx: Context): Double =
        hops.sumOf { contribution(it, ctx).first }

    /**
     * Durée réellement efficace d'un ajout. Le premier moût isomérise pendant toute
     * l'ébullition, le houblonnage à cru n'isomérise pas du tout.
     */
    private fun effectiveMinutes(hop: RecipeHop, boilTimeMin: Int): Double = when (hop.use) {
        HopUse.BOIL -> hop.timeMin.coerceAtMost(boilTimeMin).toDouble()
        HopUse.FIRST_WORT -> boilTimeMin.toDouble()
        HopUse.MASH -> boilTimeMin.toDouble()
        HopUse.WHIRLPOOL -> hop.timeMin.toDouble()
        HopUse.DRY_HOP -> 0.0
    }

    /** Correctif propre au mode d'ajout, température du whirlpool comprise. */
    private fun useFactor(hop: RecipeHop): Double = when (hop.use) {
        HopUse.FIRST_WORT -> 1.10
        HopUse.MASH -> 0.80
        HopUse.WHIRLPOOL -> whirlpoolFactor(hop.temperatureC)
        else -> 1.0
    }

    /**
     * L'isomérisation s'effondre dès que le moût passe sous l'ébullition : à 80 °C il
     * ne reste qu'environ un tiers de l'utilisation, à 70 °C presque plus rien.
     */
    fun whirlpoolFactor(tempC: Double): Double = when {
        tempC >= 99.0 -> 1.00
        tempC >= 90.0 -> 0.60
        tempC >= 85.0 -> 0.45
        tempC >= 80.0 -> 0.33
        tempC >= 75.0 -> 0.22
        tempC >= 70.0 -> 0.12
        tempC >= 60.0 -> 0.05
        else -> 0.0
    }

    /** Tinseth : utilisation = facteur de densité × facteur de durée. */
    fun tinsethUtilization(boilGravity: Double, minutes: Double): Double {
        val bigness = 1.65 * 0.000125.pow(boilGravity - 1.0)
        val timeFactor = (1.0 - exp(-0.04 * minutes)) / 4.15
        return bigness * timeFactor
    }

    /** Rager : courbe en tangente hyperbolique, pénalisée au-delà de 1.050. */
    fun ragerUtilization(boilGravity: Double, minutes: Double): Double {
        val util = (18.11 + 13.86 * tanh((minutes - 31.32) / 18.27)) / 100.0
        val gravityAdjust = if (boilGravity > 1.050) (boilGravity - 1.050) / 0.2 else 0.0
        return util / (1.0 + gravityAdjust)
    }

    /** Garetz : table d'utilisation par palier de durée, interpolée linéairement. */
    fun garetzUtilization(minutes: Double): Double {
        val times = doubleArrayOf(0.0, 10.0, 15.0, 20.0, 25.0, 30.0, 35.0, 40.0, 45.0, 50.0, 60.0, 70.0, 80.0, 90.0)
        val utils = doubleArrayOf(0.0, 2.0, 5.0, 8.0, 11.0, 14.0, 16.0, 18.0, 19.0, 20.0, 21.0, 22.0, 23.0, 24.0)
        if (minutes >= times.last()) return utils.last() / 100.0
        for (i in 0 until times.size - 1) {
            if (minutes <= times[i + 1]) {
                val t = (minutes - times[i]) / (times[i + 1] - times[i])
                return (utils[i] + t * (utils[i + 1] - utils[i])) / 100.0
            }
        }
        return 0.0
    }

    /**
     * Quantité de houblon, en grammes, pour atteindre un écart d'amertume donné —
     * l'inverse du calcul ci-dessus, utilisé par les suggestions.
     */
    fun gramsForIbuDelta(deltaIbu: Double, hop: RecipeHop, ctx: Context): Double {
        val perGram = contribution(hop.copy(amountG = 1.0), ctx).first
        return if (perGram <= 0.0) 0.0 else deltaIbu / perGram
    }
}
