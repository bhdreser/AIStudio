package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.local.ReceiptEntity
import com.example.ui.theme.ExcelGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptDetailModal(
    receipt: ReceiptEntity?,
    onDismiss: () -> Unit,
    onDelete: (ReceiptEntity) -> Unit,
    onUpdate: (ReceiptEntity) -> Unit
) {
    if (receipt == null) return

    var isEditing by remember { mutableStateOf(false) }

    var merchantName by remember(receipt) { mutableStateOf(receipt.merchantName) }
    var date by remember(receipt) { mutableStateOf(receipt.date) }
    var receiptNumber by remember(receipt) { mutableStateOf(receipt.receiptNumber) }
    var category by remember(receipt) { mutableStateOf(receipt.category) }
    var totalAmountStr by remember(receipt) { mutableStateOf(receipt.totalAmount.toString()) }
    var paymentMethod by remember(receipt) { mutableStateOf(receipt.paymentMethod) }
    var itemsSummary by remember(receipt) { mutableStateOf(receipt.itemsSummary) }
    var notes by remember(receipt) { mutableStateOf(receipt.notes) }
    var tags by remember(receipt) { mutableStateOf(receipt.tags) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isEditing) "Fiş Düzenle" else receipt.merchantName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${receipt.date} • ${receipt.category}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row {
                        IconButton(onClick = { isEditing = !isEditing }) {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.Visibility else Icons.Default.Edit,
                                contentDescription = "Düzenle"
                            )
                        }
                        IconButton(onClick = { onDelete(receipt) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Sil",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Kapat")
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (receipt.imagePath.isNotBlank()) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        ) {
                            AsyncImage(
                                model = receipt.imagePath,
                                contentDescription = "Fiş Fotoğrafı",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    if (isEditing) {
                        OutlinedTextField(
                            value = merchantName,
                            onValueChange = { merchantName = it },
                            label = { Text("Mağaza Adı") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = date,
                                onValueChange = { date = it },
                                label = { Text("Tarih") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = receiptNumber,
                                onValueChange = { receiptNumber = it },
                                label = { Text("Fiş No") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Kategori") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = totalAmountStr,
                            onValueChange = { totalAmountStr = it },
                            label = { Text("Toplam Tutar (₺)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = itemsSummary,
                            onValueChange = { itemsSummary = it },
                            label = { Text("Ürün / Hizmet Özeti") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notlar") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = tags,
                            onValueChange = { tags = it },
                            label = { Text("Etiketler (Örn: İş, Kişisel, Seyahat, Proje)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else {
                        // Display Cards
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("TOPLAM TUTAR", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    Text(
                                        text = "${String.format("%.2f", receipt.totalAmount)} ${receipt.currency}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Surface(
                                    color = ExcelGreen,
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(
                                        text = receipt.category,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        DetailRow("Fiş / Belge No", receipt.receiptNumber.ifBlank { "-" })
                        DetailRow("Ödeme Şekli", receipt.paymentMethod)
                        DetailRow("KDV Oranı & Tutarı", "%${receipt.taxRate} (${String.format("%.2f", receipt.taxAmount)} ₺)")
                        DetailRow("Matrah (KDV Hariç)", "${String.format("%.2f", receipt.subtotal)} ₺")

                        if (receipt.tags.isNotBlank()) {
                            DetailRow("Özel Etiketler", receipt.tags)
                        }

                        if (receipt.itemsSummary.isNotBlank()) {
                            DetailRow("Satın Alınan Kalemler", receipt.itemsSummary)
                        }

                        if (receipt.notes.isNotBlank()) {
                            DetailRow("Açıklama / Notlar", receipt.notes)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isEditing) {
                    Button(
                        onClick = {
                            val newTotal = totalAmountStr.toDoubleOrNull() ?: receipt.totalAmount
                            val updated = receipt.copy(
                                merchantName = merchantName,
                                date = date,
                                receiptNumber = receiptNumber,
                                category = category,
                                totalAmount = newTotal,
                                itemsSummary = itemsSummary,
                                notes = notes,
                                tags = tags
                            )
                            onUpdate(updated)
                            isEditing = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ExcelGreen)
                    ) {
                        Text("Değişiklikleri Kaydet", fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tamam")
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)
    }
}
