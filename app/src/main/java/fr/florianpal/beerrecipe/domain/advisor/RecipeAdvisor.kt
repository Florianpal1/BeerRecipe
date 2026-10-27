package fr.florianpal.beerrecipe.domain.advisor

import fr.florianpal.beerrecipe.data.entity.Equipment
import fr.florianpal.beerrecipe.data.entity.RecipeContext
import fr.florianpal.beerrecipe.domain.calc.BrewCalculator
import fr.florianpal.beerrecipe.domain.calc.IbuCalculator
import fr.florianpal.beerrecipe.domain.calc.RecipeStats
import fr.florianpal.beerrecipe.domain.model.FermentableType
import fr.florianpal.beerrecipe.domain.model.Format
import fr.florianpal.beerrecipe.domain.model.HopUse
import fr.florianpal.beerrecipe.domain.model.Severity
import fr.florianpal.beerrecipe.domain.model.TargetRange
import fr.florianpal.beerrecipe.domain.model.Units
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.pow

/**
 * Transforme les écarts entre la recette calculée et le style visé en gestes concrets :
 * combien de grammes de quel malt, combien de millilitres d'acide, quelle température.
 *
 * Toutes les quantités proposées sont calculées en inversant les formules du moteur,
 * et non estimées à la louche.
 */
object RecipeAdvisor {

    private const val ACID_LACTIC_80_MEQ_PER_ML = 10.7
    private const val BAKING_SODA_MEQ_PER_G = 11.9

    fun analyse(ctx: RecipeContext, stats: RecipeStats): List<Suggestion> {
        val out = mutableListOf<Suggestion>()
        out += structuralChecks(ctx)
        out += gravityChecks(ctx, stats)
        out += bitternessChecks(ctx, stats)
        out += colourChecks(ctx, stats)
        out += mashPhChecks(ctx, stats)
        out += fermentationChecks(ctx, stats)
        out += grainBillChecks(ctx, stats)
        out += equipmentChecks(ctx, stats)
        out += waterChecks(ctx, stats)
        return out.sortedByDescending { it.severity.ordinal }
    }

    // ---------------------------------------------------------------------------------

    private fun structuralChecks(ctx: RecipeContext): List<Suggestion> {
        val d = ctx.details
        val out = mutableListOf<Suggestion>()
        if (d.malts.isEmpty()) {
            out += Suggestion(
                "Maque", Severity.PROBLEM, "Aucun fermentescible",
                "Sans malt, aucune densité ne peut être estimée.",
                "Ajoutez au moins un malt de base.",
            )
        }
        if (d.yeasts.isEmpty()) {
            out += Suggestion(
                "Levure", Severity.WARNING, "Aucune levure",
                "La densité finale est estimée avec une atténuation générique de 75 %.",
                "Choisissez une levure pour affiner la FG et l'ABV.",
            )
        }
        if (d.hops.isEmpty() && (ctx.style?.ibuMin ?: 0.0) > 5.0) {
            out += Suggestion(
                "Houblon", Severity.WARNING, "Aucun houblon",
                "Le style visé demande au moins ${Format.ibu(ctx.style!!.ibuMin)} IBU.",
                "Ajoutez un houblon amérisant en début d'ébullition.",
            )
        }
        if (ctx.equipment == null) {
            out += Suggestion(
                "Installation", Severity.WARNING, "Aucun environnement de brassage",
                "Les volumes d'eau et l'évaporation utilisent des valeurs par défaut.",
                "Associez une installation enregistrée à la recette.",
            )
        }
        if (ctx.water == null) {
            out += Suggestion(
                "Eau", Severity.INFO, "Profil d'eau non renseigné",
                "Le pH d'empâtage est calculé comme si l'eau était déminéralisée.",
                "Renseignez votre eau pour fiabiliser l'estimation de pH.",
            )
        }
        if (d.mashSteps.isEmpty() && d.malts.any { it.type.needsMash }) {
            out += Suggestion(
                "Empâtage", Severity.INFO, "Aucun palier d'empâtage",
                "Le calcul suppose une saccharification unique à 66 °C.",
                "Ajoutez vos paliers pour un calcul d'eau de trempe exact.",
            )
        }
        return out
    }

