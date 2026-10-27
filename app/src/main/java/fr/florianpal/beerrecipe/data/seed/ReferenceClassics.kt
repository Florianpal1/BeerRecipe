package fr.florianpal.beerrecipe.data.seed

import fr.florianpal.beerrecipe.data.entity.FermentationStep
import fr.florianpal.beerrecipe.data.entity.MashStep
import fr.florianpal.beerrecipe.data.entity.Recipe
import fr.florianpal.beerrecipe.data.entity.RecipeAdditive
import fr.florianpal.beerrecipe.data.entity.RecipeHop
import fr.florianpal.beerrecipe.data.entity.RecipeMalt
import fr.florianpal.beerrecipe.data.entity.RecipeYeast
import fr.florianpal.beerrecipe.data.entity.toRecipeLine
import fr.florianpal.beerrecipe.domain.model.AdditiveStage
import fr.florianpal.beerrecipe.domain.model.HopUse

/**
 * Recettes de référence des grands styles classiques, pour 20 L. Les ingrédients sont tirés
 * des fiches du catalogue de départ par leur nom : une fiche renommée ou retirée fait échouer
 * les tests plutôt que de livrer une recette fausse.
 */
object ReferenceClassics {

    private fun malt(name: String, grams: Double): RecipeMalt =
        SeedMalts.ALL.first { it.name == name }.toRecipeLine(0, grams)

    private fun hop(
        name: String, grams: Double, use: HopUse, time: Int,
        dryHopDay: Int? = null, temperatureC: Double = 80.0,
    ): RecipeHop = SeedHops.ALL.first { it.name == name }.toRecipeLine(0, grams)
        .copy(use = use, timeMin = time, dryHopDay = dryHopDay, temperatureC = temperatureC)

    private fun boil(name: String, grams: Double, minutes: Int) = hop(name, grams, HopUse.BOIL, minutes)

    private fun yeast(name: String, grams: Double? = null): RecipeYeast =
        SeedYeasts.ALL.first { it.name == name }.let { it.toRecipeLine(0, grams ?: it.packageSizeG) }

    private fun additive(name: String, amount: Double, stage: AdditiveStage, minutes: Int): RecipeAdditive =
        SeedAdditives.ALL.first { it.name == name }.toRecipeLine(0, amount).copy(stage = stage, timeMin = minutes)

    private fun mash(name: String, temperatureC: Double, minutes: Int) =
        MashStep(recipeId = 0, name = name, temperatureC = temperatureC, timeMin = minutes)

    private fun fermentation(name: String, temperatureC: Double, days: Int) =
        FermentationStep(recipeId = 0, name = name, temperatureC = temperatureC, days = days)

    private fun recipe(name: String, boil: Int, carbonation: Double, notes: String, efficiency: Double = 72.0) =
        Recipe(
            name = name, batchVolumeL = 20.0, boilTimeMin = boil, efficiencyPct = efficiency,
            mashThicknessLPerKg = 3.0, spargeTempC = 76.0, carbonationVolumes = carbonation, notes = notes,
        )

    private val SACCHARIFICATION_MASH_OUT = { temp: Double ->
        listOf(mash("Saccharification", temp, 60), mash("Mash-out", 76.0, 10))
    }

    /** Fermentation basse : fermentation froide, repos diacétyle, longue garde. */
    private val LAGER_FERMENTATION = listOf(
        fermentation("Fermentation primaire", 11.0, 12),
        fermentation("Repos diacétyle", 16.0, 3),
        fermentation("Garde", 2.0, 28),
    )

