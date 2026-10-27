package fr.florianpal.beerrecipe.domain.calc

import fr.florianpal.beerrecipe.data.entity.Equipment
import fr.florianpal.beerrecipe.data.entity.RecipeAdditive
import fr.florianpal.beerrecipe.data.entity.RecipeContext
import fr.florianpal.beerrecipe.data.entity.RecipeMalt
import fr.florianpal.beerrecipe.data.entity.WaterProfile
import fr.florianpal.beerrecipe.domain.model.AdditiveStage
import fr.florianpal.beerrecipe.domain.model.ColorFormula
import fr.florianpal.beerrecipe.domain.model.FermentableType
import fr.florianpal.beerrecipe.domain.model.Units
import kotlin.math.pow

/**
 * Moteur de calcul d'une recette : densités, alcool, amertume, couleur, pH et volumes d'eau.
 *
 * Le moteur est volontairement sans dépendance à Room ni à l'interface : il prend un
 * [RecipeContext] et rend un [RecipeStats]. Il est donc directement testable.
 */
object BrewCalculator {

    private val DEFAULT_EQUIPMENT = Equipment(name = "Installation par défaut")

    fun compute(ctx: RecipeContext): RecipeStats {
        val recipe = ctx.details.recipe
        val eq = ctx.equipment ?: DEFAULT_EQUIPMENT
        val malts = ctx.details.malts
        val hops = ctx.details.hops
        val yeasts = ctx.details.yeasts
        val additives = ctx.details.additives

        val batchL = recipe.batchVolumeL.coerceAtLeast(0.1)
        val efficiency = recipe.efficiencyPct / 100.0

        // --- Volumes d'eau ------------------------------------------------------------
        val mashedGrainKg = malts.filter { it.type.needsMash }.sumOf { it.amountG } / 1000.0
        val totalFermentableKg = malts.sumOf { it.amountG } / 1000.0
        val postBoilHotL = (batchL + eq.trubLossL) / (1.0 - eq.coolingShrinkagePct / 100.0)
        val preBoilL = postBoilHotL + eq.boilOffRateLPerHour * (recipe.boilTimeMin / 60.0)
        val mashWaterL = (mashedGrainKg * recipe.mashThicknessLPerKg).coerceAtLeast(0.0)
        val absorptionL = mashedGrainKg * eq.grainAbsorptionLPerKg
        val totalWaterL = preBoilL + absorptionL + eq.mashTunDeadspaceL
        val spargeWaterL = (totalWaterL - mashWaterL).coerceAtLeast(0.0)
        val packagingVolumeL = (batchL - eq.fermenterLossL).coerceAtLeast(0.1)

        val mashTempC = weightedMashTemp(ctx)
        val strikeTempC = strikeTemperature(
            targetTempC = mashTempC,
            grainTempC = recipe.grainTempC,
            thicknessLPerKg = recipe.mashThicknessLPerKg,
            tunHeatCapacityLEq = eq.mashTunHeatCapacityLEq,
            mashWaterL = mashWaterL,
        )

        // --- Densités -----------------------------------------------------------------
        val gravityPoints = malts.sumOf { gravityPointsOf(it, efficiency) }
        val og = Units.pointsToSg(gravityPoints / Units.litersToGallons(batchL))
        val preBoilGravity = Units.pointsToSg(gravityPoints / Units.litersToGallons(preBoilL))
        val boilGravity = (preBoilGravity + og) / 2.0

        val sugarPoints = malts.filter { !it.type.needsMash }.sumOf { gravityPointsOf(it, efficiency) }
        val sugarFraction = if (gravityPoints > 0) sugarPoints / gravityPoints else 0.0
        val caramelFraction = weightFraction(malts, FermentableType.CARAMEL)
        val roastedFraction = weightFraction(malts, FermentableType.ROASTED)

        val effectiveAttenuation = effectiveAttenuation(ctx, mashTempC, sugarFraction, caramelFraction)
        // Seule la part fermentescible de l'extrait s'atténue ; le reste — lactose, dextrines —
        // se retrouve intégralement dans la densité finale.
        val fermentablePoints = malts.sumOf {
            gravityPointsOf(it, efficiency) * it.fermentabilityPct / 100.0
        }
        val fermentableShare = if (gravityPoints > 0) fermentablePoints / gravityPoints else 1.0
        val fg = Units.pointsToSg(
            Units.sgToPoints(og) * (1.0 - fermentableShare * effectiveAttenuation / 100.0)
        )
        val abv = alcoholByVolume(og, fg)
        val apparentAttenuation =
            if (og > 1.0) (og - fg) / (og - 1.0) * 100.0 else 0.0
        val realAttenuation = apparentAttenuation * 0.8192

        // --- Amertume -----------------------------------------------------------------
        val ibuCtx = IbuCalculator.Context(
            batchVolumeL = batchL,
            boilGravity = boilGravity,
            boilTimeMin = recipe.boilTimeMin,
            equipmentUtilizationPct = eq.hopUtilizationPct,
            formula = recipe.ibuFormula,
        )
        val hopContributions = hops.map { hop ->
            val (ibu, util) = IbuCalculator.contribution(hop, ibuCtx)
            HopContribution(hop.id, hop.name, ibu, util)
        }
        val ibu = hopContributions.sumOf { it.ibu }

        // --- Couleur ------------------------------------------------------------------
        val mcu = malts.sumOf {
            Units.gToLb(it.amountG) * Units.ebcToLovibond(it.colorEbc)
        } / Units.litersToGallons(batchL)
        val srm = colorFromMcu(mcu, recipe.colorFormula)
        val ebc = Units.srmToEbc(srm)

        // --- Eau et pH ----------------------------------------------------------------
        val sourceWater = ctx.water ?: WaterProfile(name = "Eau non renseignée")
        val mashWater = applySalts(sourceWater, additives, mashWaterL) { it.stage == AdditiveStage.MASH }
        val finalWater = applySalts(sourceWater, additives, totalWaterL) {
            it.stage == AdditiveStage.MASH || it.stage == AdditiveStage.SPARGE || it.stage == AdditiveStage.BOIL
        }
        val mashPh = estimateMashPh(malts, mashWater, mashWaterL, additives)

        // --- Levure -------------------------------------------------------------------
        val pitching = pitchingPlan(ctx, og, batchL)

        // --- Refermentation en bouteille ----------------------------------------------
        val residualCo2 = residualCo2Volumes(recipe.bottlingTempC)
        val co2Delta = (recipe.carbonationVolumes - residualCo2).coerceAtLeast(0.0)
        val primingSucrose = co2Delta * 3.84 * packagingVolumeL
        val primingDextrose = co2Delta * 4.26 * packagingVolumeL

        // --- Coût ---------------------------------------------------------------------
        val maltCost = malts.mapNotNull { m -> m.pricePerKg?.let { it * m.amountG / 1000.0 } }
        val hopCost = hops.mapNotNull { h -> h.pricePerKg?.let { it * h.amountG / 1000.0 } }
        val yeastCost = yeasts.mapNotNull { it.price }
        val additiveCost = additives.mapNotNull { it.price }
        val totalCost = (maltCost + hopCost + yeastCost + additiveCost).sum()
        val priced = maltCost.size + hopCost.size + yeastCost.size + additiveCost.size
        val lines = malts.size + hops.size + yeasts.size + additives.size

        val totalWeightG = malts.sumOf { it.amountG }
        val maltContributions = malts.map { m ->
            MaltContribution(
                maltId = m.id,
                name = m.name,
                amountG = m.amountG,
                weightPct = if (totalWeightG > 0) m.amountG / totalWeightG * 100.0 else 0.0,
                gravityPoints = gravityPointsOf(m, efficiency),
                colorPoints = Units.gToLb(m.amountG) * Units.ebcToLovibond(m.colorEbc),
            )
        }

        val ogPlato = Units.sgToPlato(og)
        val fgPlato = Units.sgToPlato(fg)
        val realExtract = 0.1808 * ogPlato + 0.8192 * fgPlato
        val abw = if (ogPlato > 0) (ogPlato - realExtract) / (2.0665 - 0.010665 * ogPlato) else 0.0
        val caloriesPer12oz = ((6.9 * abw) + 4.0 * (realExtract - 0.1)) * fg * 3.55

        return RecipeStats(
            og = og,
            fg = fg,
            abv = abv,
            ibu = ibu,
            srm = srm,
            ebc = ebc,
            colorHex = ColorScale.srmToArgb(srm),
            preBoilGravity = preBoilGravity,
            boilGravity = boilGravity,
            totalGravityPoints = gravityPoints,
            apparentAttenuationPct = apparentAttenuation,
            realAttenuationPct = realAttenuation,
            effectiveAttenuationPct = effectiveAttenuation,
            mashTempC = mashTempC,
            buGuRatio = if (og > 1.0) ibu / Units.sgToPoints(og) else 0.0,
            caloriesPer330ml = caloriesPer12oz * (330.0 / 355.0),
            caloriesPer100ml = caloriesPer12oz / 3.55,
            totalGrainKg = mashedGrainKg,
            totalFermentableKg = totalFermentableKg,
            totalHopsG = hops.sumOf { it.amountG },
            sugarFractionPct = sugarFraction * 100.0,
            caramelFractionPct = caramelFraction * 100.0,
            roastedFractionPct = roastedFraction * 100.0,
            weightedDiastaticPower = weightedDiastaticPower(malts),
            water = WaterPlan(
                mashWaterL = mashWaterL,
                spargeWaterL = spargeWaterL,
                totalWaterL = totalWaterL,
                grainAbsorptionL = absorptionL,
                preBoilVolumeL = preBoilL,
                postBoilHotVolumeL = postBoilHotL,
                batchVolumeL = batchL,
                packagingVolumeL = packagingVolumeL,
                strikeTempC = strikeTempC,
            ),
            mashPh = mashPh,
            finalWaterProfile = finalWater,
            pitching = pitching,
            primingSucroseG = primingSucrose,
            primingDextroseG = primingDextrose,
            totalCost = totalCost,
            costPerLiter = if (batchL > 0) totalCost / batchL else 0.0,
            hasPartialCost = priced in 1 until lines,
            hopContributions = hopContributions,
            maltContributions = maltContributions,
        )
    }

