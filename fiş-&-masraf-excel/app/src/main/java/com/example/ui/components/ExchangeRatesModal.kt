package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.ExcelGreen
import com.example.utils.ExchangeRateManager
import kotlinx.coroutines.launch

data class CurrencyRateItem(
    val code: String,
    val name: String,
    val rateToTry: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeRatesModal(
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var ratesList by remember { mutableStateOf<List<CurrencyRateItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var calcAmountStr by remember { mutableStateOf("100") }
    var calcCurrency by remember { mutableStateOf("USD") }
    var calcResultStr by remember { mutableStateOf("0.00 ₺") }
    var isCurrencyDropdownExpanded by remember { mutableStateOf(false) }

    val majorCurrencies = listOf(
        Pair("USD", "Amerikan Doları ($)"),
        Pair("EUR", "Euro (€)"),
        Pair("GBP", "İngiliz Sterlini (£)"),
        Pair("CHF", "İsviçre Frangı"),
        Pair("CAD", "Kanada Doları"),
        Pair("AUD", "Avustralya Doları"),
        Pair("JPY", "Japon Yeni (¥)"),
        Pair("SAR", "Suudi Arabistan Riyali"),
        Pair("AED", "BAE Dirhemi")
    )

    fun fetchRates() {
        isLoading = true
        coroutineScope.launch {
            val list = mutableListOf<CurrencyRateItem>()
            for ((code, name) in majorCurrencies) {
                val rate = ExchangeRateManager.getRateToTry(code)
                list.add(CurrencyRateItem(code, name, rate))
            }
            ratesList = list
            isLoading = false

            // Update calculation
            val amt = calcAmountStr.toDoubleOrNull() ?: 0.0
            val conversion = ExchangeRateManager.convertToTry(amt, calcCurrency)
            calcResultStr = String.format("%,.2f ₺", conversion.convertedTryAmount)
        }
    }

    LaunchedEffect(Unit) {
        fetchRates()
    }

    LaunchedEffect(calcAmountStr, calcCurrency) {
        val amt = calcAmountStr.toDoubleOrNull() ?: 0.0
        val conversion = ExchangeRateManager.convertToTry(amt, calcCurrency)
        calcResultStr = String.format("%,.2f ₺", conversion.convertedTryAmount)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = ExcelGreen.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CurrencyExchange,
                                    contentDescription = null,
                                    tint = ExcelGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Canlı Döviz Kurları",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Otomatik ₺ Dönüşümü",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = { fetchRates() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Yenile", tint = ExcelGreen)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Converter Calculator Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Hızlı Döviz Çevirici",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = calcAmountStr,
                                onValueChange = { calcAmountStr = it },
                                label = { Text("Tutar") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            ExposedDropdownMenuBox(
                                expanded = isCurrencyDropdownExpanded,
                                onExpandedChange = { isCurrencyDropdownExpanded = !isCurrencyDropdownExpanded },
                                modifier = Modifier.weight(0.9f)
                            ) {
                                OutlinedTextField(
                                    value = calcCurrency,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Birim") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCurrencyDropdownExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = isCurrencyDropdownExpanded,
                                    onDismissRequest = { isCurrencyDropdownExpanded = false }
                                ) {
                                    majorCurrencies.forEach { (code, name) ->
                                        DropdownMenuItem(
                                            text = { Text("$code - $name") },
                                            onClick = {
                                                calcCurrency = code
                                                isCurrencyDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Türk Lirası Karşılığı:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = calcResultStr,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = ExcelGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Güncel Döviz Kurları (1 Birim = ₺)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ExcelGreen)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ratesList) { item ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
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
                                            text = item.code,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        text = String.format("%,.2f ₺", item.rateToTry),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = ExcelGreen
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Kapat")
                }
            }
        }
    }
}
