package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepository(private val gameDao: GameDao) {

    val allGameStats: Flow<List<GameStat>> = gameDao.getAllGameStats()
    val wallet: Flow<UserWallet> = gameDao.getUserWallet().map { it ?: UserWallet() }
    val allGameHistory: Flow<List<GameHistoryRecord>> = gameDao.getAllGameHistory()

    fun getDailyMissionsFlow(): Flow<List<DailyMissionEntity>> {
        val today = DailyMissionGenerator.getTodayDateString()
        return gameDao.getDailyMissionsFlow(today)
    }

    suspend fun ensureTodayMissions(): List<DailyMissionEntity> {
        val today = DailyMissionGenerator.getTodayDateString()
        var missions = gameDao.getDailyMissionsSync(today)
        if (missions.isEmpty()) {
            missions = DailyMissionGenerator.generateMissionsForDate(today)
            gameDao.insertOrUpdateMissions(missions)
        }
        return missions
    }

    suspend fun recordScore(gameId: String, gameName: String, score: Int, coinsEarned: Int, userEmail: String = "") {
        val existing = gameDao.getGameStatSync(gameId) ?: GameStat(gameId = gameId, gameName = gameName)
        val newHighScore = maxOf(existing.highScore, score)
        val updatedStat = existing.copy(
            highScore = newHighScore,
            totalCoinsEarned = existing.totalCoinsEarned + coinsEarned,
            gamesPlayed = existing.gamesPlayed + 1,
            lastPlayedTimestamp = System.currentTimeMillis()
        )
        gameDao.insertOrUpdateGameStat(updatedStat)

        val currentWallet = gameDao.getUserWalletSync() ?: UserWallet()
        gameDao.insertOrUpdateWallet(currentWallet.copy(coins = currentWallet.coins + coinsEarned))

        // Record individual game session history
        gameDao.insertGameHistory(
            GameHistoryRecord(
                userEmail = userEmail.ifEmpty { "Guest" },
                gameId = gameId,
                gameName = gameName,
                score = score,
                coinsEarned = coinsEarned
            )
        )

        // Update Daily Missions progress
        updateMissionProgress(gameId, score, coinsEarned)
    }

    private suspend fun updateMissionProgress(gameId: String, score: Int, coinsEarned: Int) {
        val missions = ensureTodayMissions()
        for (mission in missions) {
            if (mission.isCompleted) continue

            var newCurrentValue = mission.currentValue
            val targetGame = mission.targetGameId

            if (targetGame == "any" || targetGame == gameId) {
                when (mission.missionType) {
                    "SCORE" -> {
                        newCurrentValue = maxOf(newCurrentValue, score)
                    }
                    "PLAY" -> {
                        newCurrentValue += 1
                    }
                    "COINS" -> {
                        newCurrentValue += coinsEarned
                    }
                }
            }

            if (newCurrentValue != mission.currentValue) {
                val isCompletedNow = newCurrentValue >= mission.targetValue
                val updatedMission = mission.copy(
                    currentValue = newCurrentValue,
                    isCompleted = isCompletedNow
                )
                gameDao.insertOrUpdateMission(updatedMission)
            }
        }
    }

    suspend fun claimMissionReward(missionId: String): Boolean {
        val mission = gameDao.getDailyMissionSync(missionId) ?: return false
        if (mission.isCompleted && !mission.isClaimed) {
            val updatedMission = mission.copy(isClaimed = true)
            gameDao.insertOrUpdateMission(updatedMission)

            val currentWallet = gameDao.getUserWalletSync() ?: UserWallet()
            gameDao.insertOrUpdateWallet(currentWallet.copy(coins = currentWallet.coins + mission.rewardCoins))
            return true
        }
        return false
    }

    suspend fun advanceLevel(gameId: String, gameName: String) {
        val existing = gameDao.getGameStatSync(gameId) ?: GameStat(gameId = gameId, gameName = gameName)
        val updatedStat = existing.copy(
            currentLevel = existing.currentLevel + 1
        )
        gameDao.insertOrUpdateGameStat(updatedStat)
    }

    suspend fun spendCoins(amount: Int): Boolean {
        val currentWallet = gameDao.getUserWalletSync() ?: UserWallet()
        if (currentWallet.coins >= amount) {
            gameDao.insertOrUpdateWallet(currentWallet.copy(coins = currentWallet.coins - amount))
            return true
        }
        return false
    }

    suspend fun equipSkin(type: String, skinId: String) {
        val currentWallet = gameDao.getUserWalletSync() ?: UserWallet()
        val updated = when (type) {
            "block" -> currentWallet.copy(equippedBlockSkin = skinId)
            "blade" -> currentWallet.copy(equippedBladeSkin = skinId)
            "hat" -> currentWallet.copy(equippedHatSkin = skinId)
            "runner" -> currentWallet.copy(equippedRunnerSkin = skinId)
            else -> currentWallet
        }
        gameDao.insertOrUpdateWallet(updated)
    }
}

