package fr.florianpal.beerrecipe.data.seed

import fr.florianpal.beerrecipe.data.entity.FermentationStep
import fr.florianpal.beerrecipe.data.entity.MashStep
import fr.florianpal.beerrecipe.data.entity.Recipe
import fr.florianpal.beerrecipe.data.entity.RecipeHop
import fr.florianpal.beerrecipe.data.entity.RecipeMalt
import fr.florianpal.beerrecipe.data.entity.RecipeYeast
import fr.florianpal.beerrecipe.domain.model.FermentableType
import fr.florianpal.beerrecipe.domain.model.Flocculation
import fr.florianpal.beerrecipe.domain.model.HopUse
import fr.florianpal.beerrecipe.domain.model.YeastForm
import fr.florianpal.beerrecipe.domain.model.YeastType

/**
 * Une recette de référence, prête à être copiée dans les recettes de l'utilisateur.
 * Les lignes portent toutes leurs caractéristiques : la recette reste juste même si
 * l'ingrédient n'existe pas, ou plus, dans le catalogue. Style et eau sont retrouvés par
 * leur nom à l'import.
 */
data class ReferenceRecipe(
    /** Code BJCP, pour retrouver le style même si l'utilisateur l'a renommé. */
    val styleCode: String,
    val styleName: String,
    val waterName: String?,
    val recipe: Recipe,
    val malts: List<RecipeMalt>,
    val hops: List<RecipeHop>,
    val yeasts: List<RecipeYeast>,
    val mashSteps: List<MashStep>,
    val fermentationSteps: List<FermentationStep>,
    val description: String = "",
)

/**
 * Recettes de référence, conçues pour un brassin d'environ 5 L avec du malt Pilsen comme
 * base. À l'import, les quantités sont mises à l'échelle du volume de l'installation.
 * L'identifiant de recette des lignes est fixé à l'import.
 */
object ReferenceRecipes {

    private fun malt(
        name: String, type: FermentableType, grams: Double, colorEbc: Double, yieldPct: Double,
        origin: String, diastatic: Double = 0.0, notes: String = "",
    ) = RecipeMalt(
        recipeId = 0, name = name, type = type, amountG = grams, colorEbc = colorEbc,
        extractYieldPct = yieldPct, origin = origin, diastaticPowerLintner = diastatic, notes = notes,
    )

    private fun hop(
        name: String, grams: Double, alpha: Double, use: HopUse, time: Int, origin: String,
        aromas: List<String>, temperatureC: Double = 80.0, dryHopDay: Int? = null,
    ) = RecipeHop(
        recipeId = 0, name = name, amountG = grams, alphaAcidPct = alpha, use = use, timeMin = time,
        origin = origin, aromas = aromas, temperatureC = temperatureC, dryHopDay = dryHopDay,
    )

    private fun yeast(
        name: String, laboratory: String, grams: Double, attenuation: Double, tempMin: Double,
        tempMax: Double, type: YeastType, flocculation: Flocculation, aromas: List<String>,
    ) = RecipeYeast(
        recipeId = 0, name = name, laboratory = laboratory, type = type, form = YeastForm.DRY,
        amountG = grams, tempMinC = tempMin, tempMaxC = tempMax, attenuationPct = attenuation,
        flocculation = flocculation, aromaProfile = aromas,
    )

    private fun mash(name: String, temperatureC: Double, minutes: Int) =
        MashStep(recipeId = 0, name = name, temperatureC = temperatureC, timeMin = minutes)

    private fun fermentation(name: String, temperatureC: Double, days: Int) =
        FermentationStep(recipeId = 0, name = name, temperatureC = temperatureC, days = days)

