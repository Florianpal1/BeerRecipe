package fr.florianpal.beerrecipe.domain.calc

import fr.florianpal.beerrecipe.domain.model.FermentableType

/**
 * Acidité propre des malts, utilisée par le modèle de pH d'empâtage.
 *
 * Chaque malt est décrit par deux grandeurs : le pH qu'il donne seul dans de l'eau distillée,
 * et son pouvoir tampon, c'est-à-dire la quantité d'acide (en mEq par kilo) qu'il faut lui
 * ajouter pour faire descendre ce pH d'une unité. Quand la fiche d'ingrédient ne les précise
 * pas, on les estime à partir du type de malt et de sa couleur : plus un malt est torréfié,
 * plus il est acide et plus il tamponne.
 */
object MaltChemistry {

    fun distilledWaterPh(type: FermentableType, colorEbc: Double, override: Double?): Double {
        override?.let { return it }
        return when (type) {
            FermentableType.BASE -> (5.80 - 0.012 * colorEbc).coerceIn(5.45, 5.90)
            FermentableType.CARAMEL -> (5.22 - 0.0025 * colorEbc).coerceIn(4.50, 5.30)
            FermentableType.ROASTED -> (4.75 - 0.0003 * colorEbc).coerceIn(4.30, 4.90)
            FermentableType.SPECIALTY -> (5.60 - 0.004 * colorEbc).coerceIn(4.80, 5.80)
            FermentableType.FLAKED, FermentableType.ADJUNCT -> 6.00
            FermentableType.ACIDULATED -> 3.50
            FermentableType.SUGAR, FermentableType.EXTRACT -> 5.40
        }
    }

    fun bufferCapacity(type: FermentableType, override: Double?): Double {
        override?.let { return it }
        return when (type) {
            FermentableType.BASE -> 35.0
            FermentableType.CARAMEL -> 50.0
            FermentableType.ROASTED -> 65.0
            FermentableType.SPECIALTY -> 42.0
            FermentableType.FLAKED, FermentableType.ADJUNCT -> 20.0
            FermentableType.ACIDULATED -> 90.0
            // Les sucres et extraits ne tamponnent pas : ils ne participent pas à l'équilibre.
            FermentableType.SUGAR, FermentableType.EXTRACT -> 0.0
        }
    }
}
