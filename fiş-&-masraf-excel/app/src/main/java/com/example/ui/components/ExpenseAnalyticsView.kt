package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ReceiptEntity
import com.example.ui.theme.ExcelGreen
import java.util.Locale

data class CategorySummary(
    val category: String,
    val totalAmount: Double,
    val count: Int,
    val percentage: Float,
    val color: Color
)

@Composable
fun ExpenseAnalyticsModal(
    receipts: List<ReceiptEntity>,
    onDismiss: () -> Unit,
    onCategorySelected: (String) -> Unit
) {
    val totalSpent = remember(receipts) { receipts.sumOf { it.totalAmount } }
    val categorySummaries = remember(receipts, totalSpent) {
        val grouped = receipts.groupBy { if (it.category.isBlank()) "Diğer" else it.category }
        grouped.map { (cat, list) ->
            val sum = list.sumOf { it.totalAmount }
            val pct = if (totalSpent > 0) (sum / totalSpent).toFloat() else 0f
            CategorySummary(
                category = cat,
                totalAmount = sum,
                count = list.size,
                percentage = pct,
                color = getCategoryColor(cat)
            )
        }.sortedByDescending { it.totalAmount }
    }

    var selectedCategoryTab by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ExcelGreen)
            ) {
                Text("Tamam", fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = ExcelGreen,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Aylık Kategori Harcama Analizi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Fiş kategorilerine göre harcama dağılımı",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
            ) {
                // Header Stat
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Toplam Analiz Edilen Harcama",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${String.format(Locale("tr", "TR"), "%.2f", totalSpent)} ₺",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = ExcelGreen
                            )
                        }

                        Surface(
                            color = ExcelGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "${receipts.size} Fiş Kaydı",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = ExcelGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                var selectedTab by remember { mutableStateOf(0) }
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = ExcelGreen
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Kategori Dağılımı", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Ay-Ay Trend (MoM)", fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTab == 1) {
                    MonthOverMonthLineChartCard(
                        receipts = receipts,
                        modifier = Modifier.padding(horizontal = 0.dp)
                    )
                } else if (categorySummaries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Henüz grafik için harcama verisi bulunmuyor.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text = "KATEGORİ BAZLI BARI GRAFİK",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categorySummaries) { summary ->
                            CategoryBarItem(
                                summary = summary,
                                onClick = {
                                    onCategorySelected(summary.category)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun CategoryBarItem(
    summary: CategorySummary,
    onClick: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = summary.percentage,
        animationSpec = tween(durationMillis = 800),
        label = "barAnimation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(summary.color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = summary.category,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${summary.count} fiş)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "${String.format(Locale("tr", "TR"), "%.2f", summary.totalAmount)} ₺",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bar Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .clip(RoundedCornerShape(5.dp))
                        .background(summary.color)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "%${String.format(Locale("tr", "TR"), "%.1f", summary.percentage * 100)} pay",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

private fun getCategoryColor(category: String): Color {
    return when {
        category.contains("Market", ignoreCase = true) -> Color(0xFF2E7D32)
        category.contains("Yiyecek", ignoreCase = true) -> Color(0xFFE65100)
        category.contains("Akaryakıt", ignoreCase = true) || category.contains("Ulaşım", ignoreCase = true) -> Color(0xFF0277BD)
        category.contains("Ofis", ignoreCase = true) -> Color(0xFF6A1B9A)
        category.contains("Konaklama", ignoreCase = true) -> Color(0xFFC2185B)
        category.contains("Sağlık", ignoreCase = true) -> Color(0xFFD32F2F)
        else -> Color(0xFF455A64)
    }
}
