package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.example.data.auth.GoogleAuthManager
import com.example.data.auth.GoogleUser
import com.example.data.db.AppDatabase
import com.example.data.db.GarageEntity
import com.example.data.db.ScoreEntity
import com.example.data.repository.GameRepository
import com.example.engine.GameEngine
import com.example.engine.GameMode
import com.example.engine.GameState
import com.example.engine.PickupType
import com.example.engine.SoundManager
import com.example.engine.VehicleSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val repository = GameRepository(database.gameDao())
    val soundManager = SoundManager(application)
    val authManager = GoogleAuthManager(application)

    val googleUser: StateFlow<GoogleUser?> = authManager.currentUser
    val gameEngine = GameEngine()

    // UI States
    private val _gameState = MutableStateFlow(GameState.MENU)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _selectedMode = MutableStateFlow(GameMode.ENDLESS)
    val selectedMode: StateFlow<GameMode> = _selectedMode.asStateFlow()

    val garageData: StateFlow<GarageEntity> = repository.garage
        .stateInScope(GarageEntity())

    val highScoresList: StateFlow<List<ScoreEntity>> = repository.highScores
        .stateInScope(emptyList())

    val topScore: StateFlow<Int> = repository.topScore
        .stateInScope(0)

    private var gameLoopJob: Job? = null

    init {
        gameEngine.onCrashCallback = {
            soundManager.playCrashSound()
            onGameOverTriggered()
        }

        gameEngine.onPickupCallback = { type ->
            when (type) {
                PickupType.COIN -> soundManager.playCoinSound()
                PickupType.BOOST_ENERGY -> soundManager.playBoostSound()
                PickupType.BATTERY_CELL -> soundManager.playBoostSound()
                PickupType.SHIELD -> soundManager.playBoostSound()
            }
        }
    }

    fun startGame(mode: GameMode = GameMode.ENDLESS) {
        _selectedMode.value = mode
        viewModelScope.launch {
            val garage = repository.garage.first()
            val vehicle = VehicleSpec.VEHICLES.find { it.id == garage.selectedVehicleId }
                ?: VehicleSpec.VEHICLES[0]

            gameEngine.initNewGame(vehicle, garage, mode)
            _gameState.value = GameState.PLAYING

            startGameLoop()
        }
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch(Dispatchers.Default) {
            var lastTimeNanos = System.nanoTime()
            while (_gameState.value == GameState.PLAYING) {
                val nowNanos = System.nanoTime()
                val dt = ((nowNanos - lastTimeNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastTimeNanos = nowNanos

                gameEngine.tick(dt)

                // Dynamic F1 Engine Pitch update
                val maxSpeed = (gameEngine.activeVehicle.baseMaxSpeed + gameEngine.activeGarage.topSpeedLevel * 10).toFloat()
                val speedRatio = (gameEngine.currentSpeedKmh.value / maxSpeed).coerceIn(0f, 1.2f)
                val isBoosting = gameEngine.isBoosting
                soundManager.updateEnginePitch(speedRatio, isBoosting)

                delay(16) // ~60 FPS
            }
        }
    }

    fun pauseGame() {
        if (_gameState.value == GameState.PLAYING) {
            _gameState.value = GameState.PAUSED
            soundManager.stopEngineSound()
            gameLoopJob?.cancel()
        }
    }

    fun resumeGame() {
        if (_gameState.value == GameState.PAUSED) {
            _gameState.value = GameState.PLAYING
            startGameLoop()
        }
    }

    private fun onGameOverTriggered() {
        gameLoopJob?.cancel()
        soundManager.stopEngineSound()
        _gameState.value = GameState.GAME_OVER

        val score = gameEngine.score.value
        val dist = gameEngine.distanceKm.value
        val topSpeed = gameEngine.topSpeedReached.value
        val coins = gameEngine.coinsEarned.value
        val timeSec = gameEngine.timeSurvivingSec.value
        val vehicleId = gameEngine.activeVehicle.id

        val user = googleUser.value
        val userEmail = user?.email
        val userName = user?.displayName

        viewModelScope.launch {
            // Save run to DB with Google account tracking
            repository.saveGameRun(
                score = score,
                distanceKm = dist,
                topSpeedKmh = topSpeed,
                coinsEarned = coins,
                survivalTimeSec = timeSec,
                vehicleId = vehicleId,
                userEmail = userEmail,
                userName = userName
            )

            // Add coins earned to Garage
            val currentGarage = repository.garage.first()
            repository.addCoins(coins, currentGarage)
        }
    }

    fun signInWithGoogle(context: Context, email: String = "lorvexia@gmail.com", name: String = "KRON Racer") {
        viewModelScope.launch {
            authManager.signInWithGoogle(context, email, name)
            soundManager.playCoinSound()
        }
    }

    fun signOutGoogle() {
        authManager.signOut()
    }

    fun navigateTo(state: GameState) {
        gameLoopJob?.cancel()
        soundManager.stopEngineSound()
        _gameState.value = state
    }

    // Garage Operations
    fun selectVehicle(vehicleId: String) {
        viewModelScope.launch {
            val current = repository.garage.first()
            val updated = current.copy(selectedVehicleId = vehicleId)
            repository.updateGarageData(updated)
        }
    }

    fun unlockVehicle(vehicle: VehicleSpec) {
        viewModelScope.launch {
            val current = repository.garage.first()
            if (current.coins >= vehicle.unlockPrice) {
                val unlockedList = parseUnlockedList(current.unlockedVehiclesJson).toMutableList()
                if (!unlockedList.contains(vehicle.id)) {
                    unlockedList.add(vehicle.id)
                    val updated = current.copy(
                        coins = current.coins - vehicle.unlockPrice,
                        unlockedVehiclesJson = serializeUnlockedList(unlockedList),
                        selectedVehicleId = vehicle.id
                    )
                    repository.updateGarageData(updated)
                    soundManager.playCoinSound()
                }
            }
        }
    }

    fun upgradeStat(statName: String) {
        viewModelScope.launch {
            val current = repository.garage.first()
            val cost = 150
            if (current.coins >= cost) {
                val updated = when (statName) {
                    "topSpeed" -> current.copy(coins = current.coins - cost, topSpeedLevel = current.topSpeedLevel + 1)
                    "acceleration" -> current.copy(coins = current.coins - cost, accelerationLevel = current.accelerationLevel + 1)
                    "boostCapacity" -> current.copy(coins = current.coins - cost, boostCapacityLevel = current.boostCapacityLevel + 1)
                    "batteryEfficiency" -> current.copy(coins = current.coins - cost, batteryEfficiencyLevel = current.batteryEfficiencyLevel + 1)
                    else -> current
                }
                repository.updateGarageData(updated)
                soundManager.playCoinSound()
            }
        }
    }

    private fun parseUnlockedList(json: String): List<String> {
        return json.replace("[", "").replace("]", "").replace("\"", "").split(",")
            .map { it.trim() }.filter { it.isNotEmpty() }
    }

    private fun serializeUnlockedList(list: List<String>): String {
        return "[\"" + list.joinToString("\",\"") + "\"]"
    }

    private fun <T> kotlinx.coroutines.flow.Flow<T>.stateInScope(initialValue: T): StateFlow<T> {
        val flow = MutableStateFlow(initialValue)
        viewModelScope.launch {
            collect { flow.value = it }
        }
        return flow.asStateFlow()
    }
}
