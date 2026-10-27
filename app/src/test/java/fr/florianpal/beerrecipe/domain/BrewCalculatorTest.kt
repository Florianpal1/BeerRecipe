package fr.florianpal.beerrecipe.domain

import fr.florianpal.beerrecipe.data.entity.Equipment
import fr.florianpal.beerrecipe.data.entity.MashStep
import fr.florianpal.beerrecipe.data.entity.Recipe
import fr.florianpal.beerrecipe.data.entity.RecipeContext
import fr.florianpal.beerrecipe.data.entity.RecipeHop
import fr.florianpal.beerrecipe.data.entity.RecipeMalt
import fr.florianpal.beerrecipe.data.entity.RecipeWithDetails
import fr.florianpal.beerrecipe.data.entity.RecipeYeast
import fr.florianpal.beerrecipe.data.entity.WaterProfile
import fr.florianpal.beerrecipe.domain.calc.BrewCalculator
import fr.florianpal.beerrecipe.domain.calc.IbuCalculator
import fr.florianpal.beerrecipe.domain.model.ColorFormula
import fr.florianpal.beerrecipe.domain.model.FermentableType
import fr.florianpal.beerrecipe.domain.model.HopUse
import fr.florianpal.beerrecipe.domain.model.IbuFormula
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Les valeurs attendues sont calculées à la main depuis les formules de référence
 * (PPG, Tinseth, Morey, bilan de charges), et non reprises de l'implémentation.
 */
class BrewCalculatorTest {

    private fun context(
        malts: List<RecipeMalt> = emptyList(),
        hops: List<RecipeHop> = emptyList(),
        yeasts: List<RecipeYeast> = emptyList(),
        mashSteps: List<MashStep> = emptyList(),
        recipe: Recipe = Recipe(name = "Test", batchVolumeL = 20.0, efficiencyPct = 75.0),
        equipment: Equipment? = Equipment(name = "Test", batchVolumeL = 20.0),
        water: WaterProfile? = null,
    ) = RecipeContext(
        details = RecipeWithDetails(
            recipe = recipe,
            malts = malts,
            hops = hops,
            yeasts = yeasts,
            mashSteps = mashSteps,
        ),
        equipment = equipment,
        style = null,
        water = water,
    )

    private fun paleMalt(grams: Double, ebc: Double = 6.0) = RecipeMalt(
        recipeId = 1,
        name = "Pale",
        type = FermentableType.BASE,
        amountG = grams,
        colorEbc = ebc,
        extractYieldPct = 80.0,
    )

    @Test
    fun `densite initiale suit la regle des points par livre et par gallon`() {
        // 5 kg à 80 % de rendement en extrait, 75 % de rendement de brasserie, 20 L.
        val stats = BrewCalculator.compute(context(malts = listOf(paleMalt(5000.0))))
        assertEquals(1.0578, stats.og, 0.0005)
    }

    @Test
    fun `les sucres ne subissent pas le rendement d'empatage`() {
        val sugar = RecipeMalt(
            recipeId = 1, name = "Dextrose", type = FermentableType.SUGAR,
            amountG = 1000.0, extractYieldPct = 100.0,
        )
        val stats = BrewCalculator.compute(context(malts = listOf(sugar)))
        // 2.2046 lb × 46.21 PPG × 100 % / 5.2834 gal = 19.29 points
        assertEquals(1.0193, stats.og, 0.0005)
    }

    @Test
    fun `utilisation Tinseth a 60 minutes et 1,050`() {
        assertEquals(0.23067, IbuCalculator.tinsethUtilization(1.050, 60.0), 0.0005)
    }

    @Test
    fun `amertume d'un houblon en pellets`() {
        val hop = RecipeHop(
            recipeId = 1, name = "Magnum", amountG = 30.0,
            alphaAcidPct = 10.0, use = HopUse.BOIL, timeMin = 60,
        )
        val ctx = IbuCalculator.Context(
            batchVolumeL = 20.0, boilGravity = 1.050, boilTimeMin = 60,
            formula = IbuFormula.TINSETH,
        )
        // 0.23067 × 1.10 (pellets) × 150 mg/L
        assertEquals(38.06, IbuCalculator.contribution(hop, ctx).first, 0.1)
    }

