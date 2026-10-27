package fr.florianpal.beerrecipe.data.repository

import fr.florianpal.beerrecipe.data.db.AppDatabase
import fr.florianpal.beerrecipe.data.entity.Additive
import fr.florianpal.beerrecipe.data.entity.BeerStyle
import fr.florianpal.beerrecipe.data.entity.BrewSession
import fr.florianpal.beerrecipe.data.entity.Equipment
import fr.florianpal.beerrecipe.data.entity.FermentationStep
import fr.florianpal.beerrecipe.data.entity.Hop
import fr.florianpal.beerrecipe.data.entity.Malt
import fr.florianpal.beerrecipe.data.entity.MashStep
import fr.florianpal.beerrecipe.data.entity.Recipe
import fr.florianpal.beerrecipe.data.entity.RecipeAdditive
import fr.florianpal.beerrecipe.data.entity.RecipeContext
import fr.florianpal.beerrecipe.data.entity.RecipeHop
import fr.florianpal.beerrecipe.data.entity.RecipeMalt
import fr.florianpal.beerrecipe.data.entity.RecipeWithDetails
import fr.florianpal.beerrecipe.data.entity.RecipeYeast
import fr.florianpal.beerrecipe.data.entity.WaterProfile
import fr.florianpal.beerrecipe.data.entity.Yeast
import fr.florianpal.beerrecipe.data.seed.ReferenceRecipe
import fr.florianpal.beerrecipe.domain.model.AmountUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Point d'entrée unique vers les données. Assemble aussi le [RecipeContext] attendu par le
 * moteur de calcul, en résolvant l'installation, le style et le profil d'eau d'une recette.
 */
class BrewRepository(private val db: AppDatabase) {

    val recipes: Flow<List<RecipeWithDetails>> = db.recipeDao().observeAll()
    val equipments: Flow<List<Equipment>> = db.equipmentDao().observeAll()
    val styles: Flow<List<BeerStyle>> = db.beerStyleDao().observeAll()
    val waterProfiles: Flow<List<WaterProfile>> = db.waterProfileDao().observeAll()
    val malts: Flow<List<Malt>> = db.maltDao().observeAll()
    val hops: Flow<List<Hop>> = db.hopDao().observeAll()
    val yeasts: Flow<List<Yeast>> = db.yeastDao().observeAll()
    val additives: Flow<List<Additive>> = db.additiveDao().observeAll()
    val sessions: Flow<List<BrewSession>> = db.brewSessionDao().observeAll()

    /** Toutes les recettes, chacune accompagnée de son contexte de calcul. */
    val recipeContexts: Flow<List<RecipeContext>> =
        combine(recipes, equipments, styles, waterProfiles) { list, eq, st, wa ->
            list.map { it.withContext(eq, st, wa) }
        }

    fun observeRecipe(id: Long): Flow<RecipeContext?> =
        combine(
            db.recipeDao().observeById(id),
            equipments,
            styles,
            waterProfiles,
        ) { details, eq, st, wa -> details?.withContext(eq, st, wa) }

    fun observeSessions(recipeId: Long): Flow<List<BrewSession>> =
        db.brewSessionDao().observeForRecipe(recipeId)

    fun observeEquipment(id: Long): Flow<Equipment?> = db.equipmentDao().observeById(id)

    private fun RecipeWithDetails.withContext(
        equipments: List<Equipment>,
        styles: List<BeerStyle>,
        waters: List<WaterProfile>,
    ) = RecipeContext(
        details = this,
        equipment = equipments.firstOrNull { it.id == recipe.equipmentId },
        style = styles.firstOrNull { it.id == recipe.styleId },
        water = waters.firstOrNull { it.id == recipe.sourceWaterId },
    )

    // --- Recettes ------------------------------------------------------------------

