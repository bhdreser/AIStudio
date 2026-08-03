package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.KronAccent
import com.example.ui.theme.KronBg
import com.example.ui.theme.KronCard
import com.example.ui.theme.KronMuted
import com.example.ui.theme.KronPosDef
import com.example.ui.theme.KronText

@Composable
fun GameHudOverlay(
    modifier: Modifier = Modifier,
    score: Int,
    timeSec: Int,
    boostPercent: Int,
    coins: Int
) {
    Surface(
        color = KronCard.copy(alpha = 0.92f),
        shape = RoundedCornerShape(22.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66334155)),
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top Header Line: Logo + Score/Time + Crowd Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // KRON DRIVE Brand Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(KronAccent, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "K",
                            color = KronBg,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                    Text(
                        text = " KRON DRIVE",
                        color = KronAccent,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                }

                // SCORE & TIME
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SCORE $score  •  TIME $timeSec",
                        color = KronText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .background(KronBg, CircleShape)
                            .border(1.dp, Color(0x66334155), CircleShape)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "CROWD",
                            color = KronMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // BOOST ENERGY Title & Percentage
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "BOOST ENERGY",
                    color = KronMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "$boostPercent%",
                    color = KronText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Gradient Boost Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(KronBg)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((boostPercent / 100f).coerceIn(0f, 1f))
                        .height(10.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    KronPosDef, // Sky cyan #38BDF8
                                    KronAccent  // Yellow #FACC15
                                )
                            )
                        )
                )
            }
        }
    }
}

