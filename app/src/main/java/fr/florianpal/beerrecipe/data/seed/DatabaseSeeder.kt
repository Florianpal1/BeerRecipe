package fr.florianpal.beerrecipe.data.seed

import android.content.Context
import android.content.SharedPreferences
import fr.florianpal.beerrecipe.data.db.AppDatabase
import fr.florianpal.beerrecipe.data.entity.Equipment
import fr.florianpal.beerrecipe.domain.model.EquipmentType

/**
 * Remplit la base au premier lancement. Chaque catalogue est garni indépendamment :
 * si l'utilisateur vide ses houblons, ils ne seront pas réinjectés tant que la table
 * n'est pas revenue à zéro ligne, ou que [CATALOG_VERSION] n'a pas augmenté.
 */
object DatabaseSeeder {

    private val DEFAULT_EQUIPMENT = Equipment(
        name = "Mon installation 20 L",
        type = EquipmentType.ALL_GRAIN,
        description = "Installation type trois cuves pour un brassin de 20 litres.",
        batchVolumeL = 20.0,
        boilTimeMin = 60,
        mashTunVolumeL = 30.0,
        kettleVolumeL = 35.0,
        fermenterVolumeL = 30.0,
        boilOffRateLPerHour = 3.0,
        brewhouseEfficiencyPct = 72.0,
        isDefault = true,
        notes = "Recalez le rendement et l'évaporation après vos premiers brassins mesurés.",
    )

    private val BIAB_EQUIPMENT = Equipment(
        name = "BIAB 20 L",
        type = EquipmentType.BIAB,
        description = "Brassage en sac, une seule cuve, sans rinçage des drêches.",
        batchVolumeL = 20.0,
        boilTimeMin = 60,
        mashTunVolumeL = 36.0,
        kettleVolumeL = 36.0,
        fermenterVolumeL = 30.0,
        boilOffRateLPerHour = 3.5,
        mashTunDeadspaceL = 0.0,
        grainAbsorptionLPerKg = 0.8,
        brewhouseEfficiencyPct = 68.0,
        mashThicknessLPerKg = 5.0,
        notes = "L'eau totale part en une fois : l'épaisseur d'empâtage est volontairement élevée.",
    )

    /**
     * Version des catalogues de référence. À incrémenter à chaque ajout dans les fichiers
     * Seed* pour que les installations existantes reçoivent les nouvelles fiches.
     */
    const val CATALOG_VERSION = 3

    private const val PREFS = "seed"
    private const val KEY_CATALOG_VERSION = "catalog_version"

    suspend fun seed(db: AppDatabase, prefs: SharedPreferences) {
        seedIfEmpty(db)
        if (prefs.getInt(KEY_CATALOG_VERSION, 1) < CATALOG_VERSION) {
            addMissing(db)
            prefs.edit().putInt(KEY_CATALOG_VERSION, CATALOG_VERSION).apply()
        }
    }

    fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /**
     * Ajoute les fiches de référence absentes, reconnues par leur nom (et par leur code
     * pour les styles). Les fiches déjà présentes ne sont jamais écrasées : l'utilisateur
     * a pu les corriger. Une fiche supprimée revient une fois, au changement de version.
     */
    suspend fun addMissing(db: AppDatabase) {
        val styleNames = db.beerStyleDao().names().lowercaseSet()
        val styleCodes = db.beerStyleDao().codes().toSet()
        db.beerStyleDao().insertAll(
            SeedStyles.ALL.filter { it.name.lowercase() !in styleNames && it.code !in styleCodes },
        )
        val maltNames = db.maltDao().names().lowercaseSet()
        db.maltDao().insertAll(SeedMalts.ALL.filter { it.name.lowercase() !in maltNames })
        val hopNames = db.hopDao().names().lowercaseSet()
        db.hopDao().insertAll(SeedHops.ALL.filter { it.name.lowercase() !in hopNames })
        val yeastNames = db.yeastDao().names().lowercaseSet()
        db.yeastDao().insertAll(SeedYeasts.ALL.filter { it.name.lowercase() !in yeastNames })
    }

    private fun List<String>.lowercaseSet(): Set<String> = mapTo(HashSet()) { it.lowercase() }

    suspend fun seedIfEmpty(db: AppDatabase) {
        if (db.beerStyleDao().count() == 0) db.beerStyleDao().insertAll(SeedStyles.ALL)
        if (db.maltDao().count() == 0) db.maltDao().insertAll(SeedMalts.ALL)
        if (db.hopDao().count() == 0) db.hopDao().insertAll(SeedHops.ALL)
        if (db.yeastDao().count() == 0) db.yeastDao().insertAll(SeedYeasts.ALL)
        if (db.additiveDao().count() == 0) db.additiveDao().insertAll(SeedAdditives.ALL)
        if (db.waterProfileDao().count() == 0) db.waterProfileDao().insertAll(SeedWaterProfiles.ALL)
        if (db.equipmentDao().count() == 0) {
            db.equipmentDao().insertAll(listOf(DEFAULT_EQUIPMENT, BIAB_EQUIPMENT))
        }
    }
}
