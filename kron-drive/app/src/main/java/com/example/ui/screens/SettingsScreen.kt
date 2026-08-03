package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.GameState
import com.example.ui.theme.KronAccent
import com.example.ui.theme.KronBg
import com.example.ui.theme.KronCard
import com.example.ui.theme.KronMuted
import com.example.ui.theme.KronPosDef
import com.example.ui.theme.KronText
import com.example.ui.viewmodel.GameViewModel

@Composable
fun SettingsScreen(viewModel: GameViewModel) {
    val context = LocalContext.current
    var soundEnabled by remember { mutableStateOf(viewModel.soundManager.soundEnabled) }
    var hapticsEnabled by remember { mutableStateOf(viewModel.soundManager.hapticsEnabled) }
    val googleUser by viewModel.googleUser.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KronBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(GameState.MENU) },
                    modifier = Modifier.background(KronCard, CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = KronAccent)
                }

                Text(
                    text = "  SETTINGS",
                    color = KronText,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Google Account Card
            Surface(
                color = KronCard,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("GOOGLE ACCOUNT & HISTORY SYNC", color = KronAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (googleUser != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFF4285F4), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("G", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(googleUser?.displayName ?: "Google User", color = KronText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(googleUser?.email ?: "", color = KronPosDef, fontSize = 12.sp)
                                }
                            }

                            OutlinedButton(
                                onClick = { viewModel.signOutGoogle() },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Çıkış Yap", color = KronMuted, fontSize = 12.sp)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Google Hesabı Bağlanmadı", color = KronText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Oyun geçmişini ve skorlarını hesabında tutmak için giriş yap.", color = KronMuted, fontSize = 12.sp)
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Audio & Haptics Card
            Surface(
                color = KronCard,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Engine & Game SFX", color = KronText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Play acceleration, boost, and crash tones", color = KronMuted, fontSize = 12.sp)
                        }
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = {
                                soundEnabled = it
                                viewModel.soundManager.soundEnabled = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = KronAccent,
                                checkedTrackColor = Color(0xFF334155)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Haptic Feedback", color = KronText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Vibrate device on collisions and steering", color = KronMuted, fontSize = 12.sp)
                        }
                        Switch(
                            checked = hapticsEnabled,
                            onCheckedChange = {
                                hapticsEnabled = it
                                viewModel.soundManager.hapticsEnabled = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = KronAccent,
                                checkedTrackColor = Color(0xFF334155)
                            )
                        )
                    }
                }
            }
        }
    }
}
