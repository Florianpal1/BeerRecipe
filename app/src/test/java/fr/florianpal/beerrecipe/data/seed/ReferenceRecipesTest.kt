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
        assertEquals(32, refs.size)
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

    @Test
    fun `les classiques tombent dans les plages de leur style pour 20 L`() {
        val equipment = Equipment(name = "20 L", batchVolumeL = 20.0)
        val misses = ReferenceClassics.ALL.mapNotNull { ref ->
            val style = SeedStyles.ALL.first { it.code == ref.styleCode && it.name == ref.styleName }
            val ctx = RecipeContext(
                details = RecipeWithDetails(
                    recipe = ref.recipe, malts = ref.malts, hops = ref.hops, yeasts = ref.yeasts,
                    additives = ref.additives, mashSteps = ref.mashSteps,
                    fermentationSteps = ref.fermentationSteps,
                ),
                equipment = equipment, style = style, water = null,
            )
            val s = BrewCalculator.compute(ctx)
            println("CLASSIC %-20s DI=%.3f (%.3f–%.3f) IBU=%.0f (%.0f–%.0f) EBC=%.0f (%.0f–%.0f)".format(
                ref.recipe.name, s.og, style.ogMin, style.ogMax, s.ibu, style.ibuMin, style.ibuMax,
                s.ebc, style.ebcMin, style.ebcMax))
            val ogOk = s.og in (style.ogMin - 0.002)..(style.ogMax + 0.002)
            val ibuOk = s.ibu in (style.ibuMin - 3)..(style.ibuMax + 3)
            // Les formules de couleur divergent pour les bières très foncées : tolérance plus large.
            val ebcOk = s.ebc in (style.ebcMin * 0.85)..(style.ebcMax * 1.3)
            if (ogOk && ibuOk && ebcOk) null
            else "${ref.recipe.name} : DI ${"%.3f".format(s.og)}, IBU ${"%.0f".format(s.ibu)}, EBC ${"%.0f".format(s.ebc)}"
        }
        assertTrue(misses.joinToString("\n"), misses.isEmpty())
    }
}