    // ---------------------------------------------------------------------------------
    // Densité
    // ---------------------------------------------------------------------------------

    /**
     * Points de densité apportés par un fermentescible. Les sucres et extraits ne passent
     * pas par l'empâtage : ils rendent la totalité de leur extrait, sans perte de rendement.
     */
    fun gravityPointsOf(malt: RecipeMalt, efficiency: Double): Double {
        val ppg = Units.SUCROSE_PPG * malt.extractYieldPct / 100.0
        val yieldFactor = if (malt.type.needsMash) efficiency else 1.0
        return Units.gToLb(malt.amountG) * ppg * yieldFactor
    }

    /** Points de densité que la maque rendrait avec un rendement parfait. */
    fun maxGravityPoints(malts: List<RecipeMalt>): Double =
        malts.sumOf { gravityPointsOf(it, 1.0) }

    /**
     * Rendement réellement obtenu, déduit d'une densité et d'un volume mesurés.
     * C'est la mesure qui permet de recaler l'installation pour les recettes suivantes.
     */
    fun measuredEfficiency(malts: List<RecipeMalt>, measuredOg: Double, volumeL: Double): Double? {
        val max = maxGravityPoints(malts)
        if (max <= 0.0 || volumeL <= 0.0) return null
        val obtained = Units.sgToPoints(measuredOg) * Units.litersToGallons(volumeL)
        return obtained / max * 100.0
    }

