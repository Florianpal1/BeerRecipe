package fr.florianpal.beerrecipe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import fr.florianpal.beerrecipe.ui.navigation.BeerRecipeRoot
import fr.florianpal.beerrecipe.ui.theme.BeerRecipeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BeerRecipeTheme {
                BeerRecipeRoot()
            }
        }
    }
}