    // ---------------------------------------------------------------------------------

    private fun gravityChecks(ctx: RecipeContext, stats: RecipeStats): List<Suggestion> {
        val style = ctx.style ?: return emptyList()
        val out = mutableListOf<Suggestion>()

        checkRange(stats.og, style.ogRange)?.let { dev ->
            val targetOg = if (dev < 0) style.ogMin else style.ogMax
            val deltaPoints = Units.sgToPoints(targetOg) - Units.sgToPoints(stats.og)
            val grams = baseMaltGramsForPoints(ctx, deltaPoints)
            out += Suggestion(
                "OG", if (abs(dev) > 0.006) Severity.PROBLEM else Severity.WARNING,
                if (dev < 0) "Densité initiale trop basse" else "Densité initiale trop haute",
                "OG estimée ${Format.gravity(stats.og)} pour une cible " +
                    "${Format.gravity(style.ogMin)} – ${Format.gravity(style.ogMax)}.",
                if (dev < 0) {
                    "Ajoutez environ ${Format.grams(grams)} de malt de base, " +
                        "ou gagnez ${pointsAsEfficiency(ctx, stats, deltaPoints)} de rendement."
                } else {
                    "Retirez environ ${Format.grams(abs(grams))} de malt de base, " +
                        "ou portez le volume à ${Format.liters(volumeForGravity(stats, targetOg))}."
                },
            )
        }

        checkRange(stats.fg, style.fgRange)?.let { dev ->
            out += Suggestion(
                "FG", Severity.WARNING,
                if (dev < 0) "Densité finale trop basse" else "Densité finale trop haute",
                "FG estimée ${Format.gravity(stats.fg)} pour une cible " +
                    "${Format.gravity(style.fgMin)} – ${Format.gravity(style.fgMax)} " +
                    "(atténuation retenue : ${Format.percent(stats.effectiveAttenuationPct)}).",
                if (dev > 0) {
                    "Empâtez plus bas — ${Format.temp(stats.mashTempC - 2)} au lieu de " +
                        "${Format.temp(stats.mashTempC)} — ou choisissez une levure plus atténuante."
                } else {
                    "Empâtez plus haut — ${Format.temp(stats.mashTempC + 2)} — ou ajoutez " +
                        "des malts caramel pour laisser du corps."
                },
            )
        }

        checkRange(stats.abv, style.abvRange)?.let { dev ->
            out += Suggestion(
                "ABV", if (abs(dev) > 1.0) Severity.PROBLEM else Severity.WARNING,
                if (dev < 0) "Titre alcoolique trop faible" else "Titre alcoolique trop élevé",
                "ABV estimé ${Format.abv(stats.abv)} pour une cible " +
                    "${Format.range(style.abvMin, style.abvMax)} % vol.",
                "L'alcool découle de l'OG et de la FG : corrigez d'abord la densité initiale.",
            )
        }
        return out
    }

    // ---------------------------------------------------------------------------------

