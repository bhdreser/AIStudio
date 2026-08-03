package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM game_stats")
    fun getAllGameStats(): Flow<List<GameStat>>

    @Query("SELECT * FROM game_stats WHERE gameId = :gameId")
    fun getGameStat(gameId: String): Flow<GameStat?>

    @Query("SELECT * FROM game_stats WHERE gameId = :gameId")
    suspend fun getGameStatSync(gameId: String): GameStat?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateGameStat(stat: GameStat)

    @Query("SELECT * FROM user_wallet WHERE id = 1")
    fun getUserWallet(): Flow<UserWallet?>

    @Query("SELECT * FROM user_wallet WHERE id = 1")
    suspend fun getUserWalletSync(): UserWallet?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWallet(wallet: UserWallet)

    @Query("SELECT * FROM daily_missions WHERE dateString = :dateString")
    fun getDailyMissionsFlow(dateString: String): Flow<List<DailyMissionEntity>>

    @Query("SELECT * FROM daily_missions WHERE dateString = :dateString")
    suspend fun getDailyMissionsSync(dateString: String): List<DailyMissionEntity>

    @Query("SELECT * FROM daily_missions WHERE id = :id")
    suspend fun getDailyMissionSync(id: String): DailyMissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMissions(missions: List<DailyMissionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMission(mission: DailyMissionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameHistory(record: GameHistoryRecord)

    @Query("SELECT * FROM game_history ORDER BY timestamp DESC")
    fun getAllGameHistory(): Flow<List<GameHistoryRecord>>

    @Query("SELECT * FROM game_history WHERE userEmail = :email ORDER BY timestamp DESC")
    fun getGameHistoryForUser(email: String): Flow<List<GameHistoryRecord>>
}

