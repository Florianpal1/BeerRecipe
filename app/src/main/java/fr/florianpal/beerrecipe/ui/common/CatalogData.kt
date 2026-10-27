package fr.florianpal.beerrecipe.ui.common

import fr.florianpal.beerrecipe.data.entity.Additive
import fr.florianpal.beerrecipe.data.entity.BeerStyle
import fr.florianpal.beerrecipe.data.entity.Equipment
import fr.florianpal.beerrecipe.data.entity.Hop
import fr.florianpal.beerrecipe.data.entity.Malt
import fr.florianpal.beerrecipe.data.entity.WaterProfile
import fr.florianpal.beerrecipe.data.entity.Yeast

/** Les référentiels dont un écran d'édition a besoin pour proposer des choix. */
data class CatalogData(
    val malts: List<Malt> = emptyList(),
    val hops: List<Hop> = emptyList(),
    val yeasts: List<Yeast> = emptyList(),
    val additives: List<Additive> = emptyList(),
    val styles: List<BeerStyle> = emptyList(),
    val equipments: List<Equipment> = emptyList(),
    val waterProfiles: List<WaterProfile> = emptyList(),
)
