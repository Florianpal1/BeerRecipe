package fr.florianpal.beerrecipe.ui.screens.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.florianpal.beerrecipe.data.entity.Additive
import fr.florianpal.beerrecipe.data.entity.BrewSession
import fr.florianpal.beerrecipe.data.entity.FermentationStep
import fr.florianpal.beerrecipe.data.entity.Hop
import fr.florianpal.beerrecipe.data.entity.Malt
import fr.florianpal.beerrecipe.data.entity.MashStep
import fr.florianpal.beerrecipe.data.entity.Recipe
import fr.florianpal.beerrecipe.data.entity.RecipeAdditive
import fr.florianpal.beerrecipe.data.entity.RecipeContext
import fr.florianpal.beerrecipe.data.entity.RecipeHop
import fr.florianpal.beerrecipe.data.entity.RecipeMalt
import fr.florianpal.beerrecipe.data.entity.RecipeYeast
import fr.florianpal.beerrecipe.data.entity.Yeast
import fr.florianpal.beerrecipe.data.entity.toRecipeLine
import fr.florianpal.beerrecipe.data.repository.BrewRepository
import fr.florianpal.beerrecipe.domain.advisor.RecipeAdvisor
import fr.florianpal.beerrecipe.domain.advisor.Suggestion
import fr.florianpal.beerrecipe.domain.calc.BrewCalculator
import fr.florianpal.beerrecipe.domain.calc.RecipeStats
import fr.florianpal.beerrecipe.ui.common.CatalogData
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** La recette, ses indicateurs calculés et les conseils qui en découlent. */
data class RecipeDetailUiState(
    val context: RecipeContext,
    val stats: RecipeStats,
    val suggestions: List<Suggestion>,
)

