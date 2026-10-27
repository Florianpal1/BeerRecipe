package fr.florianpal.beerrecipe.domain

import fr.florianpal.beerrecipe.data.entity.BeerStyle
import fr.florianpal.beerrecipe.data.entity.Equipment
import fr.florianpal.beerrecipe.data.entity.Recipe
import fr.florianpal.beerrecipe.data.entity.RecipeContext
import fr.florianpal.beerrecipe.data.entity.RecipeHop
import fr.florianpal.beerrecipe.data.entity.RecipeMalt
import fr.florianpal.beerrecipe.data.entity.RecipeWithDetails
import fr.florianpal.beerrecipe.data.entity.RecipeYeast
import fr.florianpal.beerrecipe.domain.advisor.RecipeAdvisor
import fr.florianpal.beerrecipe.domain.calc.BrewCalculator
import fr.florianpal.beerrecipe.domain.model.FermentableType
import fr.florianpal.beerrecipe.domain.model.HopUse
import fr.florianpal.beerrecipe.domain.model.Severity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeAdvisorTest {

    private val ipa = BeerStyle(
        id = 1, code = "21A", name = "American IPA", category = "IPA",
        ogMin = 1.056, ogMax = 1.070, fgMin = 1.008, fgMax = 1.014,
        abvMin = 5.5, abvMax = 7.5, ibuMin = 40.0, ibuMax = 70.0,
        ebcMin = 12.0, ebcMax = 28.0,
    )

    private fun context(
        maltG: Double,
        hopG: Double,
        style: BeerStyle? = ipa,
    ) = RecipeContext(
        details = RecipeWithDetails(
            recipe = Recipe(
                name = "Test", styleId = style?.id,
                batchVolumeL = 20.0, efficiencyPct = 75.0,
            ),
            malts = listOf(
                RecipeMalt(
                    recipeId = 1, name = "Pale", type = FermentableType.BASE,
                    amountG = maltG, colorEbc = 8.0, extractYieldPct = 80.0,
                )
            ),
            hops = listOf(
                RecipeHop(
                    recipeId = 1, name = "Magnum", amountG = hopG,
                    alphaAcidPct = 12.0, use = HopUse.BOIL, timeMin = 60,
                )
            ),
            yeasts = listOf(
                RecipeYeast(recipeId = 1, name = "US-05", amountG = 11.5, attenuationPct = 80.0)
            ),
        ),
        equipment = Equipment(name = "Test", batchVolumeL = 20.0),
        style = style,
        water = null,
    )

    private fun analyse(ctx: RecipeContext) =
        RecipeAdvisor.analyse(ctx, BrewCalculator.compute(ctx))

    @Test
    fun `une densite trop basse declenche un conseil chiffre`() {
        val suggestions = analyse(context(maltG = 3000.0, hopG = 30.0))
        val og = suggestions.firstOrNull { it.parameter == "OG" }
        assertNotNull("Un conseil sur l'OG était attendu", og)
        assertTrue(og!!.title.contains("trop basse"))
        assertTrue("Le conseil doit proposer une quantité", og.action!!.contains("g de malt de base"))
    }

    @Test
    fun `une recette dans les clous ne declenche aucun probleme`() {
        // 5.6 kg et 30 g de Magnum placent OG, IBU et couleur dans les bornes du style.
        val suggestions = analyse(context(maltG = 5600.0, hopG = 30.0))
        val problemes = suggestions.filter {
            it.severity == Severity.PROBLEM && it.parameter in setOf("OG", "IBU", "SRM / EBC", "ABV")
        }
        assertEquals(emptyList<String>(), problemes.map { it.title })
    }

    @Test
    fun `un houblonnage excessif est signale avec la quantite a retirer`() {
        val suggestions = analyse(context(maltG = 5600.0, hopG = 120.0))
        val ibu = suggestions.first { it.parameter == "IBU" }
        assertTrue(ibu.title.contains("trop marquée"))
        assertTrue(ibu.action!!.startsWith("Retirez"))
    }

    @Test
    fun `sans levure l'application previent que l'attenuation est generique`() {
        val base = context(maltG = 5600.0, hopG = 30.0)
        val sansLevure = base.copy(
            details = base.details.copy(yeasts = emptyList())
        )
        val suggestions = analyse(sansLevure)
        assertTrue(suggestions.any { it.parameter == "Levure" })
    }

    @Test
    fun `sans style vise aucune cible n'est imposee`() {
        val suggestions = analyse(context(maltG = 1000.0, hopG = 1.0, style = null))
        assertTrue(suggestions.none { it.parameter in setOf("OG", "FG", "ABV", "IBU", "SRM / EBC") })
    }
}