    /** Formule ABV dite « avancée », plus juste que (OG − FG) × 131 sur les bières fortes. */
    fun alcoholByVolume(og: Double, fg: Double): Double {
        if (og <= fg) return 0.0
        return (76.08 * (og - fg) / (1.775 - og)) * (fg / 0.794)
    }

    /**
     * Atténuation réellement attendue : celle de la levure, corrigée par la température
     * d'empâtage (un moût empâté chaud est moins fermentescible), par la part de sucres
     * simples (entièrement fermentescibles) et par la part de malts caramel (qui ne le sont pas).
     */
    fun effectiveAttenuation(
        ctx: RecipeContext,
        mashTempC: Double,
        sugarFraction: Double,
        caramelFraction: Double,
    ): Double {
        val yeasts = ctx.details.yeasts
        val base = if (yeasts.isEmpty()) {
            75.0
        } else {
            val totalG = yeasts.sumOf { it.amountG }
            if (totalG > 0) {
                yeasts.sumOf { it.attenuationPct * it.amountG } / totalG
            } else {
                yeasts.map { it.attenuationPct }.average()
            }
        }
        val tempAdjusted = base + (66.0 - mashTempC) * 1.2
        val withSugar = tempAdjusted + sugarFraction * (100.0 - tempAdjusted) * 0.6
        val withCaramel = withSugar - caramelFraction * 100.0 * 0.12
        return withCaramel.coerceIn(30.0, 100.0)
    }

