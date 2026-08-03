package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import com.example.engine.Particle
import com.example.engine.PickupItem
import com.example.engine.PickupType
import com.example.engine.TrafficCar
import com.example.engine.VehicleSpec
import kotlin.math.sin

@Composable
fun CanvasRoadView(
    modifier: Modifier = Modifier,
    playerXNormalized: Float, // -0.65 to 0.65
    roadScrollOffset: Float,  // 0.0 to 1.0
    activeVehicle: VehicleSpec,
    trafficCars: List<TrafficCar>,
    pickups: List<PickupItem>,
    particles: List<Particle>,
    isBoosting: Boolean,
    isShieldActive: Boolean,
    onDragSteer: ((Float) -> Unit)? = null
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    // Allow optional direct swipe steering on track
                    onDragSteer?.invoke(dragAmount.x / size.width)
                }
            }
    ) {
        val w = size.width
        val h = size.height

        val grassWidth = w * 0.18f
        val roadWidth = w - (grassWidth * 2f)
        val roadLeft = grassWidth
        val roadRight = w - grassWidth
        val roadCenter = w / 2f

        // 1. Draw Side Grass Shoulders (Matching Green Grass in user screenshot)
        drawRect(
            color = Color(0xFF388E3C),
            topLeft = Offset(0f, 0f),
            size = Size(grassWidth, h)
        )
        drawRect(
            color = Color(0xFF388E3C),
            topLeft = Offset(roadRight, 0f),
            size = Size(grassWidth, h)
        )

        // Draw Crowd Spectator Seat Patterns & Yellow Flags (As in screenshot)
        val accentHeight = h / 14f
        val scrollOffsetPx = (roadScrollOffset * accentHeight)

        for (i in -2..16) {
            val y = (i * accentHeight) + scrollOffsetPx

            // Crowd seat rows (white & dark green dot pattern)
            for (dot in 0..3) {
                val dotXLeft = 12f + (dot * (grassWidth * 0.22f))
                val dotXRight = roadRight + 12f + (dot * (grassWidth * 0.22f))
                val seatColor = if ((i + dot) % 2 == 0) Color(0xFF1B5E20) else Color(0xDDFFFFFF)

                drawRoundRect(
                    color = seatColor,
                    topLeft = Offset(dotXLeft, y),
                    size = Size(14f, 6f),
                    cornerRadius = CornerRadius(2f, 2f)
                )
                drawRoundRect(
                    color = seatColor,
                    topLeft = Offset(dotXRight, y),
                    size = Size(14f, 6f),
                    cornerRadius = CornerRadius(2f, 2f)
                )
            }

            // Yellow crowd flag every 3 rows
            if (i % 3 == 0) {
                // Left flag
                val flagPathLeft = Path().apply {
                    moveTo(grassWidth * 0.75f, y + 4f)
                    lineTo(grassWidth * 0.95f, y + 8f)
                    lineTo(grassWidth * 0.75f, y + 14f)
                    close()
                }
                drawLine(color = Color(0xFF212121), start = Offset(grassWidth * 0.75f, y + 2f), end = Offset(grassWidth * 0.75f, y + 20f), strokeWidth = 3f)
                drawPath(flagPathLeft, color = Color(0xFFFACC15))

                // Right flag
                val flagPathRight = Path().apply {
                    moveTo(roadRight + grassWidth * 0.25f, y + 4f)
                    lineTo(roadRight + grassWidth * 0.45f, y + 8f)
                    lineTo(roadRight + grassWidth * 0.25f, y + 14f)
                    close()
                }
                drawLine(color = Color(0xFF212121), start = Offset(roadRight + grassWidth * 0.25f, y + 2f), end = Offset(roadRight + grassWidth * 0.25f, y + 20f), strokeWidth = 3f)
                drawPath(flagPathRight, color = Color(0xFFFACC15))
            }
        }

        // 2. Red & White Kerbs along road edges (Matching F1 red/white kerbs in screenshot)
        val kerbWidth = w * 0.035f
        val kerbSegHeight = h / 24f
        val kerbScrollPx = (roadScrollOffset * kerbSegHeight * 2f)

        for (i in -4..28) {
            val y = (i * kerbSegHeight) + (kerbScrollPx % (kerbSegHeight * 2f))
            val isRed = (i % 2 == 0)
            val kerbColor = if (isRed) Color(0xFFD32F2F) else Color(0xFFFFFFFF)

            // Left Kerb
            drawRect(
                color = kerbColor,
                topLeft = Offset(roadLeft - kerbWidth, y),
                size = Size(kerbWidth, kerbSegHeight)
            )
            // Right Kerb
            drawRect(
                color = kerbColor,
                topLeft = Offset(roadRight, y),
                size = Size(kerbWidth, kerbSegHeight)
            )
        }

        // 3. Asphalt Road Surface
        drawRect(
            color = Color(0xFF263238),
            topLeft = Offset(roadLeft, 0f),
            size = Size(roadWidth, h)
        )

        // Subtle motion speed streaks on road
        if (isBoosting) {
            for (i in 0..15) {
                val lineX = roadLeft + (i / 15f) * roadWidth
                val lineY = ((i * 137 + roadScrollOffset * h * 2f) % h)
                drawLine(
                    color = Color(0x3300E5FF),
                    start = Offset(lineX, lineY),
                    end = Offset(lineX, lineY + 60f),
                    strokeWidth = 3f
                )
            }
        }

        // 4. Multi-lane White Dashed Lines (3 lanes -> 2 divider lines)
        val numLanes = 3
        val laneWidth = roadWidth / numLanes
        val dashHeight = h / 14f
        val gapHeight = h / 14f
        val dashTotal = dashHeight + gapHeight
        val dashScrollPx = (roadScrollOffset * dashTotal) % dashTotal

        for (lane in 1 until numLanes) {
            val dividerX = roadLeft + (lane * laneWidth)
            var currentY = -dashTotal + dashScrollPx
            while (currentY < h) {
                drawRoundRect(
                    color = Color(0xEEFFFFFF),
                    topLeft = Offset(dividerX - 3f, currentY),
                    size = Size(6f, dashHeight),
                    cornerRadius = CornerRadius(3f, 3f)
                )
                currentY += dashTotal
            }
        }

        // 5. Draw Pickups
        for (pickup in pickups) {
            if (pickup.collected) continue
            val px = roadCenter + (pickup.xNormalized * (roadWidth * 0.45f))
            val py = pickup.yNormalized * h

            drawPickupItem(px, py, pickup.type)
        }

        // 6. Draw Traffic Cars
        for (car in trafficCars) {
            val cx = roadCenter + (car.xNormalized * (roadWidth * 0.45f))
            val cy = car.yNormalized * h
            val carW = roadWidth * 0.18f
            val carH = h * 0.11f

            drawCarTopDown(
                centerX = cx,
                centerY = cy,
                width = carW,
                height = carH,
                bodyColor = car.color,
                accentColor = Color.White,
                isPlayer = false,
                isBoosting = false
            )
        }

        // 7. Draw Player Car
        val playerPx = roadCenter + (playerXNormalized * (roadWidth * 0.45f))
        val playerPy = h * 0.75f // ~75% down the screen
        val playerW = roadWidth * 0.19f
        val playerH = h * 0.12f

        // Shield Aura
        if (isShieldActive) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x88E040FB), Color(0x00E040FB)),
                    center = Offset(playerPx, playerPy),
                    radius = playerW * 1.3f
                ),
                radius = playerW * 1.3f,
                center = Offset(playerPx, playerPy)
            )
            drawCircle(
                color = Color(0xFFE040FB),
                radius = playerW * 1.0f,
                center = Offset(playerPx, playerPy),
                style = Stroke(width = 4f)
            )
        }

        drawCarTopDown(
            centerX = playerPx,
            centerY = playerPy,
            width = playerW,
            height = playerH,
            bodyColor = activeVehicle.primaryColor,
            accentColor = activeVehicle.accentColor,
            isPlayer = true,
            isBoosting = isBoosting
        )

        // 8. Draw Particles
        for (p in particles) {
            val px = roadCenter + (p.x * (roadWidth * 0.45f))
            val py = p.y * h
            drawCircle(
                color = p.color.copy(alpha = p.alpha),
                radius = p.size,
                center = Offset(px, py)
            )
        }
    }
}

