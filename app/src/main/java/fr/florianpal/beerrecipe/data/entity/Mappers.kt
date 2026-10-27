package fr.florianpal.beerrecipe.data.entity

import java.util.Calendar

/**
 * Passage du catalogue à la recette. La ligne de recette conserve une copie des
 * caractéristiques de l'ingrédient : c'est ce qui permet de rouvrir une recette de
 * l'an dernier et d'y retrouver l'alpha exact du houblon employé ce jour-là.
 */

fun Malt.toRecipeLine(recipeId: Long, amountG: Double = 0.0, position: Int = 0) = RecipeMalt(
    recipeId = recipeId,
    catalogId = id,
    name = name,
    type = type,
    amountG = amountG,
    origin = listOfNotNull(maltster.takeIf { it.isNotBlank() }, origin.takeIf { it.isNotBlank() })
        .joinToString(" — "),
    year = year,
    colorEbc = colorEbc,
    extractYieldPct = extractYieldPct,
    fermentabilityPct = fermentabilityPct,
    diastaticPowerLintner = diastaticPowerLintner,
    associatedStyles = associatedStyles,
    distilledWaterPh = distilledWaterPh,
    bufferCapacity = bufferCapacity,
    pricePerKg = pricePerKg,
    position = position,
)

fun Hop.toRecipeLine(recipeId: Long, amountG: Double = 0.0, position: Int = 0) = RecipeHop(
    recipeId = recipeId,
    catalogId = id,
    name = name,
    amountG = amountG,
    origin = origin,
    year = year ?: Calendar.getInstance().get(Calendar.YEAR),
    alphaAcidPct = alphaAcidPct,
    aromas = aromas,
    form = form,
    pricePerKg = pricePerKg,
    position = position,
)

fun Yeast.toRecipeLine(recipeId: Long, amountG: Double = packageSizeG, position: Int = 0) =
    RecipeYeast(
        recipeId = recipeId,
        catalogId = id,
        name = name,
        laboratory = laboratory,
        type = type,
        form = form,
        amountG = amountG,
        tempMinC = tempMinC,
        tempMaxC = tempMaxC,
        attenuationPct = attenuationAvgPct,
        flocculation = flocculation,
        aromaProfile = aromaProfile,
        popularStyles = popularStyles,
        cellsPerGramBillions = cellsPerGramBillions,
        price = pricePerUnit,
        position = position,
    )

fun Additive.toRecipeLine(recipeId: Long, amount: Double = defaultAmount, position: Int = 0) =
    RecipeAdditive(
        recipeId = recipeId,
        catalogId = id,
        name = name,
        type = type,
        stage = defaultStage,
        amount = amount,
        unit = defaultUnit,
        acidMeqPerUnit = acidMeqPerUnit,
        calciumPpmPerGPerL = calciumPpmPerGPerL,
        magnesiumPpmPerGPerL = magnesiumPpmPerGPerL,
        sodiumPpmPerGPerL = sodiumPpmPerGPerL,
        sulfatePpmPerGPerL = sulfatePpmPerGPerL,
        chloridePpmPerGPerL = chloridePpmPerGPerL,
        bicarbonatePpmPerGPerL = bicarbonatePpmPerGPerL,
        price = pricePerUnit,
        position = position,
    )
