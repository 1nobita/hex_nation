package com.example.data

import kotlinx.coroutines.flow.Flow

class HexRepository(private val db: AppDatabase) {
    fun getHexagonsForNation(nationId: String): Flow<List<Hexagon>> =
        db.hexagonDao().getHexagonsForNation(nationId)
        
    suspend fun insertHexagon(hexagon: Hexagon) = db.hexagonDao().insertHexagon(hexagon)
    
    suspend fun getHexagon(nationId: String, q: Int, r: Int): Hexagon? = 
        db.hexagonDao().getHexagon(nationId, q, r)
}
