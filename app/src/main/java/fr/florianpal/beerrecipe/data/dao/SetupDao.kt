package fr.florianpal.beerrecipe.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import fr.florianpal.beerrecipe.data.entity.BeerStyle
import fr.florianpal.beerrecipe.data.entity.Equipment
import fr.florianpal.beerrecipe.data.entity.WaterProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipmentDao {
    @Query("SELECT * FROM equipment ORDER BY isDefault DESC, name COLLATE NOCASE")
    fun observeAll(): Flow<List<Equipment>>

    @Query("SELECT * FROM equipment WHERE id = :id")
    fun observeById(id: Long): Flow<Equipment?>

    @Query("SELECT * FROM equipment WHERE id = :id")
    suspend fun byId(id: Long): Equipment?

    @Query("SELECT * FROM equipment ORDER BY isDefault DESC, id LIMIT 1")
    suspend fun firstOrDefault(): Equipment?

    @Query("SELECT COUNT(*) FROM equipment")
    suspend fun count(): Int

    @Query("UPDATE equipment SET isDefault = 0")
    suspend fun clearDefault()

    @Upsert suspend fun upsert(equipment: Equipment): Long
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertAll(items: List<Equipment>)
    @Delete suspend fun delete(equipment: Equipment)
}

@Dao
interface WaterProfileDao {
    @Query("SELECT * FROM water_profile ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<WaterProfile>>

    @Query("SELECT * FROM water_profile WHERE id = :id")
    suspend fun byId(id: Long): WaterProfile?

    @Query("SELECT COUNT(*) FROM water_profile")
    suspend fun count(): Int

    @Upsert suspend fun upsert(profile: WaterProfile): Long
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertAll(items: List<WaterProfile>)
    @Delete suspend fun delete(profile: WaterProfile)
}

@Dao
interface BeerStyleDao {
    @Query("SELECT * FROM beer_style ORDER BY code COLLATE NOCASE")
    fun observeAll(): Flow<List<BeerStyle>>

    @Query("SELECT * FROM beer_style WHERE id = :id")
    suspend fun byId(id: Long): BeerStyle?

    @Query("SELECT COUNT(*) FROM beer_style")
    suspend fun count(): Int

    @Query("SELECT name FROM beer_style")
    suspend fun names(): List<String>

    @Query("SELECT code FROM beer_style")
    suspend fun codes(): List<String>

    @Upsert suspend fun upsert(style: BeerStyle): Long
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertAll(items: List<BeerStyle>)
    @Delete suspend fun delete(style: BeerStyle)
}