class RecipeDetailViewModel(
    private val repository: BrewRepository,
    private val recipeId: Long,
) : ViewModel() {

    val state: StateFlow<RecipeDetailUiState?> = repository.observeRecipe(recipeId)
        .map { ctx ->
            ctx?.let {
                val stats = BrewCalculator.compute(it)
                RecipeDetailUiState(it, stats, RecipeAdvisor.analyse(it, stats))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val catalog: StateFlow<CatalogData> = combine(
        repository.malts,
        repository.hops,
        repository.yeasts,
        repository.additives,
        repository.styles,
    ) { malts, hops, yeasts, additives, styles ->
        CatalogData(malts = malts, hops = hops, yeasts = yeasts, additives = additives, styles = styles)
    }.combine(repository.equipments) { data, equipments ->
        data.copy(equipments = equipments)
    }.combine(repository.waterProfiles) { data, waters ->
        data.copy(waterProfiles = waters)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CatalogData())

    val sessions: StateFlow<List<BrewSession>> = repository.observeSessions(recipeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private fun currentRecipe(): Recipe? = state.value?.context?.details?.recipe

    /** Applique une modification au corps de la recette et l'enregistre aussitôt. */
    fun editRecipe(block: (Recipe) -> Recipe) {
        val recipe = currentRecipe() ?: return
        viewModelScope.launch { repository.saveRecipe(block(recipe)) }
    }

    // --- Malts ---------------------------------------------------------------------

    fun addMalt(malt: Malt, amountG: Double) = viewModelScope.launch {
        val position = state.value?.context?.details?.malts?.size ?: 0
        repository.saveRecipeMalt(malt.toRecipeLine(recipeId, amountG = amountG, position = position))
    }

    fun addBlankMalt() = viewModelScope.launch {
        val position = state.value?.context?.details?.malts?.size ?: 0
        repository.saveRecipeMalt(
            RecipeMalt(recipeId = recipeId, name = "Nouveau malt", amountG = 500.0, position = position)
        )
    }

    fun saveMalt(item: RecipeMalt) = viewModelScope.launch { repository.saveRecipeMalt(item) }
    fun deleteMalt(item: RecipeMalt) = viewModelScope.launch { repository.deleteRecipeMalt(item) }

    // --- Houblons ------------------------------------------------------------------

    fun addHop(hop: Hop, amountG: Double) = viewModelScope.launch {
        val position = state.value?.context?.details?.hops?.size ?: 0
        repository.saveRecipeHop(hop.toRecipeLine(recipeId, amountG = amountG, position = position))
    }

    fun addBlankHop() = viewModelScope.launch {
        val position = state.value?.context?.details?.hops?.size ?: 0
        repository.saveRecipeHop(
            RecipeHop(recipeId = recipeId, name = "Nouveau houblon", amountG = 20.0, position = position)
        )
    }

    fun saveHop(item: RecipeHop) = viewModelScope.launch { repository.saveRecipeHop(item) }
    fun deleteHop(item: RecipeHop) = viewModelScope.launch { repository.deleteRecipeHop(item) }

    // --- Levures -------------------------------------------------------------------

    fun addYeast(yeast: Yeast, amountG: Double) = viewModelScope.launch {
        val position = state.value?.context?.details?.yeasts?.size ?: 0
        repository.saveRecipeYeast(yeast.toRecipeLine(recipeId, amountG = amountG, position = position))
    }

    fun saveYeast(item: RecipeYeast) = viewModelScope.launch { repository.saveRecipeYeast(item) }
    fun deleteYeast(item: RecipeYeast) = viewModelScope.launch { repository.deleteRecipeYeast(item) }

    // --- Additifs ------------------------------------------------------------------

    fun addAdditive(additive: Additive, amount: Double) = viewModelScope.launch {
        val position = state.value?.context?.details?.additives?.size ?: 0
        repository.saveRecipeAdditive(additive.toRecipeLine(recipeId, amount = amount, position = position))
    }

    fun addBlankAdditive() = viewModelScope.launch {
        val position = state.value?.context?.details?.additives?.size ?: 0
        repository.saveRecipeAdditive(
            RecipeAdditive(recipeId = recipeId, name = "Nouvel ajout", position = position)
        )
    }

    fun saveAdditive(item: RecipeAdditive) = viewModelScope.launch { repository.saveRecipeAdditive(item) }
    fun deleteAdditive(item: RecipeAdditive) = viewModelScope.launch { repository.deleteRecipeAdditive(item) }

    // --- Paliers -------------------------------------------------------------------

    fun addMashStep() = viewModelScope.launch {
        val steps = state.value?.context?.details?.mashSteps.orEmpty()
        repository.saveMashStep(
            MashStep(
                recipeId = recipeId,
                name = "Nouveau palier",
                temperatureC = steps.lastOrNull()?.temperatureC?.plus(6.0) ?: 66.0,
                timeMin = 20,
                position = steps.size,
            )
        )
    }

    fun saveMashStep(item: MashStep) = viewModelScope.launch { repository.saveMashStep(item) }
    fun deleteMashStep(item: MashStep) = viewModelScope.launch { repository.deleteMashStep(item) }

    fun addFermentationStep() = viewModelScope.launch {
        val steps = state.value?.context?.details?.fermentationSteps.orEmpty()
        repository.saveFermentationStep(
            FermentationStep(
                recipeId = recipeId,
                name = if (steps.isEmpty()) "Fermentation primaire" else "Garde",
                temperatureC = if (steps.isEmpty()) 19.0 else 4.0,
                days = if (steps.isEmpty()) 14 else 21,
                position = steps.size,
            )
        )
    }

    fun saveFermentationStep(item: FermentationStep) =
        viewModelScope.launch { repository.saveFermentationStep(item) }

    fun deleteFermentationStep(item: FermentationStep) =
        viewModelScope.launch { repository.deleteFermentationStep(item) }

    // --- Journal de brassage -------------------------------------------------------

    fun addSession() = viewModelScope.launch {
        val stats = state.value?.stats
        repository.saveSession(
            BrewSession(
                recipeId = recipeId,
                measuredOg = stats?.og,
                measuredFg = stats?.fg,
                measuredVolumeL = stats?.water?.batchVolumeL,
            )
        )
    }

    fun saveSession(session: BrewSession) = viewModelScope.launch { repository.saveSession(session) }
    fun deleteSession(session: BrewSession) = viewModelScope.launch { repository.deleteSession(session) }

    // --- Actions sur la recette ----------------------------------------------------

    fun scaleTo(volumeL: Double) {
        val context = state.value?.context ?: return
        viewModelScope.launch { repository.scaleRecipe(context.details, volumeL, context.batchVolumeL) }
    }

    fun toggleFavourite() {
        val recipe = currentRecipe() ?: return
        viewModelScope.launch { repository.setFavourite(recipe.id, !recipe.isFavourite) }
    }

    /**
     * Reprend les réglages de l'installation choisie. Un changement d'installation
     * n'a de sens que s'il entraîne volumes, durée et rendement avec lui.
     */
    fun applyEquipment(equipmentId: Long) {
        val equipment = catalog.value.equipments.firstOrNull { it.id == equipmentId } ?: return
        editRecipe {
            it.copy(
                equipmentId = equipment.id,
                batchVolumeL = equipment.batchVolumeL,
                boilTimeMin = equipment.boilTimeMin,
                efficiencyPct = equipment.brewhouseEfficiencyPct,
                mashThicknessLPerKg = equipment.mashThicknessLPerKg,
            )
        }
    }

    companion object {
        fun factory(repository: BrewRepository, recipeId: Long) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                RecipeDetailViewModel(repository, recipeId) as T
        }
    }
}
