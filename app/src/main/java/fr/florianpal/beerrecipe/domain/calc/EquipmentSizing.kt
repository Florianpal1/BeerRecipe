package fr.florianpal.beerrecipe.domain.calc

import fr.florianpal.beerrecipe.data.entity.Equipment
import kotlin.math.ceil

/**
 * Contrôle des cuves d'une installation pour un poids de grain donné : le volume que chaque
 * étape occupe réellement, et une taille conseillée qui garde la marge de travail d'usage.
 */
object EquipmentSizing {

    /** Volume occupé par le grain dans la maische, en litres par kilo, en plus de l'eau. */
    const val GRAIN_DISPLACEMENT_L_PER_KG = 0.67

    /** Place pour remuer la maische sans déborder. */
    private const val MASH_TUN_MARGIN = 1.15
    /** Garde contre le débordement à la reprise de l'ébullition. */
    private const val KETTLE_MARGIN = 1.25
    /** Espace de tête pour la mousse de fermentation. */
    private const val FERMENTER_MARGIN = 1.25

    data class Vessel(
        val label: String,
        /** Volume occupé dans la cuve, en litres. */
        val neededL: Double,
        /** Contenance déclarée de la cuve, en litres. */
        val currentL: Double,
        /** Contenance conseillée, marge comprise, arrondie au litre supérieur. */
        val suggestedL: Double,
    ) {
        val tooSmall: Boolean get() = neededL > currentL
    }

    fun vessels(eq: Equipment, grainKg: Double): List<Vessel> {
        val mashL = grainKg * eq.mashThicknessFor(grainKg) + grainKg * GRAIN_DISPLACEMENT_L_PER_KG
        return listOf(
            vessel("Cuve d'empâtage", mashL, eq.mashTunVolumeL, MASH_TUN_MARGIN),
            vessel("Cuve d'ébullition", eq.preBoilVolumeL, eq.kettleVolumeL, KETTLE_MARGIN),
            vessel("Fermenteur", eq.batchVolumeL, eq.fermenterVolumeL, FERMENTER_MARGIN),
        )
    }

    /** Reprend les tailles conseillées dans l'installation. */
    fun applySuggested(eq: Equipment, grainKg: Double): Equipment {
        val (mashTun, kettle, fermenter) = vessels(eq, grainKg).map { it.suggestedL }
        return eq.copy(mashTunVolumeL = mashTun, kettleVolumeL = kettle, fermenterVolumeL = fermenter)
    }

    private fun vessel(label: String, neededL: Double, currentL: Double, margin: Double) =
        Vessel(label, neededL, currentL, ceil(neededL * margin))
}