    @Test
    fun `le houblonnage a cru n'apporte aucune amertume`() {
        val hop = RecipeHop(
            recipeId = 1, name = "Citra", amountG = 100.0,
            alphaAcidPct = 12.0, use = HopUse.DRY_HOP, timeMin = 5,
        )
        val ctx = IbuCalculator.Context(20.0, 1.050, 60)
        assertEquals(0.0, IbuCalculator.contribution(hop, ctx).first, 0.0001)
    }

    @Test
    fun `le whirlpool tiede isomerise moins qu'une ebullition`() {
        val chaud = RecipeHop(
            recipeId = 1, name = "Citra", amountG = 50.0, alphaAcidPct = 12.0,
            use = HopUse.WHIRLPOOL, timeMin = 20, temperatureC = 99.0,
        )
        val tiede = chaud.copy(temperatureC = 75.0)
        val ctx = IbuCalculator.Context(20.0, 1.050, 60)
        val ibuChaud = IbuCalculator.contribution(chaud, ctx).first
        val ibuTiede = IbuCalculator.contribution(tiede, ctx).first
        assertTrue(ibuTiede < ibuChaud * 0.3)
    }

    @Test
    fun `couleur par la formule de Morey`() {
        val stats = BrewCalculator.compute(
            context(
                malts = listOf(paleMalt(5000.0, ebc = 6.0)),
                recipe = Recipe(
                    name = "T", batchVolumeL = 20.0, efficiencyPct = 75.0,
                    colorFormula = ColorFormula.MOREY,
                ),
            )
        )
        assertEquals(5.02, stats.srm, 0.05)
        assertEquals(9.89, stats.ebc, 0.1)
    }

    @Test
    fun `titre alcoolique par la formule avancee`() {
        assertEquals(6.51, BrewCalculator.alcoholByVolume(1.060, 1.012), 0.02)
    }

    @Test
    fun `une maque de malt de base dans de l'eau pure garde son pH natif`() {
        val malts = listOf(paleMalt(5000.0, ebc = 4.0))
        val eauPure = WaterProfile(name = "Osmosée")
        val breakdown = BrewCalculator.estimateMashPh(malts, eauPure, 15.0, emptyList())
        // 5.80 − 0.012 × 4 EBC
        assertEquals(5.752, breakdown.estimatedPh, 0.002)
    }

    @Test
    fun `l'alcalinite residuelle fait monter le pH d'empatage`() {
        val malts = listOf(paleMalt(5000.0, ebc = 4.0))
        // 122 ppm de bicarbonate = 100 ppm d'alcalinité CaCO3, sans calcium pour la compenser.
        val eauDure = WaterProfile(name = "Dure", bicarbonate = 122.0)
        val breakdown = BrewCalculator.estimateMashPh(malts, eauDure, 15.0, emptyList())
        assertEquals(100.0, breakdown.residualAlkalinity, 0.5)
        // 30 mEq apportés sur 175 mEq/pH de pouvoir tampon : +0.171
        assertEquals(5.923, breakdown.estimatedPh, 0.005)
    }

    @Test
    fun `les malts torrefies acidifient la maque`() {
        val base = listOf(paleMalt(5000.0, ebc = 4.0))
        val avecTorrefie = base + RecipeMalt(
            recipeId = 1, name = "Black", type = FermentableType.ROASTED,
            amountG = 300.0, colorEbc = 1300.0, extractYieldPct = 70.0,
        )
        val eau = WaterProfile(name = "Osmosée")
        val sans = BrewCalculator.estimateMashPh(base, eau, 15.0, emptyList()).estimatedPh
        val avec = BrewCalculator.estimateMashPh(avecTorrefie, eau, 15.0, emptyList()).estimatedPh
        assertTrue("Le malt torréfié doit faire baisser le pH", avec < sans - 0.1)
    }

    @Test
    fun `temperature d'eau de trempe`() {
        val strike = BrewCalculator.strikeTemperature(
            targetTempC = 66.0, grainTempC = 20.0,
            thicknessLPerKg = 3.0, tunHeatCapacityLEq = 1.5, mashWaterL = 15.0,
        )
        assertEquals(77.0, strike, 0.1)
    }

