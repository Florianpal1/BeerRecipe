package fr.florianpal.beerrecipe.data.db

import androidx.room.TypeConverter
import fr.florianpal.beerrecipe.domain.model.AdditiveStage
import fr.florianpal.beerrecipe.domain.model.AdditiveType
import fr.florianpal.beerrecipe.domain.model.AmountUnit
import fr.florianpal.beerrecipe.domain.model.ColorFormula
import fr.florianpal.beerrecipe.domain.model.EquipmentType
import fr.florianpal.beerrecipe.domain.model.FermentableType
import fr.florianpal.beerrecipe.domain.model.Flocculation
import fr.florianpal.beerrecipe.domain.model.HopForm
import fr.florianpal.beerrecipe.domain.model.HopPurpose
import fr.florianpal.beerrecipe.domain.model.HopUse
import fr.florianpal.beerrecipe.domain.model.IbuFormula
import fr.florianpal.beerrecipe.domain.model.MashStepType
import fr.florianpal.beerrecipe.domain.model.YeastForm
import fr.florianpal.beerrecipe.domain.model.YeastType

/**
 * Conversions Room. Les énumérations sont stockées par leur nom et non par leur rang,
 * pour qu'un ajout dans la liste ne décale pas les données déjà enregistrées.
 */
class Converters {

    @TypeConverter
    fun stringListToDb(value: List<String>?): String =
        value.orEmpty().filter { it.isNotBlank() }.joinToString(SEPARATOR)

    @TypeConverter
    fun dbToStringList(value: String?): List<String> =
        value?.split(SEPARATOR)?.filter { it.isNotBlank() } ?: emptyList()

    @TypeConverter fun fermentableTypeToDb(v: FermentableType) = v.name
    @TypeConverter fun dbToFermentableType(v: String) = enumOr(v, FermentableType.BASE)

    @TypeConverter fun hopUseToDb(v: HopUse) = v.name
    @TypeConverter fun dbToHopUse(v: String) = enumOr(v, HopUse.BOIL)

    @TypeConverter fun hopFormToDb(v: HopForm) = v.name
    @TypeConverter fun dbToHopForm(v: String) = enumOr(v, HopForm.PELLET)

    @TypeConverter fun hopPurposeToDb(v: HopPurpose) = v.name
    @TypeConverter fun dbToHopPurpose(v: String) = enumOr(v, HopPurpose.DUAL)

    @TypeConverter fun yeastTypeToDb(v: YeastType) = v.name
    @TypeConverter fun dbToYeastType(v: String) = enumOr(v, YeastType.ALE)

    @TypeConverter fun yeastFormToDb(v: YeastForm) = v.name
    @TypeConverter fun dbToYeastForm(v: String) = enumOr(v, YeastForm.DRY)

    @TypeConverter fun flocculationToDb(v: Flocculation) = v.name
    @TypeConverter fun dbToFlocculation(v: String) = enumOr(v, Flocculation.MEDIUM)

    @TypeConverter fun additiveTypeToDb(v: AdditiveType) = v.name
    @TypeConverter fun dbToAdditiveType(v: String) = enumOr(v, AdditiveType.OTHER)

    @TypeConverter fun additiveStageToDb(v: AdditiveStage) = v.name
    @TypeConverter fun dbToAdditiveStage(v: String) = enumOr(v, AdditiveStage.BOIL)

    @TypeConverter fun amountUnitToDb(v: AmountUnit) = v.name
    @TypeConverter fun dbToAmountUnit(v: String) = enumOr(v, AmountUnit.G)

    @TypeConverter fun mashStepTypeToDb(v: MashStepType) = v.name
    @TypeConverter fun dbToMashStepType(v: String) = enumOr(v, MashStepType.INFUSION)

    @TypeConverter fun equipmentTypeToDb(v: EquipmentType) = v.name
    @TypeConverter fun dbToEquipmentType(v: String) = enumOr(v, EquipmentType.ALL_GRAIN)

    @TypeConverter fun ibuFormulaToDb(v: IbuFormula) = v.name
    @TypeConverter fun dbToIbuFormula(v: String) = enumOr(v, IbuFormula.TINSETH)

    @TypeConverter fun colorFormulaToDb(v: ColorFormula) = v.name
    @TypeConverter fun dbToColorFormula(v: String) = enumOr(v, ColorFormula.MOREY)

    private inline fun <reified T : Enum<T>> enumOr(value: String, fallback: T): T =
        runCatching { enumValueOf<T>(value) }.getOrDefault(fallback)

    private companion object {
        /** Séparateur d'unité ASCII : impossible à saisir dans un champ de texte. */
        const val SEPARATOR = "\u001F"
    }
}
