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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.theme.KronAccent
import com.example.ui.theme.KronBg
import com.example.ui.theme.KronCard
import com.example.ui.theme.KronMuted
import com.example.ui.theme.KronPosDef
import com.example.ui.theme.KronText
import com.example.ui.viewmodel.GameViewModel

@Composable
fun ScoresScreen(viewModel: GameViewModel) {
    val scores by viewModel.highScoresList.collectAsState()
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
                    text = "  HIGH SCORES & RUNS",
                    color = KronText,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Account status banner
            Surface(
                color = KronCard,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66334155)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(if (googleUser != null) Color(0xFF4285F4) else KronMuted, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (googleUser != null) "G" else "A",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (googleUser != null) "Google Account: ${googleUser?.displayName}" else "Anonymous Local Driver",
                            color = KronText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (googleUser != null) "${googleUser?.email} • Saved Runs Synced" else "Sign in with Google on Main Menu to link future runs",
                            color = if (googleUser != null) KronPosDef else KronMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            if (scores.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = KronMuted, modifier = Modifier.padding(16.dp))
                        Text("No runs recorded yet!", color = KronText, fontWeight = FontWeight.Bold)
                        Text("Start a drive to set your first score record.", color = KronMuted, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(scores) { index, item ->
                        Surface(
                            color = KronCard,
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66334155)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "#${index + 1}",
                                        color = if (index == 0) KronAccent else KronMuted,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        modifier = Modifier.padding(end = 14.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "SCORE: ${item.score}",
                                            color = KronText,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = "${"%.1f".format(item.distanceKm)} KM • ${item.topSpeedKmh} KM/H MAX",
                                            color = KronPosDef,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (item.userEmail != null) {
                                            Text(
                                                text = "Driver: ${item.userEmail}",
                                                color = KronAccent,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "+${item.coinsEarned} coins",
                                    color = KronAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
