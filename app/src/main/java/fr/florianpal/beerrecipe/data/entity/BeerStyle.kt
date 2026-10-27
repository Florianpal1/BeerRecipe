package fr.florianpal.beerrecipe.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import fr.florianpal.beerrecipe.domain.model.TargetRange

/**
 * Style de bière visé par une recette. Les intervalles sont ceux du guide BJCP ;
 * ce sont eux que le moteur de suggestions utilise comme cible.
 */
@Entity(tableName = "beer_style")
data class BeerStyle(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val name: String,
    val category: String,
    val ogMin: Double,
    val ogMax: Double,
    val fgMin: Double,
    val fgMax: Double,
    val abvMin: Double,
    val abvMax: Double,
    val ibuMin: Double,
    val ibuMax: Double,
    val ebcMin: Double,
    val ebcMax: Double,
    /** Fourchette de pH d'empâtage conseillée pour ce style. */
    val mashPhMin: Double = 5.2,
    val mashPhMax: Double = 5.6,
    val carbonationMin: Double = 2.0,
    val carbonationMax: Double = 2.6,
    val description: String = "",
    val isCustom: Boolean = false,
) {
    val ogRange get() = TargetRange(ogMin, ogMax)
    val fgRange get() = TargetRange(fgMin, fgMax)
    val abvRange get() = TargetRange(abvMin, abvMax)
    val ibuRange get() = TargetRange(ibuMin, ibuMax)
    val ebcRange get() = TargetRange(ebcMin, ebcMax)
    val mashPhRange get() = TargetRange(mashPhMin, mashPhMax)
    val carbonationRange get() = TargetRange(carbonationMin, carbonationMax)

    val displayName: String get() = if (code.isBlank()) name else "$code — $name"
}
