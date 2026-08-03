package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM game_scores ORDER BY score DESC LIMIT 20")
    fun getHighScores(): Flow<List<ScoreEntity>>

    @Query("SELECT MAX(score) FROM game_scores")
    fun getTopScore(): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: ScoreEntity)

    @Query("SELECT * FROM user_garage WHERE id = 1")
    fun getGarage(): Flow<GarageEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateGarage(garage: GarageEntity)
}
