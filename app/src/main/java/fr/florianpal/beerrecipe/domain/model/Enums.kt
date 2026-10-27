package fr.florianpal.beerrecipe.domain.model

/** Toutes les énumérations du domaine brassicole, avec leur libellé français d'affichage. */

interface Labelled {
    val label: String
}

/** Nature d'un fermentescible : pilote le rendement, la couleur et le pH d'empâtage. */
enum class FermentableType(override val label: String) : Labelled {
    BASE("Malt de base"),
    CARAMEL("Malt caramel / cristal"),
    ROASTED("Malt torréfié"),
    SPECIALTY("Malt spécial"),
    FLAKED("Flocons / cru"),
    ADJUNCT("Adjuvant amylacé"),
    SUGAR("Sucre"),
    EXTRACT("Extrait de malt"),
    ACIDULATED("Malt acidulé");

    /** Les sucres et extraits ne passent pas par l'empâtage : rendement d'extraction de 100 %. */
    val needsMash: Boolean get() = this != SUGAR && this != EXTRACT

    companion object {
        fun fromLabel(label: String): FermentableType =
            entries.firstOrNull { it.label == label } ?: SPECIALTY
    }
}

/** Moment et mode d'ajout du houblon : détermine l'utilisation des alpha-acides. */
enum class HopUse(override val label: String) : Labelled {
    MASH("Empâtage"),
    FIRST_WORT("Premier moût"),
    BOIL("Ébullition"),
    WHIRLPOOL("Whirlpool"),
    DRY_HOP("Houblonnage à cru");
}

enum class HopForm(override val label: String, val utilizationFactor: Double) : Labelled {
    PELLET("Pellets T90", 1.10),
    CRYO("Cryo / T45", 1.15),
    CONE("Cônes entiers", 1.00),
    PLUG("Plugs", 1.02),
    EXTRACT("Extrait CO2", 1.25);
}

enum class HopPurpose(override val label: String) : Labelled {
    BITTERING("Amérisant"),
    AROMA("Aromatique"),
    DUAL("Mixte");
}

enum class YeastType(override val label: String) : Labelled {
    ALE("Ale (haute fermentation)"),
    LAGER("Lager (basse fermentation)"),
    WHEAT("Levure de blé"),
    KVEIK("Kveik"),
    BRETT("Brettanomyces"),
    MIXED("Culture mixte / lacto"),
    WINE("Vin / champagne");

    /** Taux d'ensemencement de référence en millions de cellules / mL / °P. */
    val pitchRateMillionsPerMlPerPlato: Double
        get() = when (this) {
            LAGER -> 1.5
            KVEIK -> 0.5
            else -> 0.75
        }
}

enum class YeastForm(override val label: String) : Labelled {
    DRY("Sèche"),
    LIQUID("Liquide"),
    SLURRY("Levain / slurry");
}

enum class Flocculation(override val label: String) : Labelled {
    LOW("Faible"),
    MEDIUM("Moyenne"),
    HIGH("Élevée"),
    VERY_HIGH("Très élevée");
}

enum class AdditiveType(override val label: String) : Labelled {
    SPICE("Épice"),
    HERB("Herbe / plante"),
    FRUIT("Fruit"),
    FINING("Clarifiant"),
    WATER_AGENT("Sel de brassage"),
    ACID("Acide"),
    NUTRIENT("Nutriment levure"),
    WOOD("Bois / chêne"),
    FLAVOUR("Arôme"),
    OTHER("Autre");
}

enum class AdditiveStage(override val label: String) : Labelled {
    MASH("Empâtage"),
    SPARGE("Rinçage"),
    BOIL("Ébullition"),
    WHIRLPOOL("Whirlpool"),
    PRIMARY("Fermentation primaire"),
    SECONDARY("Garde / secondaire"),
    BOTTLING("Embouteillage");
}

enum class AmountUnit(override val label: String) : Labelled {
    G("g"),
    KG("kg"),
    ML("mL"),
    L("L"),
    UNIT("unité(s)"),
    TSP("c. à café");

    /** Conversion vers une base grammes ou millilitres pour les totaux et les coûts. */
    fun toBase(amount: Double): Double = when (this) {
        G, ML, UNIT -> amount
        KG, L -> amount * 1000.0
        TSP -> amount * 5.0
    }
}

enum class MashStepType(override val label: String) : Labelled {
    INFUSION("Infusion"),
    TEMPERATURE("Montée en température"),
    DECOCTION("Décoction");
}

enum class EquipmentType(override val label: String) : Labelled {
    ALL_GRAIN("Tout grain (3 cuves)"),
    BIAB("BIAB (Brew In A Bag)"),
    PARTIAL_MASH("Empâtage partiel"),
    EXTRACT("Extrait");
}

/** Formules d'estimation de l'amertume. Tinseth est la référence pour le tout grain moderne. */
enum class IbuFormula(override val label: String) : Labelled {
    TINSETH("Tinseth"),
    RAGER("Rager"),
    GARETZ("Garetz");
}

enum class ColorFormula(override val label: String) : Labelled {
    MOREY("Morey"),
    DANIELS("Daniels"),
    MOSHER("Mosher");
}

enum class Severity(override val label: String) : Labelled {
    INFO("Information"),
    WARNING("À surveiller"),
    PROBLEM("Hors cible");
}
