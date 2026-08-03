package com.example.data.repository

import com.example.data.db.GameDao
import com.example.data.db.GarageEntity
import com.example.data.db.ScoreEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepository(private val gameDao: GameDao) {
    val highScores: Flow<List<ScoreEntity>> = gameDao.getHighScores()
    val topScore: Flow<Int> = gameDao.getTopScore().map { it ?: 0 }
    val garage: Flow<GarageEntity> = gameDao.getGarage().map { it ?: GarageEntity() }

    suspend fun saveGameRun(
        score: Int,
        distanceKm: Float,
        topSpeedKmh: Int,
        coinsEarned: Int,
        survivalTimeSec: Int,
        vehicleId: String,
        userEmail: String? = null,
        userName: String? = null
    ) {
        val scoreEntity = ScoreEntity(
            score = score,
            distanceKm = distanceKm,
            topSpeedKmh = topSpeedKmh,
            coinsEarned = coinsEarned,
            survivalTimeSec = survivalTimeSec,
            vehicleId = vehicleId,
            userEmail = userEmail,
            userName = userName
        )
        gameDao.insertScore(scoreEntity)
    }

    suspend fun updateGarageData(garage: GarageEntity) {
        gameDao.updateGarage(garage)
    }

    suspend fun addCoins(coins: Int, currentGarage: GarageEntity) {
        val updated = currentGarage.copy(coins = currentGarage.coins + coins)
        gameDao.updateGarage(updated)
    }
}
