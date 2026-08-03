package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ReceiptEntity
import com.example.ui.theme.ExcelGreen
import java.text.SimpleDateFormat
import java.util.*

data class MonthlySpending(
    val yearMonthKey: String,
    val monthLabel: String,
    val totalAmount: Double,
    val receiptCount: Int,
    val momChangePercent: Double?
)

fun calculateMonthlySpendingList(receipts: List<ReceiptEntity>): List<MonthlySpending> {
    if (receipts.isEmpty()) return emptyList()

    val df1 = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val df2 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val monthKeyFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    val labelFormat = SimpleDateFormat("MMM yy", Locale("tr", "TR"))

    val groupedMap = mutableMapOf<String, Pair<Double, Int>>()
    val dateForGroup = mutableMapOf<String, Date>()

    receipts.forEach { receipt ->
        val dateObj = try {
            df1.parse(receipt.date)
        } catch (e: Exception) {
            try { df2.parse(receipt.date) } catch (e2: Exception) { Date(receipt.timestamp) }
        } ?: Date(receipt.timestamp)

        val key = monthKeyFormat.format(dateObj)
        val current = groupedMap.getOrDefault(key, Pair(0.0, 0))
        groupedMap[key] = Pair(current.first + receipt.totalAmount, current.second + 1)
        if (!dateForGroup.containsKey(key)) {
            dateForGroup[key] = dateObj
        }
    }

    val sortedKeys = groupedMap.keys.sorted()
    val result = mutableListOf<MonthlySpending>()

    var prevAmount: Double? = null
    sortedKeys.forEach { key ->
        val (total, count) = groupedMap[key]!!
        val dateObj = dateForGroup[key] ?: Date()
        val label = labelFormat.format(dateObj).replaceFirstChar { it.uppercase() }

        val changePct = if (prevAmount != null && prevAmount!! > 0) {
            ((total - prevAmount!!) / prevAmount!!) * 100.0
        } else {
            null
        }

        result.add(
            MonthlySpending(
                yearMonthKey = key,
                monthLabel = label,
                totalAmount = total,
                receiptCount = count,
                momChangePercent = changePct
            )
        )
        prevAmount = total
    }

    return result
}

