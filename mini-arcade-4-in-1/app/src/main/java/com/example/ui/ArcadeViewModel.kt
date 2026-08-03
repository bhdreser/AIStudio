package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DailyMissionEntity
import com.example.data.GameHistoryRecord
import com.example.data.GameRepository
import com.example.data.GameStat
import com.example.data.UserSettings
import com.example.data.UserSettingsRepository
import com.example.data.UserWallet
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ArcadeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository
    private val settingsRepository: UserSettingsRepository

    val gameStats: StateFlow<List<GameStat>>
    val wallet: StateFlow<UserWallet>
    val userSettings: StateFlow<UserSettings>
    val dailyMissions: StateFlow<List<DailyMissionEntity>>
    val gameHistory: StateFlow<List<GameHistoryRecord>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = GameRepository(db.gameDao())
        settingsRepository = UserSettingsRepository(application)

        gameStats = repository.allGameStats.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        wallet = repository.wallet.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserWallet()
        )

        userSettings = settingsRepository.userSettings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings()
        )

        dailyMissions = repository.getDailyMissionsFlow().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        gameHistory = repository.allGameHistory.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            repository.ensureTodayMissions()
        }
    }

    fun claimMissionReward(missionId: String, onClaimed: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.claimMissionReward(missionId)
            onClaimed(success)
        }
    }


    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSoundEnabled(enabled)
        }
    }

    fun setMusicEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setMusicEnabled(enabled)
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkMode(enabled)
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            settingsRepository.setLanguage(language)
        }
    }

    fun setBlockTheme(theme: String) {
        viewModelScope.launch {
            settingsRepository.setBlockTheme(theme)
        }
    }

    fun loginGoogle(email: String, name: String) {
        viewModelScope.launch {
            settingsRepository.loginGoogle(email = email, name = name)
        }
    }

    fun logoutGoogle() {
        viewModelScope.launch {
            settingsRepository.logoutGoogle()
        }
    }

    fun recordGameScore(gameId: String, gameName: String, score: Int, coinsEarned: Int) {
        viewModelScope.launch {
            val email = userSettings.value.googleEmail
            repository.recordScore(gameId, gameName, score, coinsEarned, userEmail = email)
        }
    }

    fun advanceGameLevel(gameId: String, gameName: String) {
        viewModelScope.launch {
            repository.advanceLevel(gameId, gameName)
        }
    }

    fun buySkin(type: String, skinId: String, cost: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.spendCoins(cost)
            if (success) {
                repository.equipSkin(type, skinId)
            }
            onResult(success)
        }
    }

    fun equipSkin(type: String, skinId: String) {
        viewModelScope.launch {
            repository.equipSkin(type, skinId)
        }
    }
}

