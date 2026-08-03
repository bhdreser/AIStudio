package com.example.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

object ExchangeRateManager {

    private val fallbackRates = mapOf(
        "USD" to 36.50,
        "EUR" to 39.80,
        "GBP" to 46.20,
        "CHF" to 41.50,
        "CAD" to 26.50,
        "AUD" to 23.80,
        "JPY" to 0.24,
        "SAR" to 9.70,
        "AED" to 9.90,
        "TRY" to 1.0,
        "₺" to 1.0
    )

    private val cachedLiveRates = mutableMapOf<String, Double>()

    fun normalizeCurrency(rawCurrency: String): String {
        val clean = rawCurrency.trim().uppercase(Locale.ROOT)
        return when {
            clean.contains("$") || clean.contains("USD") -> "USD"
            clean.contains("€") || clean.contains("EUR") -> "EUR"
            clean.contains("£") || clean.contains("GBP") -> "GBP"
            clean.contains("₣") || clean.contains("CHF") -> "CHF"
            clean.contains("¥") || clean.contains("JPY") -> "JPY"
            clean.contains("SAR") -> "SAR"
            clean.contains("AED") -> "AED"
            clean.contains("CAD") -> "CAD"
            clean.contains("AUD") -> "AUD"
            clean.contains("₺") || clean.contains("TL") || clean.contains("TRY") -> "TRY"
            else -> if (clean.length == 3) clean else "TRY"
        }
    }

    suspend fun getRateToTry(currencyCode: String): Double = withContext(Dispatchers.IO) {
        val code = normalizeCurrency(currencyCode)
        if (code == "TRY" || code == "₺") return@withContext 1.0

        if (cachedLiveRates.containsKey(code)) {
            return@withContext cachedLiveRates[code] ?: fallbackRates[code] ?: 36.50
        }

        try {
            val url = URL("https://open.er-api.com/v6/latest/$code")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 4000
            conn.readTimeout = 4000

            if (conn.responseCode == 200) {
                val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonStr)
                val rates = json.optJSONObject("rates")
                if (rates != null && rates.has("TRY")) {
                    val rate = rates.getDouble("TRY")
                    cachedLiveRates[code] = rate
                    Log.d("ExchangeRateManager", "Fetched rate for $code -> TRY: $rate")
                    return@withContext rate
                }
            }
        } catch (e: Exception) {
            Log.e("ExchangeRateManager", "Failed to fetch live rate for $code: ${e.message}")
        }

        return@withContext fallbackRates[code] ?: 36.50
    }

    data class ConversionResult(
        val originalAmount: Double,
        val originalCurrency: String,
        val convertedTryAmount: Double,
        val exchangeRate: Double,
        val conversionNote: String
    )

    suspend fun convertToTry(amount: Double, rawCurrency: String): ConversionResult {
        val code = normalizeCurrency(rawCurrency)
        if (code == "TRY" || code == "₺") {
            return ConversionResult(
                originalAmount = amount,
                originalCurrency = "₺",
                convertedTryAmount = amount,
                exchangeRate = 1.0,
                conversionNote = ""
            )
        }

        val rate = getRateToTry(code)
        val converted = amount * rate
        val note = "[Döviz: %.2f %s = %.2f ₺ (Kur: 1 %s = %.2f ₺)]".format(
            Locale.US, amount, code, converted, code, rate
        )

        return ConversionResult(
            originalAmount = amount,
            originalCurrency = code,
            convertedTryAmount = converted,
            exchangeRate = rate,
            conversionNote = note
        )
    }
}