    private fun bitternessChecks(ctx: RecipeContext, stats: RecipeStats): List<Suggestion> {
        val out = mutableListOf<Suggestion>()
        val style = ctx.style

        if (style != null) {
            checkRange(stats.ibu, style.ibuRange)?.let { dev ->
                val target = if (dev < 0) style.ibuMin else style.ibuMax
                val deltaIbu = target - stats.ibu
                val reference = ctx.details.hops
                    .filter { it.use == HopUse.BOIL || it.use == HopUse.FIRST_WORT }
                    .maxByOrNull { it.timeMin }
                val action = if (reference != null) {
                    val eq = ctx.equipment ?: Equipment(name = "")
                    val ibuCtx = IbuCalculator.Context(
                        batchVolumeL = ctx.details.recipe.batchVolumeL,
                        boilGravity = stats.boilGravity,
                        boilTimeMin = ctx.details.recipe.boilTimeMin,
                        equipmentUtilizationPct = eq.hopUtilizationPct,
                        formula = ctx.details.recipe.ibuFormula,
                    )
                    val grams = IbuCalculator.gramsForIbuDelta(deltaIbu, reference, ibuCtx)
                    if (grams >= 0) {
                        "Ajoutez ${Format.grams(grams)} de ${reference.name} à " +
                            "${reference.timeMin} min."
                    } else {
                        "Retirez ${Format.grams(abs(grams))} de ${reference.name} à " +
                            "${reference.timeMin} min."
                    }
                } else {
                    "Ajoutez un houblon amérisant en début d'ébullition."
                }
                out += Suggestion(
                    "IBU", if (abs(dev) > 10) Severity.PROBLEM else Severity.WARNING,
                    if (dev < 0) "Amertume trop faible" else "Amertume trop marquée",
                    "${Format.ibu(stats.ibu)} IBU estimés pour une cible " +
                        "${Format.ibu(style.ibuMin)} – ${Format.ibu(style.ibuMax)} " +
                        "(formule ${ctx.details.recipe.ibuFormula.label}).",
                    action,
                )
            }
        }

        if (stats.og > 1.010 && stats.buGuRatio > 0) {
            val balance = when {
                stats.buGuRatio < 0.35 -> "nettement maltée"
                stats.buGuRatio < 0.55 -> "équilibrée, côté malt"
                stats.buGuRatio < 0.78 -> "équilibrée, côté houblon"
                else -> "franchement amère"
            }
            out += Suggestion(
                "Équilibre", Severity.INFO, "Rapport BU:GU de %.2f".format(stats.buGuRatio),
                "La bière se présentera comme $balance.",
                null,
            )
        }

        // Le houblon perd ses alpha-acides avec le temps : au-delà de deux récoltes, l'amertume calculée est optimiste.
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        ctx.details.hops.mapNotNull { it.year }.minOrNull()?.let { oldest ->
            if (currentYear - oldest >= 2) {
                out += Suggestion(
                    "Houblon", Severity.WARNING, "Houblons de récolte ancienne",
                    "La plus ancienne récolte utilisée date de $oldest. Un houblon bien conservé " +
                        "perd environ 15 % de ses alpha-acides par an.",
                    "Mesurez ou corrigez le taux d'alpha dans la fiche avant de brasser.",
                )
            }
        }

        val whirlpoolHot = ctx.details.hops.filter {
            it.use == HopUse.WHIRLPOOL && it.temperatureC >= 90.0
        }
        if (whirlpoolHot.isNotEmpty()) {
            out += Suggestion(
                "Whirlpool", Severity.INFO, "Whirlpool au-dessus de 90 °C",
                "À cette température l'isomérisation continue : ces ajouts comptent déjà pour " +
                    "${Format.ibu(whirlpoolHot.sumOf { h -> stats.hopContributions.firstOrNull { it.hopId == h.id }?.ibu ?: 0.0 })} IBU.",
                "Descendez vers 75–80 °C pour garder l'arôme sans ajouter d'amertume.",
            )
        }
        return out
    }

    // ---------------------------------------------------------------------------------

    private fun colourChecks(ctx: RecipeContext, stats: RecipeStats): List<Suggestion> {
        val style = ctx.style ?: return emptyList()
        val dev = checkRange(stats.ebc, style.ebcRange) ?: return emptyList()
        val target = if (dev < 0) style.ebcMin else style.ebcMax
        val grams = darkMaltGramsForColour(ctx, stats, target)
        return listOf(
            Suggestion(
                "SRM / EBC", if (abs(dev) > style.ebcMax * 0.3) Severity.PROBLEM else Severity.WARNING,
                if (dev < 0) "Robe trop claire" else "Robe trop foncée",
                "${Format.ebc(stats.ebc)} (${Format.srm(stats.srm)}) pour une cible " +
                    "${Format.ebc(style.ebcMin)} – ${Format.ebc(style.ebcMax)}.",
                if (dev < 0) {
                    "Ajoutez environ ${Format.grams(grams)} de malt torréfié à 1300 EBC " +
                        "(type Carafa III), qui colore sans apporter d'astringence."
                } else {
                    "Retirez environ ${Format.grams(abs(grams))} de votre malt le plus coloré " +
                        "(${ctx.details.malts.maxByOrNull { it.colorEbc }?.name ?: "—"})."
                },
            )
        )
    }

