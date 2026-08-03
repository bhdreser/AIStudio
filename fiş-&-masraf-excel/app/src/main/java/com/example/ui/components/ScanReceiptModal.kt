package com.example.ui.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.remote.ParsedReceipt
import com.example.ui.theme.ExcelGreen

val CATEGORY_OPTIONS = listOf(
    "Market",
    "Yiyecek & İçecek",
    "Akaryakıt & Ulaşım",
    "Ofis Malzemesi",
    "Konaklama & Seyahat",
    "Sağlık & Bakım",
    "Eğlence",
    "Diğer"
)

val PAYMENT_OPTIONS = listOf(
    "Kredi Kartı",
    "Nakit",
    "Bankamatik Kartı",
    "Havale / EFT",
    "Diğer"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanReceiptModal(
    isScanning: Boolean,
    progressMessage: String,
    scanError: String?,
    parsedReceipt: ParsedReceipt?,
    imageUri: Uri?,
    onDismiss: () -> Unit,
    onSave: (ParsedReceipt, Uri?, String) -> Unit
) {
    if (!isScanning && parsedReceipt == null && scanError == null) return

    Dialog(
        onDismissRequest = { if (!isScanning) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = ExcelGreen,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isScanning) "AI Fiş Taranıyor..." else "Fiş Bilgileri Doğrulama",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!isScanning) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Kapat")
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                if (isScanning) {
                    // Scanning State Animation
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (imageUri != null) {
                                Box(
                                    modifier = Modifier
                                        .size(200.dp, 280.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(2.dp, ExcelGreen, RoundedCornerShape(16.dp))
                                ) {
                                    AsyncImage(
                                        model = imageUri,
                                        contentDescription = "Taranan Fiş",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(50.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 4.dp
                                    )
                                }
                            } else {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(60.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = progressMessage,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Gemini AI mağaza adı, tutar, KDV ve tarihi çıkarıyor...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // Result verification / editing form
                    var merchantName by remember(parsedReceipt) { mutableStateOf(parsedReceipt?.merchantName ?: "") }
                    var date by remember(parsedReceipt) { mutableStateOf(parsedReceipt?.date ?: "") }
                    var receiptNumber by remember(parsedReceipt) { mutableStateOf(parsedReceipt?.receiptNumber ?: "") }
                    var category by remember(parsedReceipt) { mutableStateOf(parsedReceipt?.category ?: "Market") }
                    var totalAmountStr by remember(parsedReceipt) { mutableStateOf(parsedReceipt?.totalAmount?.toString() ?: "0.0") }
                    var currency by remember(parsedReceipt) { mutableStateOf(parsedReceipt?.currency ?: "₺") }
                    var taxRateStr by remember(parsedReceipt) { mutableStateOf(parsedReceipt?.taxRate?.toString() ?: "20.0") }
                    var taxAmountStr by remember(parsedReceipt) { mutableStateOf(parsedReceipt?.taxAmount?.toString() ?: "0.0") }
                    var paymentMethod by remember(parsedReceipt) { mutableStateOf(parsedReceipt?.paymentMethod ?: "Kredi Kartı") }
                    var itemsSummary by remember(parsedReceipt) { mutableStateOf(parsedReceipt?.itemsSummary ?: "") }
                    var notes by remember(parsedReceipt) { mutableStateOf(parsedReceipt?.notes ?: "") }

                    var isCategoryExpanded by remember { mutableStateOf(false) }
                    var isPaymentExpanded by remember { mutableStateOf(false) }
                    var isCurrencyExpanded by remember { mutableStateOf(false) }

                    if (scanError != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = scanError,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (imageUri != null) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                            ) {
                                AsyncImage(
                                    model = imageUri,
                                    contentDescription = "Fiş Görseli",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        // Merchant Name
                        OutlinedTextField(
                            value = merchantName,
                            onValueChange = { merchantName = it },
                            label = { Text("Mağaza / Firma Adı *") },
                            leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Date and Receipt Number
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = date,
                                onValueChange = { date = it },
                                label = { Text("Tarih (GG.AA.YYYY)") },
                                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = receiptNumber,
                                onValueChange = { receiptNumber = it },
                                label = { Text("Fiş No") },
                                leadingIcon = { Icon(Icons.Default.Receipt, contentDescription = null) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        // Category Dropdown
                        ExposedDropdownMenuBox(
                            expanded = isCategoryExpanded,
                            onExpandedChange = { isCategoryExpanded = !isCategoryExpanded }
                        ) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Kategori") },
                                leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = isCategoryExpanded,
                                onDismissRequest = { isCategoryExpanded = false }
                            ) {
                                CATEGORY_OPTIONS.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            category = option
                                            isCategoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Amounts & Currency
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = totalAmountStr,
                                onValueChange = { totalAmountStr = it },
                                label = { Text("Tutar *") },
                                leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1.2f),
                                singleLine = true
                            )

                            ExposedDropdownMenuBox(
                                expanded = isCurrencyExpanded,
                                onExpandedChange = { isCurrencyExpanded = !isCurrencyExpanded },
                                modifier = Modifier.weight(0.9f)
                            ) {
                                OutlinedTextField(
                                    value = currency,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Para Birimi") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCurrencyExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = isCurrencyExpanded,
                                    onDismissRequest = { isCurrencyExpanded = false }
                                ) {
                                    listOf("₺", "USD", "EUR", "GBP", "CHF", "CAD", "AUD", "JPY", "SAR", "AED").forEach { c ->
                                        DropdownMenuItem(
                                            text = { Text(c) },
                                            onClick = {
                                                currency = c
                                                isCurrencyExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Payment Method
                        ExposedDropdownMenuBox(
                            expanded = isPaymentExpanded,
                            onExpandedChange = { isPaymentExpanded = !isPaymentExpanded }
                        ) {
                            OutlinedTextField(
                                value = paymentMethod,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Ödeme Şekli") },
                                leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isPaymentExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = isPaymentExpanded,
                                onDismissRequest = { isPaymentExpanded = false }
                            ) {
                                PAYMENT_OPTIONS.forEach { method ->
                                    DropdownMenuItem(
                                        text = { Text(method) },
                                        onClick = {
                                            paymentMethod = method
                                            isPaymentExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Items Summary
                        OutlinedTextField(
                            value = itemsSummary,
                            onValueChange = { itemsSummary = it },
                            label = { Text("Ürün / Hizmet Özeti") },
                            leadingIcon = { Icon(Icons.Default.ShoppingBag, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2
                        )

                        // Notes
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Not / Açıklama") },
                            leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("İptal")
                        }

                        Button(
                            onClick = {
                                val total = totalAmountStr.toDoubleOrNull() ?: 0.0
                                val rate = taxRateStr.toDoubleOrNull() ?: 20.0
                                val taxAmt = taxAmountStr.toDoubleOrNull() ?: (total - (total * 100 / (100 + rate)))
                                val sub = total - taxAmt

                                val updatedParsed = ParsedReceipt(
                                    merchantName = merchantName,
                                    date = date,
                                    receiptNumber = receiptNumber,
                                    category = category,
                                    subtotal = sub,
                                    taxAmount = taxAmt,
                                    taxRate = rate,
                                    totalAmount = total,
                                    currency = currency,
                                    paymentMethod = paymentMethod,
                                    itemsSummary = itemsSummary,
                                    notes = notes
                                )
                                onSave(updatedParsed, imageUri, notes)
                            },
                            modifier = Modifier.weight(1.5f),
                            colors = ButtonDefaults.buttonColors(containerColor = ExcelGreen)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Fişi Kaydet", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
