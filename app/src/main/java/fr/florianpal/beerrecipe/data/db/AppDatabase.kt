package fr.florianpal.beerrecipe.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import fr.florianpal.beerrecipe.data.dao.AdditiveDao
import fr.florianpal.beerrecipe.data.dao.BeerStyleDao
import fr.florianpal.beerrecipe.data.dao.BrewSessionDao
import fr.florianpal.beerrecipe.data.dao.EquipmentDao
import fr.florianpal.beerrecipe.data.dao.HopDao
import fr.florianpal.beerrecipe.data.dao.MaltDao
import fr.florianpal.beerrecipe.data.dao.RecipeDao
import fr.florianpal.beerrecipe.data.dao.WaterProfileDao
import fr.florianpal.beerrecipe.data.dao.YeastDao
import fr.florianpal.beerrecipe.data.entity.Additive
import fr.florianpal.beerrecipe.data.entity.BeerStyle
import fr.florianpal.beerrecipe.data.entity.BrewSession
import fr.florianpal.beerrecipe.data.entity.Equipment
import fr.florianpal.beerrecipe.data.entity.FermentationStep
import fr.florianpal.beerrecipe.data.entity.Hop
import fr.florianpal.beerrecipe.data.entity.Malt
import fr.florianpal.beerrecipe.data.entity.MashStep
import fr.florianpal.beerrecipe.data.entity.Recipe
import fr.florianpal.beerrecipe.data.entity.RecipeAdditive
import fr.florianpal.beerrecipe.data.entity.RecipeHop
import fr.florianpal.beerrecipe.data.entity.RecipeMalt
import fr.florianpal.beerrecipe.data.entity.RecipeYeast
import fr.florianpal.beerrecipe.data.entity.WaterProfile
import fr.florianpal.beerrecipe.data.entity.Yeast

@Database(
    entities = [
        Equipment::class,
        WaterProfile::class,
        BeerStyle::class,
        Malt::class,
        Hop::class,
        Yeast::class,
        Additive::class,
        Recipe::class,
        RecipeMalt::class,
        RecipeHop::class,
        RecipeYeast::class,
        RecipeAdditive::class,
        MashStep::class,
        FermentationStep::class,
        BrewSession::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun equipmentDao(): EquipmentDao
    abstract fun waterProfileDao(): WaterProfileDao
    abstract fun beerStyleDao(): BeerStyleDao
    abstract fun maltDao(): MaltDao
    abstract fun hopDao(): HopDao
    abstract fun yeastDao(): YeastDao
    abstract fun additiveDao(): AdditiveDao
    abstract fun recipeDao(): RecipeDao
    abstract fun brewSessionDao(): BrewSessionDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "beer-recipe.db",
            )
                // Les clés étrangères pilotent la suppression en cascade des lignes de recette.
                .build()
                .also { instance = it }
        }
    }
}
