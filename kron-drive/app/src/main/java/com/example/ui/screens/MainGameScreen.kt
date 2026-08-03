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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.components.ArcSpeedometer
import com.example.ui.components.CanvasRoadView
import com.example.ui.components.ControlButtons
import com.example.ui.components.GameHudOverlay
import com.example.ui.viewmodel.GameViewModel

@Composable
fun MainGameScreen(viewModel: GameViewModel) {
    val gameState by viewModel.gameState.collectAsState()

    val pX by viewModel.gameEngine.playerX.collectAsState()
    val roadOffset by viewModel.gameEngine.roadScrollOffset.collectAsState()
    val speedKmh by viewModel.gameEngine.currentSpeedKmh.collectAsState()
    val torqueNm by viewModel.gameEngine.currentTorqueNm.collectAsState()
    val boostEnergy by viewModel.gameEngine.boostEnergy.collectAsState()
    val batteryLevel by viewModel.gameEngine.batteryLevel.collectAsState()
    val score by viewModel.gameEngine.score.collectAsState()
    val timeSec by viewModel.gameEngine.timeSurvivingSec.collectAsState()
    val coinsEarned by viewModel.gameEngine.coinsEarned.collectAsState()
    val distanceKm by viewModel.gameEngine.distanceKm.collectAsState()
    val topSpeed by viewModel.gameEngine.topSpeedReached.collectAsState()

    val trafficCars by viewModel.gameEngine.trafficCars.collectAsState()
    val pickups by viewModel.gameEngine.pickups.collectAsState()
    val particles by viewModel.gameEngine.particles.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1B1F))
    ) {
        // 1. Race Track Canvas (2D Vertical Scrolling)
        CanvasRoadView(
            modifier = Modifier.fillMaxSize(),
            playerXNormalized = pX,
            roadScrollOffset = roadOffset,
            activeVehicle = viewModel.gameEngine.activeVehicle,
            trafficCars = trafficCars,
            pickups = pickups,
            particles = particles,
            isBoosting = viewModel.gameEngine.isBoosting,
            isShieldActive = viewModel.gameEngine.isShieldActive,
            onDragSteer = { delta ->
                viewModel.gameEngine.targetPlayerX.value =
                    (viewModel.gameEngine.targetPlayerX.value + delta * 2.2f).coerceIn(-0.62f, 0.62f)
            }
        )

        // 2. Arc Gauge Speedometer (Placed on Mid-Left Overlay, exact match to screenshot)
        ArcSpeedometer(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 6.dp),
            speedKmh = speedKmh.toInt(),
            torqueNm = torqueNm,
            batteryPercent = batteryLevel.toInt()
        )

        // 3. Top HUD Bar (Exact match to screenshot)
        GameHudOverlay(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 4.dp),
            score = score,
            timeSec = timeSec,
            boostPercent = boostEnergy.toInt(),
            coins = coinsEarned
        )

        // Pause Button on Top Right below HUD
        IconButton(
            onClick = { viewModel.pauseGame() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 80.dp, end = 12.dp)
                .background(Color(0x88000000), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Pause,
                contentDescription = "Pause Game",
                tint = Color.White
            )
        }

        // 4. On-screen Controls (Bottom Left/Right, exact match to screenshot)
        ControlButtons(
            modifier = Modifier.align(Alignment.BottomCenter),
            onSteerLeftState = { viewModel.gameEngine.isSteeringLeft = it },
            onSteerRightState = { viewModel.gameEngine.isSteeringRight = it },
            onBrakeState = { viewModel.gameEngine.isBraking = it },
            onBoostState = { viewModel.gameEngine.isBoosting = it }
        )

        // 5. Pause Modal Dialog
        if (gameState == GameState.PAUSED) {
            Surface(
                color = Color(0xCC1C1B1F),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Surface(
                        color = Color(0xFF2B2930),
                        shape = RoundedCornerShape(28.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF49454F)),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "GAME PAUSED",
                                color = Color(0xFFD0BCFF),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = { viewModel.resumeGame() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF381E72))
                                Text(" Resume Drive", color = Color(0xFF381E72), fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { viewModel.startGame(viewModel.selectedMode.value) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF49454F)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFFE6E1E5))
                                Text(" Restart", color = Color(0xFFE6E1E5), fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedButton(
                                onClick = { viewModel.navigateTo(GameState.MENU) },
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF49454F)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(" Main Menu", color = Color(0xFFD0BCFF), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 6. Game Over Screen / Crash Summary Modal
        if (gameState == GameState.GAME_OVER) {
            Surface(
                color = Color(0xDD1C1B1F),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Surface(
                        color = Color(0xFF2B2930),
                        shape = RoundedCornerShape(28.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF49454F)),
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "RUN FINISHED!",
                                color = Color(0xFFFFB4AB),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "KRON DRIVE TELEMETRY",
                                color = Color(0xFF938F99),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Score summary card
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1C1B1F), RoundedCornerShape(18.dp))
                                    .border(1.dp, Color(0xFF49454F), RoundedCornerShape(18.dp))
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("FINAL SCORE", color = Color(0xFF938F99), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("$score", color = Color(0xFFFFD8E4), fontSize = 28.sp, fontWeight = FontWeight.Black)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("TIME SURVIVED", color = Color(0xFF938F99), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("${timeSec}s", color = Color(0xFFE6E1E5), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Distance, Top Speed, Coins Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SummaryStatBox(
                                    modifier = Modifier.weight(1f),
                                    title = "DISTANCE",
                                    value = "%.1f KM".format(distanceKm),
                                    color = Color(0xFF7CCFFF)
                                )
                                SummaryStatBox(
                                    modifier = Modifier.weight(1f),
                                    title = "TOP SPEED",
                                    value = "$topSpeed KM/H",
                                    color = Color(0xFFD0BCFF)
                                )
                                SummaryStatBox(
                                    modifier = Modifier.weight(1f),
                                    title = "COINS",
                                    value = "+$coinsEarned",
                                    color = Color(0xFFFFD8E4)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = { viewModel.startGame(viewModel.selectedMode.value) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF)),
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF381E72))
                                Text(" PLAY AGAIN", color = Color(0xFF381E72), fontWeight = FontWeight.Black, fontSize = 16.sp)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.navigateTo(GameState.GARAGE) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF49454F)),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Garage", color = Color(0xFFE6E1E5), fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { viewModel.navigateTo(GameState.MENU) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1B1F)),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Main Menu", color = Color(0xFFD0BCFF), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryStatBox(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    color: Color
) {
    Box(
        modifier = modifier
            .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
