package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_garage")
data class GarageEntity(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 250,
    val selectedVehicleId: String = "kron_alpha",
    val unlockedVehiclesJson: String = "[\"kron_alpha\"]",
    val topSpeedLevel: Int = 1,
    val accelerationLevel: Int = 1,
    val boostCapacityLevel: Int = 1,
    val batteryEfficiencyLevel: Int = 1,
    val customColorHex: String = "#E53935"
)