    val ALL: List<ReferenceRecipe> = listOf(
        // --- Lagers -----------------------------------------------------------------
        ReferenceRecipe(
            styleCode = "5D", styleName = "German Pils", waterName = "Pilsen",
            description = "Pils allemande sèche et nette, amertume franche, Magnum et Tettnang.",
            recipe = recipe("Pils allemande", boil = 90, carbonation = 2.6,
                notes = "Ébullition de 90 min pour chasser le DMS du malt Pilsner. Deux sachets de levure."),
            malts = listOf(malt("Malt Pilsner", 4000.0), malt("Carapils / Carafoam", 200.0)),
            hops = listOf(boil("Magnum", 15.0, 60), boil("Tettnang", 30.0, 15), boil("Tettnang", 20.0, 5)),
            yeasts = listOf(yeast("SafLager W-34/70", 23.0)),
            mashSteps = listOf(mash("Saccharification", 64.0, 60), mash("Mash-out", 76.0, 10)),
            fermentationSteps = LAGER_FERMENTATION,
        ),
        ReferenceRecipe(
            styleCode = "4A", styleName = "Munich Helles", waterName = "Pilsen",
            description = "Blonde de Munich ronde et maltée, amertume discrète au Hersbrucker.",
            recipe = recipe("Munich Helles", boil = 90, carbonation = 2.5,
                notes = "Le malt prime : amertume basse, empâtage en deux paliers pour le corps."),
            malts = listOf(malt("Malt Pilsner", 3600.0), malt("Malt Munich clair", 300.0),
                malt("Carapils / Carafoam", 150.0)),
            hops = listOf(boil("Hersbrucker", 25.0, 60), boil("Hersbrucker", 15.0, 15)),
            yeasts = listOf(yeast("SafLager S-189", 23.0)),
            mashSteps = listOf(mash("Saccharification bêta", 63.0, 30), mash("Saccharification alpha", 72.0, 30),
                mash("Mash-out", 78.0, 10)),
            fermentationSteps = LAGER_FERMENTATION,
        ),
        ReferenceRecipe(
            styleCode = "7A", styleName = "Vienna Lager", waterName = "Vienne",
            description = "Lager ambrée et toastée, malt Vienna majoritaire, Perle et Spalt.",
            recipe = recipe("Vienna Lager", boil = 60, carbonation = 2.5,
                notes = "Robe cuivrée tirée du malt Vienna ; garder une amertume mesurée."),
            malts = listOf(malt("Malt Vienna", 3400.0), malt("Malt Munich clair", 800.0),
                malt("Caramunich I", 350.0)),
            hops = listOf(boil("Perle", 20.0, 60), boil("Spalt", 20.0, 15)),
            yeasts = listOf(yeast("M84 Bohemian Lager", 20.0)),
            mashSteps = SACCHARIFICATION_MASH_OUT(66.0),
            fermentationSteps = LAGER_FERMENTATION,
        ),
        ReferenceRecipe(
            styleCode = "6A", styleName = "Märzen", waterName = "Munich",
            description = "Bière de l'Oktoberfest traditionnelle : riche, maltée, notes de croûte de pain.",
            recipe = recipe("Märzen", boil = 60, carbonation = 2.5,
                notes = "Malts Munich et Vienna pour la richesse, melanoidin pour la croûte de pain."),
            malts = listOf(malt("Malt Munich clair", 2500.0), malt("Malt Vienna", 1800.0),
                malt("Malt Pilsner", 500.0), malt("Caramunich II", 200.0), malt("Malt Melanoidin", 100.0)),
            hops = listOf(boil("Magnum", 10.0, 60), boil("Tettnang", 20.0, 20)),
            yeasts = listOf(yeast("Novalager", 22.0)),
            mashSteps = SACCHARIFICATION_MASH_OUT(66.0),
            fermentationSteps = LAGER_FERMENTATION,
        ),
        ReferenceRecipe(
            styleCode = "8B", styleName = "Schwarzbier", waterName = "Munich",
            description = "Lager noire légère : torréfaction douce grâce au Carafa Special III.",
            recipe = recipe("Schwarzbier", boil = 60, carbonation = 2.5,
                notes = "Le Carafa décortiqué noircit sans l'âpreté des malts torréfiés classiques."),
            malts = listOf(malt("Malt Pilsner", 2000.0), malt("Malt Munich clair", 1900.0),
                malt("Carafa Special III", 200.0), malt("Caramunich I", 150.0)),
            hops = listOf(boil("Magnum", 12.0, 60), boil("Tettnang", 20.0, 15)),
            yeasts = listOf(yeast("SafLager W-34/70", 23.0)),
            mashSteps = SACCHARIFICATION_MASH_OUT(66.0),
            fermentationSteps = LAGER_FERMENTATION,
        ),

        // --- Ales allemandes et belges ------------------------------------------------
        ReferenceRecipe(
            styleCode = "5B", styleName = "Kölsch", waterName = "Équilibrée",
            description = "Ale de Cologne fermentée fraîche puis gardée au froid : délicate et désaltérante.",
            recipe = recipe("Kölsch", boil = 90, carbonation = 2.5,
                notes = "Fermenter frais (16 °C) puis garder au froid pour une finale de lager."),
            malts = listOf(malt("Malt Pilsner", 3800.0), malt("Malt de blé", 300.0)),
            hops = listOf(boil("Spalt Select", 30.0, 60), boil("Tettnang", 15.0, 10)),
            yeasts = listOf(yeast("SafAle K-97")),
            mashSteps = SACCHARIFICATION_MASH_OUT(65.0),
            fermentationSteps = listOf(fermentation("Fermentation primaire", 16.0, 10),
                fermentation("Garde à froid", 2.0, 21)),
        ),
        ReferenceRecipe(
            styleCode = "10A", styleName = "Weissbier", waterName = "Équilibrée",
            description = "Blanche bavaroise : banane et clou de girofle, mousse généreuse.",
            recipe = recipe("Weissbier", boil = 60, carbonation = 3.3,
                notes = "Le palier à 45 °C favorise le clou de girofle. Pas de garde à froid : la levure en suspension fait le style."),
            malts = listOf(malt("Malt de blé", 2500.0), malt("Malt Pilsner", 1700.0)),
            hops = listOf(boil("Tettnang", 15.0, 60)),
            yeasts = listOf(yeast("Munich Classic")),
            mashSteps = listOf(mash("Repos férulique", 45.0, 15), mash("Saccharification bêta", 63.0, 30),
                mash("Saccharification alpha", 72.0, 20), mash("Mash-out", 78.0, 10)),
            fermentationSteps = listOf(fermentation("Fermentation primaire", 18.0, 10)),
        ),
        ReferenceRecipe(
            styleCode = "24A", styleName = "Witbier", waterName = "Équilibrée",
            description = "Blanche belge au froment cru, coriandre et écorces d'orange amère.",
            recipe = recipe("Witbier", boil = 60, carbonation = 2.8,
                notes = "Le froment cru demande un palier protéinique. Épices en fin d'ébullition."),
            malts = listOf(malt("Malt Pilsner", 2200.0), malt("Froment cru", 1800.0),
                malt("Flocons d'avoine", 300.0)),
            hops = listOf(boil("Styrian Goldings", 20.0, 60)),
            yeasts = listOf(yeast("Wit")),
            additives = listOf(
                additive("Coriandre (graines concassées)", 15.0, AdditiveStage.BOIL, 5),
                additive("Écorces d'orange amère", 15.0, AdditiveStage.BOIL, 5),
            ),
            mashSteps = listOf(mash("Repos protéinique", 50.0, 15), mash("Saccharification", 65.0, 45),
                mash("Mash-out", 76.0, 10)),
            fermentationSteps = listOf(fermentation("Fermentation primaire", 20.0, 10)),
        ),
        ReferenceRecipe(
            styleCode = "25B", styleName = "Saison", waterName = "Équilibrée",
            description = "Saison très sèche et épicée, levure Belle Saison en montée libre.",
            recipe = recipe("Saison", boil = 60, carbonation = 3.0,
                notes = "Empâtage froid et un peu de dextrose pour une bière très sèche. Laisser monter jusqu'à 27 °C."),
            malts = listOf(malt("Malt Pilsner", 3600.0), malt("Malt de blé", 400.0),
                malt("Malt Munich clair", 400.0), malt("Caramunich I", 80.0), malt("Dextrose", 300.0)),
            hops = listOf(boil("Magnum", 12.0, 60), boil("Strisselspalt", 25.0, 15),
                boil("Styrian Goldings", 25.0, 5)),
            yeasts = listOf(yeast("Belle Saison")),
            mashSteps = SACCHARIFICATION_MASH_OUT(64.0),
            fermentationSteps = listOf(fermentation("Fermentation primaire", 20.0, 3),
                fermentation("Montée libre", 27.0, 11)),
        ),
        ReferenceRecipe(
            styleCode = "26B", styleName = "Belgian Dubbel", waterName = "Équilibrée",
            description = "Brune d'abbaye : fruits secs, caramel, sucre candi foncé.",
            recipe = recipe("Dubbel", boil = 60, carbonation = 2.6,
                notes = "Le sucre candi foncé s'ajoute en fin d'ébullition. Deux sachets de levure."),
            malts = listOf(malt("Malt Pilsner", 4000.0), malt("Malt Munich clair", 700.0),
                malt("Malt d'abbaye", 250.0), malt("Special B", 120.0), malt("Sucre candi foncé", 400.0)),
            hops = listOf(boil("Magnum", 12.0, 60), boil("Styrian Goldings", 15.0, 15)),
            yeasts = listOf(yeast("Abbaye", 22.0)),
            mashSteps = SACCHARIFICATION_MASH_OUT(66.0),
            fermentationSteps = listOf(fermentation("Fermentation primaire", 19.0, 4),
                fermentation("Montée libre", 23.0, 10), fermentation("Garde", 10.0, 14)),
        ),
        ReferenceRecipe(
            styleCode = "26C", styleName = "Belgian Tripel", waterName = "Pilsen",
            description = "Triple dorée, forte et sèche : 100 % Pilsner, sucre candi clair, Saaz.",
            recipe = recipe("Tripel", boil = 90, carbonation = 3.0,
                notes = "Le sucre candi assèche la bière malgré sa force. Démarrer à 18 °C, finir vers 25 °C."),
            malts = listOf(malt("Malt Pilsner", 5500.0), malt("Sucre candi clair", 1000.0)),
            hops = listOf(boil("Magnum", 15.0, 60), boil("Saaz", 30.0, 15), boil("Saaz", 20.0, 2)),
            yeasts = listOf(yeast("SafAle BE-256", 23.0)),
            mashSteps = SACCHARIFICATION_MASH_OUT(64.0),
            fermentationSteps = listOf(fermentation("Fermentation primaire", 18.0, 4),
                fermentation("Montée libre", 25.0, 10), fermentation("Garde", 10.0, 21)),
        ),

        // --- Ales britanniques et irlandaises -------------------------------------------
        ReferenceRecipe(
            styleCode = "11B", styleName = "Best Bitter", waterName = "Burton-on-Trent",
            description = "Bitter anglaise : Maris Otter, crystal, Challenger et East Kent Goldings.",
            recipe = recipe("Best Bitter", boil = 60, carbonation = 1.9,
                notes = "Peu gazeuse, servie fraîche plutôt que froide."),
            malts = listOf(malt("Maris Otter", 3300.0), malt("Crystal 60 L", 350.0), malt("Malt Biscuit", 100.0)),
            hops = listOf(boil("Challenger", 20.0, 60), boil("East Kent Goldings", 25.0, 15),
                boil("East Kent Goldings", 20.0, 2)),
            yeasts = listOf(yeast("Heritage English Ale")),
            mashSteps = SACCHARIFICATION_MASH_OUT(67.0),
            fermentationSteps = listOf(fermentation("Fermentation primaire", 19.0, 10)),
        ),
        ReferenceRecipe(
            styleCode = "12C", styleName = "English IPA", waterName = "Burton-on-Trent",
            description = "IPA à l'anglaise : amère et terreuse, Target et East Kent Goldings à cru.",
            recipe = recipe("English IPA", boil = 60, carbonation = 2.1,
                notes = "L'eau de Burton, riche en sulfates, souligne une amertume sèche."),
            malts = listOf(malt("Maris Otter", 4800.0), malt("Crystal 60 L", 200.0)),
            hops = listOf(boil("Target", 25.0, 60), boil("Challenger", 20.0, 15),
                boil("East Kent Goldings", 30.0, 2),
                hop("East Kent Goldings", 30.0, HopUse.DRY_HOP, 4, dryHopDay = 7)),
            yeasts = listOf(yeast("Nottingham")),
            mashSteps = SACCHARIFICATION_MASH_OUT(66.0),
            fermentationSteps = listOf(fermentation("Fermentation primaire", 19.0, 11)),
        ),
        ReferenceRecipe(
            styleCode = "13C", styleName = "English Porter", waterName = "Londres",
            description = "Porter londonien : brown malt, chocolat, notes de toffee.",
            recipe = recipe("English Porter", boil = 60, carbonation = 2.0,
                notes = "Le brown malt apporte le côté grillé sec propre au porter historique."),
            malts = listOf(malt("Maris Otter", 3300.0), malt("Brown malt", 350.0),
                malt("Crystal 60 L", 300.0), malt("Malt chocolat", 200.0)),
            hops = listOf(boil("East Kent Goldings", 25.0, 60), boil("Challenger", 10.0, 60)),
            yeasts = listOf(yeast("Windsor")),
            mashSteps = SACCHARIFICATION_MASH_OUT(68.0),
            fermentationSteps = listOf(fermentation("Fermentation primaire", 19.0, 10)),
        ),
        ReferenceRecipe(
            styleCode = "15B", styleName = "Irish Stout", waterName = "Dublin",
            description = "Stout irlandaise sèche : orge torréfiée, flocons d'orge, café noir.",
            recipe = recipe("Irish Stout", boil = 60, carbonation = 2.0,
                notes = "L'orge torréfiée non maltée donne le café sec ; les flocons d'orge, la mousse crémeuse."),
            malts = listOf(malt("Maris Otter", 2900.0), malt("Flocons d'orge", 600.0),
                malt("Orge torréfiée", 350.0)),
            hops = listOf(boil("Target", 20.0, 60)),
            yeasts = listOf(yeast("SafAle S-04")),
            mashSteps = SACCHARIFICATION_MASH_OUT(66.0),
            fermentationSteps = listOf(fermentation("Fermentation primaire", 18.0, 10)),
        ),
        ReferenceRecipe(
            styleCode = "16B", styleName = "Oatmeal Stout", waterName = "Dublin",
            description = "Stout veloutée à l'avoine, chocolat et café doux.",
            recipe = recipe("Oatmeal Stout", boil = 60, carbonation = 2.1,
                notes = "Les flocons d'avoine donnent la texture soyeuse ; empâtage chaud pour le corps."),
            malts = listOf(malt("Maris Otter", 3600.0), malt("Flocons d'avoine", 500.0),
                malt("Malt chocolat", 300.0), malt("Crystal 60 L", 250.0), malt("Orge torréfiée", 150.0)),
            hops = listOf(boil("East Kent Goldings", 30.0, 60), boil("Challenger", 10.0, 60)),
            yeasts = listOf(yeast("House Ale")),
            mashSteps = SACCHARIFICATION_MASH_OUT(68.0),
            fermentationSteps = listOf(fermentation("Fermentation primaire", 19.0, 10)),
        ),
        ReferenceRecipe(
            styleCode = "15A", styleName = "Irish Red Ale", waterName = "Équilibrée",
            description = "Rousse irlandaise caramel et biscuitée, finale sèche.",
            recipe = recipe("Irish Red Ale", boil = 60, carbonation = 2.2,
                notes = "Une pincée d'orge torréfiée pour la robe rubis et la finale sèche."),
            malts = listOf(malt("Maris Otter", 3300.0), malt("Caramunich I", 200.0),
                malt("Crystal 60 L", 100.0), malt("Orge torréfiée", 60.0)),
            hops = listOf(boil("Challenger", 15.0, 60), boil("East Kent Goldings", 15.0, 15)),
            yeasts = listOf(yeast("SafAle S-04")),
            mashSteps = SACCHARIFICATION_MASH_OUT(66.0),
            fermentationSteps = listOf(fermentation("Fermentation primaire", 18.0, 10)),
        ),

        // --- Ales américaines et grande stout --------------------------------------------
        ReferenceRecipe(
            styleCode = "21A", styleName = "American IPA", waterName = "Pale Ale houblonnée (moderne)",
            description = "IPA West Coast : claire, sèche, résineuse — Columbus, Centennial, Simcoe.",
            recipe = recipe("American IPA", boil = 60, carbonation = 2.4,
                notes = "Empâtage froid pour une finale sèche. Houblonnage à cru en fin de fermentation."),
            malts = listOf(malt("Malt Pale Ale", 5000.0), malt("Carapils / Carafoam", 250.0),
                malt("Caramel Hell", 300.0)),
            hops = listOf(boil("Columbus (CTZ)", 15.0, 60), boil("Centennial", 20.0, 10),
                hop("Simcoe", 30.0, HopUse.WHIRLPOOL, 20, temperatureC = 80.0),
                hop("Centennial", 30.0, HopUse.DRY_HOP, 4, dryHopDay = 6),
                hop("Simcoe", 30.0, HopUse.DRY_HOP, 4, dryHopDay = 6)),
            yeasts = listOf(yeast("SafAle US-05")),
            mashSteps = SACCHARIFICATION_MASH_OUT(65.0),
            fermentationSteps = listOf(fermentation("Fermentation primaire", 18.0, 10)),
        ),
        ReferenceRecipe(
            styleCode = "19A", styleName = "American Amber Ale", waterName = "Équilibrée",
            description = "Ambrée américaine : caramel et agrumes, Centennial et Cascade.",
            recipe = recipe("American Amber Ale", boil = 60, carbonation = 2.4,
                notes = "Équilibre entre le caramel des malts crystal et l'agrume du Cascade."),
            malts = listOf(malt("Malt Pale Ale", 4000.0), malt("Crystal 60 L", 350.0),
                malt("Caramunich II", 150.0)),
            hops = listOf(boil("Centennial", 12.0, 60), boil("Cascade", 30.0, 15), boil("Cascade", 30.0, 2)),
            yeasts = listOf(yeast("BRY-97")),
            mashSteps = SACCHARIFICATION_MASH_OUT(67.0),
            fermentationSteps = listOf(fermentation("Fermentation primaire", 18.0, 10)),
        ),
        ReferenceRecipe(
            styleCode = "20C", styleName = "Imperial Stout", waterName = "Dublin",
            description = "Stout impériale : très forte, chocolat noir, café, longue garde.",
            recipe = recipe("Imperial Stout", boil = 90, carbonation = 2.0, efficiency = 65.0,
                notes = "Rendement prévu à 65 % pour une telle densité ; extrait foncé en fin d'ébullition. Deux sachets de levure, puis plusieurs semaines de garde.",
            ),
            malts = listOf(malt("Maris Otter", 6500.0), malt("Orge torréfiée", 400.0),
                malt("Malt chocolat", 400.0), malt("Crystal 60 L", 350.0), malt("Flocons d'avoine", 300.0),
                malt("Malt black (torréfié)", 100.0), malt("Extrait de malt foncé (sec)", 400.0)),
            hops = listOf(boil("Magnum", 35.0, 60), boil("Challenger", 20.0, 15)),
            yeasts = listOf(yeast("SafAle US-05", 23.0)),
            mashSteps = SACCHARIFICATION_MASH_OUT(66.0),
            fermentationSteps = listOf(fermentation("Fermentation primaire", 18.0, 7),
                fermentation("Fin de fermentation", 21.0, 7), fermentation("Garde", 12.0, 30)),
        ),
    )
}