private fun DrawScope.drawCarTopDown(
    centerX: Float,
    centerY: Float,
    width: Float,
    height: Float,
    bodyColor: Color,
    accentColor: Color,
    isPlayer: Boolean,
    isBoosting: Boolean
) {
    val left = centerX - (width / 2f)
    val top = centerY - (height / 2f)

    // 1. Soft Oval Shadow beneath body only (NO square black outline!)
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x77000000), Color(0x00000000)),
            center = Offset(centerX + 2f, centerY + 8f),
            radius = height * 0.45f
        ),
        topLeft = Offset(centerX - width * 0.55f, centerY - height * 0.45f + 8f),
        size = Size(width * 1.10f, height * 0.90f)
    )

    // Nitro boost thruster effect
    if (isBoosting) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFEA00), Color(0xFFFF3D00), Color(0x00000000)),
                center = Offset(centerX, top + height + 12f),
                radius = width * 0.8f
            ),
            radius = width * 0.8f,
            center = Offset(centerX, top + height + 12f)
        )
    }

    // 2. Tucked Sleek Wheels (High performance rubber tires + metallic alloy rims)
    val tireW = width * 0.22f
    val tireH = height * 0.24f
    val frontY = top + height * 0.16f
    val rearY = top + height * 0.64f
    val tireColor = Color(0xFF1E293B)
    val rimColor = Color(0xFF94A3B8)
    val caliperColor = Color(0xFFEF4444)

    // Left Front Wheel
    drawRoundRect(color = tireColor, topLeft = Offset(left - tireW * 0.15f, frontY), size = Size(tireW, tireH), cornerRadius = CornerRadius(4f, 4f))
    drawRoundRect(color = rimColor, topLeft = Offset(left + tireW * 0.1f, frontY + tireH * 0.2f), size = Size(tireW * 0.5f, tireH * 0.6f), cornerRadius = CornerRadius(2f, 2f))
    drawRect(color = caliperColor, topLeft = Offset(left + tireW * 0.25f, frontY + tireH * 0.35f), size = Size(tireW * 0.3f, tireH * 0.3f))

    // Right Front Wheel
    drawRoundRect(color = tireColor, topLeft = Offset(left + width - tireW * 0.85f, frontY), size = Size(tireW, tireH), cornerRadius = CornerRadius(4f, 4f))
    drawRoundRect(color = rimColor, topLeft = Offset(left + width - tireW * 0.6f, frontY + tireH * 0.2f), size = Size(tireW * 0.5f, tireH * 0.6f), cornerRadius = CornerRadius(2f, 2f))
    drawRect(color = caliperColor, topLeft = Offset(left + width - tireW * 0.55f, frontY + tireH * 0.35f), size = Size(tireW * 0.3f, tireH * 0.3f))

    // Left Rear Wheel
    drawRoundRect(color = tireColor, topLeft = Offset(left - tireW * 0.15f, rearY), size = Size(tireW, tireH * 1.1f), cornerRadius = CornerRadius(4f, 4f))
    drawRoundRect(color = rimColor, topLeft = Offset(left + tireW * 0.1f, rearY + tireH * 0.25f), size = Size(tireW * 0.5f, tireH * 0.6f), cornerRadius = CornerRadius(2f, 2f))

    // Right Rear Wheel
    drawRoundRect(color = tireColor, topLeft = Offset(left + width - tireW * 0.85f, rearY), size = Size(tireW, tireH * 1.1f), cornerRadius = CornerRadius(4f, 4f))
    drawRoundRect(color = rimColor, topLeft = Offset(left + width - tireW * 0.6f, rearY + tireH * 0.25f), size = Size(tireW * 0.5f, tireH * 0.6f), cornerRadius = CornerRadius(2f, 2f))

    // 3. Main Aerodynamic Car Body (Hypercar / GT / F1 fusion)
    val bodyPath = Path().apply {
        // Nose / Front bumper tip
        moveTo(centerX, top + height * 0.02f)
        // Right front curve over wheel arch
        cubicTo(
            centerX + width * 0.28f, top + height * 0.06f,
            centerX + width * 0.45f, top + height * 0.15f,
            centerX + width * 0.45f, top + height * 0.28f
        )
        // Tapered waist (door indentation)
        cubicTo(
            centerX + width * 0.42f, top + height * 0.42f,
            centerX + width * 0.38f, top + height * 0.52f,
            centerX + width * 0.46f, top + height * 0.68f
        )
        // Muscular rear fender over rear wheel
        cubicTo(
            centerX + width * 0.48f, top + height * 0.82f,
            centerX + width * 0.40f, top + height * 0.95f,
            centerX + width * 0.24f, top + height * 0.98f
        )
        // Rear diffuser center notch
        lineTo(centerX - width * 0.24f, top + height * 0.98f)
        // Left rear fender
        cubicTo(
            centerX - width * 0.40f, top + height * 0.95f,
            centerX - width * 0.48f, top + height * 0.82f,
            centerX - width * 0.46f, top + height * 0.68f
        )
        // Left waist
        cubicTo(
            centerX - width * 0.38f, top + height * 0.52f,
            centerX - width * 0.42f, top + height * 0.42f,
            centerX - width * 0.45f, top + height * 0.28f
        )
        // Left front curve over wheel arch
        cubicTo(
            centerX - width * 0.45f, top + height * 0.15f,
            centerX - width * 0.28f, top + height * 0.06f,
            centerX, top + height * 0.02f
        )
        close()
    }

    // Body base paint
    drawPath(path = bodyPath, color = bodyColor)

    // Body 3D Light Shading (Gloss Highlight Gradient on left/top curves)
    val bodyHighlightPath = Path().apply {
        moveTo(centerX - width * 0.05f, top + height * 0.04f)
        lineTo(centerX - width * 0.12f, top + height * 0.30f)
        lineTo(centerX - width * 0.28f, top + height * 0.70f)
        lineTo(centerX - width * 0.18f, top + height * 0.94f)
        lineTo(centerX - width * 0.08f, top + height * 0.94f)
        lineTo(centerX - width * 0.20f, top + height * 0.68f)
        lineTo(centerX - width * 0.05f, top + height * 0.30f)
        close()
    }
    drawPath(path = bodyHighlightPath, color = Color(0x35FFFFFF))

    // 4. Center Racing Stripe (Dual Accent Stripes)
    val stripeW = width * 0.10f
    drawRect(
        color = accentColor,
        topLeft = Offset(centerX - stripeW / 2f, top + height * 0.04f),
        size = Size(stripeW, height * 0.92f)
    )

    // 5. Sleek Curved Cockpit & Windshield Glass
    val cockpitPath = Path().apply {
        moveTo(centerX, top + height * 0.28f)
        cubicTo(
            centerX + width * 0.26f, top + height * 0.32f,
            centerX + width * 0.26f, top + height * 0.58f,
            centerX, top + height * 0.64f
        )
        cubicTo(
            centerX - width * 0.26f, top + height * 0.58f,
            centerX - width * 0.26f, top + height * 0.32f,
            centerX, top + height * 0.28f
        )
        close()
    }

    // Cockpit dark tint
    drawPath(path = cockpitPath, color = Color(0xFF0F172A))

    // Windshield Metallic Blue Reflection Glare
    val glassGlarePath = Path().apply {
        moveTo(centerX - width * 0.14f, top + height * 0.32f)
        lineTo(centerX + width * 0.02f, top + height * 0.32f)
        lineTo(centerX - width * 0.08f, top + height * 0.54f)
        lineTo(centerX - width * 0.18f, top + height * 0.50f)
        close()
    }
    drawPath(path = glassGlarePath, color = Color(0x8838BDF8))

    // Driver Helmet inside cockpit
    drawCircle(
        color = Color(0xFFF1F5F9),
        radius = width * 0.09f,
        center = Offset(centerX, top + height * 0.47f)
    )
    drawRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(centerX - width * 0.06f, top + height * 0.44f),
        size = Size(width * 0.12f, height * 0.03f)
    )

    // 6. Integrated Aerodynamic Rear Wing (Matches body color with carbon center)
    val wingTop = top + height * 0.86f
    val wingW = width * 0.88f
    val wingH = height * 0.08f

    // Carbon Fiber Wing Blade
    drawRoundRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(centerX - wingW / 2f, wingTop),
        size = Size(wingW, wingH),
        cornerRadius = CornerRadius(3f, 3f)
    )
    // Wing endplates (Matching car body color)
    drawRoundRect(
        color = bodyColor,
        topLeft = Offset(centerX - wingW / 2f - 2f, wingTop - 2f),
        size = Size(width * 0.08f, wingH + 4f),
        cornerRadius = CornerRadius(2f, 2f)
    )
    drawRoundRect(
        color = bodyColor,
        topLeft = Offset(centerX + wingW / 2f - width * 0.08f + 2f, wingTop - 2f),
        size = Size(width * 0.08f, wingH + 4f),
        cornerRadius = CornerRadius(2f, 2f)
    )

    // 7. Headlights & Taillights
    // Front LED Headlight Strips (Cyan / Xenon White glow)
    drawLine(color = Color(0xFF38BDF8), start = Offset(centerX - width * 0.28f, top + height * 0.08f), end = Offset(centerX - width * 0.12f, top + height * 0.04f), strokeWidth = 3f)
    drawLine(color = Color(0xFF38BDF8), start = Offset(centerX + width * 0.28f, top + height * 0.08f), end = Offset(centerX + width * 0.12f, top + height * 0.04f), strokeWidth = 3f)

    // Rear LED Light Bar (Bright Red glowing strip)
    drawLine(color = Color(0xFFEF4444), start = Offset(centerX - width * 0.26f, top + height * 0.96f), end = Offset(centerX + width * 0.26f, top + height * 0.96f), strokeWidth = 3f)
}

