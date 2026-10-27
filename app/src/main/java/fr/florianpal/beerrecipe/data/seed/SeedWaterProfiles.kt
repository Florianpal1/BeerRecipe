package fr.florianpal.beerrecipe.data.seed

import fr.florianpal.beerrecipe.data.entity.WaterProfile

/**
 * Profils d'eau historiques. Chaque grande ville brassicole doit son style à la
 * composition de sa nappe : Pilsen et son eau presque pure, Burton et ses sulfates,
 * Dublin et son alcalinité que seule l'orge torréfiée parvient à compenser.
 */
object SeedWaterProfiles {

    private fun profile(
        name: String,
        ca: Double, mg: Double, na: Double,
        so4: Double, cl: Double, hco3: Double,
        notes: String,
    ) = WaterProfile(
        name = name, calcium = ca, magnesium = mg, sodium = na,
        sulfate = so4, chloride = cl, bicarbonate = hco3,
        isCustom = false, notes = notes,
    )

    val ALL: List<WaterProfile> = listOf(
        profile("Eau osmosée", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            "Table rase : construisez le profil voulu à partir de zéro."),
        profile("Pilsen", 7.0, 2.0, 2.0, 5.0, 5.0, 15.0,
            "Eau extrêmement douce : la seule qui permette une Pilsner aussi délicate."),
        profile("Burton-on-Trent", 275.0, 40.0, 25.0, 610.0, 35.0, 270.0,
            "Sulfates massifs : l'amertume sèche et minérale des pale ales anglaises."),
        profile("Dublin", 118.0, 4.0, 12.0, 55.0, 19.0, 160.0,
            "Alcalinité élevée, équilibrée par l'orge torréfiée de la stout."),
        profile("Munich", 77.0, 17.0, 4.0, 18.0, 8.0, 295.0,
            "Faite pour les maisches foncées : trop alcaline pour une bière pâle."),
        profile("Londres", 52.0, 32.0, 86.0, 32.0, 34.0, 104.0,
            "Profil des porters historiques."),
        profile("Dortmund", 225.0, 40.0, 60.0, 120.0, 60.0, 220.0,
            "Minéralisation forte mais équilibrée : l'export lager."),
        profile("Vienne", 200.0, 60.0, 8.0, 125.0, 12.0, 120.0,
            "Pour les lagers ambrées."),
        profile("Édimbourg", 100.0, 20.0, 55.0, 140.0, 50.0, 285.0,
            "L'eau des ales écossaises maltées."),
        profile("Pale Ale houblonnée (moderne)", 110.0, 10.0, 15.0, 200.0, 50.0, 0.0,
            "Rapport sulfate/chlorure de 4:1 : amertume nette et finale sèche."),
        profile("NEIPA / malté (moderne)", 110.0, 10.0, 15.0, 75.0, 150.0, 0.0,
            "Chlorures dominants : bouche ronde et juteuse."),
        profile("Équilibrée", 80.0, 10.0, 15.0, 100.0, 100.0, 0.0,
            "Point de départ neutre pour la plupart des styles."),
    )
}
