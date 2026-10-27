package fr.florianpal.beerrecipe.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import fr.florianpal.beerrecipe.AppContainer
import fr.florianpal.beerrecipe.BeerRecipeApp

/** Récupère le conteneur de l'application depuis n'importe quel composable. */
@Composable
fun appContainer(): AppContainer =
    (LocalContext.current.applicationContext as BeerRecipeApp).container
