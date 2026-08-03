package com.example.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

object DailyMissionGenerator {

    private data class MissionTemplate(
        val titleTr: String,
        val titleEn: String,
        val targetGameId: String,
        val missionType: String, // "SCORE", "PLAY", "COINS"
        val targetValue: Int,
        val rewardCoins: Int
    )

    private val MISSION_POOL = listOf(
        MissionTemplate("Block Blast'ta 300 Puan Yap", "Score 300 in Block Blast", "block_blast", "SCORE", 300, 100),
        MissionTemplate("Block Blast'ta 500 Puan Yap", "Score 500 in Block Blast", "block_blast", "SCORE", 500, 150),
        MissionTemplate("Slice It All'da 200 Puan Yap", "Score 200 in Slice It All", "slice_it", "SCORE", 200, 100),
        MissionTemplate("Slice It All'da 2 Oyun Oyna", "Play 2 Games in Slice It All", "slice_it", "PLAY", 2, 80),
        MissionTemplate("Pizza Ready'de 50 Coin Kazan", "Earn 50 Coins in Pizza Ready", "pizza_ready", "COINS", 50, 120),
        MissionTemplate("Pizza Ready'de Seviye Atla", "Advance Level in Pizza Ready", "pizza_ready", "PLAY", 1, 100),
        MissionTemplate("Fun Race 3D'de 200 Puan Yap", "Score 200 in Fun Race 3D", "fun_race", "SCORE", 200, 100),
        MissionTemplate("Fun Race 3D'de 2 Oyun Oyna", "Play 2 Games in Fun Race 3D", "fun_race", "PLAY", 2, 80),
        MissionTemplate("Herhangi Bir Oyunda 500 Puan Yap", "Score 500 in Any Game", "any", "SCORE", 500, 150),
        MissionTemplate("Toplam 3 Oyun Oyna", "Play 3 Games Total", "any", "PLAY", 3, 100),
        MissionTemplate("Toplam 100 Coin Kazan", "Earn 100 Coins Total", "any", "COINS", 100, 150)
    )

    fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun generateMissionsForDate(dateString: String): List<DailyMissionEntity> {
        val seed = dateString.hashCode()
        val random = Random(seed)
        val shuffled = MISSION_POOL.shuffled(random)
        val selected = shuffled.take(3)

        return selected.mapIndexed { index, template ->
            DailyMissionEntity(
                id = "${dateString}_m${index + 1}",
                dateString = dateString,
                titleTr = template.titleTr,
                titleEn = template.titleEn,
                targetGameId = template.targetGameId,
                missionType = template.missionType,
                targetValue = template.targetValue,
                currentValue = 0,
                rewardCoins = template.rewardCoins,
                isCompleted = false,
                isClaimed = false
            )
        }
    }
}