    val ALL: List<ReferenceRecipe> = listOf(
        ReferenceRecipe(
            styleCode = "X1",
            styleName = "NEPA (Hazy Pale Ale)",
            waterName = "Pale Ale houblonnée (moderne)",
            recipe = Recipe(
                name = "NEIPA maison", batchVolumeL = 5.0, boilTimeMin = 90, efficiencyPct = 73.0,
                mashThicknessLPerKg = 3.0, spargeTempC = 80.0, carbonationVolumes = 2.4,
                notes = "",
            ),
            malts = listOf(
                malt("Malt Pilsner", FermentableType.BASE, 1000.0, 3.5, 81.0, "Allemagne", diastatic = 105.0),
            ),
            hops = listOf(
                hop("El Dorado", 1.0, 12.8, HopUse.BOIL, 60, "États-Unis", listOf("poire", "pastèque", "bonbon")),
                hop("Callista", 10.0, 4.0, HopUse.BOIL, 60, "Allemagne", listOf("framboise", "abricot", "fruit de la passion")),
                hop("Hallertau Mittelfrüh", 10.0, 3.6, HopUse.BOIL, 60, "Allemagne", listOf("floral", "épicé", "herbacé")),
            ),
            yeasts = listOf(
                yeast("Saturated", "WHC Lab", 5.5, 71.0, 18.0, 22.0, YeastType.ALE, Flocculation.MEDIUM, listOf("fruits à noyau", "mangue", "ananas")),
            ),
            mashSteps = listOf(
                mash("Repos protéinique", 53.0, 10),
                mash("Saccharification bêta", 63.0, 25),
                mash("Saccharification alpha", 73.0, 25),
                mash("Fin d'empâtage", 70.0, 5),
            ),
            fermentationSteps = listOf(
                fermentation("Fermentation primaire", 19.0, 14),
            ),
        ),
        ReferenceRecipe(
            styleCode = "X1",
            styleName = "NEPA (Hazy Pale Ale)",
            waterName = "Équilibrée",
            recipe = Recipe(
                name = "NEIPA maison au blé", batchVolumeL = 5.0, boilTimeMin = 60, efficiencyPct = 75.0,
                mashThicknessLPerKg = 4.2, spargeTempC = 76.0, carbonationVolumes = 2.4,
                notes = "",
            ),
            malts = listOf(
                malt("Malt de blé", FermentableType.BASE, 1000.0, 4.0, 84.0, "Allemagne", diastatic = 100.0),
                malt("Extrait de malt clair (sec)", FermentableType.EXTRACT, 100.0, 15.0, 92.0, "France", diastatic = 0.0),
            ),
            hops = listOf(
                hop("Nugget", 2.0, 11.1, HopUse.BOIL, 60, "États-Unis", listOf("herbacé", "épicé", "résineux")),
                hop("Callista", 10.0, 3.5, HopUse.BOIL, 60, "Allemagne", listOf("framboise", "abricot", "fruit de la passion")),
                hop("Fuggle", 5.0, 2.7, HopUse.BOIL, 60, "Royaume-Uni", listOf("bois", "terreux", "menthe")),
            ),
            yeasts = listOf(
                yeast("Saturated", "WHC Lab", 5.5, 71.0, 18.0, 22.0, YeastType.ALE, Flocculation.MEDIUM, listOf("fruits à noyau", "mangue", "ananas")),
            ),
            mashSteps = listOf(
                mash("Repos protéinique", 53.0, 10),
                mash("Saccharification bêta", 63.0, 25),
                mash("Saccharification alpha", 73.0, 25),
                mash("Mash-out", 78.0, 5),
            ),
            fermentationSteps = listOf(
                fermentation("Fermentation primaire", 19.0, 14),
            ),
        ),
        ReferenceRecipe(
            styleCode = "21C",
            styleName = "NEIPA (Hazy IPA)",
            waterName = "NEIPA / malté (moderne)",
            recipe = Recipe(
                name = "NEIPA Citra El Dorado", batchVolumeL = 5.0, boilTimeMin = 60, efficiencyPct = 75.0,
                mashThicknessLPerKg = 3.0, spargeTempC = 80.0, carbonationVolumes = 2.3,
                notes = "Fermenter vers 19 °C. Houblonnage à cru en fin de fermentation active (jour 3-4), contact limité pour garder le côté juteux.",
            ),
            malts = listOf(
                malt("Malt Pilsen bio", FermentableType.BASE, 1300.0, 3.5, 80.5, "Dingemans — Belgique", diastatic = 80.0),
            ),
            hops = listOf(
                hop("Nugget", 1.5, 13.0, HopUse.BOIL, 60, "États-Unis", listOf("herbacé", "épicé", "résineux")),
                hop("Citra", 12.0, 12.0, HopUse.WHIRLPOOL, 20, "États-Unis", listOf("fruits tropicaux", "litchi", "pamplemousse"), temperatureC = 80.0),
                hop("El Dorado", 8.0, 15.0, HopUse.WHIRLPOOL, 20, "États-Unis", listOf("poire", "pastèque", "bonbon"), temperatureC = 80.0),
                hop("Citra", 15.0, 12.0, HopUse.DRY_HOP, 4, "États-Unis", listOf("fruits tropicaux", "litchi", "pamplemousse"), dryHopDay = 3),
                hop("El Dorado", 10.0, 15.0, HopUse.DRY_HOP, 4, "États-Unis", listOf("poire", "pastèque", "bonbon"), dryHopDay = 3),
            ),
            yeasts = listOf(
                yeast("New England", "Lallemand", 5.5, 80.5, 18.0, 22.0, YeastType.ALE, Flocculation.MEDIUM, listOf("fruits tropicaux", "fruits à noyau", "pêche")),
            ),
            mashSteps = listOf(
                mash("Saccharification", 67.0, 60),
                mash("Mash-out", 76.0, 10),
            ),
            fermentationSteps = listOf(
                fermentation("Fermentation primaire", 19.0, 10),
                fermentation("Garde à froid", 3.0, 3),
            ),
        ),
        ReferenceRecipe(
            styleCode = "X1",
            styleName = "NEPA (Hazy Pale Ale)",
            waterName = "NEIPA / malté (moderne)",
            recipe = Recipe(
                name = "Hazy Pale Azacca Callista", batchVolumeL = 5.0, boilTimeMin = 60, efficiencyPct = 75.0,
                mashThicknessLPerKg = 3.0, spargeTempC = 80.0, carbonationVolumes = 2.3,
                notes = "Azacca pour la mangue, Callista pour le fruit rouge et la passion : une pale ale trouble et légère.",
            ),
            malts = listOf(
                malt("Malt Pilsen bio", FermentableType.BASE, 1050.0, 3.5, 80.5, "Dingemans — Belgique", diastatic = 80.0),
            ),
            hops = listOf(
                hop("Azacca", 10.0, 15.0, HopUse.WHIRLPOOL, 20, "États-Unis", listOf("mangue", "agrumes", "herbacé"), temperatureC = 80.0),
                hop("Callista", 10.0, 3.5, HopUse.WHIRLPOOL, 20, "Allemagne", listOf("framboise", "abricot", "fruit de la passion"), temperatureC = 80.0),
                hop("Azacca", 12.0, 15.0, HopUse.DRY_HOP, 3, "États-Unis", listOf("mangue", "agrumes", "herbacé"), dryHopDay = 3),
                hop("Callista", 10.0, 3.5, HopUse.DRY_HOP, 3, "Allemagne", listOf("framboise", "abricot", "fruit de la passion"), dryHopDay = 3),
            ),
            yeasts = listOf(
                yeast("Pineapple Passion", "WHC Lab", 5.5, 75.0, 18.0, 22.0, YeastType.ALE, Flocculation.MEDIUM, listOf("ananas", "fruits tropicaux")),
            ),
            mashSteps = listOf(
                mash("Saccharification", 67.0, 60),
                mash("Mash-out", 76.0, 10),
            ),
            fermentationSteps = listOf(
                fermentation("Fermentation primaire", 20.0, 10),
                fermentation("Garde à froid", 3.0, 3),
            ),
        ),
        ReferenceRecipe(
            styleCode = "X2",
            styleName = "Session IPA",
            waterName = "Pale Ale houblonnée (moderne)",
            recipe = Recipe(
                name = "Session IPA El Dorado Azacca", batchVolumeL = 5.0, boilTimeMin = 60, efficiencyPct = 75.0,
                mashThicknessLPerKg = 3.0, spargeTempC = 80.0, carbonationVolumes = 2.4,
                notes = "Empâtage chaud pour garder du corps malgré la faible densité.",
            ),
            malts = listOf(
                malt("Malt Pilsen bio", FermentableType.BASE, 850.0, 3.5, 80.5, "Dingemans — Belgique", diastatic = 80.0),
            ),
            hops = listOf(
                hop("Nugget", 1.0, 13.0, HopUse.BOIL, 60, "États-Unis", listOf("herbacé", "épicé", "résineux")),
                hop("El Dorado", 6.0, 15.0, HopUse.WHIRLPOOL, 20, "États-Unis", listOf("poire", "pastèque", "bonbon"), temperatureC = 80.0),
                hop("Azacca", 6.0, 15.0, HopUse.WHIRLPOOL, 20, "États-Unis", listOf("mangue", "agrumes", "herbacé"), temperatureC = 80.0),
                hop("El Dorado", 10.0, 15.0, HopUse.DRY_HOP, 3, "États-Unis", listOf("poire", "pastèque", "bonbon"), dryHopDay = 3),
                hop("Azacca", 8.0, 15.0, HopUse.DRY_HOP, 3, "États-Unis", listOf("mangue", "agrumes", "herbacé"), dryHopDay = 3),
            ),
            yeasts = listOf(
                yeast("New England", "Lallemand", 5.5, 80.5, 18.0, 22.0, YeastType.ALE, Flocculation.MEDIUM, listOf("fruits tropicaux", "fruits à noyau", "pêche")),
            ),
            mashSteps = listOf(
                mash("Saccharification", 68.0, 60),
                mash("Mash-out", 76.0, 10),
            ),
            fermentationSteps = listOf(
                fermentation("Fermentation primaire", 19.0, 8),
                fermentation("Garde à froid", 3.0, 3),
            ),
        ),
        ReferenceRecipe(
            styleCode = "X4",
            styleName = "Hazy Double IPA",
            waterName = "NEIPA / malté (moderne)",
            recipe = Recipe(
                name = "Double Hazy Tropical", batchVolumeL = 5.0, boilTimeMin = 60, efficiencyPct = 70.0,
                mashThicknessLPerKg = 3.0, spargeTempC = 80.0, carbonationVolumes = 2.3,
                notes = "Densité élevée : rendement prévu à 70 %. Sachet entier de levure. Laisser monter à 21 °C en fin de fermentation. Extrait sec ajouté à 10 min de la fin d'ébullition : il monte la densité sans charger la maische.",
            ),
            malts = listOf(
                malt("Malt Pilsen bio", FermentableType.BASE, 1400.0, 3.5, 80.5, "Dingemans — Belgique", diastatic = 80.0),
                malt("Extrait de malt clair (sec)", FermentableType.EXTRACT, 200.0, 15.0, 92.0, "France", diastatic = 0.0, notes = "Ajouter à 10 min de la fin d'ébullition, bien délayé."),
            ),
            hops = listOf(
                hop("Nugget", 2.0, 13.0, HopUse.BOIL, 60, "États-Unis", listOf("herbacé", "épicé", "résineux")),
                hop("Citra", 15.0, 12.0, HopUse.WHIRLPOOL, 20, "États-Unis", listOf("fruits tropicaux", "litchi", "pamplemousse"), temperatureC = 80.0),
                hop("Azacca", 10.0, 15.0, HopUse.WHIRLPOOL, 20, "États-Unis", listOf("mangue", "agrumes", "herbacé"), temperatureC = 80.0),
                hop("El Dorado", 10.0, 15.0, HopUse.WHIRLPOOL, 20, "États-Unis", listOf("poire", "pastèque", "bonbon"), temperatureC = 80.0),
                hop("Citra", 10.0, 12.0, HopUse.DRY_HOP, 5, "États-Unis", listOf("fruits tropicaux", "litchi", "pamplemousse"), dryHopDay = 3),
                hop("El Dorado", 10.0, 15.0, HopUse.DRY_HOP, 4, "États-Unis", listOf("poire", "pastèque", "bonbon"), dryHopDay = 8),
                hop("Azacca", 10.0, 15.0, HopUse.DRY_HOP, 5, "États-Unis", listOf("mangue", "agrumes", "herbacé"), dryHopDay = 3),
                hop("Citra", 10.0, 12.0, HopUse.DRY_HOP, 4, "États-Unis", listOf("fruits tropicaux", "litchi", "pamplemousse"), dryHopDay = 8),
            ),
            yeasts = listOf(
                yeast("New England", "Lallemand", 11.0, 80.5, 18.0, 22.0, YeastType.ALE, Flocculation.MEDIUM, listOf("fruits tropicaux", "fruits à noyau", "pêche")),
            ),
            mashSteps = listOf(
                mash("Saccharification", 66.0, 60),
                mash("Mash-out", 76.0, 10),
            ),
            fermentationSteps = listOf(
                fermentation("Fermentation primaire", 19.0, 7),
                fermentation("Fin de fermentation", 21.0, 5),
                fermentation("Garde à froid", 3.0, 3),
            ),
        ),
        ReferenceRecipe(
            styleCode = "18B",
            styleName = "American Pale Ale",
            waterName = "Pale Ale houblonnée (moderne)",
            recipe = Recipe(
                name = "Pale Ale Citra Nugget", batchVolumeL = 5.0, boilTimeMin = 60, efficiencyPct = 75.0,
                mashThicknessLPerKg = 3.0, spargeTempC = 80.0, carbonationVolumes = 2.4,
                notes = "Pale ale plus amère et plus claire que les hazy : Nugget pour l'amertume, Citra pour l'arôme.",
            ),
            malts = listOf(
                malt("Malt Pilsen bio", FermentableType.BASE, 1050.0, 3.5, 80.5, "Dingemans — Belgique", diastatic = 80.0),
            ),
            hops = listOf(
                hop("Nugget", 3.0, 13.0, HopUse.BOIL, 60, "États-Unis", listOf("herbacé", "épicé", "résineux")),
                hop("Citra", 5.0, 12.0, HopUse.BOIL, 10, "États-Unis", listOf("fruits tropicaux", "litchi", "pamplemousse")),
                hop("Citra", 10.0, 12.0, HopUse.WHIRLPOOL, 15, "États-Unis", listOf("fruits tropicaux", "litchi", "pamplemousse"), temperatureC = 80.0),
                hop("Citra", 10.0, 12.0, HopUse.DRY_HOP, 3, "États-Unis", listOf("fruits tropicaux", "litchi", "pamplemousse"), dryHopDay = 7),
            ),
            yeasts = listOf(
                yeast("Pineapple Passion", "WHC Lab", 5.5, 75.0, 18.0, 22.0, YeastType.ALE, Flocculation.MEDIUM, listOf("ananas", "fruits tropicaux")),
            ),
            mashSteps = listOf(
                mash("Saccharification", 65.0, 60),
                mash("Mash-out", 76.0, 10),
            ),
            fermentationSteps = listOf(
                fermentation("Fermentation primaire", 19.0, 10),
                fermentation("Garde à froid", 3.0, 3),
            ),
        ),
        ReferenceRecipe(
            styleCode = "23A",
            styleName = "Berliner Weisse",
            waterName = "Équilibrée",
            recipe = Recipe(
                name = "Sour Callista El Dorado", batchVolumeL = 5.0, boilTimeMin = 60, efficiencyPct = 75.0,
                mashThicknessLPerKg = 3.0, spargeTempC = 80.0, carbonationVolumes = 2.7,
                notes = "Philly Sour produit l'acidité seule : amertume très basse. Houblonnage à cru léger une fois l'acidité installée.",
            ),
            malts = listOf(
                malt("Malt Pilsen bio", FermentableType.BASE, 650.0, 3.5, 80.5, "Dingemans — Belgique", diastatic = 80.0),
            ),
            hops = listOf(
                hop("Callista", 3.0, 3.5, HopUse.BOIL, 15, "Allemagne", listOf("framboise", "abricot", "fruit de la passion")),
                hop("Callista", 8.0, 3.5, HopUse.DRY_HOP, 3, "Allemagne", listOf("framboise", "abricot", "fruit de la passion"), dryHopDay = 10),
                hop("El Dorado", 8.0, 15.0, HopUse.DRY_HOP, 3, "États-Unis", listOf("poire", "pastèque", "bonbon"), dryHopDay = 10),
            ),
            yeasts = listOf(
                yeast("Wildbrew Philly Sour", "Lallemand", 5.5, 78.5, 20.0, 25.0, YeastType.MIXED, Flocculation.MEDIUM, listOf("acidité lactique", "fruits rouges")),
            ),
            mashSteps = listOf(
                mash("Saccharification", 65.0, 60),
                mash("Mash-out", 76.0, 10),
            ),
            fermentationSteps = listOf(
                fermentation("Fermentation acide", 22.0, 14),
                fermentation("Garde à froid", 3.0, 3),
            ),
        ),
        ReferenceRecipe(
            styleCode = "31A",
            styleName = "Bière personnalisée",
            waterName = "Équilibrée",
            recipe = Recipe(
                name = "Sour Barbe Rouge", batchVolumeL = 5.0, boilTimeMin = 60, efficiencyPct = 75.0,
                mashThicknessLPerKg = 3.0, spargeTempC = 80.0, carbonationVolumes = 2.7,
                notes = "Sour aux fruits rouges sans fruit : Barbe Rouge en houblonnage à cru pour la fraise et la cerise.",
            ),
            malts = listOf(
                malt("Malt Pilsen bio", FermentableType.BASE, 750.0, 3.5, 80.5, "Dingemans — Belgique", diastatic = 80.0),
            ),
            hops = listOf(
                hop("Barbe Rouge", 2.0, 9.0, HopUse.BOIL, 30, "France (Alsace)", listOf("fruits rouges", "cerise", "fraise")),
                hop("Barbe Rouge", 15.0, 9.0, HopUse.DRY_HOP, 3, "France (Alsace)", listOf("fruits rouges", "cerise", "fraise"), dryHopDay = 10),
            ),
            yeasts = listOf(
                yeast("Wildbrew Philly Sour", "Lallemand", 5.5, 78.5, 20.0, 25.0, YeastType.MIXED, Flocculation.MEDIUM, listOf("acidité lactique", "fruits rouges")),
            ),
            mashSteps = listOf(
                mash("Saccharification", 65.0, 60),
                mash("Mash-out", 76.0, 10),
            ),
            fermentationSteps = listOf(
                fermentation("Fermentation acide", 22.0, 14),
                fermentation("Garde à froid", 3.0, 3),
            ),
        ),
        ReferenceRecipe(
            styleCode = "12A",
            styleName = "British Golden Ale",
            waterName = "Équilibrée",
            recipe = Recipe(
                name = "Golden Ale Fuggle", batchVolumeL = 5.0, boilTimeMin = 60, efficiencyPct = 75.0,
                mashThicknessLPerKg = 3.0, spargeTempC = 80.0, carbonationVolumes = 2.0,
                notes = "Ale anglaise dorée : Fuggle terreux et boisé, S-33 pour les esters ronds.",
            ),
            malts = listOf(
                malt("Malt Pilsen bio", FermentableType.BASE, 900.0, 3.5, 80.5, "Dingemans — Belgique", diastatic = 80.0),
            ),
            hops = listOf(
                hop("Nugget", 2.0, 13.0, HopUse.BOIL, 60, "États-Unis", listOf("herbacé", "épicé", "résineux")),
                hop("Fuggle", 6.0, 4.5, HopUse.BOIL, 15, "Royaume-Uni", listOf("bois", "terreux", "menthe")),
                hop("Fuggle", 6.0, 4.5, HopUse.WHIRLPOOL, 10, "Royaume-Uni", listOf("bois", "terreux", "menthe"), temperatureC = 80.0),
            ),
            yeasts = listOf(
                yeast("SafAle S-33", "Fermentis", 5.5, 70.0, 15.0, 20.0, YeastType.ALE, Flocculation.MEDIUM, listOf("fruité", "esters", "rondeur")),
            ),
            mashSteps = listOf(
                mash("Saccharification", 67.0, 60),
                mash("Mash-out", 76.0, 10),
            ),
            fermentationSteps = listOf(
                fermentation("Fermentation primaire", 19.0, 10),
                fermentation("Garde", 10.0, 7),
            ),
        ),
        ReferenceRecipe(
            styleCode = "25A",
            styleName = "Belgian Blond Ale",
            waterName = "Équilibrée",
            recipe = Recipe(
                name = "Blonde belge Hallertau", batchVolumeL = 5.0, boilTimeMin = 60, efficiencyPct = 73.0,
                mashThicknessLPerKg = 3.0, spargeTempC = 80.0, carbonationVolumes = 2.7,
                notes = "Empâtage plus froid pour une bière sèche. Démarrer à 18 °C puis laisser monter à 22 °C. Extrait sec ajouté à 10 min de la fin d'ébullition : il monte la densité sans charger la maische.",
            ),
            malts = listOf(
                malt("Malt Pilsen bio", FermentableType.BASE, 1250.0, 3.5, 80.5, "Dingemans — Belgique", diastatic = 80.0),
                malt("Extrait de malt clair (sec)", FermentableType.EXTRACT, 140.0, 15.0, 92.0, "France", diastatic = 0.0, notes = "Ajouter à 10 min de la fin d'ébullition, bien délayé."),
            ),
            hops = listOf(
                hop("Hallertau Mittelfrüh", 8.0, 4.0, HopUse.BOIL, 60, "Allemagne", listOf("floral", "épicé", "herbacé")),
                hop("Hallertau Mittelfrüh", 6.0, 4.0, HopUse.BOIL, 15, "Allemagne", listOf("floral", "épicé", "herbacé")),
            ),
            yeasts = listOf(
                yeast("SafAle S-33", "Fermentis", 5.5, 70.0, 15.0, 20.0, YeastType.ALE, Flocculation.MEDIUM, listOf("fruité", "esters", "rondeur")),
            ),
            mashSteps = listOf(
                mash("Saccharification", 64.0, 60),
                mash("Mash-out", 76.0, 10),
            ),
            fermentationSteps = listOf(
                fermentation("Fermentation primaire", 18.0, 4),
                fermentation("Montée libre", 22.0, 10),
                fermentation("Garde à froid", 3.0, 7),
            ),
        ),
        ReferenceRecipe(
            styleCode = "18A",
            styleName = "Blonde Ale",
            waterName = "Équilibrée",
            recipe = Recipe(
                name = "Blonde Hallertau Callista", batchVolumeL = 5.0, boilTimeMin = 60, efficiencyPct = 75.0,
                mashThicknessLPerKg = 3.0, spargeTempC = 80.0, carbonationVolumes = 2.4,
                notes = "Blonde facile : Hallertau pour l'amertume douce, Callista pour une touche fruitée.",
            ),
            malts = listOf(
                malt("Malt Pilsen bio", FermentableType.BASE, 1000.0, 3.5, 80.5, "Dingemans — Belgique", diastatic = 80.0),
            ),
            hops = listOf(
                hop("Hallertau Mittelfrüh", 5.0, 4.0, HopUse.BOIL, 60, "Allemagne", listOf("floral", "épicé", "herbacé")),
                hop("Callista", 8.0, 3.5, HopUse.BOIL, 10, "Allemagne", listOf("framboise", "abricot", "fruit de la passion")),
                hop("Callista", 8.0, 3.5, HopUse.WHIRLPOOL, 15, "Allemagne", listOf("framboise", "abricot", "fruit de la passion"), temperatureC = 80.0),
            ),
            yeasts = listOf(
                yeast("SafAle S-33", "Fermentis", 5.5, 70.0, 15.0, 20.0, YeastType.ALE, Flocculation.MEDIUM, listOf("fruité", "esters", "rondeur")),
            ),
            mashSteps = listOf(
                mash("Saccharification", 65.0, 60),
                mash("Mash-out", 76.0, 10),
            ),
            fermentationSteps = listOf(
                fermentation("Fermentation primaire", 17.0, 10),
                fermentation("Garde à froid", 3.0, 5),
            ),
        ),
    ).map { ref ->
        // Positions dans l'ordre de déclaration, comme à la saisie.
        ref.copy(
            description = DESCRIPTIONS[ref.recipe.name].orEmpty(),
            malts = ref.malts.mapIndexed { i, it -> it.copy(position = i) },
            hops = ref.hops.mapIndexed { i, it -> it.copy(position = i) },
            yeasts = ref.yeasts.mapIndexed { i, it -> it.copy(position = i) },
            mashSteps = ref.mashSteps.mapIndexed { i, it -> it.copy(position = i) },
            fermentationSteps = ref.fermentationSteps.mapIndexed { i, it -> it.copy(position = i) },
        )
    }

