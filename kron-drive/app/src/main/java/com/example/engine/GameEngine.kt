package com.example.engine

import androidx.compose.ui.graphics.Color
import com.example.data.db.GarageEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Random

class GameEngine {
    private val random = Random()

    // Engine States
    val playerX = MutableStateFlow(0.0f) // -0.65 (left edge) to 0.65 (right edge)
    val targetPlayerX = MutableStateFlow(0.0f)
    val currentSpeedKmh = MutableStateFlow(81f)
    val currentTorqueNm = MutableStateFlow(387)
    val boostEnergy = MutableStateFlow(100f) // 0 to 100%
    val batteryLevel = MutableStateFlow(100f) // 0 to 100%
    val score = MutableStateFlow(28)
    val timeSurvivingSec = MutableStateFlow(40)
    val distanceKm = MutableStateFlow(3.4f)
    val coinsEarned = MutableStateFlow(12)
    val topSpeedReached = MutableStateFlow(81)

    // Control Inputs
    var isSteeringLeft = false
    var isSteeringRight = false
    var isBraking = false
    var isBoosting = false

    // Game Objects
    val trafficCars = MutableStateFlow<List<TrafficCar>>(emptyList())
    val pickups = MutableStateFlow<List<PickupItem>>(emptyList())
    val particles = MutableStateFlow<List<Particle>>(emptyList())

    // Game Environment
    val roadScrollOffset = MutableStateFlow(0f)
    var activeVehicle: VehicleSpec = VehicleSpec.VEHICLES[0]
    var activeGarage: GarageEntity = GarageEntity()
    var isShieldActive = false
    var shieldTimeLeft = 0f

    // Callback on Crash / Game Over
    var onCrashCallback: (() -> Unit)? = null
    var onPickupCallback: ((PickupType) -> Unit)? = null

    private var spawnTimer = 0f
    private var pickupSpawnTimer = 0f
    private var timeAccumulator = 0f
    private var isGameOverInternal = false

    fun initNewGame(vehicle: VehicleSpec, garage: GarageEntity, mode: GameMode) {
        activeVehicle = vehicle
        activeGarage = garage
        playerX.value = 0.0f
        targetPlayerX.value = 0.0f

        val topSpeedBonus = garage.topSpeedLevel * 10
        val baseSpeed = (vehicle.baseMaxSpeed * 0.5f) + topSpeedBonus * 0.2f
        currentSpeedKmh.value = baseSpeed
        currentTorqueNm.value = (vehicle.baseTorque + (baseSpeed * 1.5f)).toInt()

        boostEnergy.value = 100f
        batteryLevel.value = 100f
        score.value = 0
        timeSurvivingSec.value = 0
        distanceKm.value = 0.0f
        coinsEarned.value = 0
        topSpeedReached.value = baseSpeed.toInt()

        trafficCars.value = emptyList()
        pickups.value = emptyList()
        particles.value = emptyList()

        isShieldActive = false
        shieldTimeLeft = 0f
        spawnTimer = 0f
        pickupSpawnTimer = 0f
        timeAccumulator = 0f
        isGameOverInternal = false
    }

