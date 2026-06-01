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

    // UserProfile queries
    @Query("SELECT * FROM user_profiles WHERE userId = :userId LIMIT 1")
    fun getUserProfile(userId: String): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfile: UserProfile)

    // Owned Hexagons query
    @Query("SELECT * FROM hexagons WHERE ownerId = :ownerId")
    fun getOwnedHexagons(ownerId: String): Flow<List<Hexagon>>

    // Ownership logs query
    @Query("SELECT * FROM hex_ownership_logs WHERE nationId = :nationId AND q = :q AND r = :r ORDER BY acquiredAt DESC")
    fun getHexOwnershipLogs(nationId: String, q: Int, r: Int): Flow<List<HexOwnershipLog>>

    @Insert
    suspend fun insertHexOwnershipLog(log: HexOwnershipLog)
}
