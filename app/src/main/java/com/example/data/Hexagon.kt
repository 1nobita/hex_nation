package com.example.data

import androidx.room.Entity

@Entity(tableName = "hexagons", primaryKeys = ["nationId", "q", "r"])
data class Hexagon(
    val nationId: String,
    val q: Int,
    val r: Int,
    val color: Long,
    val ownerId: String,
    val numFlips: Int,
    val lastPurchaseTimestamp: Long
)
