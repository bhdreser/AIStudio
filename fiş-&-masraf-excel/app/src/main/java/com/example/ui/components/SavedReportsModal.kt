package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.data.local.ReportEntity
import com.example.ui.theme.ExcelGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedReportsModal(
    reports: List<ReportEntity>,
    allReceipts: List<ReceiptEntity>,
    onDismiss: () -> Unit,
    onCreateReport: (title: String, description: String, versionName: String, receiptIds: List<Long>) -> Unit,
    onDeleteReport: (Long) -> Unit,
    onAnalyzeReport: (ReportEntity) -> Unit,
    onExportReport: (ReportEntity) -> Unit,
    onExportReportPdf: (ReportEntity) -> Unit = {},
    aiAnalysisLoading: Boolean = false,
    selectedReportAnalysis: String? = null
) {
    var isCreatingNewReport by remember { mutableStateOf(false) }
    var selectedReportForView by remember { mutableStateOf<ReportEntity?>(null) }

    // Form states for creation
    var reportTitle by remember { mutableStateOf("") }
    var reportDescription by remember { mutableStateOf("") }
    var versionName by remember { mutableStateOf("v1.0") }
    var selectedReceiptIds by remember { mutableStateOf(allReceipts.map { it.id }.toSet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            if (isCreatingNewReport) {
                Button(
                    onClick = {
                        if (reportTitle.isNotBlank()) {
                            onCreateReport(
                                reportTitle.trim(),
                                reportDescription.trim(),
                                versionName.trim().ifBlank { "v1.0" },
                                selectedReceiptIds.toList()
                            )
                            isCreatingNewReport = false
                            reportTitle = ""
                            reportDescription = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExcelGreen),
                    enabled = reportTitle.isNotBlank() && selectedReceiptIds.isNotEmpty()
                ) {
                    Text("Raporu Kaydet", fontWeight = FontWeight.Bold)
                }
            } else if (selectedReportForView != null) {
                Button(
                    onClick = { selectedReportForView = null },
                    colors = ButtonDefaults.buttonColors(containerColor = ExcelGreen)
                ) {
                    Text("Geri Dön")
                }
            } else {
                Button(
                    onClick = { isCreatingNewReport = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ExcelGreen)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Yeni Özel Rapor Oluştur", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                if (isCreatingNewReport) {
                    isCreatingNewReport = false
                } else if (selectedReportForView != null) {
                    selectedReportForView = null
                } else {
                    onDismiss()
                }
            }) {
                Text(if (isCreatingNewReport || selectedReportForView != null) "İptal" else "Kapat")
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FolderSpecial,
                    contentDescription = null,
                    tint = ExcelGreen,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = when {
                            isCreatingNewReport -> "Yeni Özel Rapor Tanımla"
                            selectedReportForView != null -> selectedReportForView!!.title
                            else -> "Kayıtlı Masraf Raporları"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when {
                            isCreatingNewReport -> "İstediğiniz isim ve versiyonla masrafları gruplayın"
                            selectedReportForView != null -> "Rapor Versiyonu: ${selectedReportForView!!.versionName}"
                            else -> "Özel isimli versiyonlanmış rapor arşiviniz"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
            ) {
                if (isCreatingNewReport) {
                    // Create Form
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = reportTitle,
                            onValueChange = { reportTitle = it },
                            label = { Text("Rapor Adı (Örn: Temmuz 2026 Almanya Gezisi)") },
                            placeholder = { Text("Özel rapor adınızı girin...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = versionName,
                                onValueChange = { versionName = it },
                                label = { Text("Versiyon") },
                                placeholder = { Text("v1.0") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        OutlinedTextField(
                            value = reportDescription,
                            onValueChange = { reportDescription = it },
                            label = { Text("Açıklama veya Notlar (Opsiyonel)") },
                            placeholder = { Text("Rapor amacı, departman veya bütçe kodu...") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Text(
                            text = "DÂHİL EDİLECEK FİŞLERİ SEÇİN (${selectedReceiptIds.size}/${allReceipts.size})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (allReceipts.isEmpty()) {
                            Text(
                                text = "Henüz kayıtlı fiş bulunmuyor. Önce fiş ekleyin.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                LazyColumn(
                                    modifier = Modifier.padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(allReceipts) { receipt ->
                                        val isChecked = selectedReceiptIds.contains(receipt.id)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedReceiptIds = if (isChecked) {
                                                        selectedReceiptIds - receipt.id
                                                    } else {
                                                        selectedReceiptIds + receipt.id
                                                    }
                                                }
                                                .padding(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = isChecked,
                                                onCheckedChange = { checked ->
                                                    selectedReceiptIds = if (checked == true) {
                                                        selectedReceiptIds + receipt.id
                                                    } else {
                                                        selectedReceiptIds - receipt.id
                                                    }
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = receipt.merchantName,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "${receipt.date} - ${receipt.category}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = "${String.format("%.2f", receipt.totalAmount)} ${receipt.currency}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = ExcelGreen
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (selectedReportForView != null) {
                    // View Report Detail
                    val report = selectedReportForView!!
                    val reportReceipts = remember(report, allReceipts) {
                        val ids = report.receiptIds.split(",").mapNotNull { it.trim().toLongOrNull() }.toSet()
                        allReceipts.filter { ids.contains(it.id) }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Stat Cards
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ExcelGreen.copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Toplam Rapor Tutarı", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = "${String.format(Locale("tr", "TR"), "%.2f", report.totalAmount)} ₺",
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = ExcelGreen
                                        )
                                    }
                                    Surface(
                                        color = ExcelGreen,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = report.versionName,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                if (report.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = report.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Actions Bar: AI Analysis, Excel Export & PDF Export
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { onAnalyzeReport(report) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AI Rapor Analizi & Vergi Tavsiyesi", style = MaterialTheme.typography.labelMedium)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onExportReportPdf(report) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("PDF Belgesi", style = MaterialTheme.typography.labelMedium)
                                }

                                Button(
                                    onClick = { onExportReport(report) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = ExcelGreen),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Excel Tablosu", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }

                        // AI Analysis Output Box
                        if (aiAnalysisLoading) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = ExcelGreen)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Gemini Yapay Zeka Raporu Analiz Ediyor...", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        } else if (!selectedReportAnalysis.isNullOrBlank()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = ButtonDefaults.outlinedButtonBorder
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ExcelGreen)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Yapay Zeka Yönetici Özeti",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = selectedReportAnalysis,
                                        style = MaterialTheme.typography.bodySmall,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        Text(
                            text = "RAPORDAKİ FİŞLER (${reportReceipts.size})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        reportReceipts.forEach { receipt ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(receipt.merchantName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text("${receipt.date} • ${receipt.category}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(
                                        text = "${String.format("%.2f", receipt.totalAmount)} ${receipt.currency}",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ExcelGreen,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Reports List View
                    if (reports.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Henüz kayıtlı raporunuz bulunmuyor.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Fişlerinizi ay, gezi veya proje bazında özel isim vererek raporlayabilirsiniz.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(reports, key = { it.id }) { report ->
                                ReportItemCard(
                                    report = report,
                                    onClick = { selectedReportForView = report },
                                    onDelete = { onDeleteReport(report.id) },
                                    onExport = { onExportReport(report) },
                                    onExportPdf = { onExportReportPdf(report) }
                                )
                            }
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun ReportItemCard(
    report: ReportEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit,
    onExportPdf: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        color = ExcelGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = ExcelGreen
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = report.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Oluşturulma: ${report.createdAt.ifBlank { "Yeni" }} • ${report.receiptCount} Fiş",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = report.versionName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Toplam Tutar",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${String.format(Locale("tr", "TR"), "%.2f", report.totalAmount)} ₺",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = ExcelGreen
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onExportPdf, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF İndir", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onExport, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.TableChart, contentDescription = "Excel İndir", tint = ExcelGreen, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Sil", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
