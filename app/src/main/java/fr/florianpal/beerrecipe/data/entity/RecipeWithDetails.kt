package fr.florianpal.beerrecipe.data.entity

import androidx.room.Embedded
import androidx.room.Relation

/** Une recette et tout ce qu'elle contient, chargées en une seule observation Room. */
data class RecipeWithDetails(
    @Embedded val recipe: Recipe,
    @Relation(parentColumn = "id", entityColumn = "recipeId")
    val malts: List<RecipeMalt> = emptyList(),
    @Relation(parentColumn = "id", entityColumn = "recipeId")
    val hops: List<RecipeHop> = emptyList(),
    @Relation(parentColumn = "id", entityColumn = "recipeId")
    val yeasts: List<RecipeYeast> = emptyList(),
    @Relation(parentColumn = "id", entityColumn = "recipeId")
    val additives: List<RecipeAdditive> = emptyList(),
    @Relation(parentColumn = "id", entityColumn = "recipeId")
    val mashSteps: List<MashStep> = emptyList(),
    @Relation(parentColumn = "id", entityColumn = "recipeId")
    val fermentationSteps: List<FermentationStep> = emptyList(),
) {
    val sortedMalts get() = malts.sortedWith(compareBy({ it.position }, { -it.amountG }))
    val sortedHops get() = hops.sortedWith(compareBy({ it.use.ordinal }, { -it.timeMin }, { it.position }))
    val sortedYeasts get() = yeasts.sortedBy { it.position }
    val sortedAdditives get() = additives.sortedWith(compareBy({ it.stage.ordinal }, { it.position }))
    val sortedMashSteps get() = mashSteps.sortedBy { it.position }
    val sortedFermentationSteps get() = fermentationSteps.sortedBy { it.position }

    /** Poids des grains qui passent à l'empâtage, en kilos : sucres et extraits n'y vont pas. */
    val mashedGrainKg get() = malts.filter { it.type.needsMash }.sumOf { it.amountG } / 1000.0
}

/**
 * Contexte complet d'un calcul : la recette, l'installation et l'eau de brassage.
 * C'est l'unique entrée du moteur de calcul, qui reste ainsi sans dépendance à Room.
 */
data class RecipeContext(
    val details: RecipeWithDetails,
    val equipment: Equipment?,
    val style: BeerStyle?,
    val water: WaterProfile?,
) {
    /** Vrai quand l'installation fixe l'eau : le volume en fermenteur en découle. */
    val usesFixedWater: Boolean get() = equipment?.usesFixedWater == true

    /** Volume en fermenteur réellement visé : celui de la recette, ou celui que permet l'eau fixée. */
    val batchVolumeL: Double
        get() = equipment?.fixedTotalWaterL
            ?.let { equipment.batchVolumeFromWater(it, details.mashedGrainKg, details.recipe.boilTimeMin) }
            ?: details.recipe.batchVolumeL

    /** Épaisseur d'empâtage réellement appliquée : en eau fixe, elle suit le grain. */
    val mashThicknessLPerKg: Double
        get() = equipment?.takeIf { usesFixedWater && details.mashedGrainKg > 0 }
            ?.mashThicknessFor(details.mashedGrainKg)
            ?: details.recipe.mashThicknessLPerKg
}
