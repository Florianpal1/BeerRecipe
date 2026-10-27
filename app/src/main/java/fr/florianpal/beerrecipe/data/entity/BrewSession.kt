package fr.florianpal.beerrecipe.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Journal d'un brassin réellement effectué. Confronter le mesuré au calculé est
 * le seul moyen de recaler le rendement de l'installation pour les recettes suivantes.
 */
@Entity(
    tableName = "brew_session",
    foreignKeys = [ForeignKey(
        entity = Recipe::class,
        parentColumns = ["id"],
        childColumns = ["recipeId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("recipeId")],
)
data class BrewSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val brewDate: Long = System.currentTimeMillis(),
    val measuredPreBoilGravity: Double? = null,
    val measuredPreBoilVolumeL: Double? = null,
    val measuredOg: Double? = null,
    val measuredFg: Double? = null,
    val measuredVolumeL: Double? = null,
    val measuredMashPh: Double? = null,
    val measuredMashTempC: Double? = null,
    val fermentationTempC: Double? = null,
    val bottledDate: Long? = null,
    val rating: Int? = null,
    val tastingNotes: String = "",
    val notes: String = "",
)