    /** Crée une recette vierge, pré-réglée sur l'installation par défaut. */
    suspend fun createRecipe(name: String): Long {
        val equipment = db.equipmentDao().firstOrDefault()
        val recipe = Recipe(
            name = name,
            equipmentId = equipment?.id,
            batchVolumeL = equipment?.batchVolumeL ?: 20.0,
            boilTimeMin = equipment?.boilTimeMin ?: 60,
            efficiencyPct = equipment?.brewhouseEfficiencyPct ?: 72.0,
            mashThicknessLPerKg = equipment?.mashThicknessLPerKg ?: 3.0,
        )
        val id = db.recipeDao().upsert(recipe)
        db.recipeDao().insertMashSteps(
            listOf(MashStep(recipeId = id, name = "Saccharification", temperatureC = 66.0, timeMin = 60))
        )
        db.recipeDao().insertFermentationSteps(
            listOf(FermentationStep(recipeId = id, name = "Fermentation primaire", temperatureC = 19.0, days = 14))
        )
        return id
    }

    suspend fun saveRecipe(recipe: Recipe) {
        db.recipeDao().upsert(recipe.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteRecipe(recipe: Recipe) = db.recipeDao().delete(recipe)

    suspend fun setFavourite(id: Long, favourite: Boolean) =
        db.recipeDao().setFavourite(id, favourite)

    suspend fun duplicateRecipe(details: RecipeWithDetails): Long =
        db.recipeDao().duplicate(details, "${details.recipe.name} (copie)")

    /**
     * Copie une recette de référence dans les recettes de l'utilisateur, sur son installation
     * par défaut. Les quantités suivent le volume de l'installation ; style, eau et fiches de
     * catalogue sont retrouvés par leur nom quand ils existent.
     */
    suspend fun importReferenceRecipe(ref: ReferenceRecipe): Long {
        val equipment = db.equipmentDao().firstOrDefault()
        val factor = equipment?.batchVolumeL?.takeIf { it > 0 }
            ?.let { it / ref.recipe.batchVolumeL } ?: 1.0
        // Le nom d'abord ; à défaut le code BJCP, s'il ne désigne qu'un style (21B en couvre plusieurs).
        val allStyles = styles.first()
        val style = allStyles.firstOrNull { it.name.equals(ref.styleName, ignoreCase = true) }
            ?: allStyles.filter { it.code == ref.styleCode }.singleOrNull()
        val water = ref.waterName?.let { name ->
            waterProfiles.first().firstOrNull { it.name.equals(name, ignoreCase = true) }
        }
        fun <T> List<T>.idOf(name: String, nameOf: (T) -> String, idOf: (T) -> Long): Long? =
            firstOrNull { nameOf(it).equals(name, ignoreCase = true) }?.let(idOf)
        val catalogMalts = malts.first()
        val catalogHops = hops.first()
        val catalogYeasts = yeasts.first()
        val now = System.currentTimeMillis()

        val id = db.recipeDao().upsert(
            ref.recipe.copy(
                id = 0,
                styleId = style?.id,
                equipmentId = equipment?.id,
                sourceWaterId = water?.id,
                batchVolumeL = ref.recipe.batchVolumeL * factor,
                createdAt = now,
                updatedAt = now,
            )
        )
        db.recipeDao().insertMalts(ref.malts.map {
            it.copy(id = 0, recipeId = id, amountG = it.amountG * factor,
                catalogId = catalogMalts.idOf(it.name, Malt::name, Malt::id))
        })
        db.recipeDao().insertHops(ref.hops.map {
            it.copy(id = 0, recipeId = id, amountG = it.amountG * factor,
                catalogId = catalogHops.idOf(it.name, Hop::name, Hop::id))
        })
        db.recipeDao().insertYeasts(ref.yeasts.map {
            it.copy(id = 0, recipeId = id, amountG = it.amountG * factor,
                catalogId = catalogYeasts.idOf(it.name, Yeast::name, Yeast::id))
        })
        val catalogAdditives = additives.first()
        db.recipeDao().insertAdditives(ref.additives.map {
            // Une pastille ou une gousse ne se fractionne pas : seules les masses et volumes suivent.
            val amount = if (it.unit == AmountUnit.UNIT) it.amount else it.amount * factor
            it.copy(id = 0, recipeId = id, amount = amount,
                catalogId = catalogAdditives.idOf(it.name, Additive::name, Additive::id))
        })
        db.recipeDao().insertMashSteps(ref.mashSteps.map { it.copy(id = 0, recipeId = id) })
        db.recipeDao().insertFermentationSteps(ref.fermentationSteps.map { it.copy(id = 0, recipeId = id) })
        return id
    }

    /**
     * Met à l'échelle une recette vers un autre volume. Les ingrédients suivent
     * proportionnellement ; le rendement, lui, ne change pas avec le volume.
     * [currentVolumeL] est le volume effectif, qui peut venir de l'eau de départ de l'installation.
     */
    suspend fun scaleRecipe(
        details: RecipeWithDetails,
        newVolumeL: Double,
        currentVolumeL: Double = details.recipe.batchVolumeL,
    ) {
        val factor = newVolumeL / currentVolumeL
        if (factor <= 0 || !factor.isFinite()) return
        details.malts.forEach { db.recipeDao().upsertMalt(it.copy(amountG = it.amountG * factor)) }
        details.hops.forEach { db.recipeDao().upsertHop(it.copy(amountG = it.amountG * factor)) }
        details.yeasts.forEach { db.recipeDao().upsertYeast(it.copy(amountG = it.amountG * factor)) }
        details.additives.forEach { db.recipeDao().upsertAdditive(it.copy(amount = it.amount * factor)) }
        saveRecipe(details.recipe.copy(batchVolumeL = newVolumeL))
    }

    // --- Lignes d'ingrédients ------------------------------------------------------

    suspend fun saveRecipeMalt(item: RecipeMalt) = db.recipeDao().upsertMalt(item)
    suspend fun deleteRecipeMalt(item: RecipeMalt) = db.recipeDao().deleteMalt(item)
    suspend fun saveRecipeHop(item: RecipeHop) = db.recipeDao().upsertHop(item)
    suspend fun deleteRecipeHop(item: RecipeHop) = db.recipeDao().deleteHop(item)
    suspend fun saveRecipeYeast(item: RecipeYeast) = db.recipeDao().upsertYeast(item)
    suspend fun deleteRecipeYeast(item: RecipeYeast) = db.recipeDao().deleteYeast(item)
    suspend fun saveRecipeAdditive(item: RecipeAdditive) = db.recipeDao().upsertAdditive(item)
    suspend fun deleteRecipeAdditive(item: RecipeAdditive) = db.recipeDao().deleteAdditive(item)
    suspend fun saveMashStep(item: MashStep) = db.recipeDao().upsertMashStep(item)
    suspend fun deleteMashStep(item: MashStep) = db.recipeDao().deleteMashStep(item)
    suspend fun saveFermentationStep(item: FermentationStep) =
        db.recipeDao().upsertFermentationStep(item)
    suspend fun deleteFermentationStep(item: FermentationStep) =
        db.recipeDao().deleteFermentationStep(item)

    // --- Catalogues ----------------------------------------------------------------

    suspend fun saveMalt(malt: Malt) = db.maltDao().upsert(malt)
    suspend fun deleteMalt(malt: Malt) = db.maltDao().delete(malt)
    suspend fun saveHop(hop: Hop) = db.hopDao().upsert(hop)
    suspend fun deleteHop(hop: Hop) = db.hopDao().delete(hop)
    suspend fun saveYeast(yeast: Yeast) = db.yeastDao().upsert(yeast)
    suspend fun deleteYeast(yeast: Yeast) = db.yeastDao().delete(yeast)
    suspend fun saveAdditive(additive: Additive) = db.additiveDao().upsert(additive)
    suspend fun deleteAdditive(additive: Additive) = db.additiveDao().delete(additive)
    suspend fun saveStyle(style: BeerStyle) = db.beerStyleDao().upsert(style)
    suspend fun deleteStyle(style: BeerStyle) = db.beerStyleDao().delete(style)
    suspend fun saveWaterProfile(profile: WaterProfile) = db.waterProfileDao().upsert(profile)
    suspend fun deleteWaterProfile(profile: WaterProfile) = db.waterProfileDao().delete(profile)

    // --- Installations -------------------------------------------------------------

    suspend fun saveEquipment(equipment: Equipment): Long {
        if (equipment.isDefault) db.equipmentDao().clearDefault()
        return db.equipmentDao().upsert(equipment)
    }

    suspend fun deleteEquipment(equipment: Equipment) = db.equipmentDao().delete(equipment)

    // --- Journal de brassage -------------------------------------------------------

    suspend fun saveSession(session: BrewSession) = db.brewSessionDao().upsert(session)
    suspend fun deleteSession(session: BrewSession) = db.brewSessionDao().delete(session)
}