    private val DESCRIPTIONS get() = mapOf(
        "NEIPA maison" to "Empâtage par paliers, El Dorado, Callista et Hallertau, levure Saturated.",
        "NEIPA maison au blé" to "100 % malt de blé et un peu d'extrait, Nugget, Callista et Fuggle.",
        "NEIPA Citra El Dorado" to "Trouble et juteuse : whirlpool et houblonnage à cru Citra et El Dorado à J3.",
        "Hazy Pale Azacca Callista" to "Pale ale trouble et légère : mangue d'Azacca, fruits rouges de Callista.",
        "Session IPA El Dorado Azacca" to "Moins de 4 % d'alcool, beaucoup d'arôme, empâtage chaud pour le corps.",
        "Double Hazy Tropical" to "Hazy double IPA, extrait sec en fin d'ébullition, double houblonnage à cru J3 et J8.",
        "Pale Ale Citra Nugget" to "Pale ale américaine plus amère et plus claire, Citra en arôme.",
        "Sour Callista El Dorado" to "Sour légère avec Philly Sour, houblonnage à cru fruité à J10.",
        "Sour Barbe Rouge" to "Sour aux notes de fraise et de cerise, sans fruit : Barbe Rouge à cru.",
        "Golden Ale Fuggle" to "Ale anglaise dorée, Fuggle terreux et S-33.",
        "Blonde belge Hallertau" to "Blonde belge sèche, S-33 en montée libre, extrait sec pour la densité.",
        "Blonde Hallertau Callista" to "Blonde facile, amertume douce, touche fruitée de Callista.",
    )
}
