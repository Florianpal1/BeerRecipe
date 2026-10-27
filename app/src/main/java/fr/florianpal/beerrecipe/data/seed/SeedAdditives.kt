package fr.florianpal.beerrecipe.data.seed

import fr.florianpal.beerrecipe.data.entity.Additive
import fr.florianpal.beerrecipe.domain.model.AdditiveStage
import fr.florianpal.beerrecipe.domain.model.AdditiveType
import fr.florianpal.beerrecipe.domain.model.AmountUnit

/**
 * Additifs, sels, acides, épices et clarifiants.
 *
 * Pour les sels, les coefficients donnent les ppm apportés par gramme dissous dans un litre
 * d'eau ; ils découlent directement de la masse molaire du sel. Pour les acides, le nombre
 * de milliéquivalents par millilitre sert au calcul du pH d'empâtage.
 */
object SeedAdditives {

    private fun salt(
        name: String,
        ca: Double = 0.0, mg: Double = 0.0, na: Double = 0.0,
        so4: Double = 0.0, cl: Double = 0.0, hco3: Double = 0.0,
        notes: String = "",
    ) = Additive(
        name = name,
        type = AdditiveType.WATER_AGENT,
        defaultStage = AdditiveStage.MASH,
        defaultUnit = AmountUnit.G,
        defaultAmount = 2.0,
        calciumPpmPerGPerL = ca,
        magnesiumPpmPerGPerL = mg,
        sodiumPpmPerGPerL = na,
        sulfatePpmPerGPerL = so4,
        chloridePpmPerGPerL = cl,
        bicarbonatePpmPerGPerL = hco3,
        notes = notes,
    )

    private fun acid(name: String, meqPerMl: Double, notes: String) = Additive(
        name = name,
        type = AdditiveType.ACID,
        defaultStage = AdditiveStage.MASH,
        defaultUnit = AmountUnit.ML,
        defaultAmount = 3.0,
        acidMeqPerUnit = meqPerMl,
        notes = notes,
    )

    private fun other(
        name: String,
        type: AdditiveType,
        stage: AdditiveStage,
        unit: AmountUnit,
        amount: Double,
        notes: String = "",
    ) = Additive(
        name = name, type = type, defaultStage = stage,
        defaultUnit = unit, defaultAmount = amount, notes = notes,
    )

    val ALL: List<Additive> = listOf(
        // --- Sels de brassage ------------------------------------------------------
        salt("Gypse (CaSO4, 2H2O)", ca = 232.8, so4 = 557.7,
            notes = "Accentue l'amertume et la sécheresse : le sel des IPA."),
        salt("Chlorure de calcium (CaCl2, 2H2O)", ca = 272.6, cl = 482.3,
            notes = "Arrondit la bouche et souligne le malt."),
        salt("Sel d'Epsom (MgSO4, 7H2O)", mg = 98.6, so4 = 389.6,
            notes = "À doser avec parcimonie : au-delà de 30 ppm de magnésium, goût amer et astringent."),
        salt("Sel de table (NaCl)", na = 393.4, cl = 606.7,
            notes = "Quelques ppm de sodium élargissent la perception du malt."),
        salt("Bicarbonate de sodium (NaHCO3)", na = 273.7, hco3 = 726.3,
            notes = "Remonte le pH d'empâtage des maques très torréfiées."),
        salt("Craie (CaCO3)", ca = 400.4, hco3 = 1220.0,
            notes = "Très peu soluble dans l'eau froide : préférez le bicarbonate."),

        // --- Acides ----------------------------------------------------------------
        acid("Acide lactique 80 %", 10.7,
            "Environ 10.7 mEq par mL. Au-delà de 5 mL pour 20 L, un goût lactique se perçoit."),
        acid("Acide phosphorique 75 %", 11.4,
            "Plus neutre au goût que l'acide lactique, préféré pour les bières pâles."),

        // --- Clarifiants -----------------------------------------------------------
        other("Mousse d'Irlande", AdditiveType.FINING, AdditiveStage.BOIL, AmountUnit.G, 1.0,
            "Ajouter 15 min avant la fin d'ébullition pour une cassure à chaud nette."),
        other("Whirlfloc", AdditiveType.FINING, AdditiveStage.BOIL, AmountUnit.UNIT, 1.0,
            "Un comprimé pour 20 L, 10 min avant la fin."),
        other("Gélatine", AdditiveType.FINING, AdditiveStage.SECONDARY, AmountUnit.G, 5.0,
            "À froid, après fermentation : clarifie en 48 h."),

        // --- Nutriments ------------------------------------------------------------
        other("Nutriment levure", AdditiveType.NUTRIENT, AdditiveStage.BOIL, AmountUnit.G, 2.0,
            "Indispensable au-delà de 1.070 ou avec beaucoup de sucres simples."),
        other("Enzyme amyloglucosidase", AdditiveType.NUTRIENT, AdditiveStage.PRIMARY, AmountUnit.ML, 2.0,
            "Rend fermentescibles les dextrines : pour les Brut IPA et les bières très sèches."),

        // --- Épices et aromates ----------------------------------------------------
        other("Coriandre (graines concassées)", AdditiveType.SPICE, AdditiveStage.BOIL, AmountUnit.G, 15.0,
            "5 min avant la fin. Concasser juste avant usage."),
        other("Écorces d'orange amère", AdditiveType.SPICE, AdditiveStage.BOIL, AmountUnit.G, 25.0,
            "Le duo classique de la witbier avec la coriandre."),
        other("Graines de paradis", AdditiveType.SPICE, AdditiveStage.BOIL, AmountUnit.G, 5.0,
            "Poivré et chaleureux, typique des bières d'hiver."),
        other("Gingembre frais râpé", AdditiveType.SPICE, AdditiveStage.BOIL, AmountUnit.G, 20.0),
        other("Cannelle (bâtons)", AdditiveType.SPICE, AdditiveStage.BOIL, AmountUnit.G, 3.0),
        other("Gousses de vanille", AdditiveType.FLAVOUR, AdditiveStage.SECONDARY, AmountUnit.UNIT, 2.0,
            "Fendues et macérées dans un peu d'alcool neutre avant ajout."),
        other("Zestes de citron", AdditiveType.SPICE, AdditiveStage.WHIRLPOOL, AmountUnit.G, 20.0),
        other("Fèves de cacao concassées", AdditiveType.FLAVOUR, AdditiveStage.SECONDARY, AmountUnit.G, 100.0),
        other("Café fraîchement moulu", AdditiveType.FLAVOUR, AdditiveStage.SECONDARY, AmountUnit.G, 60.0,
            "En infusion à froid 24 h, puis ajout du liquide filtré."),

        // --- Fruits et bois --------------------------------------------------------
        other("Purée de fruits", AdditiveType.FRUIT, AdditiveStage.SECONDARY, AmountUnit.KG, 2.0,
            "Compter 100 à 200 g par litre. Ajoute de la densité fermentescible."),
        other("Copeaux de chêne français", AdditiveType.WOOD, AdditiveStage.SECONDARY, AmountUnit.G, 30.0,
            "2 à 6 semaines de contact, à goûter régulièrement."),
        other("Copeaux de chêne américain", AdditiveType.WOOD, AdditiveStage.SECONDARY, AmountUnit.G, 30.0,
            "Vanille et noix de coco, plus démonstratif que le chêne français."),

        // --- Embouteillage ---------------------------------------------------------
        other("Sucre de refermentation", AdditiveType.OTHER, AdditiveStage.BOTTLING, AmountUnit.G, 140.0,
            "La quantité exacte est calculée par la recette selon la carbonatation visée."),
    )
}
