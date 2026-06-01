package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "hex_ownership_logs",
    indices = [
        Index("nationId", "q", "r")
    ]
)
data class HexOwnershipLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nationId: String,
    val q: Int,
    val r: Int,
    val ownerId: String,
    val acquiredAt: Long,
    val durationSeconds: Long = 0L
)