    // ---------------------------------------------------------------------------------

    private fun mashPhChecks(ctx: RecipeContext, stats: RecipeStats): List<Suggestion> {
        if (ctx.details.malts.none { it.type.needsMash }) return emptyList()
        val range = ctx.style?.mashPhRange ?: TargetRange(5.2, 5.6)
        val ph = stats.mashPh.estimatedPh
        val out = mutableListOf<Suggestion>()
        val dev = checkRange(ph, range)

        if (dev == null) {
            out += Suggestion(
                "pH d'empâtage", Severity.INFO, "pH estimé à ${Format.ph(ph)}",
                "Dans la fenêtre ${Format.range(range.min, range.max, 2)} attendue pour ce style. " +
                    "L'estimation reste indicative : mesurez le pH sur un échantillon refroidi.",
                null,
            )
            return out
        }

        val target = if (dev < 0) range.min else range.max
        val meq = (ph - target) * stats.mashPh.bufferCapacity
        val action = if (dev > 0) {
            val ml = meq / ACID_LACTIC_80_MEQ_PER_ML
            "Ajoutez environ %.1f mL d'acide lactique à 80 %%, ou remplacez %s de malt de base par du malt acidulé."
                .format(ml, Format.grams(acidulatedGramsFor(meq)))
        } else {
            val g = abs(meq) / BAKING_SODA_MEQ_PER_G
            "Ajoutez environ %.1f g de bicarbonate de sodium à l'empâtage, ou réduisez la part de malts torréfiés."
                .format(g)
        }

        out += Suggestion(
            "pH d'empâtage", if (abs(dev) > 0.15) Severity.PROBLEM else Severity.WARNING,
            if (dev < 0) "pH d'empâtage trop acide" else "pH d'empâtage trop élevé",
            "pH estimé ${Format.ph(ph)} pour une cible ${Format.range(range.min, range.max, 2)}. " +
                "Alcalinité résiduelle de l'eau : %.0f ppm CaCO3.".format(stats.mashPh.residualAlkalinity),
            action,
        )
        return out
    }

    // ---------------------------------------------------------------------------------

    private fun fermentationChecks(ctx: RecipeContext, stats: RecipeStats): List<Suggestion> {
        val out = mutableListOf<Suggestion>()
        val yeasts = ctx.details.yeasts

        if (yeasts.isNotEmpty()) {
            val ratio = stats.pitching.ratio
            if (ratio < 0.7) {
                out += Suggestion(
                    "Ensemencement", Severity.PROBLEM, "Sous-ensemencement",
                    "Il faudrait environ %.0f milliards de cellules ; la recette en apporte %.0f."
                        .format(stats.pitching.requiredBillionCells, stats.pitching.providedBillionCells),
                    "Portez la levure sèche à ${Format.grams(stats.pitching.recommendedDryYeastG)} " +
                        "ou préparez un pied de cuve.",
                )
            } else if (ratio > 2.0) {
                out += Suggestion(
                    "Ensemencement", Severity.WARNING, "Sur-ensemencement",
                    "La recette apporte %.1f fois la quantité de cellules nécessaire.".format(ratio),
                    "Réduisez à ${Format.grams(stats.pitching.recommendedDryYeastG)} : " +
                        "un excès de levure appauvrit le profil aromatique.",
                )
            }

            ctx.details.fermentationSteps.forEach { step ->
                val y = yeasts.first()
                if (step.temperatureC < y.tempMinC || step.temperatureC > y.tempMaxC) {
                    out += Suggestion(
                        "Fermentation", Severity.WARNING,
                        "Palier « ${step.name} » hors plage de la levure",
                        "${Format.temp(step.temperatureC)} alors que ${y.name} travaille entre " +
                            "${Format.temp(y.tempMinC)} et ${Format.temp(y.tempMaxC)}.",
                        if (step.temperatureC > y.tempMaxC) {
                            "Au-dessus de la plage, la levure produit des alcools supérieurs et des esters solvantés."
                        } else {
                            "En dessous, la fermentation risque de s'arrêter avant la FG visée."
                        },
                    )
                }
            }
        }

        if (stats.abv > 11.0 && yeasts.isNotEmpty()) {
            out += Suggestion(
                "Fermentation", Severity.WARNING, "Bière forte",
                "À ${Format.abv(stats.abv)}, vérifiez la tolérance à l'alcool de la souche.",
                "Oxygénez bien le moût et ajoutez un nutriment azoté en début de fermentation.",
            )
        }
        return out
    }