@Composable
fun MonthOverMonthLineChartCard(
    receipts: List<ReceiptEntity>,
    modifier: Modifier = Modifier,
    onMonthClick: ((MonthlySpending) -> Unit)? = null
) {
    val monthlyData = remember(receipts) { calculateMonthlySpendingList(receipts) }
    var selectedIndex by remember(monthlyData) { mutableStateOf(if (monthlyData.isNotEmpty()) monthlyData.lastIndex else -1) }

    val selectedData = if (selectedIndex in monthlyData.indices) monthlyData[selectedIndex] else null
    val latestMomChange = monthlyData.lastOrNull()?.momChangePercent

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Card Title & MoM Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = ExcelGreen.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ShowChart,
                                contentDescription = null,
                                tint = ExcelGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Ay-Ay Harcama Trendi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Aylık değişim karşılaştırması (MoM)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Latest MoM Badge
                if (latestMomChange != null) {
                    val isIncrease = latestMomChange > 0
                    val isFlat = Math.abs(latestMomChange) < 0.1
                    val badgeBg = when {
                        isFlat -> MaterialTheme.colorScheme.surfaceVariant
                        isIncrease -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                        else -> ExcelGreen.copy(alpha = 0.15f)
                    }
                    val badgeContentColor = when {
                        isFlat -> MaterialTheme.colorScheme.onSurfaceVariant
                        isIncrease -> MaterialTheme.colorScheme.error
                        else -> ExcelGreen
                    }
                    val icon = when {
                        isFlat -> Icons.Default.TrendingFlat
                        isIncrease -> Icons.Default.TrendingUp
                        else -> Icons.Default.TrendingDown
                    }

                    Surface(
                        color = badgeBg,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(icon, contentDescription = null, tint = badgeContentColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format(Locale("tr", "TR"), "%+.1f%%", latestMomChange),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = badgeContentColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (monthlyData.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Henüz aylık trend çizelgesi için veri yok.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Line Chart Canvas
                LineChartCanvas(
                    dataList = monthlyData,
                    selectedIndex = selectedIndex,
                    onSelectIndex = { idx ->
                        selectedIndex = idx
                        if (idx in monthlyData.indices && onMonthClick != null) {
                            onMonthClick(monthlyData[idx])
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Detail Card for Selected Month
                if (selectedData != null) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${selectedData.monthLabel} Harcaması",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = String.format(Locale("tr", "TR"), "%,.2f ₺", selectedData.totalAmount),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ExcelGreen
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${selectedData.receiptCount} Fiş",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                                if (selectedData.momChangePercent != null) {
                                    val pct = selectedData.momChangePercent
                                    val color = if (pct > 0) MaterialTheme.colorScheme.error else ExcelGreen
                                    Text(
                                        text = String.format(Locale("tr", "TR"), "Önceki aya göre: %+.1f%%", pct),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = color
                                    )
                                } else {
                                    Text(
                                        text = "İlk Kayıtlı Ay",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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
private fun LineChartCanvas(
    dataList: List<MonthlySpending>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val maxVal = remember(dataList) {
        val highest = dataList.maxOfOrNull { it.totalAmount } ?: 1.0
        if (highest == 0.0) 100.0 else highest * 1.15
    }

    val chartLineColor = ExcelGreen
    val gridLineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    val textStyle = TextStyle(
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .pointerInput(dataList) {
                detectTapGestures { tapOffset ->
                    val width = size.width
                    val paddingLeft = 40.dp.toPx()
                    val paddingRight = 20.dp.toPx()
                    val usableWidth = width - paddingLeft - paddingRight

                    if (dataList.isNotEmpty()) {
                        val stepX = if (dataList.size > 1) usableWidth / (dataList.size - 1) else usableWidth / 2
                        var closestIndex = 0
                        var minDistance = Float.MAX_VALUE

                        for (i in dataList.indices) {
                            val x = if (dataList.size > 1) paddingLeft + i * stepX else paddingLeft + usableWidth / 2
                            val dist = Math.abs(tapOffset.x - x)
                            if (dist < minDistance) {
                                minDistance = dist
                                closestIndex = i
                            }
                        }
                        onSelectIndex(closestIndex)
                    }
                }
            }
    ) {
        val width = size.width
        val height = size.height

        val paddingLeft = 40.dp.toPx()
        val paddingRight = 20.dp.toPx()
        val paddingTop = 24.dp.toPx()
        val paddingBottom = 28.dp.toPx()

        val usableWidth = width - paddingLeft - paddingRight
        val usableHeight = height - paddingTop - paddingBottom

        // Draw 3 horizontal grid lines (Max, Mid, Min)
        val gridSteps = 3
        for (i in 0 until gridSteps) {
            val ratio = i.toFloat() / (gridSteps - 1)
            val y = paddingTop + ratio * usableHeight
            val valueAtY = maxVal * (1f - ratio)

            drawLine(
                color = gridLineColor,
                start = Offset(paddingLeft, y),
                end = Offset(width - paddingRight, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
            )

            // Y-Axis label
            val yLabelText = if (valueAtY >= 1000) {
                String.format(Locale("tr", "TR"), "%.0fK", valueAtY / 1000)
            } else {
                String.format(Locale("tr", "TR"), "%.0f", valueAtY)
            }
            val textLayoutResult = textMeasurer.measure(yLabelText, style = textStyle)
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(paddingLeft - textLayoutResult.size.width - 6.dp.toPx(), y - textLayoutResult.size.height / 2)
            )
        }

        if (dataList.isEmpty()) return@Canvas

        val stepX = if (dataList.size > 1) usableWidth / (dataList.size - 1) else usableWidth / 2

        // Compute Points
        val points = dataList.mapIndexed { i, item ->
            val x = if (dataList.size > 1) paddingLeft + i * stepX else paddingLeft + usableWidth / 2
            val yRatio = (item.totalAmount / maxVal).toFloat().coerceIn(0f, 1f)
            val y = paddingTop + (1f - yRatio) * usableHeight
            Offset(x, y)
        }

        // Build Curved Path
        val path = Path()
        val fillPath = Path()

        if (points.isNotEmpty()) {
            path.moveTo(points[0].x, points[0].y)
            fillPath.moveTo(points[0].x, height - paddingBottom)
            fillPath.lineTo(points[0].x, points[0].y)

            for (i in 0 until points.size - 1) {
                val p1 = points[i]
                val p2 = points[i + 1]
                val controlX1 = p1.x + (p2.x - p1.x) / 2
                val controlY1 = p1.y
                val controlX2 = p1.x + (p2.x - p1.x) / 2
                val controlY2 = p2.y

                path.cubicTo(controlX1, controlY1, controlX2, controlY2, p2.x, p2.y)
                fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, p2.x, p2.y)
            }

            fillPath.lineTo(points.last().x, height - paddingBottom)
            fillPath.close()

            // Draw Gradient Shading below line
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        chartLineColor.copy(alpha = 0.35f),
                        chartLineColor.copy(alpha = 0.02f)
                    ),
                    startY = paddingTop,
                    endY = height - paddingBottom
                )
            )

            // Draw Smooth Line
            drawPath(
                path = path,
                color = chartLineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        // Draw Nodes and Labels
        points.forEachIndexed { i, point ->
            val isSelected = i == selectedIndex
            val item = dataList[i]

            // Node Circle
            if (isSelected) {
                drawCircle(
                    color = chartLineColor.copy(alpha = 0.25f),
                    radius = 12.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = Color.White,
                    radius = 7.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = chartLineColor,
                    radius = 5.dp.toPx(),
                    center = point
                )
            } else {
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = chartLineColor,
                    radius = 3.5.dp.toPx(),
                    center = point
                )
            }

            // X-Axis Month Label
            val labelTextResult = textMeasurer.measure(item.monthLabel, style = textStyle)
            drawText(
                textLayoutResult = labelTextResult,
                topLeft = Offset(point.x - labelTextResult.size.width / 2, height - paddingBottom + 6.dp.toPx())
            )

            // Selected value floating bubble on top
            if (isSelected) {
                val valStr = String.format(Locale("tr", "TR"), "%.0f ₺", item.totalAmount)
                val bubbleStyle = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = chartLineColor
                )
                val valTextResult = textMeasurer.measure(valStr, style = bubbleStyle)
                drawText(
                    textLayoutResult = valTextResult,
                    topLeft = Offset(
                        (point.x - valTextResult.size.width / 2).coerceIn(4.dp.toPx(), width - valTextResult.size.width - 4.dp.toPx()),
                        (point.y - valTextResult.size.height - 8.dp.toPx()).coerceAtLeast(0f)
                    )
                )
            }
        }
    }
}
