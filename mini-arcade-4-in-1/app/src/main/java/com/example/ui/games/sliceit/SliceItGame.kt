package com.example.ui.games.sliceit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
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
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

enum class FruitType(
    val emoji: String,
    val color: Color,
    val splashColor: Color,
    val isBomb: Boolean = false
) {
    WATERMELON("🍉", Color(0xFF22C55E), Color(0xFFEF4444)),
    APPLE("🍎", Color(0xFFEF4444), Color(0xFFFCA5A5)),
    BANANA("🍌", Color(0xFFEAB308), Color(0xFFFEF08A)),
    ORANGE("🍊", Color(0xFFF97316), Color(0xFFFFEDD5)),
    STRAWBERRY("🍓", Color(0xFFEC4899), Color(0xFFFBCFE8)),
    PINEAPPLE("🍍", Color(0xFFCA8A04), Color(0xFFFEF08A)),
    BOMB("💣", Color(0xFF1E293B), Color(0xFFF59E0B), isBomb = true)
}

data class FlyingFruit(
    val id: Int,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val type: FruitType,
    val radius: Float = 42f,
    var rotation: Float = 0f,
    var rotSpeed: Float = 3f,
    var isSliced: Boolean = false,
    var slicedHalf1X: Float = 0f,
    var slicedHalf1Y: Float = 0f,
    var slicedHalf2X: Float = 0f,
    var slicedHalf2Y: Float = 0f,
    var sliceVx1: Float = -4f,
    var sliceVx2: Float = 4f
)

data class JuiceParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    val radius: Float,
    var alpha: Float = 1.0f
)

