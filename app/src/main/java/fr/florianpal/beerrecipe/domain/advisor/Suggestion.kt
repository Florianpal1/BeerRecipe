package fr.florianpal.beerrecipe.domain.advisor

import fr.florianpal.beerrecipe.domain.model.Severity

/**
 * Un conseil adressé au brasseur. [action] porte le geste chiffré à effectuer ;
 * c'est ce qui distingue un diagnostic utile d'un simple constat.
 */
data class Suggestion(
    val parameter: String,
    val severity: Severity,
    val title: String,
    val detail: String,
    val action: String? = null,
)
