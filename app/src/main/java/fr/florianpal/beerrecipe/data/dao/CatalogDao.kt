package fr.florianpal.beerrecipe.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import fr.florianpal.beerrecipe.data.entity.Additive
import fr.florianpal.beerrecipe.data.entity.Hop
import fr.florianpal.beerrecipe.data.entity.Malt
import fr.florianpal.beerrecipe.data.entity.Yeast
import kotlinx.coroutines.flow.Flow

@Dao
interface MaltDao {
    @Query("SELECT * FROM malt ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<Malt>>

    @Query("SELECT * FROM malt WHERE id = :id")
    suspend fun byId(id: Long): Malt?

    @Query("SELECT COUNT(*) FROM malt")
    suspend fun count(): Int

    @Query("SELECT name FROM malt")
    suspend fun names(): List<String>

    @Upsert suspend fun upsert(malt: Malt): Long
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertAll(malts: List<Malt>)
    @Delete suspend fun delete(malt: Malt)
}

@Dao
interface HopDao {
    @Query("SELECT * FROM hop ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<Hop>>

    @Query("SELECT * FROM hop WHERE id = :id")
    suspend fun byId(id: Long): Hop?

    @Query("SELECT COUNT(*) FROM hop")
    suspend fun count(): Int

    @Query("SELECT name FROM hop")
    suspend fun names(): List<String>

    @Upsert suspend fun upsert(hop: Hop): Long
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertAll(hops: List<Hop>)
    @Delete suspend fun delete(hop: Hop)
}

@Dao
interface YeastDao {
    @Query("SELECT * FROM yeast ORDER BY laboratory COLLATE NOCASE, name COLLATE NOCASE")
    fun observeAll(): Flow<List<Yeast>>

    @Query("SELECT * FROM yeast WHERE id = :id")
    suspend fun byId(id: Long): Yeast?

    @Query("SELECT COUNT(*) FROM yeast")
    suspend fun count(): Int

    @Query("SELECT name FROM yeast")
    suspend fun names(): List<String>

    @Upsert suspend fun upsert(yeast: Yeast): Long
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertAll(yeasts: List<Yeast>)
    @Delete suspend fun delete(yeast: Yeast)
}

@Dao
interface AdditiveDao {
    @Query("SELECT * FROM additive ORDER BY type, name COLLATE NOCASE")
    fun observeAll(): Flow<List<Additive>>

    @Query("SELECT * FROM additive WHERE id = :id")
    suspend fun byId(id: Long): Additive?

    @Query("SELECT COUNT(*) FROM additive")
    suspend fun count(): Int

    @Upsert suspend fun upsert(additive: Additive): Long
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertAll(additives: List<Additive>)
    @Delete suspend fun delete(additive: Additive)
}
