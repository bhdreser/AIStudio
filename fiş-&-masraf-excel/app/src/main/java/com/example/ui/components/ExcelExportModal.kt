package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.local.ReceiptEntity
import com.example.ui.theme.ExcelGreen

@Composable
fun ExcelExportModal(
    receiptsCount: Int,
    totalSpend: Double,
    onDismiss: () -> Unit,
    onConfirmExport: (useHtmlXls: Boolean) -> Unit,
    onConfirmExportPdf: () -> Unit = {}
) {
    var selectedOption by remember { mutableStateOf(0) } // 0 = Excel (.xls), 1 = CSV (.csv), 2 = PDF Document (.pdf)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    tint = ExcelGreen,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Dışa Aktar & Raporla",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Fişleriniz otomatik olarak sütunlandırılmış Excel, CSV veya resmi PDF masraf raporuna dönüştürülecektir.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Summary Badge Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = ExcelGreen.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Aktarılacak Fiş Sayısı",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$receiptsCount Adet Fiş",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Toplam Masraf",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${String.format("%.2f", totalSpend)} ₺",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ExcelGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Format Options
                Text(
                    text = "Dosya Biçimi Seçin:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                FormatOptionCard(
                    title = "PDF Rapor Belgesi (.pdf)",
                    description = "Şirket içi onaylar, muhasebe ve e-posta gönderimi için resmi A4 PDF formatı.",
                    isSelected = selectedOption == 2,
                    onClick = { selectedOption = 2 }
                )

                Spacer(modifier = Modifier.height(8.dp))

                FormatOptionCard(
                    title = "Excel Çalışma Kitabı (.xls)",
                    description = "Renkli başlıklar, KDV sütunları ve hazır tablo biçimi ile Microsoft Excel tablosu.",
                    isSelected = selectedOption == 0,
                    onClick = { selectedOption = 0 }
                )

                Spacer(modifier = Modifier.height(8.dp))

                FormatOptionCard(
                    title = "CSV Metin Formatı (.csv)",
                    description = "UTF-8 Türkçe karakter destekli standart virgüllü/noktalı virgüllü liste.",
                    isSelected = selectedOption == 1,
                    onClick = { selectedOption = 1 }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Vazgeç")
                    }

                    Button(
                        onClick = {
                            when (selectedOption) {
                                2 -> onConfirmExportPdf()
                                0 -> onConfirmExport(true)
                                else -> onConfirmExport(false)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedOption == 2) MaterialTheme.colorScheme.tertiary else ExcelGreen
                        ),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Icon(
                            imageVector = if (selectedOption == 2) Icons.Default.PictureAsPdf else Icons.Default.FileDownload,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (selectedOption == 2) "PDF İndir" else "Excel'i İndir", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun FormatOptionCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) ExcelGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) ExcelGreen else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = ExcelGreen)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