    // ---------------------------------------------------------------------------------

    private fun grainBillChecks(ctx: RecipeContext, stats: RecipeStats): List<Suggestion> {
        val out = mutableListOf<Suggestion>()

        if (stats.caramelFractionPct > 20.0) {
            out += Suggestion(
                "Maque", Severity.WARNING, "Beaucoup de malts caramel",
                "${Format.percent(stats.caramelFractionPct)} de la maque. Au-delà de 20 %, " +
                    "la bière devient sirupeuse et peine à finir sa fermentation.",
                "Ramenez les caramels sous 15 % et compensez la couleur avec un malt Munich.",
            )
        }
        if (stats.roastedFractionPct > 12.0) {
            out += Suggestion(
                "Maque", Severity.WARNING, "Torréfiés dominants",
                "${Format.percent(stats.roastedFractionPct)} de malts torréfiés : " +
                    "risque d'astringence et d'amertume brûlée.",
                "Passez une partie en malt déshuské (Carafa Special), moins âpre à couleur égale.",
            )
        }
        if (stats.sugarFractionPct > 25.0) {
            out += Suggestion(
                "Maque", Severity.WARNING, "Part de sucres élevée",
                "${Format.percent(stats.sugarFractionPct)} des points de densité viennent de sucres simples.",
                "Au-delà de 20 %, le corps s'effondre — sauf style belge où c'est recherché.",
            )
        }

        val adjuncts = ctx.details.malts.filter {
            it.type == FermentableType.FLAKED || it.type == FermentableType.ADJUNCT
        }
        if (adjuncts.isNotEmpty() && stats.weightedDiastaticPower in 0.1..30.0) {
            out += Suggestion(
                "Empâtage", Severity.PROBLEM, "Pouvoir diastasique insuffisant",
                "%.0f °Lintner en moyenne alors que la maque contient des crus non maltés."
                    .format(stats.weightedDiastaticPower),
                "Visez au moins 35 °Lintner : augmentez la part de Pilsner ou de Pale Ale.",
            )
        }

        ctx.details.malts.forEach { malt ->
            val pct = stats.maltContributions.firstOrNull { it.maltId == malt.id }?.weightPct ?: 0.0
            if (malt.type == FermentableType.ROASTED && pct > 10.0) {
                out += Suggestion(
                    "Maque", Severity.INFO, "${malt.name} à ${Format.percent(pct)}",
                    "Une proportion aussi haute de malt torréfié est réservée aux stouts et porters.",
                    null,
                )
            }
        }
        return out
    }

    // ---------------------------------------------------------------------------------

