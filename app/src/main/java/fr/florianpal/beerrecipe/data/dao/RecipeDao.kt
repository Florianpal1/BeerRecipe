package fr.florianpal.beerrecipe.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import fr.florianpal.beerrecipe.data.entity.BrewSession
import fr.florianpal.beerrecipe.data.entity.FermentationStep
import fr.florianpal.beerrecipe.data.entity.MashStep
import fr.florianpal.beerrecipe.data.entity.Recipe
import fr.florianpal.beerrecipe.data.entity.RecipeAdditive
import fr.florianpal.beerrecipe.data.entity.RecipeHop
import fr.florianpal.beerrecipe.data.entity.RecipeMalt
import fr.florianpal.beerrecipe.data.entity.RecipeWithDetails
import fr.florianpal.beerrecipe.data.entity.RecipeYeast
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    @Transaction
    @Query("SELECT * FROM recipe ORDER BY isFavourite DESC, updatedAt DESC")
    fun observeAll(): Flow<List<RecipeWithDetails>>

    @Transaction
    @Query("SELECT * FROM recipe WHERE id = :id")
    fun observeById(id: Long): Flow<RecipeWithDetails?>

    @Transaction
    @Query("SELECT * FROM recipe WHERE id = :id")
    suspend fun byId(id: Long): RecipeWithDetails?

    @Upsert suspend fun upsert(recipe: Recipe): Long
    @Delete suspend fun delete(recipe: Recipe)

    @Query("UPDATE recipe SET isFavourite = :favourite, updatedAt = :now WHERE id = :id")
    suspend fun setFavourite(id: Long, favourite: Boolean, now: Long = System.currentTimeMillis())

    @Query("UPDATE recipe SET updatedAt = :now WHERE id = :id")
    suspend fun touch(id: Long, now: Long = System.currentTimeMillis())

    // --- Lignes d'ingrédients -----------------------------------------------------

    @Upsert suspend fun upsertMalt(item: RecipeMalt): Long
    @Delete suspend fun deleteMalt(item: RecipeMalt)
    @Insert suspend fun insertMalts(items: List<RecipeMalt>)
    @Query("DELETE FROM recipe_malt WHERE recipeId = :recipeId")
    suspend fun clearMalts(recipeId: Long)

    @Upsert suspend fun upsertHop(item: RecipeHop): Long
    @Delete suspend fun deleteHop(item: RecipeHop)
    @Insert suspend fun insertHops(items: List<RecipeHop>)
    @Query("DELETE FROM recipe_hop WHERE recipeId = :recipeId")
    suspend fun clearHops(recipeId: Long)

    @Upsert suspend fun upsertYeast(item: RecipeYeast): Long
    @Delete suspend fun deleteYeast(item: RecipeYeast)
    @Insert suspend fun insertYeasts(items: List<RecipeYeast>)
    @Query("DELETE FROM recipe_yeast WHERE recipeId = :recipeId")
    suspend fun clearYeasts(recipeId: Long)

    @Upsert suspend fun upsertAdditive(item: RecipeAdditive): Long
    @Delete suspend fun deleteAdditive(item: RecipeAdditive)
    @Insert suspend fun insertAdditives(items: List<RecipeAdditive>)
    @Query("DELETE FROM recipe_additive WHERE recipeId = :recipeId")
    suspend fun clearAdditives(recipeId: Long)

    @Upsert suspend fun upsertMashStep(item: MashStep): Long
    @Delete suspend fun deleteMashStep(item: MashStep)
    @Insert suspend fun insertMashSteps(items: List<MashStep>)

    @Upsert suspend fun upsertFermentationStep(item: FermentationStep): Long
    @Delete suspend fun deleteFermentationStep(item: FermentationStep)
    @Insert suspend fun insertFermentationSteps(items: List<FermentationStep>)

    /**
     * Duplique une recette avec toutes ses lignes. Les identifiants sont remis à zéro
     * pour que Room en attribue de nouveaux.
     */
    @Transaction
    suspend fun duplicate(source: RecipeWithDetails, newName: String): Long {
        val now = System.currentTimeMillis()
        val newId = upsert(
            source.recipe.copy(id = 0, name = newName, createdAt = now, updatedAt = now, isFavourite = false)
        )
        insertMalts(source.malts.map { it.copy(id = 0, recipeId = newId) })
        insertHops(source.hops.map { it.copy(id = 0, recipeId = newId) })
        insertYeasts(source.yeasts.map { it.copy(id = 0, recipeId = newId) })
        insertAdditives(source.additives.map { it.copy(id = 0, recipeId = newId) })
        insertMashSteps(source.mashSteps.map { it.copy(id = 0, recipeId = newId) })
        insertFermentationSteps(source.fermentationSteps.map { it.copy(id = 0, recipeId = newId) })
        return newId
    }
}

@Dao
interface BrewSessionDao {
    @Query("SELECT * FROM brew_session ORDER BY brewDate DESC")
    fun observeAll(): Flow<List<BrewSession>>

    @Query("SELECT * FROM brew_session WHERE recipeId = :recipeId ORDER BY brewDate DESC")
    fun observeForRecipe(recipeId: Long): Flow<List<BrewSession>>

    @Query("SELECT * FROM brew_session WHERE id = :id")
    suspend fun byId(id: Long): BrewSession?

    @Upsert suspend fun upsert(session: BrewSession): Long
    @Delete suspend fun delete(session: BrewSession)
}