    /** Température d'empâtage retenue : moyenne des paliers de saccharification, pondérée par leur durée. */
    fun weightedMashTemp(ctx: RecipeContext): Double {
        val steps = ctx.details.mashSteps.filter { it.temperatureC in 60.0..76.0 && it.timeMin > 0 }
        if (steps.isEmpty()) return 66.0
        val totalTime = steps.sumOf { it.timeMin }
        return steps.sumOf { it.temperatureC * it.timeMin } / totalTime
    }

    /**
     * Température de l'eau d'empâtage. Le grain absorbe de la chaleur (coefficient 0.41 face
     * à l'eau) et la cuve aussi, d'où la correction par sa capacité thermique équivalente.
     */
    fun strikeTemperature(
        targetTempC: Double,
        grainTempC: Double,
        thicknessLPerKg: Double,
        tunHeatCapacityLEq: Double,
        mashWaterL: Double,
    ): Double {
        if (thicknessLPerKg <= 0.0) return targetTempC
        val grainCorrection = (0.4172 / thicknessLPerKg) * (targetTempC - grainTempC)
        val tunCorrection =
            if (mashWaterL > 0) tunHeatCapacityLEq * (targetTempC - grainTempC) / mashWaterL else 0.0
        return targetTempC + grainCorrection + tunCorrection
    }

    /**
     * Pouvoir diastasique moyen de la maque, en °Lintner. En dessous d'environ 30,
     * les enzymes des malts de base ne suffisent plus à convertir l'amidon des crus.
     */
    fun weightedDiastaticPower(malts: List<RecipeMalt>): Double {
        val mashed = malts.filter { it.type.needsMash }
        val totalKg = mashed.sumOf { it.amountG } / 1000.0
        if (totalKg <= 0.0) return 0.0
        return mashed.sumOf { it.diastaticPowerLintner * it.amountG / 1000.0 } / totalKg
    }

    private fun weightFraction(malts: List<RecipeMalt>, type: FermentableType): Double {
        val total = malts.sumOf { it.amountG }
        if (total <= 0.0) return 0.0
        return malts.filter { it.type == type }.sumOf { it.amountG } / total
    }

    // ---------------------------------------------------------------------------------
    // Couleur
    // ---------------------------------------------------------------------------------

    fun colorFromMcu(mcu: Double, formula: ColorFormula): Double {
        if (mcu <= 0.0) return 0.0
        return when (formula) {
            ColorFormula.MOREY -> 1.4922 * mcu.pow(0.6859)
            ColorFormula.DANIELS -> 0.2 * mcu + 8.4
            ColorFormula.MOSHER -> 0.3 * mcu + 4.7
        }
    }

    // ---------------------------------------------------------------------------------
    // Eau et pH
    // ---------------------------------------------------------------------------------

    /** Applique les sels de brassage à un profil d'eau, pour un volume d'eau donné. */
    fun applySalts(
        source: WaterProfile,
        additives: List<RecipeAdditive>,
        waterVolumeL: Double,
        filter: (RecipeAdditive) -> Boolean,
    ): WaterProfile {
        if (waterVolumeL <= 0.0) return source
        var ca = source.calcium
        var mg = source.magnesium
        var na = source.sodium
        var so4 = source.sulfate
        var cl = source.chloride
        var hco3 = source.bicarbonate
        additives.filter(filter).forEach { a ->
            val grams = a.unit.toBase(a.amount)
            val perLiter = grams / waterVolumeL
            ca += a.calciumPpmPerGPerL * perLiter
            mg += a.magnesiumPpmPerGPerL * perLiter
            na += a.sodiumPpmPerGPerL * perLiter
            so4 += a.sulfatePpmPerGPerL * perLiter
            cl += a.chloridePpmPerGPerL * perLiter
            hco3 += a.bicarbonatePpmPerGPerL * perLiter
        }
        return source.copy(
            calcium = ca, magnesium = mg, sodium = na,
            sulfate = so4, chloride = cl, bicarbonate = hco3,
        )
    }