    private fun equipmentChecks(ctx: RecipeContext, stats: RecipeStats): List<Suggestion> {
        val eq = ctx.equipment ?: return emptyList()
        val out = mutableListOf<Suggestion>()
        val w = stats.water

        // Le grain occupe environ 0.67 L par kilo en plus de l'eau d'empâtage.
        val mashVolumeL = w.mashWaterL + stats.totalGrainKg * 0.67
        if (mashVolumeL > eq.mashTunVolumeL) {
            out += Suggestion(
                "Installation", Severity.PROBLEM, "Cuve d'empâtage trop petite",
                "L'empâtage occupe ${Format.liters(mashVolumeL)} pour une cuve de " +
                    "${Format.liters(eq.mashTunVolumeL)}.",
                "Épaississez la maque à %.1f L/kg ou scindez le brassin."
                    .format(((eq.mashTunVolumeL - stats.totalGrainKg * 0.67) / stats.totalGrainKg.coerceAtLeast(0.1))),
            )
        }
        if (w.preBoilVolumeL > eq.kettleVolumeL) {
            out += Suggestion(
                "Installation", Severity.PROBLEM, "Cuve d'ébullition trop petite",
                "${Format.liters(w.preBoilVolumeL)} à faire bouillir dans " +
                    "${Format.liters(eq.kettleVolumeL)}.",
                "Réduisez le volume de brassin ou allongez l'ébullition pour partir de moins d'eau.",
            )
        }
        if (ctx.details.recipe.mashThicknessLPerKg !in 2.0..4.5 && stats.totalGrainKg > 0) {
            out += Suggestion(
                "Empâtage", Severity.INFO,
                "Épaisseur d'empâtage inhabituelle",
                "%.1f L/kg. La plage courante va de 2.5 à 4 L/kg."
                    .format(ctx.details.recipe.mashThicknessLPerKg),
                "Une maque épaisse protège les enzymes, une maque fluide améliore la conversion.",
            )
        }
        val pilsnerShare = ctx.details.malts
            .filter { it.name.contains("pilsner", ignoreCase = true) }
            .sumOf { it.amountG } / ctx.details.malts.sumOf { it.amountG }.coerceAtLeast(1.0)
        if (pilsnerShare > 0.5 && ctx.details.recipe.boilTimeMin < 90) {
            out += Suggestion(
                "Ébullition", Severity.WARNING, "Ébullition courte avec du malt Pilsner",
                "Le malt Pilsner libère du précurseur de DMS, qui demande une ébullition longue et découverte.",
                "Portez l'ébullition à 90 minutes.",
            )
        }
        if (ctx.details.recipe.efficiencyPct !in 50.0..90.0) {
            out += Suggestion(
                "Rendement", Severity.WARNING, "Rendement hors du plausible",
                "${Format.percent(ctx.details.recipe.efficiencyPct)} saisis. " +
                    "Une installation amateur tourne entre 60 et 80 %.",
                "Recalez le rendement à partir d'un brassin mesuré dans le journal.",
            )
        }
        return out
    }

    // ---------------------------------------------------------------------------------

    private fun waterChecks(ctx: RecipeContext, stats: RecipeStats): List<Suggestion> {
        val water = stats.finalWaterProfile
        if (ctx.water == null) return emptyList()
        val out = mutableListOf<Suggestion>()
        val ratio = water.sulfateChlorideRatio
        val hoppy = (ctx.style?.ibuMax ?: 0.0) >= 40.0

        if (ratio.isFinite()) {
            when {
                hoppy && ratio < 1.0 -> out += Suggestion(
                    "Eau", Severity.INFO, "Équilibre sulfate / chlorure trop rond",
                    "Rapport de %.1f pour un style houblonné.".format(ratio),
                    "Montez vers 2:1 en ajoutant du gypse (CaSO4) à l'empâtage.",
                )
                !hoppy && ratio > 2.0 -> out += Suggestion(
                    "Eau", Severity.INFO, "Équilibre sulfate / chlorure très sec",
                    "Rapport de %.1f pour un style malté.".format(ratio),
                    "Descendez vers 1:1 avec du chlorure de calcium (CaCl2) pour arrondir la bouche.",
                )
            }
        }
        if (water.calcium < 50.0) {
            out += Suggestion(
                "Eau", Severity.WARNING, "Calcium insuffisant",
                "%.0f ppm. En dessous de 50 ppm, la clarification et la floculation souffrent."
                    .format(water.calcium),
                "Visez 50–150 ppm avec du gypse ou du chlorure de calcium.",
            )
        }
        if (water.sodium > 150.0) {
            out += Suggestion(
                "Eau", Severity.WARNING, "Sodium élevé",
                "%.0f ppm : au-delà de 150 ppm, la bière prend un goût salé.".format(water.sodium),
                "Diluez avec de l'eau osmosée.",
            )
        }
        return out
    }

