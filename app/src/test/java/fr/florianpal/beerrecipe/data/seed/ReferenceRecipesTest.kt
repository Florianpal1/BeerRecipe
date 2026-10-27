package fr.florianpal.beerrecipe.data.seed

import fr.florianpal.beerrecipe.data.entity.Equipment
import fr.florianpal.beerrecipe.data.entity.RecipeContext
import fr.florianpal.beerrecipe.data.entity.RecipeWithDetails
import fr.florianpal.beerrecipe.domain.calc.BrewCalculator
import fr.florianpal.beerrecipe.domain.model.HopUse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReferenceRecipesTest {

    private val refs = ReferenceRecipes.ALL

    @Test
    fun `les recettes de reference sont toutes la et portent des noms distincts`() {
        assertEquals(12, refs.size)
        assertEquals(refs.size, refs.map { it.recipe.name }.toSet().size)
        refs.forEach { assertTrue("${it.recipe.name} sans description", it.description.isNotBlank()) }
    }

    @Test
    fun `style et eau existent dans les catalogues de depart`() {
        val styles = SeedStyles.ALL.map { it.code to it.name }.toSet()
        val waters = SeedWaterProfiles.ALL.map { it.name }.toSet()
        refs.forEach { ref ->
            assertTrue("style inconnu : ${ref.styleName}", (ref.styleCode to ref.styleName) in styles)
            ref.waterName?.let { assertTrue("eau inconnue : $it", it in waters) }
        }
    }

    @Test
    fun `chaque recette est complete et ses houblons a cru ont un jour d ajout`() {
        refs.forEach { ref ->
            val name = ref.recipe.name
            assertTrue("$name sans malt", ref.malts.isNotEmpty())
            assertTrue("$name sans levure", ref.yeasts.isNotEmpty())
            assertTrue("$name sans palier", ref.mashSteps.isNotEmpty())
            assertTrue("$name : quantité nulle", (ref.malts.map { it.amountG } + ref.hops.map { it.amountG }).all { it > 0 })
            ref.hops.filter { it.use == HopUse.DRY_HOP }.forEach {
                assertTrue("$name : ${it.name} à cru sans jour", it.dryHopDay != null)
            }
        }
    }

    @Test
    fun `les densites calculees restent plausibles pour 5 L`() {
        refs.forEach { ref ->
            val ctx = RecipeContext(
                details = RecipeWithDetails(
                    recipe = ref.recipe, malts = ref.malts, hops = ref.hops, yeasts = ref.yeasts,
                    mashSteps = ref.mashSteps, fermentationSteps = ref.fermentationSteps,
                ),
                equipment = Equipment(name = "5 L", batchVolumeL = 5.0),
                style = null,
                water = null,
            )
            val og = BrewCalculator.compute(ctx).og
            assertTrue("${ref.recipe.name} : DI $og", og in 1.025..1.095)
        }
    }
}