    fun tick(dt: Float) {
        if (isGameOverInternal) return

        // 1. Time Accumulator & Seconds
        timeAccumulator += dt
        if (timeAccumulator >= 1.0f) {
            timeAccumulator -= 1.0f
            timeSurvivingSec.value += 1
            // Natural score gain by distance
            score.value += (currentSpeedKmh.value / 15f).toInt()
        }

        // 2. Shield countdown
        if (isShieldActive) {
            shieldTimeLeft -= dt
            if (shieldTimeLeft <= 0f) {
                isShieldActive = false
            }
        }

        // 3. Speed & Torque Physics
        val topSpeedMax = activeVehicle.baseMaxSpeed + (activeGarage.topSpeedLevel * 12)
        var targetSpeed = 70f + (timeSurvivingSec.value * 0.5f) // Gradually speeds up overtime

        if (isBoosting && boostEnergy.value > 1.0f) {
            targetSpeed += 65f + (activeGarage.boostCapacityLevel * 5)
            boostEnergy.value = (boostEnergy.value - dt * 25f).coerceAtLeast(0f)
            spawnBoostParticles()
        } else if (isBoosting && boostEnergy.value <= 1.0f) {
            isBoosting = false
        } else {
            // Natural boost recharge
            boostEnergy.value = (boostEnergy.value + dt * 4f).coerceAtMost(100f)
        }

        if (isBraking) {
            targetSpeed -= 45f
            spawnBrakeSparks()
        }

        targetSpeed = targetSpeed.coerceIn(40f, topSpeedMax.toFloat())

        // Smooth speed lerp
        val currentS = currentSpeedKmh.value
        val newS = currentS + (targetSpeed - currentS) * (dt * 3.5f)
        currentSpeedKmh.value = newS

        if (newS.toInt() > topSpeedReached.value) {
            topSpeedReached.value = newS.toInt()
        }

        // Torque calculation: Torque = BaseTorque + (Speed * 1.8)
        val torqueVal = (activeVehicle.baseTorque + (newS * 1.8f)).toInt()
        currentTorqueNm.value = torqueVal

        // Distance updated
        val distanceDelta = (newS / 3600f) * dt
        distanceKm.value += distanceDelta

        // 4. Steering Physics (x: -0.65 left to +0.65 right)
        val steerSpeed = 1.8f + (activeVehicle.baseAcceleration * 0.3f)
        if (isSteeringLeft) {
            targetPlayerX.value = (targetPlayerX.value - dt * steerSpeed).coerceIn(-0.62f, 0.62f)
        }
        if (isSteeringRight) {
            targetPlayerX.value = (targetPlayerX.value + dt * steerSpeed).coerceIn(-0.62f, 0.62f)
        }

        // Lerp player actual position to target X
        val currX = playerX.value
        val nextX = currX + (targetPlayerX.value - currX) * (dt * 12f)
        playerX.value = nextX

        // Side kerb scrape check
        if (kotlin.math.abs(nextX) > 0.58f) {
            batteryLevel.value = (batteryLevel.value - dt * 15f).coerceAtLeast(0f)
            spawnSparks(nextX, 0.75f, 3)
            if (batteryLevel.value <= 0f && !isShieldActive) {
                triggerGameOver()
                return
            }
        }

        // 5. Road Scrolling
        val scrollSpeedFactor = newS / 80f
        roadScrollOffset.value = (roadScrollOffset.value + dt * 1.8f * scrollSpeedFactor) % 1.0f

        // 6. Spawn & Move Traffic
        spawnTimer += dt * (scrollSpeedFactor * 0.8f)
        if (spawnTimer >= 1.4f) {
            spawnTimer = 0f
            spawnTrafficCar()
        }

        val updatedTraffic = mutableListOf<TrafficCar>()
        val pX = playerX.value
        val pY = 0.75f // Player car fixed Y

        for (car in trafficCars.value) {
            // Speed relative to player: if car is slower than player, it moves down screen
            val relativeSpeed = (newS - car.speedKmh) / 100f
            car.yNormalized += (relativeSpeed + 0.35f) * dt * scrollSpeedFactor

            // Check collision with player
            val dx = kotlin.math.abs(car.xNormalized - pX)
            val dy = kotlin.math.abs(car.yNormalized - pY)

            if (dx < 0.16f && dy < 0.12f) {
                if (isShieldActive) {
                    // Destroy traffic car on shield collision
                    spawnExplosion(car.xNormalized, car.yNormalized)
                    score.value += 100
                } else {
                    // Collision damage or Game Over
                    batteryLevel.value = (batteryLevel.value - 40f).coerceAtLeast(0f)
                    spawnExplosion(car.xNormalized, car.yNormalized)

                    if (batteryLevel.value <= 0f) {
                        triggerGameOver()
                        return
                    } else {
                        // Knockback car
                        car.yNormalized += 0.3f
                    }
                }
            } else if (car.yNormalized < 1.3f) {
                updatedTraffic.add(car)
            } else {
                // Successfully passed car -> score overtake!
                score.value += 20
            }
        }
        trafficCars.value = updatedTraffic

        // 7. Spawn & Move Pickups
        pickupSpawnTimer += dt
        if (pickupSpawnTimer >= 2.2f) {
            pickupSpawnTimer = 0f
            spawnPickup()
        }

        val updatedPickups = mutableListOf<PickupItem>()
        for (pickup in pickups.value) {
            pickup.yNormalized += 0.55f * dt * scrollSpeedFactor

            val dx = kotlin.math.abs(pickup.xNormalized - pX)
            val dy = kotlin.math.abs(pickup.yNormalized - pY)

            if (!pickup.collected && dx < 0.18f && dy < 0.12f) {
                pickup.collected = true
                applyPickup(pickup.type)
                onPickupCallback?.invoke(pickup.type)
            } else if (pickup.yNormalized < 1.3f && !pickup.collected) {
                updatedPickups.add(pickup)
            }
        }
        pickups.value = updatedPickups

        // 8. Update Particles
        updateParticles(dt)
    }

    private fun spawnTrafficCar() {
        val lanes = listOf(-0.45f, -0.15f, 0.15f, 0.45f)
        val selectedLaneIdx = random.nextInt(lanes.size)
        val laneX = lanes[selectedLaneIdx]

        // Don't spawn if another car is right near top in same lane
        if (trafficCars.value.any { it.lane == selectedLaneIdx && it.yNormalized < 0.15f }) {
            return
        }

        val type = random.nextInt(4)
        val colors = listOf(
            Color(0xFFFFD54F), // Yellow Formula / GT
            Color(0xFF29B6F6), // Blue Sedan
            Color(0xFF81C784), // Green Racer
            Color(0xFFAB47BC)  // Purple Sport
        )
        val carSpeed = random.nextInt(30) + 40f // 40-70 km/h

        val newCar = TrafficCar(
            id = System.nanoTime(),
            xNormalized = laneX,
            yNormalized = -0.2f,
            lane = selectedLaneIdx,
            speedKmh = carSpeed,
            type = type,
            color = colors[type]
        )

        trafficCars.value = trafficCars.value + newCar
    }

