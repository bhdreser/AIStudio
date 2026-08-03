package com.example.ui.games.funrace

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FunRaceGame(
    highScore: Int,
    onBack: () -> Unit,
    onGameOver: (score: Int, coins: Int) -> Unit
) {
    var playerPos by remember { mutableFloatStateOf(0f) }
    var ai1Pos by remember { mutableFloatStateOf(0f) }
    var ai2Pos by remember { mutableFloatStateOf(0f) }

    var lives by remember { mutableIntStateOf(3) }
    var currentLevel by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var coinsEarned by remember { mutableIntStateOf(0) }

    var isHoldingRun by remember { mutableStateOf(false) }
    var isRaceActive by remember { mutableStateOf(true) }
    var isFinished by remember { mutableStateOf(false) }

    var statusText by remember { mutableStateOf("KOŞMAK İÇİN EKRANA BASILI TUTUN! 🏃") }
    var winnerTitle by remember { mutableStateOf("") }

    // Obstacle animation values
    var hammerAngle by remember { mutableFloatStateOf(0f) }
    var pendulumAngle by remember { mutableFloatStateOf(0f) }
    var pusherOffset by remember { mutableFloatStateOf(0f) }

    val textMeasurer = rememberTextMeasurer()

    fun resetRace() {
        playerPos = 0f
        ai1Pos = 0f
        ai2Pos = 0f
        lives = 3
        isRaceActive = true
        isFinished = false
        statusText = "KOŞMAK İÇİN EKRANA BASILI TUTUN! 🏃"
        winnerTitle = ""
    }

    fun nextLevel() {
        currentLevel++
        resetRace()
        statusText = "SEVİYE $currentLevel BAŞLADI!"
    }

    // Main Physics Loop
    LaunchedEffect(isRaceActive, isFinished) {
        var time = 0f
        while (isRaceActive && !isFinished) {
            delay(16) // ~60 FPS
            time += 0.05f

            // Animate Obstacles
            hammerAngle = (time * 4f) % (2f * Math.PI.toFloat())
            pendulumAngle = sin(time * 3.5f) * 1.2f
            pusherOffset = (sin(time * 4.5f) + 1f) / 2f

            // AI Speed depends on current level
            val baseAiSpeed = 0.28f + (currentLevel * 0.04f)

            // AI 1 Logic (Dodges obstacles slightly)
            val ai1Blocked = (ai1Pos in 22f..28f && hammerAngle % 3.14f in 1.1f..2.0f) ||
                    (ai1Pos in 52f..58f && pendulumAngle in -0.5f..0.5f)
            if (!ai1Blocked) {
                ai1Pos += baseAiSpeed
            }

            // AI 2 Logic
            val ai2Blocked = (ai2Pos in 52f..58f && pendulumAngle in -0.5f..0.5f) ||
                    (ai2Pos in 77f..83f && pusherOffset > 0.6f)
            if (!ai2Blocked) {
                ai2Pos += baseAiSpeed * 0.95f
            }

            // Player Running logic
            if (isHoldingRun) {
                playerPos += 0.38f

                // Check Collisions
                val isHammerDangerous = (hammerAngle % 3.14f) in 1.1f..2.0f
                val isPendulumDangerous = pendulumAngle in -0.5f..0.5f
                val isPusherDangerous = pusherOffset > 0.6f

                val hitHammer = playerPos in 22f..28f && isHammerDangerous
                val hitPendulum = playerPos in 52f..58f && isPendulumDangerous
                val hitPusher = playerPos in 77f..83f && isPusherDangerous

                if (hitHammer || hitPendulum || hitPusher) {
                    playerPos = (playerPos - 12f).coerceAtLeast(0f)
                    lives--
                    statusText = "ENGEL ÇARPTI! GERİ SÜRÜKLENDİN!"
                    if (lives <= 0) {
                        isFinished = true
                        winnerTitle = "KAYBETTİN! ❌"
                        onGameOver(score, coinsEarned)
                    }
                }
            }

            // Check Finish Line (100m)
            if (playerPos >= 100f || ai1Pos >= 100f || ai2Pos >= 100f) {
                isFinished = true
                val place = when {
                    playerPos >= ai1Pos && playerPos >= ai2Pos -> 1
                    playerPos >= ai1Pos || playerPos >= ai2Pos -> 2
                    else -> 3
                }

                if (place == 1) {
                    winnerTitle = "1. OLDUN! 🏆 ŞAMPİYON!"
                    score += 500 * currentLevel
                    coinsEarned += 50 * currentLevel
                } else {
                    winnerTitle = "$place. OLDUN!"
                    score += 150
                    coinsEarned += 15
                }
                onGameOver(score, coinsEarned)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(12.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "FUN RACE 3D 🏃‍♂️",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = NeonGold
                )
                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = { resetRace() },
                modifier = Modifier.testTag("reset_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = NeonCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Race Status Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).padding(end = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("SEVİYE $currentLevel", fontSize = 12.sp, fontWeight = FontWeight.Black, color = NeonCyan)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("100M PARKUR", fontSize = 10.sp, color = Color.Gray)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).padding(horizontal = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("SENİN KONUMUN", fontSize = 10.sp, color = Color.Gray)
                    Text("${playerPos.toInt()}m / 100m", fontSize = 14.sp, fontWeight = FontWeight.Black, color = NeonGold)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).padding(start = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Heart",
                            tint = if (index < lives) Color(0xFFEF4444) else Color.DarkGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main Race 3D Canvas Track
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF020617))
                .border(2.dp, NeonGold.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isHoldingRun = true
                            tryAwaitRelease()
                            isHoldingRun = false
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasW = size.width
                val canvasH = size.height

                val trackLeft = canvasW * 0.15f
                val trackRight = canvasW * 0.85f
                val trackWidth = trackRight - trackLeft

                // Draw Track Lanes
                drawRoundRect(
                    color = Color(0xFF1E293B),
                    topLeft = Offset(trackLeft, 40f),
                    size = Size(trackWidth, canvasH - 80f),
                    cornerRadius = CornerRadius(20f, 20f)
                )

                // Draw Finish Line (Top 100m)
                val finishY = 70f
                drawRect(
                    color = Color.White,
                    topLeft = Offset(trackLeft, finishY),
                    size = Size(trackWidth, 20f)
                )

                // Checkered finish details
                repeat(8) { i ->
                    if (i % 2 == 0) {
                        drawRect(
                            color = Color.Black,
                            topLeft = Offset(trackLeft + (i * trackWidth / 8), finishY),
                            size = Size(trackWidth / 8, 20f)
                        )
                    }
                }

                drawText(
                    textMeasurer = textMeasurer,
                    text = "🏁 BİTİŞ ÇİZGİSİ (100M)",
                    style = TextStyle(fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold),
                    topLeft = Offset(trackLeft + 10f, finishY - 25f)
                )

                // Function to map progress (0 to 100) to Y screen coordinate
                fun getTrackY(prog: Float): Float {
                    val startY = canvasH - 120f
                    val endY = finishY + 30f
                    return startY - ((prog / 100f) * (startY - endY))
                }

                // Draw Obstacle 1: Crusher Hammer (25m)
                val obs1Y = getTrackY(25f)
                rotate(degrees = (hammerAngle * 57.3f), pivot = Offset(trackLeft + trackWidth / 2, obs1Y)) {
                    drawRect(
                        color = Color(0xFFEF4444),
                        topLeft = Offset(trackLeft + 20f, obs1Y - 12f),
                        size = Size(trackWidth - 40f, 24f)
                    )
                }
                drawText(
                    textMeasurer = textMeasurer,
                    text = "🔨 ÇEVRİLEN BALTA (25M)",
                    style = TextStyle(fontSize = 10.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold),
                    topLeft = Offset(trackLeft + 10f, obs1Y - 30f)
                )

                // Draw Obstacle 2: Swinging Pendulum (55m)
                val obs2Y = getTrackY(55f)
                val pendulumX = (trackLeft + trackWidth / 2) + (sin(pendulumAngle) * (trackWidth / 2.5f))
                drawLine(
                    color = Color.Yellow,
                    start = Offset(trackLeft + trackWidth / 2, obs2Y - 40f),
                    end = Offset(pendulumX, obs2Y),
                    strokeWidth = 6f
                )
                drawCircle(
                    color = Color(0xFFEAB308),
                    radius = 22f,
                    center = Offset(pendulumX, obs2Y)
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = "⚖️ SALLANAN SARKAÇ (55M)",
                    style = TextStyle(fontSize = 10.sp, color = Color(0xFFEAB308), fontWeight = FontWeight.Bold),
                    topLeft = Offset(trackLeft + 10f, obs2Y - 30f)
                )

                // Draw Obstacle 3: Pusher Barrier (80m)
                val obs3Y = getTrackY(80f)
                val pusherWidth = (trackWidth * 0.7f) * pusherOffset
                drawRect(
                    color = Color(0xFFA855F7),
                    topLeft = Offset(trackLeft, obs3Y - 10f),
                    size = Size(pusherWidth, 20f)
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = "🛑 İTİCİ DUVAR (80M)",
                    style = TextStyle(fontSize = 10.sp, color = Color(0xFFA855F7), fontWeight = FontWeight.Bold),
                    topLeft = Offset(trackLeft + 10f, obs3Y - 30f)
                )

                // Draw Runners on Track Lanes
                val lane1X = trackLeft + (trackWidth * 0.25f)
                val lane2X = trackLeft + (trackWidth * 0.50f)
                val lane3X = trackLeft + (trackWidth * 0.75f)

                // AI 1 Runner (Red 🔴)
                val ai1Y = getTrackY(ai1Pos)
                drawCircle(color = Color(0xFFEF4444), radius = 16f, center = Offset(lane1X, ai1Y))
                drawText(
                    textMeasurer = textMeasurer,
                    text = "🔴 AI 1",
                    style = TextStyle(fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold),
                    topLeft = Offset(lane1X - 15f, ai1Y - 30f)
                )

                // Player Runner (Gold 🟡)
                val playerY = getTrackY(playerPos)
                drawCircle(color = NeonGold, radius = 20f, center = Offset(lane2X, playerY))
                drawCircle(color = Color.White, radius = 8f, center = Offset(lane2X, playerY))
                drawText(
                    textMeasurer = textMeasurer,
                    text = "🟡 SEN",
                    style = TextStyle(fontSize = 11.sp, color = NeonGold, fontWeight = FontWeight.Black),
                    topLeft = Offset(lane2X - 15f, playerY - 35f)
                )

                // AI 2 Runner (Blue 🔵)
                val ai2Y = getTrackY(ai2Pos)
                drawCircle(color = Color(0xFF38BDF8), radius = 16f, center = Offset(lane3X, ai2Y))
                drawText(
                    textMeasurer = textMeasurer,
                    text = "🔵 AI 2",
                    style = TextStyle(fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold),
                    topLeft = Offset(lane3X - 15f, ai2Y - 30f)
                )
            }

            // Big Hold to Run Overlay Prompt
            if (!isHoldingRun && !isFinished) {
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = "Hold",
                            tint = NeonGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "KOŞMAK İÇİN PARMAĞINI BASILI TUT!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGold
                        )
                    }
                }
            }

            // Finish Modal
            if (isFinished) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .padding(24.dp)
                            .border(2.dp, NeonGold, RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(winnerTitle, fontSize = 22.sp, fontWeight = FontWeight.Black, color = NeonGold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("SEVİYE $currentLevel TAMAMLANDI", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                            Text("KAZANILAN COIN: +$coinsEarned 🪙", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)

                            Spacer(modifier = Modifier.height(16.dp))

                            Row {
                                Button(
                                    onClick = { resetRace() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                    modifier = Modifier.weight(1f).padding(end = 4.dp)
                                ) {
                                    Text("TEKRAR", fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                Button(
                                    onClick = { nextLevel() },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                                ) {
                                    Text("SONRAKİ SEVİYE ➡️", fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