    /**
     * pH d'empâtage, par bilan de charges.
     *
     * Chaque malt se comporte comme un tampon : il faut lui apporter
     * `masse × pouvoir_tampon × (pH_eau_distillée − pH_final)` milliéquivalents d'acide
     * pour l'amener au pH visé. L'alcalinité résiduelle de l'eau joue en sens inverse,
     * et les acides ajoutés s'additionnent. En résolvant pour le pH final :
     *
     *     pH = (Σ m·b·pH_DI + alcalinité_mEq − acide_mEq) / Σ m·b
     */
    fun estimateMashPh(
        malts: List<RecipeMalt>,
        mashWater: WaterProfile,
        mashWaterL: Double,
        additives: List<RecipeAdditive>,
    ): MashPhBreakdown {
        var weightedPh = 0.0
        var buffer = 0.0
        malts.filter { it.type.needsMash || it.type == FermentableType.ACIDULATED }.forEach { m ->
            val kg = m.amountG / 1000.0
            val b = MaltChemistry.bufferCapacity(m.type, m.bufferCapacity)
            val ph = MaltChemistry.distilledWaterPh(m.type, m.colorEbc, m.distilledWaterPh)
            weightedPh += kg * b * ph
            buffer += kg * b
        }

        val residualAlkalinity = mashWater.residualAlkalinity
        // 1 mEq d'alcalinité vaut 50 mg de CaCO3 : d'où la division par 50.
        val alkalinityMeq = residualAlkalinity / 50.0 * mashWaterL
        val acidMeq = additives
            .filter { it.stage == AdditiveStage.MASH }
            .sumOf { (it.acidMeqPerUnit ?: 0.0) * it.amount }

        val ph = if (buffer <= 0.0) {
            mashWater.ph
        } else {
            ((weightedPh + alkalinityMeq - acidMeq) / buffer).coerceIn(3.5, 7.5)
        }
        val diPh = if (buffer <= 0.0) mashWater.ph else weightedPh / buffer

        return MashPhBreakdown(
            estimatedPh = ph,
            distilledWaterPh = diPh,
            residualAlkalinity = residualAlkalinity,
            alkalinityMeq = alkalinityMeq,
            acidMeq = acidMeq,
            bufferCapacity = buffer,
            mashWaterProfile = mashWater,
        )
    }

    // ---------------------------------------------------------------------------------
    // Levure et refermentation
    // ---------------------------------------------------------------------------------

    fun pitchingPlan(ctx: RecipeContext, og: Double, batchVolumeL: Double): PitchingPlan {
        val yeasts = ctx.details.yeasts
        val plato = Units.sgToPlato(og).coerceAtLeast(0.0)
        val rate = yeasts.maxOfOrNull { it.type.pitchRateMillionsPerMlPerPlato } ?: 0.75
        // millions de cellules/mL/°P × mL × °P = millions ; /1000 pour obtenir des milliards.
        val required = rate * (batchVolumeL * 1000.0) * plato / 1000.0
        val provided = yeasts.sumOf { it.amountG * it.cellsPerGramBillions }
        val cellsPerGram = yeasts.firstOrNull()?.cellsPerGramBillions ?: 10.0
        return PitchingPlan(
            requiredBillionCells = required,
            providedBillionCells = provided,
            recommendedDryYeastG = if (cellsPerGram > 0) required / cellsPerGram else 0.0,
            actualYeastG = yeasts.sumOf { it.amountG },
        )
    }

    /** CO2 déjà dissous dans la bière à la température d'embouteillage, en volumes. */
    fun residualCo2Volumes(bottlingTempC: Double): Double {
        val f = Units.celsiusToFahrenheit(bottlingTempC)
        return 3.0378 - 0.050062 * f + 0.00026555 * f * f
    }
}
