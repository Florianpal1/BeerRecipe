package fr.florianpal.beerrecipe.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Profil d'eau, en ppm (mg/L). Sert au calcul du pH d'empâtage et à l'équilibre
 * sulfate/chlorure qui oriente la bière vers l'amer sec ou vers le malté rond.
 */
@Entity(tableName = "water_profile")
data class WaterProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val calcium: Double = 0.0,
    val magnesium: Double = 0.0,
    val sodium: Double = 0.0,
    val sulfate: Double = 0.0,
    val chloride: Double = 0.0,
    val bicarbonate: Double = 0.0,
    val ph: Double = 7.0,
    val isCustom: Boolean = true,
    val notes: String = "",
) {
    /** Alcalinité exprimée en ppm de CaCO3, à partir des bicarbonates. */
    val alkalinityAsCaCO3: Double get() = bicarbonate * 50.0 / 61.0

    /**
     * Alcalinité résiduelle : la part d'alcalinité que le calcium et le magnésium
     * ne neutralisent pas et qui fait donc monter le pH d'empâtage.
     */
    val residualAlkalinity: Double
        get() = alkalinityAsCaCO3 - (calcium / 3.5 + magnesium / 7.0)

    /** Rapport sulfate / chlorure : > 2 accentue l'amertume, < 0.8 arrondit le malt. */
    val sulfateChlorideRatio: Double
        get() = if (chloride <= 0.0) Double.POSITIVE_INFINITY else sulfate / chloride
}