data class SliceTrailPoint(
    val point: Offset,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun SliceItGame(
    highScore: Int,
    onBack: () -> Unit,
    onGameOver: (score: Int, coins: Int) -> Unit
) {
    var score by remember { mutableIntStateOf(0) }
    var coinsEarned by remember { mutableIntStateOf(0) }
    var level by remember { mutableIntStateOf(1) }
    var fruitsSlicedInLevel by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(3) }

    var isGameActive by remember { mutableStateOf(true) }
    var isFinished by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("MEYVELERİ PARMAĞINLA DİLİMLE! 🗡️") }
    var comboText by remember { mutableStateOf("") }

    val fruits = remember { mutableStateListOf<FlyingFruit>() }
    val particles = remember { mutableStateListOf<JuiceParticle>() }
    val trailPoints = remember { mutableStateListOf<SliceTrailPoint>() }

    var nextFruitId by remember { mutableIntStateOf(1) }
    val gravity = 0.45f

    val textMeasurer = rememberTextMeasurer()

    fun getLevelTarget(): Int = 10 + (level * 5)

    fun spawnFruitBatch(canvasWidth: Float, canvasHeight: Float) {
        val count = when {
            level <= 1 -> 1
            level <= 2 -> if (Random.nextFloat() < 0.4f) 2 else 1
            else -> Random.nextInt(1, 4)
        }

        repeat(count) {
            val isBombChance = when {
                level <= 1 -> false
                level <= 2 -> Random.nextFloat() < 0.2f
                else -> Random.nextFloat() < 0.35f
            }

            val type = if (isBombChance) {
                FruitType.BOMB
            } else {
                FruitType.values().filter { !it.isBomb }.random()
            }

            val spawnX = Random.nextFloat() * (canvasWidth * 0.7f) + (canvasWidth * 0.15f)
            val spawnY = canvasHeight + 50f
            val vx = (Random.nextFloat() - 0.5f) * 6f
            val vy = -(18f + Random.nextFloat() * 5f + (level * 0.5f))

            fruits.add(
                FlyingFruit(
                    id = nextFruitId++,
                    x = spawnX,
                    y = spawnY,
                    vx = vx,
                    vy = vy,
                    type = type,
                    rotSpeed = (Random.nextFloat() - 0.5f) * 8f
                )
            )
        }
    }

    fun resetGame() {
        score = 0
        coinsEarned = 0
        level = 1
        fruitsSlicedInLevel = 0
        lives = 3
        isGameActive = true
        isFinished = false
        statusText = "MEYVELERİ PARMAĞINLA DİLİMLE! 🗡️"
        comboText = ""
        fruits.clear()
        particles.clear()
        trailPoints.clear()
    }

    // Main Loop
    LaunchedEffect(isGameActive, isFinished) {
        var lastSpawnTime = System.currentTimeMillis()

        while (isGameActive && !isFinished) {
            delay(16) // ~60 FPS
            val now = System.currentTimeMillis()

            // Remove old trail points
            trailPoints.removeAll { now - it.timestamp > 150 }

            // Spawn check
            if (now - lastSpawnTime > maxOf(1200 - (level * 80), 600)) {
                lastSpawnTime = now
                spawnFruitBatch(1000f, 1600f)
            }

            // Update Particles
            val particleIterator = particles.iterator()
            while (particleIterator.hasNext()) {
                val p = particleIterator.next()
                p.x += p.vx
                p.y += p.vy
                p.vy += 0.2f
                p.alpha -= 0.03f
                if (p.alpha <= 0f) {
                    particleIterator.remove()
                }
            }

            // Update Fruits
            val fruitIterator = fruits.iterator()
            while (fruitIterator.hasNext()) {
                val fruit = fruitIterator.next()

                if (!fruit.isSliced) {
                    fruit.x += fruit.vx
                    fruit.y += fruit.vy
                    fruit.vy += gravity
                    fruit.rotation += fruit.rotSpeed

                    // Dropped out of screen without slicing
                    if (fruit.y > 1800f && fruit.vy > 0) {
                        fruitIterator.remove()
                        if (!fruit.type.isBomb) {
                            lives--
                            if (lives <= 0) {
                                isFinished = true
                                statusText = "OYN BİTTİ! MEYVELERİ KAÇIRDIN!"
                                onGameOver(score, coinsEarned)
                            }
                        }
                    }
                } else {
                    // Sliced halves movement
                    fruit.slicedHalf1X += fruit.sliceVx1
                    fruit.slicedHalf1Y += fruit.vy
                    fruit.slicedHalf2X += fruit.sliceVx2
                    fruit.slicedHalf2Y += fruit.vy
                    fruit.vy += gravity

                    if (fruit.slicedHalf1Y > 2000f) {
                        fruitIterator.remove()
                    }
                }
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
                    text = "FRUIT SLICE NINJA 🗡️",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = NeonCyan
                )
                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = { resetGame() },
                modifier = Modifier.testTag("reset_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = NeonGold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Level & Score Info Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Level & Level Target Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).padding(end = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SEVİYE $level",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonGold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (fruitsSlicedInLevel.toFloat() / getLevelTarget().toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = NeonGold,
                        trackColor = Color(0xFF334155)
                    )
                }
            }

            // Score Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).padding(horizontal = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("SKOR", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("$score", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            }

            // Lives Hearts Card
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
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main Game Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E1B4B), Color(0xFF0F172A))
                    )
                )
                .border(2.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .pointerInput(isGameActive, isFinished) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            trailPoints.add(SliceTrailPoint(offset))
                        },
                        onDrag = { change, _ ->
                            val currentPos = change.position
                            trailPoints.add(SliceTrailPoint(currentPos))

                            if (!isGameActive || isFinished) return@detectDragGestures

                            // Check collision with fruits
                            fruits.forEach { fruit ->
                                if (!fruit.isSliced) {
                                    val dist = hypot(currentPos.x - fruit.x, currentPos.y - fruit.y)
                                    if (dist < fruit.radius * 1.3f) {
                                        // SLICE HAPPENED!
                                        fruit.isSliced = true
                                        fruit.slicedHalf1X = fruit.x - 15f
                                        fruit.slicedHalf1Y = fruit.y
                                        fruit.slicedHalf2X = fruit.x + 15f
                                        fruit.slicedHalf2Y = fruit.y

                                        if (fruit.type.isBomb) {
                                            // HIT BOMB!
                                            lives--
                                            statusText = "BOMBA PATLADI! 💥"
                                            repeat(25) {
                                                particles.add(
                                                    JuiceParticle(
                                                        x = fruit.x,
                                                        y = fruit.y,
                                                        vx = (Random.nextFloat() - 0.5f) * 16f,
                                                        vy = (Random.nextFloat() - 0.5f) * 16f,
                                                        color = Color(0xFFF97316),
                                                        radius = Random.nextFloat() * 8f + 4f
                                                    )
                                                )
                                            }
                                            if (lives <= 0) {
                                                isFinished = true
                                                onGameOver(score, coinsEarned)
                                            }
                                        } else {
                                            // SLICED FRUIT!
                                            score += 15 * level
                                            coinsEarned += 2
                                            fruitsSlicedInLevel++

                                            // Spawn juice particles
                                            repeat(12) {
                                                particles.add(
                                                    JuiceParticle(
                                                        x = fruit.x,
                                                        y = fruit.y,
                                                        vx = (Random.nextFloat() - 0.5f) * 12f,
                                                        vy = (Random.nextFloat() - 0.5f) * 12f,
                                                        color = fruit.type.splashColor,
                                                        radius = Random.nextFloat() * 6f + 3f
                                                    )
                                                )
                                            }

                                            // Check Level Up
                                            if (fruitsSlicedInLevel >= getLevelTarget()) {
                                                level++
                                                fruitsSlicedInLevel = 0
                                                statusText = "TEBRİKLER! SEVİYE $level OLDUN! 🎉"
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasW = size.width
                val canvasH = size.height

                // Draw Juice Particles
                particles.forEach { p ->
                    drawCircle(
                        color = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f)),
                        radius = p.radius,
                        center = Offset(p.x, p.y)
                    )
                }

                // Draw Flying Fruits
                fruits.forEach { fruit ->
                    if (!fruit.isSliced) {
                        rotate(degrees = fruit.rotation, pivot = Offset(fruit.x, fruit.y)) {
                            drawText(
                                textMeasurer = textMeasurer,
                                text = fruit.type.emoji,
                                style = TextStyle(fontSize = 42.sp),
                                topLeft = Offset(fruit.x - 30f, fruit.y - 30f)
                            )
                        }
                    } else {
                        // Draw Sliced Halves
                        drawText(
                            textMeasurer = textMeasurer,
                            text = fruit.type.emoji,
                            style = TextStyle(fontSize = 28.sp),
                            topLeft = Offset(fruit.slicedHalf1X - 20f, fruit.slicedHalf1Y - 20f)
                        )
                        drawText(
                            textMeasurer = textMeasurer,
                            text = fruit.type.emoji,
                            style = TextStyle(fontSize = 28.sp),
                            topLeft = Offset(fruit.slicedHalf2X - 20f, fruit.slicedHalf2Y - 20f)
                        )
                    }
                }

                // Draw Blade Slash Trail
                if (trailPoints.size > 1) {
                    val path = Path()
                    path.moveTo(trailPoints.first().point.x, trailPoints.first().point.y)
                    for (i in 1 until trailPoints.size) {
                        path.lineTo(trailPoints[i].point.x, trailPoints[i].point.y)
                    }

                    // Outer Blade Glow
                    drawPath(
                        path = path,
                        color = NeonCyan.copy(alpha = 0.6f),
                        style = Stroke(width = 14f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    // Inner Sharp White Blade
                    drawPath(
                        path = path,
                        color = Color.White,
                        style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }

            // Game Over Modal Overlay
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
                            Text("🎮 OYUN BİTTİ", fontSize = 22.sp, fontWeight = FontWeight.Black, color = NeonGold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("SEVİYE: $level", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                            Text("SKOR: $score", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text("KAZANILAN COIN: +$coinsEarned 🪙", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NeonGold)

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { resetGame() },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("YENİDEN OYNA", fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}
