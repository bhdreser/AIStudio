package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_stats")
data class GameStat(
    @PrimaryKey val gameId: String,
    val gameName: String,
    val highScore: Int = 0,
    val totalCoinsEarned: Int = 0,
    val gamesPlayed: Int = 0,
    val currentLevel: Int = 1,
    val lastPlayedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_wallet")
data class UserWallet(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 500,
    val equippedBlockSkin: String = "classic",
    val equippedBladeSkin: String = "katana",
    val equippedHatSkin: String = "chef",
    val equippedRunnerSkin: String = "hero"
)

@Entity(tableName = "daily_missions")
data class DailyMissionEntity(
    @PrimaryKey val id: String,
    val dateString: String,
    val titleTr: String,
    val titleEn: String,
    val targetGameId: String,
    val missionType: String, // "SCORE", "PLAY", "COINS"
    val targetValue: Int,
    val currentValue: Int = 0,
    val rewardCoins: Int,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false
)

@Entity(tableName = "game_history")
data class GameHistoryRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val gameId: String,
    val gameName: String,
    val score: Int,
    val coinsEarned: Int,
    val timestamp: Long = System.currentTimeMillis()
)

