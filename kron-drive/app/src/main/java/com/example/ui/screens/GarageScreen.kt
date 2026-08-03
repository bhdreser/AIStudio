package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.GameState
import com.example.engine.VehicleSpec
import com.example.ui.theme.KronAccent
import com.example.ui.theme.KronBg
import com.example.ui.theme.KronCard
import com.example.ui.theme.KronMuted
import com.example.ui.theme.KronPosDef
import com.example.ui.theme.KronPositive
import com.example.ui.theme.KronText
import com.example.ui.viewmodel.GameViewModel

@Composable
fun GarageScreen(viewModel: GameViewModel) {
    val garageData by viewModel.garageData.collectAsState()

    val unlockedIds = garageData.unlockedVehiclesJson
        .replace("[", "").replace("]", "").replace("\"", "").split(",")
        .map { it.trim() }

    val currentSelected = VehicleSpec.VEHICLES.find { it.id == garageData.selectedVehicleId }
        ?: VehicleSpec.VEHICLES[0]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KronBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(GameState.MENU) },
                    modifier = Modifier.background(KronCard, CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = KronAccent)
                }

                Text(
                    text = "VEHICLE GARAGE",
                    color = KronText,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    letterSpacing = 1.sp
                )

                // Coins Pill
                Surface(
                    color = KronCard,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66334155))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${garageData.coins} ",
                            color = KronAccent,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(KronAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$",
                                color = KronBg,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Vehicle Selector Horizontal Scroll
            Text(
                text = "SELECT VEHICLE",
                color = KronMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(VehicleSpec.VEHICLES) { vehicle ->
                    val isUnlocked = unlockedIds.contains(vehicle.id)
                    val isSelected = (vehicle.id == garageData.selectedVehicleId)

                    Surface(
                        color = KronCard,
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) KronAccent else Color(0x66334155)
                        ),
                        modifier = Modifier
                            .size(width = 210.dp, height = 155.dp)
                            .clickable {
                                if (isUnlocked) {
                                    viewModel.selectVehicle(vehicle.id)
                                }
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(vehicle.primaryColor, CircleShape)
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = KronAccent)
                                } else if (!isUnlocked) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = KronMuted)
                                }
                            }

                            Column {
                                Text(
                                    text = vehicle.name,
                                    color = KronText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = vehicle.subtitle,
                                    color = KronAccent,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            }

                            if (!isUnlocked) {
                                Button(
                                    onClick = { viewModel.unlockVehicle(vehicle) },
                                    colors = ButtonDefaults.buttonColors(containerColor = KronPosDef),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().height(36.dp)
                                ) {
                                    Text("Unlock ${vehicle.unlockPrice}", color = KronBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            } else if (!isSelected) {
                                OutlinedButton(
                                    onClick = { viewModel.selectVehicle(vehicle.id) },
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66334155)),
                                    modifier = Modifier.fillMaxWidth().height(36.dp)
                                ) {
                                    Text("Select", color = KronText, fontSize = 12.sp)
                                }
                            } else {
                                Text("Equipped", color = KronAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Selected Vehicle Active Specs & Upgrades
            Text(
                text = "PERFORMANCE UPGRADES",
                color = KronMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = KronCard,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    UpgradeRow(
                        title = "Top Speed (${currentSelected.baseMaxSpeed + garageData.topSpeedLevel * 10} KM/H)",
                        level = garageData.topSpeedLevel,
                        maxLevel = 10,
                        onUpgrade = { viewModel.upgradeStat("topSpeed") },
                        canAfford = garageData.coins >= 150
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    UpgradeRow(
                        title = "Torque / Accel (${currentSelected.baseTorque + garageData.accelerationLevel * 20} Nm)",
                        level = garageData.accelerationLevel,
                        maxLevel = 10,
                        onUpgrade = { viewModel.upgradeStat("acceleration") },
                        canAfford = garageData.coins >= 150
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    UpgradeRow(
                        title = "Boost Capacity (+${garageData.boostCapacityLevel * 10}%)",
                        level = garageData.boostCapacityLevel,
                        maxLevel = 10,
                        onUpgrade = { viewModel.upgradeStat("boostCapacity") },
                        canAfford = garageData.coins >= 150
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    UpgradeRow(
                        title = "Battery Efficiency (+${garageData.batteryEfficiencyLevel * 15}%)",
                        level = garageData.batteryEfficiencyLevel,
                        maxLevel = 10,
                        onUpgrade = { viewModel.upgradeStat("batteryEfficiency") },
                        canAfford = garageData.coins >= 150
                    )
                }
            }
        }
    }
}

@Composable
private fun UpgradeRow(
    title: String,
    level: Int,
    maxLevel: Int,
    onUpgrade: () -> Unit,
    canAfford: Boolean
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = KronText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Lvl $level ", color = KronPosDef, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Button(
                    onClick = onUpgrade,
                    enabled = (level < maxLevel && canAfford),
                    colors = ButtonDefaults.buttonColors(containerColor = KronAccent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("+150", color = KronBg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { level / maxLevel.toFloat() },
            color = KronPosDef,
            trackColor = KronBg,
            modifier = Modifier.fillMaxWidth().height(6.dp)
        )
    }
}

