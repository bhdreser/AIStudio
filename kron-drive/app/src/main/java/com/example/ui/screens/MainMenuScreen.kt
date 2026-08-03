package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.GameMode
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
fun MainMenuScreen(viewModel: GameViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val garageData by viewModel.garageData.collectAsState()
    val topScore by viewModel.topScore.collectAsState()
    val googleUser by viewModel.googleUser.collectAsState()

    val selectedVehicle = VehicleSpec.VEHICLES.find { it.id == garageData.selectedVehicleId }
        ?: VehicleSpec.VEHICLES[0]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KronBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: High Score & Coins
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // High Score Pill
                Surface(
                    color = KronCard,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66334155))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Leaderboard, contentDescription = null, tint = KronAccent, modifier = Modifier.size(18.dp))
                        Text(
                            text = " BEST: $topScore",
                            color = KronText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Coins Pill
                Surface(
                    color = KronCard,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66334155))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
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

            // Middle Banner: KRON DRIVE Logo & Car Preview
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(KronAccent, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("K", color = KronBg, fontWeight = FontWeight.Black, fontSize = 24.sp)
                    }
                    Text(
                        text = " KRON DRIVE",
                        color = KronAccent,
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp,
                        letterSpacing = 2.sp
                    )
                }

                Text(
                    text = "STREET RACING & HIGHWAY DODGE",
                    color = KronMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                // Google Account Sync Banner
                Surface(
                    color = KronCard,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66334155)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    if (googleUser != null) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color(0xFF4285F4), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("G", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = googleUser?.displayName ?: "Google Driver",
                                        color = KronText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${googleUser?.email} • Synced",
                                        color = KronPosDef,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            TextButton(onClick = { viewModel.signOutGoogle() }) {
                                Text("Çıkış", color = KronMuted, fontSize = 12.sp)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color(0xFF4285F4), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("G", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Google ile Giriş Yap",
                                        color = KronText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Geçmişini hesabında sakla",
                                        color = KronMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Button(
                                onClick = { viewModel.signInWithGoogle(context) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Giriş Yap", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Selected Vehicle Card
                Surface(
                    color = KronCard,
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66334155)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "ACTIVE VEHICLE",
                                color = KronMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = selectedVehicle.name,
                                color = KronText,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = selectedVehicle.subtitle,
                                color = KronAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { viewModel.navigateTo(GameState.GARAGE) },
                            colors = ButtonDefaults.buttonColors(containerColor = KronBg),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66334155))
                        ) {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = KronAccent)
                            Text(" Garage", fontWeight = FontWeight.Bold, color = KronText)
                        }
                    }
                }
            }

            // Bottom Buttons: Start Drive, Time Attack, Leaderboard, Settings
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Main Start Drive Button (KRON Yellow #FACC15 with Dark Background text #020617)
                Button(
                    onClick = { viewModel.startGame(GameMode.ENDLESS) },
                    colors = ButtonDefaults.buttonColors(containerColor = KronAccent),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = KronBg, modifier = Modifier.size(28.dp))
                    Text(
                        text = " START DRIVE",
                        color = KronBg,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp
                    )
                }

                // Secondary Row: Time Rush & Leaderboards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.startGame(GameMode.TIME_ATTACK) },
                        colors = ButtonDefaults.buttonColors(containerColor = KronPosDef),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = KronBg, modifier = Modifier.size(20.dp))
                        Text(" Time Rush", fontWeight = FontWeight.Bold, color = KronBg)
                    }

                    Button(
                        onClick = { viewModel.navigateTo(GameState.SCORES) },
                        colors = ButtonDefaults.buttonColors(containerColor = KronCard),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66334155)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Leaderboard, contentDescription = null, tint = KronAccent, modifier = Modifier.size(20.dp))
                        Text(" Records", fontWeight = FontWeight.Bold, color = KronText)
                    }

                    OutlinedButton(
                        onClick = { viewModel.navigateTo(GameState.SETTINGS) },
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66334155)),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = KronAccent)
                    }
                }
            }
        }
    }
}

