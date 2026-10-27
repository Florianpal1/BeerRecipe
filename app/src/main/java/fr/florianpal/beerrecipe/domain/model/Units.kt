package fr.florianpal.beerrecipe.domain.model

import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Conversions entre les unités métriques utilisées par l'application et les unités impériales
 * dans lesquelles les formules brassicoles historiques (Tinseth, Morey, PPG) sont exprimées.
 */
object Units {
    const val KG_PER_LB = 0.45359237
    const val L_PER_GALLON = 3.785411784

    /** Rendement maximal théorique du saccharose, en points par livre et par gallon. */
    const val SUCROSE_PPG = 46.21

    fun kgToLb(kg: Double): Double = kg / KG_PER_LB
    fun gToLb(g: Double): Double = (g / 1000.0) / KG_PER_LB
    fun litersToGallons(l: Double): Double = l / L_PER_GALLON
    fun celsiusToFahrenheit(c: Double): Double = c * 9.0 / 5.0 + 32.0
    fun fahrenheitToCelsius(f: Double): Double = (f - 32.0) * 5.0 / 9.0

    /** EBC et SRM mesurent la même couleur sur deux échelles proportionnelles. */
    fun srmToEbc(srm: Double): Double = srm * 1.97
    fun ebcToSrm(ebc: Double): Double = ebc / 1.97

    /** Le degré Lovibond est l'unité dans laquelle la formule de Morey attend les malts. */
    fun ebcToLovibond(ebc: Double): Double = (ebcToSrm(ebc) + 0.76) / 1.3546
    fun lovibondToEbc(lovibond: Double): Double = srmToEbc(1.3546 * lovibond - 0.76)

    /** Densité spécifique vers degrés Plato (polynôme ASBC). */
    fun sgToPlato(sg: Double): Double =
        -616.868 + 1111.14 * sg - 630.272 * sg.pow(2) + 135.997 * sg.pow(3)

    fun platoToSg(plato: Double): Double =
        1.0 + plato / (258.6 - (plato / 258.2) * 227.1)

    /** « Points de densité » : 1.048 vaut 48 points. */
    fun sgToPoints(sg: Double): Double = (sg - 1.0) * 1000.0
    fun pointsToSg(points: Double): Double = 1.0 + points / 1000.0

    fun round(value: Double, decimals: Int): Double {
        val factor = 10.0.pow(decimals)
        return (value * factor).roundToInt() / factor
    }
}

/** Formatage homogène des grandeurs brassicoles dans toute l'interface. */
object Format {
    fun gravity(sg: Double): String = String.format("%.3f", sg)
    fun percent(value: Double, decimals: Int = 1): String = String.format("%.${decimals}f %%", value)
    fun abv(value: Double): String = String.format("%.1f %% vol.", value)
    fun ibu(value: Double): String = value.roundToInt().toString()
    fun ebc(value: Double): String = String.format("%.0f EBC", value)
    fun srm(value: Double): String = String.format("%.1f SRM", value)
    fun ph(value: Double): String = String.format("%.2f", value)
    fun liters(value: Double): String = String.format("%.1f L", value)
    fun grams(value: Double): String =
        if (value >= 1000) String.format("%.2f kg", value / 1000.0) else String.format("%.0f g", value)
    fun kg(value: Double): String = String.format("%.2f kg", value)
    fun temp(value: Double): String = String.format("%.1f °C", value)
    fun money(value: Double): String = String.format("%.2f €", value)
    fun range(min: Double, max: Double, decimals: Int = 1): String =
        String.format("%.${decimals}f – %.${decimals}f", min, max)
}

/** Intervalle cible d'un paramètre, tel que défini par un style de bière. */
data class TargetRange(val min: Double, val max: Double) {
    val mid: Double get() = (min + max) / 2.0
    operator fun contains(value: Double): Boolean = value in min..max

    /** Écart signé par rapport à l'intervalle : 0 si la valeur est dedans. */
    fun deviation(value: Double): Double = when {
        value < min -> value - min
        value > max -> value - max
        else -> 0.0
    }

    /** Position relative dans l'intervalle, saturée à [0, 1], pour les jauges de l'interface. */
    fun fraction(value: Double): Double =
        if (max <= min) 0.5 else ((value - min) / (max - min)).coerceIn(0.0, 1.0)
}
