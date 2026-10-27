package fr.florianpal.beerrecipe.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import fr.florianpal.beerrecipe.domain.model.AdditiveStage
import fr.florianpal.beerrecipe.domain.model.AdditiveType
import fr.florianpal.beerrecipe.domain.model.AmountUnit
import fr.florianpal.beerrecipe.domain.model.ColorFormula
import fr.florianpal.beerrecipe.domain.model.FermentableType
import fr.florianpal.beerrecipe.domain.model.Flocculation
import fr.florianpal.beerrecipe.domain.model.HopForm
import fr.florianpal.beerrecipe.domain.model.HopUse
import fr.florianpal.beerrecipe.domain.model.IbuFormula
import fr.florianpal.beerrecipe.domain.model.MashStepType
import fr.florianpal.beerrecipe.domain.model.YeastForm
import fr.florianpal.beerrecipe.domain.model.YeastType

/**
 * Une recette. Les ingrédients sont copiés depuis le catalogue au moment de l'ajout :
 * la recette garde ainsi la trace exacte du lot utilisé (origine, année, alpha du jour)
 * même si la fiche de catalogue est modifiée ou supprimée ensuite.
 */
@Entity(
    tableName = "recipe",
    indices = [Index("styleId"), Index("equipmentId")],
)
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val author: String = "",
    val styleId: Long? = null,
    val equipmentId: Long? = null,

    /** Volume et durée propres à la recette : par défaut repris de l'installation. */
    val batchVolumeL: Double = 20.0,
    val boilTimeMin: Int = 60,
    val efficiencyPct: Double = 72.0,

    val sourceWaterId: Long? = null,
    val mashThicknessLPerKg: Double = 3.0,
    val grainTempC: Double = 20.0,
    val spargeTempC: Double = 76.0,

    val ibuFormula: IbuFormula = IbuFormula.TINSETH,
    val colorFormula: ColorFormula = ColorFormula.MOREY,
    /** Carbonatation visée, en volumes de CO2. */
    val carbonationVolumes: Double = 2.4,
    val bottlingTempC: Double = 20.0,

    val notes: String = "",
    val tastingNotes: String = "",
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isFavourite: Boolean = false,
)

@Entity(
    tableName = "recipe_malt",
    foreignKeys = [ForeignKey(
        entity = Recipe::class,
        parentColumns = ["id"],
        childColumns = ["recipeId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("recipeId")],
)
data class RecipeMalt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    /** Fiche de catalogue d'origine, si la ligne en vient. */
    val catalogId: Long? = null,
    val name: String,
    val type: FermentableType = FermentableType.BASE,
    /** Quantité en grammes. */
    val amountG: Double = 0.0,
    val origin: String = "",
    val year: Int? = null,
    val colorEbc: Double = 4.0,
    val extractYieldPct: Double = 80.0,
    /** Part fermentescible de l'extrait, en pourcentage (0 pour le lactose). */
    val fermentabilityPct: Double = 100.0,
    /** Pouvoir diastasique en °Lintner : la réserve d'enzymes disponible pour les crus. */
    val diastaticPowerLintner: Double = 0.0,
    val associatedStyles: List<String> = emptyList(),
    val distilledWaterPh: Double? = null,
    val bufferCapacity: Double? = null,
    val pricePerKg: Double? = null,
    val position: Int = 0,
    val notes: String = "",
)

@Entity(
    tableName = "recipe_hop",
    foreignKeys = [ForeignKey(
        entity = Recipe::class,
        parentColumns = ["id"],
        childColumns = ["recipeId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("recipeId")],
)
data class RecipeHop(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val catalogId: Long? = null,
    val name: String,
    /** Quantité en grammes. */
    val amountG: Double = 0.0,
    val origin: String = "",
    val year: Int? = null,
    val alphaAcidPct: Double = 5.0,
    val aromas: List<String> = emptyList(),
    val form: HopForm = HopForm.PELLET,
    val use: HopUse = HopUse.BOIL,
    /** Durée d'infusion en minutes ; pour un houblonnage à cru, une durée en jours. */
    val timeMin: Int = 60,
    /** Température du whirlpool, en °C : elle commande l'isomérisation résiduelle. */
    val temperatureC: Double = 80.0,
    /** Prix au kilo, facultatif. */
    val pricePerKg: Double? = null,
    val position: Int = 0,
    val notes: String = "",
)

@Entity(
    tableName = "recipe_yeast",
    foreignKeys = [ForeignKey(
        entity = Recipe::class,
        parentColumns = ["id"],
        childColumns = ["recipeId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("recipeId")],
)
data class RecipeYeast(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val catalogId: Long? = null,
    val name: String,
    val laboratory: String = "",
    val type: YeastType = YeastType.ALE,
    val form: YeastForm = YeastForm.DRY,
    /** Quantité en grammes. */
    val amountG: Double = 11.5,
    val tempMinC: Double = 18.0,
    val tempMaxC: Double = 22.0,
    /** Atténuation apparente retenue pour le calcul de densité finale. */
    val attenuationPct: Double = 77.0,
    val flocculation: Flocculation = Flocculation.MEDIUM,
    val aromaProfile: List<String> = emptyList(),
    val popularStyles: List<String> = emptyList(),
    val cellsPerGramBillions: Double = 10.0,
    val price: Double? = null,
    val position: Int = 0,
    val notes: String = "",
)

@Entity(
    tableName = "recipe_additive",
    foreignKeys = [ForeignKey(
        entity = Recipe::class,
        parentColumns = ["id"],
        childColumns = ["recipeId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("recipeId")],
)
data class RecipeAdditive(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val catalogId: Long? = null,
    val name: String,
    val type: AdditiveType = AdditiveType.OTHER,
    val stage: AdditiveStage = AdditiveStage.BOIL,
    val amount: Double = 1.0,
    val unit: AmountUnit = AmountUnit.G,
    val timeMin: Int = 10,
    val acidMeqPerUnit: Double? = null,
    val calciumPpmPerGPerL: Double = 0.0,
    val magnesiumPpmPerGPerL: Double = 0.0,
    val sodiumPpmPerGPerL: Double = 0.0,
    val sulfatePpmPerGPerL: Double = 0.0,
    val chloridePpmPerGPerL: Double = 0.0,
    val bicarbonatePpmPerGPerL: Double = 0.0,
    val price: Double? = null,
    val position: Int = 0,
    val notes: String = "",
)

@Entity(
    tableName = "mash_step",
    foreignKeys = [ForeignKey(
        entity = Recipe::class,
        parentColumns = ["id"],
        childColumns = ["recipeId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("recipeId")],
)
data class MashStep(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val name: String = "Saccharification",
    val type: MashStepType = MashStepType.INFUSION,
    val temperatureC: Double = 66.0,
    val timeMin: Int = 60,
    val rampMin: Int = 0,
    /** Eau ajoutée pour un palier par infusion, en litres. */
    val infusionL: Double? = null,
    val position: Int = 0,
)

@Entity(
    tableName = "fermentation_step",
    foreignKeys = [ForeignKey(
        entity = Recipe::class,
        parentColumns = ["id"],
        childColumns = ["recipeId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("recipeId")],
)
data class FermentationStep(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val name: String = "Fermentation primaire",
    val temperatureC: Double = 19.0,
    val days: Int = 14,
    val pressureBar: Double? = null,
    val position: Int = 0,
)
