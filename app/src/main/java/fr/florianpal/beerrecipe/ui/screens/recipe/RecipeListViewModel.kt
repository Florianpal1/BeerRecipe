package fr.florianpal.beerrecipe.ui.screens.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.florianpal.beerrecipe.data.entity.RecipeContext
import fr.florianpal.beerrecipe.data.repository.BrewRepository
import fr.florianpal.beerrecipe.data.seed.ReferenceRecipe
import fr.florianpal.beerrecipe.data.seed.ReferenceRecipes
import fr.florianpal.beerrecipe.domain.calc.BrewCalculator
import fr.florianpal.beerrecipe.domain.calc.RecipeStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Une recette telle qu'elle apparaît dans la liste : son contexte et ses indicateurs. */
data class RecipeCardState(
    val context: RecipeContext,
    val stats: RecipeStats,
)

class RecipeListViewModel(private val repository: BrewRepository) : ViewModel() {

    private val query = MutableStateFlow("")
    val searchQuery: StateFlow<String> = query

    val recipes: StateFlow<List<RecipeCardState>> =
        combine(repository.recipeContexts, query) { contexts, q ->
            contexts
                .filter { q.isBlank() || it.details.recipe.name.contains(q, ignoreCase = true) ||
                    it.style?.name?.contains(q, ignoreCase = true) == true }
                .map { RecipeCardState(it, BrewCalculator.compute(it)) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun search(value: String) { query.value = value }

    suspend fun createRecipe(name: String): Long = repository.createRecipe(name)

    val referenceRecipes: List<ReferenceRecipe> = ReferenceRecipes.ALL

    suspend fun importReference(ref: ReferenceRecipe): Long = repository.importReferenceRecipe(ref)

    fun duplicate(card: RecipeCardState) = viewModelScope.launch {
        repository.duplicateRecipe(card.context.details)
    }

    fun delete(card: RecipeCardState) = viewModelScope.launch {
        repository.deleteRecipe(card.context.details.recipe)
    }

    fun toggleFavourite(card: RecipeCardState) = viewModelScope.launch {
        val recipe = card.context.details.recipe
        repository.setFavourite(recipe.id, !recipe.isFavourite)
    }

    companion object {
        fun factory(repository: BrewRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                RecipeListViewModel(repository) as T
        }
    }
}