    // ---------------------------------------------------------------------------------
    // Inversions des formules du moteur
    // ---------------------------------------------------------------------------------

    /** Écart signé à l'intervalle cible, ou null si la valeur est dedans. */
    private fun checkRange(value: Double, range: TargetRange): Double? =
        range.deviation(value).takeIf { it != 0.0 }

    /** Masse de malt de base nécessaire pour gagner (ou perdre) un nombre de points d'OG. */
    private fun baseMaltGramsForPoints(
        ctx: RecipeContext,
        deltaPoints: Double,
    ): Double {
        val efficiency = ctx.details.recipe.efficiencyPct / 100.0
        val reference = ctx.details.malts.firstOrNull { it.type == FermentableType.BASE }
        val yieldPct = reference?.extractYieldPct ?: 80.0
        val ppg = Units.SUCROSE_PPG * yieldPct / 100.0
        if (ppg <= 0 || efficiency <= 0) return 0.0
        val gallons = Units.litersToGallons(ctx.details.recipe.batchVolumeL)
        val lb = deltaPoints * gallons / (ppg * efficiency)
        return lb * Units.KG_PER_LB * 1000.0
    }

    /** Rendement supplémentaire qui apporterait le même gain de densité. */
    private fun pointsAsEfficiency(
        ctx: RecipeContext,
        stats: RecipeStats,
        deltaPoints: Double,
    ): String {
        if (stats.totalGravityPoints <= 0) return "—"
        val gallons = Units.litersToGallons(ctx.details.recipe.batchVolumeL)
        val currentPoints = stats.totalGravityPoints / gallons
        if (currentPoints <= 0) return "—"
        val factor = (currentPoints + deltaPoints) / currentPoints
        val newEfficiency = ctx.details.recipe.efficiencyPct * factor
        return "%.0f points".format(newEfficiency - ctx.details.recipe.efficiencyPct)
    }

    /** Volume qui amènerait la densité à la cible, à extrait constant. */
    private fun volumeForGravity(stats: RecipeStats, targetOg: Double): Double {
        val targetPoints = Units.sgToPoints(targetOg)
        if (targetPoints <= 0) return 0.0
        // Les points d'extrait sont fixés par la maque : seul le volume peut encore bouger.
        return stats.totalGravityPoints / targetPoints * Units.L_PER_GALLON
    }

    /**
     * Masse de malt sombre pour atteindre une couleur cible, en inversant Morey :
     * MCU = (SRM / 1.4922) ^ (1 / 0.6859).
     */
    private fun darkMaltGramsForColour(
        ctx: RecipeContext,
        stats: RecipeStats,
        targetEbc: Double,
    ): Double {
        val gallons = Units.litersToGallons(ctx.details.recipe.batchVolumeL)
        fun mcuFor(ebc: Double): Double =
            (Units.ebcToSrm(ebc) / 1.4922).pow(1.0 / 0.6859)
        val deltaMcu = mcuFor(targetEbc) - mcuFor(stats.ebc)
        // Carafa III : 1300 EBC, soit environ 490 °Lovibond.
        val lovibond = Units.ebcToLovibond(1300.0)
        val lb = deltaMcu * gallons / lovibond
        return lb * Units.KG_PER_LB * 1000.0
    }

    /** Masse de malt acidulé apportant un nombre donné de milliéquivalents d'acide. */
    private fun acidulatedGramsFor(meq: Double): Double {
        // Le malt acidulé titre environ 1.5 % d'acide lactique, soit ~166 mEq/kg.
        val meqPerKg = 166.0
        return (meq / meqPerKg).coerceAtLeast(0.0) * 1000.0
    }
}
