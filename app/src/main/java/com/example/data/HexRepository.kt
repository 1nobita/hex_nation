package com.example.data

import kotlinx.coroutines.flow.Flow

class HexRepository(private val db: AppDatabase) {
    fun getHexagonsForNation(nationId: String): Flow<List<Hexagon>> =
        db.hexagonDao().getHexagonsForNation(nationId)
        
    suspend fun insertHexagon(hexagon: Hexagon) = db.hexagonDao().insertHexagon(hexagon)
    
    suspend fun getHexagon(nationId: String, q: Int, r: Int): Hexagon? = 
        db.hexagonDao().getHexagon(nationId, q, r)

    fun getUserProfile(userId: String): Flow<UserProfile?> = db.hexagonDao().getUserProfile(userId)
    
    suspend fun insertUserProfile(userProfile: UserProfile) = db.hexagonDao().insertUserProfile(userProfile)

    fun getOwnedHexagons(ownerId: String): Flow<List<Hexagon>> = db.hexagonDao().getOwnedHexagons(ownerId)

    fun getHexOwnershipLogs(nationId: String, q: Int, r: Int): Flow<List<HexOwnershipLog>> = 
        db.hexagonDao().getHexOwnershipLogs(nationId, q, r)

    suspend fun insertHexOwnershipLog(log: HexOwnershipLog) = db.hexagonDao().insertHexOwnershipLog(log)
}