private fun DrawScope.drawPickupItem(x: Float, y: Float, type: PickupType) {
    val radius = 24f
    when (type) {
        PickupType.COIN -> {
            drawCircle(color = Color(0xFFFFD54F), radius = radius, center = Offset(x, y))
            drawCircle(color = Color(0xFFFF8F00), radius = radius, center = Offset(x, y), style = Stroke(width = 4f))
            // Inner 'C' / '$' coin symbol
            drawCircle(color = Color(0xFFFFF8E1), radius = radius * 0.45f, center = Offset(x, y))
        }
        PickupType.BOOST_ENERGY -> {
            drawCircle(color = Color(0xFF00E5FF), radius = radius, center = Offset(x, y))
            drawCircle(color = Color(0xFF0288D1), radius = radius, center = Offset(x, y), style = Stroke(width = 4f))
            // Lightning symbol
            val boltPath = Path().apply {
                moveTo(x + 2f, y - 12f)
                lineTo(x - 8f, y + 2f)
                lineTo(x, y + 2f)
                lineTo(x - 2f, y + 12f)
                lineTo(x + 8f, y - 2f)
                lineTo(x, y - 2f)
                close()
            }
            drawPath(boltPath, color = Color.White)
        }
        PickupType.BATTERY_CELL -> {
            drawCircle(color = Color(0xFF76FF03), radius = radius, center = Offset(x, y))
            drawCircle(color = Color(0xFF33691E), radius = radius, center = Offset(x, y), style = Stroke(width = 4f))
            // Plus / Battery symbol
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(x - 8f, y - 10f),
                size = Size(16f, 20f),
                cornerRadius = CornerRadius(3f, 3f)
            )
        }
        PickupType.SHIELD -> {
            drawCircle(color = Color(0xFFE040FB), radius = radius, center = Offset(x, y))
            drawCircle(color = Color(0xFFAA00FF), radius = radius, center = Offset(x, y), style = Stroke(width = 4f))
            drawCircle(color = Color.White, radius = radius * 0.4f, center = Offset(x, y))
        }
    }
}
