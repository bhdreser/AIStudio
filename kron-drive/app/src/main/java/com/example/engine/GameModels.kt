package com.example.engine

import androidx.compose.ui.graphics.Color

enum class GameState {
    MENU,
    PLAYING,
    PAUSED,
    GAME_OVER,
    GARAGE,
    SCORES,
    SETTINGS
}

enum class GameMode {
    ENDLESS,
    TIME_ATTACK
}

enum class PickupType {
    COIN,
    BOOST_ENERGY,
    BATTERY_CELL,
    SHIELD
}

data class VehicleSpec(
    val id: String,
    val name: String,
    val subtitle: String,
    val primaryColor: Color,
    val accentColor: Color,
    val baseMaxSpeed: Int, // e.g. 140
    val baseTorque: Int,   // e.g. 320
    val baseAcceleration: Float, // e.g. 1.2f
    val baseBatteryCapacity: Float, // 100f
    val unlockPrice: Int,
    val description: String
) {
    companion object {
        val VEHICLES = listOf(
            VehicleSpec(
                id = "kron_alpha",
                name = "KRON Alpha",
                subtitle = "Formula Red Spec",
                primaryColor = Color(0xFFE53935), // Bright Formula Red
                accentColor = Color(0xFFFFFFFF),  // Pure White
                baseMaxSpeed = 150,
                baseTorque = 360,
                baseAcceleration = 1.2f,
                baseBatteryCapacity = 100f,
                unlockPrice = 0,
                description = "Classic open-wheel Formula racer. Pure speed and agile cornering."
            ),
            VehicleSpec(
                id = "kron_thunder",
                name = "KRON Thunder",
                subtitle = "Yellow Sprint Spec",
                primaryColor = Color(0xFFFACC15), // F1 Yellow
                accentColor = Color(0xFFFFFFFF),  // White
                baseMaxSpeed = 175,
                baseTorque = 420,
                baseAcceleration = 1.5f,
                baseBatteryCapacity = 110f,
                unlockPrice = 500,
                description = "High torque sprint monster with massive acceleration."
            ),
            VehicleSpec(
                id = "kron_cyber",
                name = "KRON Cyber",
                subtitle = "Electric Cyan F1",
                primaryColor = Color(0xFF38BDF8), // Electric Sky Cyan
                accentColor = Color(0xFFFFFFFF),  // White
                baseMaxSpeed = 200,
                baseTorque = 480,
                baseAcceleration = 1.8f,
                baseBatteryCapacity = 130f,
                unlockPrice = 1200,
                description = "Futuristic hypercar. Extreme top speed and battery efficiency."
            ),
            VehicleSpec(
                id = "kron_phantom",
                name = "KRON Phantom",
                subtitle = "Midnight Stealth F1",
                primaryColor = Color(0xFF1E293B), // Dark Slate
                accentColor = Color(0xFFFACC15),  // Yellow accent
                baseMaxSpeed = 220,
                baseTorque = 550,
                baseAcceleration = 2.0f,
                baseBatteryCapacity = 150f,
                unlockPrice = 2500,
                description = "Midnight stealth prototype with max torque and auto shield."
            )
        )
    }
}

data class TrafficCar(
    val id: Long,
    var xNormalized: Float, // -0.7 to 0.7 (relative to road center)
    var yNormalized: Float, // -0.2 (top offscreen) to 1.2 (bottom offscreen)
    val lane: Int,          // 0, 1, 2, 3
    val speedKmh: Float,    // speed relative to road
    val type: Int,          // 0: Sedan, 1: Formula Yellow, 2: Truck, 3: Police
    val color: Color,
    val widthNormalized: Float = 0.22f,
    val heightNormalized: Float = 0.14f
)

data class PickupItem(
    val id: Long,
    var xNormalized: Float,
    var yNormalized: Float,
    val type: PickupType,
    var collected: Boolean = false
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var color: Color,
    var alpha: Float,
    var size: Float,
    var life: Float,      // 1.0 to 0.0
    var maxLife: Float
)
