package com.example.ui.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.remote.ParsedReceipt
import com.example.ui.theme.ExcelGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualAddModal(
    onDismiss: () -> Unit,
    onSave: (ParsedReceipt, String) -> Unit
) {
    var merchantName by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())) }
    var receiptNumber by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Market") }
    var totalAmountStr by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("₺") }
    var taxRateStr by remember { mutableStateOf("20.0") }
    var paymentMethod by remember { mutableStateOf("Kredi Kartı") }
    var itemsSummary by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("İş") }

    var isCategoryExpanded by remember { mutableStateOf(false) }
    var isPaymentExpanded by remember { mutableStateOf(false) }
    var isCurrencyExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AddCard, contentDescription = null, tint = ExcelGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Manuel Fiş / Masraf Ekle",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = merchantName,
                        onValueChange = { merchantName = it },
                        label = { Text("Mağaza / Firma Adı *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("Tarih (GG.AA.YYYY)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = receiptNumber,
                            onValueChange = { receiptNumber = it },
                            label = { Text("Fiş No") },
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

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = totalAmountStr,
                            onValueChange = { totalAmountStr = it },
                            label = { Text("Tutar *") },
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

                    // Payment Method Dropdown
                    ExposedDropdownMenuBox(
                        expanded = isPaymentExpanded,
                        onExpandedChange = { isPaymentExpanded = !isPaymentExpanded }
                    ) {
                        OutlinedTextField(
                            value = paymentMethod,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Ödeme Şekli") },
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

                    OutlinedTextField(
                        value = itemsSummary,
                        onValueChange = { itemsSummary = it },
                        label = { Text("Satın Alınan Ürünler / Özeti") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notlar / Açıklama") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Özel Etiketler (Örn: İş, Kişisel, Seyahat)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("İptal")
                    }

                    Button(
                        onClick = {
                            val total = totalAmountStr.toDoubleOrNull() ?: 0.0
                            val rate = taxRateStr.toDoubleOrNull() ?: 20.0
                            val taxAmt = total - (total * 100 / (100 + rate))
                            val sub = total - taxAmt

                            val parsed = ParsedReceipt(
                                merchantName = merchantName.ifBlank { "Diğer Mağaza" },
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
                                notes = notes,
                                tags = tags.ifBlank { "İş" }
                            )
                            onSave(parsed, notes)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ExcelGreen),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ekle", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
