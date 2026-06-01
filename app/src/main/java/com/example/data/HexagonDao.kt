package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HexagonDao {
    @Query("SELECT * FROM hexagons WHERE nationId = :nationId")
    fun getHexagonsForNation(nationId: String): Flow<List<Hexagon>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHexagon(hexagon: Hexagon)
    
    @Query("SELECT * FROM hexagons WHERE nationId = :nationId AND q = :q AND r = :r LIMIT 1")
    suspend fun getHexagon(nationId: String, q: Int, r: Int): Hexagon?
}