    private fun spawnPickup() {
        val lanes = listOf(-0.45f, -0.15f, 0.15f, 0.45f)
        val laneX = lanes[random.nextInt(lanes.size)]

        val roll = random.nextInt(100)
        val type = when {
            roll < 55 -> PickupType.COIN
            roll < 80 -> PickupType.BOOST_ENERGY
            roll < 93 -> PickupType.BATTERY_CELL
            else -> PickupType.SHIELD
        }

        val pickup = PickupItem(
            id = System.nanoTime(),
            xNormalized = laneX,
            yNormalized = -0.15f,
            type = type
        )

        pickups.value = pickups.value + pickup
    }

    private fun applyPickup(type: PickupType) {
        when (type) {
            PickupType.COIN -> {
                coinsEarned.value += 1
                score.value += 50
                spawnSparks(playerX.value, 0.75f, 8, Color(0xFFFFD54F))
            }
            PickupType.BOOST_ENERGY -> {
                boostEnergy.value = (boostEnergy.value + 35f).coerceAtMost(100f)
                score.value += 30
                spawnSparks(playerX.value, 0.75f, 8, Color(0xFF00E5FF))
            }
            PickupType.BATTERY_CELL -> {
                batteryLevel.value = (batteryLevel.value + 30f).coerceAtMost(100f)
                score.value += 30
                spawnSparks(playerX.value, 0.75f, 8, Color(0xFF76FF03))
            }
            PickupType.SHIELD -> {
                isShieldActive = true
                shieldTimeLeft = 6.0f
                score.value += 100
                spawnSparks(playerX.value, 0.75f, 12, Color(0xFFE040FB))
            }
        }
    }

    private fun triggerGameOver() {
        if (isGameOverInternal) return
        isGameOverInternal = true
        onCrashCallback?.invoke()
    }

    private fun spawnBoostParticles() {
        val pX = playerX.value
        val pY = 0.82f // Exhaust position
        val pList = particles.value.toMutableList()

        for (i in 0..2) {
            pList.add(
                Particle(
                    x = pX + (random.nextFloat() * 0.04f - 0.02f),
                    y = pY,
                    vx = (random.nextFloat() * 0.06f - 0.03f),
                    vy = 0.4f + random.nextFloat() * 0.3f,
                    color = if (random.nextBoolean()) Color(0xFF00E5FF) else Color(0xFFFF9100),
                    alpha = 0.9f,
                    size = random.nextFloat() * 12f + 8f,
                    life = 1.0f,
                    maxLife = 0.25f
                )
            )
        }
        particles.value = pList.takeLast(60)
    }

    private fun spawnBrakeSparks() {
        val pX = playerX.value
        val pY = 0.80f
        val pList = particles.value.toMutableList()

        for (i in 0..1) {
            pList.add(
                Particle(
                    x = pX + (if (i == 0) -0.06f else 0.06f),
                    y = pY,
                    vx = (random.nextFloat() * 0.1f - 0.05f),
                    vy = -0.1f - random.nextFloat() * 0.2f,
                    color = Color(0xFFFF3D00),
                    alpha = 0.8f,
                    size = random.nextFloat() * 8f + 4f,
                    life = 1.0f,
                    maxLife = 0.2f
                )
            )
        }
        particles.value = pList.takeLast(60)
    }

    private fun spawnSparks(x: Float, y: Float, count: Int, customColor: Color? = null) {
        val pList = particles.value.toMutableList()
        for (i in 0 until count) {
            pList.add(
                Particle(
                    x = x + (random.nextFloat() * 0.08f - 0.04f),
                    y = y + (random.nextFloat() * 0.08f - 0.04f),
                    vx = (random.nextFloat() * 0.4f - 0.2f),
                    vy = (random.nextFloat() * 0.4f - 0.2f),
                    color = customColor ?: Color(0xFFFFD54F),
                    alpha = 1.0f,
                    size = random.nextFloat() * 10f + 6f,
                    life = 1.0f,
                    maxLife = 0.35f
                )
            )
        }
        particles.value = pList.takeLast(60)
    }

    private fun spawnExplosion(x: Float, y: Float) {
        val pList = particles.value.toMutableList()
        for (i in 0..25) {
            pList.add(
                Particle(
                    x = x,
                    y = y,
                    vx = (random.nextFloat() * 0.8f - 0.4f),
                    vy = (random.nextFloat() * 0.8f - 0.4f),
                    color = if (i % 2 == 0) Color(0xFFFF1744) else Color(0xFFFFC400),
                    alpha = 1.0f,
                    size = random.nextFloat() * 18f + 10f,
                    life = 1.0f,
                    maxLife = 0.6f
                )
            )
        }
        particles.value = pList.takeLast(80)
    }

    private fun updateParticles(dt: Float) {
        val active = mutableListOf<Particle>()
        for (p in particles.value) {
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.life -= dt / p.maxLife
            p.alpha = (p.life).coerceIn(0f, 1f)

            if (p.life > 0f) {
                active.add(p)
            }
        }
        particles.value = active
    }
}
