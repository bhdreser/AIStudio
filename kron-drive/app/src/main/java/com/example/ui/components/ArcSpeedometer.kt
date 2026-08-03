package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.KronAccent
import com.example.ui.theme.KronCard
import com.example.ui.theme.KronMuted
import com.example.ui.theme.KronNegative
import com.example.ui.theme.KronPosDef
import com.example.ui.theme.KronText

@Composable
fun ArcSpeedometer(
    modifier: Modifier = Modifier,
    speedKmh: Int,
    torqueNm: Int,
    batteryPercent: Int
) {
    Box(
        modifier = modifier.size(175.dp),
        contentAlignment = Alignment.Center
    ) {
        // 1. Canvas for Arc Gauges and Grid Lines
        Canvas(modifier = Modifier.size(175.dp)) {
            val centerPx = Offset(size.width / 2f, size.height / 2f)
            val radiusPx = (size.width / 2f) - 16f

            // Dark semi-transparent circular backdrop
            drawCircle(
                color = KronCard.copy(alpha = 0.75f),
                radius = radiusPx + 10f,
                center = centerPx
            )

            // Outer subtle grid circle
            drawCircle(
                color = Color(0x66334155),
                radius = radiusPx + 2f,
                center = centerPx,
                style = Stroke(width = 1.5f)
            )

            // Main Arc Gauge (from 140 deg to 300 deg sweep)
            val startAngle = 135f
            val maxSweep = 230f
            val currentSweep = ((speedKmh / 240f) * maxSweep).coerceIn(10f, maxSweep)

            // Inactive arc track
            drawArc(
                color = Color(0x44334155),
                startAngle = startAngle,
                sweepAngle = maxSweep,
                useCenter = false,
                topLeft = Offset(16f, 16f),
                size = Size(size.width - 32f, size.height - 32f),
                style = Stroke(width = 12f, cap = StrokeCap.Round)
            )

            // Active sweep arc track with Kron gradient
            drawArc(
                brush = Brush.sweepGradient(
                    0.0f to KronPosDef,  // Cyan #38BDF8
                    0.6f to KronAccent,  // Yellow #FACC15
                    1.0f to KronNegative // Red #F87171
                ),
                startAngle = startAngle,
                sweepAngle = currentSweep,
                useCenter = false,
                topLeft = Offset(16f, 16f),
                size = Size(size.width - 32f, size.height - 32f),
                style = Stroke(width = 12f, cap = StrokeCap.Round)
            )
        }

        // 2. Central Text Overlay
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 6.dp)
        ) {
            // TORQUE Label
            Text(
                text = "TORQUE",
                color = KronPosDef,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "${torqueNm}nm",
                color = KronText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            // Big Speed Number
            Text(
                text = "$speedKmh",
                color = KronText,
                fontSize = 46.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif
            )

            // KM / H Label
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "KM / H",
                    color = KronPosDef,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // BATTERY Level
            Text(
                text = "BATTERY $batteryPercent%",
                color = if (batteryPercent > 20) KronPosDef else KronNegative,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

