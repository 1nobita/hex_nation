package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey
    val userId: String,
    val placeOfOrigin: String = "Unknown",
    val bio: String = "",
    val profilePictureUri: String = ""
)
