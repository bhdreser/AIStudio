package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_scores")
data class ScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val score: Int,
    val distanceKm: Float,
    val topSpeedKmh: Int,
    val coinsEarned: Int,
    val survivalTimeSec: Int,
    val vehicleId: String,
    val userEmail: String? = null,
    val userName: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
