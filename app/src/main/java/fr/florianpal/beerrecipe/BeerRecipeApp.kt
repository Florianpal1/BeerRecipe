package fr.florianpal.beerrecipe

import android.app.Application
import fr.florianpal.beerrecipe.data.db.AppDatabase
import fr.florianpal.beerrecipe.data.repository.BrewRepository
import fr.florianpal.beerrecipe.data.seed.DatabaseSeeder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Conteneur d'application. L'injection de dépendances tient en un objet : la base,
 * le dépôt et un périmètre de coroutines pour l'amorçage des catalogues.
 */
class BeerRecipeApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.applicationScope.launch {
            DatabaseSeeder.seed(container.database, DatabaseSeeder.prefs(this@BeerRecipeApp))
        }
    }
}

class AppContainer(app: Application) {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val database: AppDatabase = AppDatabase.get(app)
    val repository: BrewRepository = BrewRepository(database)
}
