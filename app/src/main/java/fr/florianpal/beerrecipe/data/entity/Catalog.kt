package fr.florianpal.beerrecipe.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import fr.florianpal.beerrecipe.domain.model.AdditiveStage
import fr.florianpal.beerrecipe.domain.model.AdditiveType
import fr.florianpal.beerrecipe.domain.model.AmountUnit
import fr.florianpal.beerrecipe.domain.model.FermentableType
import fr.florianpal.beerrecipe.domain.model.Flocculation
import fr.florianpal.beerrecipe.domain.model.HopForm
import fr.florianpal.beerrecipe.domain.model.HopPurpose
import fr.florianpal.beerrecipe.domain.model.YeastForm
import fr.florianpal.beerrecipe.domain.model.YeastType

/**
 * Catalogue d'ingrédients : le garde-manger de la brasserie.
 * Une ligne de recette part toujours d'une fiche de catalogue, puis en fige une copie
 * pour que la recette reste fidèle même si la fiche change plus tard.
 */

@Entity(tableName = "malt")
data class Malt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: FermentableType = FermentableType.BASE,
    /** Origine : pays ou malterie. */
    val origin: String = "",
    val maltster: String = "",
    /** Année de récolte, si elle est connue. */
    val year: Int? = null,
    /** Couleur du malt en EBC. */
    val colorEbc: Double = 4.0,
    /** Rendement en extrait, mouture fine, matière sèche, en pourcentage. */
    val extractYieldPct: Double = 80.0,
    /**
     * Part de l'extrait que la levure peut consommer. 100 % pour un malt ou un sucre
     * ordinaire, 0 % pour le lactose qui sucre sans jamais fermenter.
     */
    val fermentabilityPct: Double = 100.0,
    val moisturePct: Double = 4.0,
    /** Pouvoir diastasique en °Lintner : capacité à convertir l'amidon, y compris celui des crus. */
    val diastaticPowerLintner: Double = 0.0,
    /** Proportion maximale conseillée dans la maque, en pourcentage. */
    val maxBatchPct: Double = 100.0,
    /** Styles de bière dans lesquels ce malt est traditionnel. */
    val associatedStyles: List<String> = emptyList(),
    /** pH du malt en eau distillée : point de départ du calcul de pH d'empâtage. */
    val distilledWaterPh: Double? = null,
    /** Pouvoir tampon en mEq par kilo et par unité de pH. */
    val bufferCapacity: Double? = null,
    val pricePerKg: Double? = null,
    val stockG: Double = 0.0,
    val notes: String = "",
    val isCustom: Boolean = false,
)

@Entity(tableName = "hop")
data class Hop(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val origin: String = "",
    /** Année de récolte : l'alpha baisse avec l'âge du houblon. */
    val year: Int? = null,
    val alphaAcidPct: Double = 5.0,
    val betaAcidPct: Double = 4.0,
    val cohumulonePct: Double? = null,
    val totalOilMlPer100g: Double? = null,
    val form: HopForm = HopForm.PELLET,
    val purpose: HopPurpose = HopPurpose.DUAL,
    /** Descripteurs aromatiques : agrumes, résineux, floral… */
    val aromas: List<String> = emptyList(),
    val substitutes: List<String> = emptyList(),
    /** Prix au kilo, facultatif. */
    val pricePerKg: Double? = null,
    val stockG: Double = 0.0,
    val notes: String = "",
    val isCustom: Boolean = false,
)

@Entity(tableName = "yeast")
data class Yeast(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val laboratory: String = "",
    val productId: String = "",
    val type: YeastType = YeastType.ALE,
    val form: YeastForm = YeastForm.DRY,
    /** Plage de température de fermentation conseillée, en °C. */
    val tempMinC: Double = 18.0,
    val tempMaxC: Double = 22.0,
    /** Atténuation apparente, en pourcentage. */
    val attenuationMinPct: Double = 73.0,
    val attenuationMaxPct: Double = 80.0,
    val flocculation: Flocculation = Flocculation.MEDIUM,
    val alcoholTolerancePct: Double = 10.0,
    /** Profil aromatique produit par la souche. */
    val aromaProfile: List<String> = emptyList(),
    /** Styles populaires associés à cette levure. */
    val popularStyles: List<String> = emptyList(),
    /** Cellules viables par gramme, en milliards : sert au calcul d'ensemencement. */
    val cellsPerGramBillions: Double = 10.0,
    val pricePerUnit: Double? = null,
    /** Masse d'un sachet, en grammes. */
    val packageSizeG: Double = 11.5,
    val stockG: Double = 0.0,
    val notes: String = "",
    val isCustom: Boolean = false,
) {
    val attenuationAvgPct: Double get() = (attenuationMinPct + attenuationMaxPct) / 2.0
    val tempRangeLabel: String get() = String.format("%.0f – %.0f °C", tempMinC, tempMaxC)
}

@Entity(tableName = "additive")
data class Additive(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: AdditiveType = AdditiveType.OTHER,
    val defaultStage: AdditiveStage = AdditiveStage.BOIL,
    val defaultUnit: AmountUnit = AmountUnit.G,
    val defaultAmount: Double = 1.0,
    /**
     * Pour les acides : milliéquivalents de protons apportés par unité de base (mL ou g).
     * Pour les sels : non utilisé, l'effet passe par les ions ci-dessous.
     */
    val acidMeqPerUnit: Double? = null,
    /** Ions apportés, en ppm par gramme et par litre d'eau traitée. */
    val calciumPpmPerGPerL: Double = 0.0,
    val magnesiumPpmPerGPerL: Double = 0.0,
    val sodiumPpmPerGPerL: Double = 0.0,
    val sulfatePpmPerGPerL: Double = 0.0,
    val chloridePpmPerGPerL: Double = 0.0,
    val bicarbonatePpmPerGPerL: Double = 0.0,
    val pricePerUnit: Double? = null,
    val stock: Double = 0.0,
    val notes: String = "",
    val isCustom: Boolean = false,
)