    @Test
    fun `sucre de refermentation pour 2,4 volumes a 20 degres`() {
        val recipe = Recipe(
            name = "T", batchVolumeL = 20.0, efficiencyPct = 75.0,
            carbonationVolumes = 2.4, bottlingTempC = 20.0,
        )
        val stats = BrewCalculator.compute(
            context(malts = listOf(paleMalt(5000.0)), recipe = recipe)
        )
        // 19 L embouteillés, 0.861 volume de CO2 résiduel, 3.84 g/L par volume de saccharose
        assertEquals(112.3, stats.primingSucroseG, 1.0)
    }

    @Test
    fun `le rendement reel se deduit d'une densite mesuree`() {
        val efficiency = BrewCalculator.measuredEfficiency(
            malts = listOf(paleMalt(5000.0)),
            measuredOg = 1.050,
            volumeL = 20.0,
        )
        assertEquals(64.8, efficiency!!, 0.3)
    }

    @Test
    fun `le lactose ne fermente pas et releve la densite finale`() {
        val levure = RecipeYeast(recipeId = 1, name = "US-05", amountG = 11.5, attenuationPct = 80.0)
        val sansLactose = BrewCalculator.compute(
            context(malts = listOf(paleMalt(5000.0)), yeasts = listOf(levure))
        )
        val avecLactose = BrewCalculator.compute(
            context(
                malts = listOf(
                    paleMalt(5000.0),
                    RecipeMalt(
                        recipeId = 1, name = "Lactose", type = FermentableType.SUGAR,
                        amountG = 500.0, extractYieldPct = 100.0, fermentabilityPct = 0.0,
                    ),
                ),
                yeasts = listOf(levure),
            )
        )
        assertTrue(avecLactose.fg > sansLactose.fg + 0.006)
    }

    @Test
    fun `un empatage chaud attenue moins qu'un empatage froid`() {
        val levure = RecipeYeast(recipeId = 1, name = "US-05", amountG = 11.5, attenuationPct = 80.0)
        fun fgPour(temp: Double) = BrewCalculator.compute(
            context(
                malts = listOf(paleMalt(5000.0)),
                yeasts = listOf(levure),
                mashSteps = listOf(MashStep(recipeId = 1, temperatureC = temp, timeMin = 60)),
            )
        ).fg
        assertTrue(fgPour(70.0) > fgPour(63.0))
    }

    @Test
    fun `les volumes d'eau s'enchainent sans perte inexpliquee`() {
        val equipment = Equipment(
            name = "T", batchVolumeL = 20.0, trubLossL = 1.5,
            coolingShrinkagePct = 4.0, boilOffRateLPerHour = 3.0,
            grainAbsorptionLPerKg = 1.0, mashTunDeadspaceL = 0.5,
        )
        val stats = BrewCalculator.compute(
            context(malts = listOf(paleMalt(5000.0)), equipment = equipment)
        )
        val w = stats.water
        // (20 + 1.5) / 0.96 = 22.396 L chauds, plus 3 L évaporés sur une heure
        assertEquals(22.396, w.postBoilHotVolumeL, 0.01)
        assertEquals(25.396, w.preBoilVolumeL, 0.01)
        assertEquals(15.0, w.mashWaterL, 0.01)
        assertEquals(5.0, w.grainAbsorptionL, 0.01)
        assertEquals(w.totalWaterL, w.mashWaterL + w.spargeWaterL, 0.01)
    }

    @Test
    fun `l'ensemencement suit la densite du mout`() {
        val levure = RecipeYeast(
            recipeId = 1, name = "US-05", amountG = 11.5,
            attenuationPct = 80.0, cellsPerGramBillions = 10.0,
        )
        val stats = BrewCalculator.compute(
            context(malts = listOf(paleMalt(5000.0)), yeasts = listOf(levure))
        )
        // 0.75 M/mL/°P × 20 000 mL × ~14.3 °P ≈ 214 milliards
        assertEquals(214.0, stats.pitching.requiredBillionCells, 6.0)
        assertEquals(115.0, stats.pitching.providedBillionCells, 0.1)
    }
}
