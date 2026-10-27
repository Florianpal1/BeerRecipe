package fr.florianpal.beerrecipe.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import fr.florianpal.beerrecipe.ui.screens.catalog.CatalogScreen
import fr.florianpal.beerrecipe.ui.screens.equipment.EquipmentScreen
import fr.florianpal.beerrecipe.ui.screens.recipe.RecipeDetailScreen
import fr.florianpal.beerrecipe.ui.screens.recipe.RecipeListScreen
import fr.florianpal.beerrecipe.ui.screens.style.StyleScreen

/** Destinations principales, accessibles depuis la barre de navigation. */
enum class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    RECIPES("recipes", "Recettes", Icons.Default.LocalDrink),
    CATALOG("catalog", "Catalogue", Icons.Default.Inventory2),
    EQUIPMENT("equipment", "Installations", Icons.Default.Science),
    STYLES("styles", "Styles", Icons.AutoMirrored.Filled.MenuBook),
}

object Routes {
    const val RECIPE_DETAIL = "recipe/{recipeId}"
    fun recipeDetail(id: Long) = "recipe/$id"
}

@Composable
fun BeerRecipeRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = TopLevelDestination.entries.any { it.route == currentRoute }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    // Une seule instance par onglet, et l'état est restauré
                                    // quand on y revient.
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.RECIPES.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(TopLevelDestination.RECIPES.route) {
                RecipeListScreen(
                    onOpenRecipe = { id -> navController.navigate(Routes.recipeDetail(id)) },
                )
            }
            composable(TopLevelDestination.CATALOG.route) { CatalogScreen() }
            composable(TopLevelDestination.EQUIPMENT.route) { EquipmentScreen() }
            composable(TopLevelDestination.STYLES.route) { StyleScreen() }
            composable(
                route = Routes.RECIPE_DETAIL,
                arguments = listOf(navArgument("recipeId") { type = NavType.LongType }),
            ) { entry ->
                RecipeDetailScreen(
                    recipeId = entry.arguments?.getLong("recipeId") ?: 0L,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
