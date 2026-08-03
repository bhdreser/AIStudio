package com.example.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.Locale
import java.util.concurrent.TimeUnit

data class ParsedReceipt(
    val merchantName: String = "",
    val date: String = "",
    val receiptNumber: String = "",
    val category: String = "Diğer",
    val subtotal: Double = 0.0,
    val taxAmount: Double = 0.0,
    val taxRate: Double = 20.0,
    val totalAmount: Double = 0.0,
    val currency: String = "₺",
    val paymentMethod: String = "Kredi Kartı",
    val itemsSummary: String = "",
    val notes: String = "",
    val tags: String = "İş"
)

class GeminiReceiptParser(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    companion object {
        fun suggestCategoryAndTags(merchantName: String, rawCategory: String): Pair<String, String> {
            val merchantLower = merchantName.lowercase(Locale("tr", "TR"))
            var category = rawCategory
            var suggestedTag = "İş"

            // 1. Market / Süpermarket
            if (merchantLower.contains("migros") || merchantLower.contains("bim") || merchantLower.contains("a101") ||
                merchantLower.contains("şok") || merchantLower.contains("carrefour") || merchantLower.contains("macrocenter") ||
                merchantLower.contains("file") || merchantLower.contains("metro market") || merchantLower.contains("groseri") ||
                merchantLower.contains("kim market") || merchantLower.contains("hakmar") || merchantLower.contains("onur market")
            ) {
                category = "Market"
                suggestedTag = "Kişisel"
            }
            // 2. Yiyecek & İçecek
            else if (merchantLower.contains("starbucks") || merchantLower.contains("köfteci") || merchantLower.contains("mcdonald") ||
                merchantLower.contains("burger king") || merchantLower.contains("domino") || merchantLower.contains("kfc") ||
                merchantLower.contains("simit sarayı") || merchantLower.contains("kahve dünyası") || merchantLower.contains("tavuk dünyası") ||
                merchantLower.contains("big chefs") || merchantLower.contains("espressolab") || merchantLower.contains("cookshop") ||
                merchantLower.contains("restoran") || merchantLower.contains("lokanta") || merchantLower.contains("pastane") ||
                merchantLower.contains("kafe") || merchantLower.contains("cafe") || merchantLower.contains("yemek")
            ) {
                category = "Yiyecek & İçecek"
                suggestedTag = "İş"
            }
            // 3. Akaryakıt & Ulaşım
            else if (merchantLower.contains("shell") || merchantLower.contains("opet") || merchantLower.contains("petrol ofisi") ||
                merchantLower.contains("bp ") || merchantLower.contains("total") || merchantLower.contains("aytemiz") ||
                merchantLower.contains("uber") || merchantLower.contains("bitaksi") || merchantLower.contains("taksi") ||
                merchantLower.contains("ispark") || merchantLower.contains("otopark") || merchantLower.contains("hgs") || merchantLower.contains("ogs")
            ) {
                category = "Akaryakıt & Ulaşım"
                suggestedTag = "Seyahat"
            }
            // 4. Ofis Malzemesi & Teknoloji
            else if (merchantLower.contains("nezih") || merchantLower.contains("d&r") || merchantLower.contains("kırtasiye") ||
                merchantLower.contains("koçtaş") || merchantLower.contains("bauhaus") || merchantLower.contains("ikea") ||
                merchantLower.contains("teknosa") || merchantLower.contains("vatan") || merchantLower.contains("mediamarkt") ||
                merchantLower.contains("apple") || merchantLower.contains("samsung") || merchantLower.contains("fotokopi")
            ) {
                category = "Ofis Malzemesi"
                suggestedTag = "Proje"
            }
            // 5. Konaklama & Seyahat
            else if (merchantLower.contains("hilton") || merchantLower.contains("marriott") || merchantLower.contains("radisson") ||
                merchantLower.contains("airbnb") || merchantLower.contains("booking") || merchantLower.contains("thy") ||
                merchantLower.contains("türk hava") || merchantLower.contains("pegasus") || merchantLower.contains("sunexpress") ||
                merchantLower.contains("kamil koç") || merchantLower.contains("metro turizm") || merchantLower.contains("hotel") ||
                merchantLower.contains("otel")
            ) {
                category = "Konaklama & Seyahat"
                suggestedTag = "Trip"
            }
            // 6. Sağlık & Bakım
            else if (merchantLower.contains("eczane") || merchantLower.contains("rossmann") || merchantLower.contains("watsons") ||
                merchantLower.contains("gratis") || merchantLower.contains("hastane") || merchantLower.contains("poliklinik") ||
                merchantLower.contains("optik") || merchantLower.contains("medikal")
            ) {
                category = "Sağlık & Bakım"
                suggestedTag = "Acil"
            }

            if (category == "Diğer" && rawCategory != "Diğer") {
                category = rawCategory
            }

            return Pair(category, suggestedTag)
        }
    }

    suspend fun parseReceiptImage(imageUri: Uri): Result<ParsedReceipt> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext Result.failure(
                    IllegalStateException("Gemini API anahtarı ayarlanmamış. Lütfen AI Studio Secrets panelinden GEMINI_API_KEY anahtarını girin.")
                )
            }

            val base64Image = uriToBase64(imageUri)
                ?: return@withContext Result.failure(IllegalArgumentException("Görsel okunamadı veya dönüştürülemedi."))

            val promptText = """
                Sen profesyonel bir fiş, fatura ve masraf tarama uzmanısın. Bu fiş/fatura fotoğrafını ayrıntılı incele ve aşağıdaki alanları içeren JSON çıktısı üret:
                1. merchantName: Mağaza, restoran veya şirket adı (Örn: Migros, Shell, Starbucks)
                2. date: Fiş tarihi (tercihen GG.AA.YYYY formatında)
                3. receiptNumber: Fiş No / Fatura No / Z No
                4. category: Şu kategorilerden tam olarak biri olmalı ["Yiyecek & İçecek", "Market", "Akaryakıt & Ulaşım", "Ofis Malzemesi", "Konaklama & Seyahat", "Sağlık & Bakım", "Eğlence", "Diğer"]
                5. subtotal: Ara Toplam / Matrah (Sayısal KDV hariç)
                6. taxAmount: Toplam KDV Tutarı (Sayısal)
                7. taxRate: Uygulanan KDV oranı (örneğin %20 ise 20.0, %10 ise 10.0)
                8. totalAmount: Ödenecek Toplam Tutar (Sayısal)
                9. currency: Para birimi (Örn: "₺", "TL", "$", "€")
                10. paymentMethod: Ödeme Şekli ("Kredi Kartı", "Nakit", "Bankamatik Kartı", "Diğer")
                11. itemsSummary: Fişteki ürünlerin ve hizmetlerin özeti
                12. notes: Varsa ekstra notlar

                Sadece saf geçerli bir JSON nesnesi döndür. Ek açıklama yazma.
                Şema:
                {
                  "merchantName": "...",
                  "date": "...",
                  "receiptNumber": "...",
                  "category": "...",
                  "subtotal": 0.0,
                  "taxAmount": 0.0,
                  "taxRate": 20.0,
                  "totalAmount": 0.0,
                  "currency": "₺",
                  "paymentMethod": "Kredi Kartı",
                  "itemsSummary": "...",
                  "notes": "..."
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().put("text", promptText))
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Image)
                                })
                            })
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.1)
                })
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val requestBody = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e("GeminiReceiptParser", "API Error: ${response.code} $responseString")
                return@withContext Result.failure(Exception("Gemini API hatası (${response.code}): $responseString"))
            }

            val parsedResponse = parseGeminiJsonResponse(responseString)
            Result.success(parsedResponse)
        } catch (e: Exception) {
            Log.e("GeminiReceiptParser", "Failed to parse receipt", e)
            Result.failure(e)
        }
    }

    private fun uriToBase64(uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return null
            inputStream?.close()

            // Scale down if image is too large to save bandwidth & speed up inference
            val maxDimension = 1500
            val width = originalBitmap.width
            val height = originalBitmap.height
            val bitmap = if (width > maxDimension || height > maxDimension) {
                val scale = maxDimension.toFloat() / Math.max(width, height)
                Bitmap.createScaledBitmap(originalBitmap, (width * scale).toInt(), (height * scale).toInt(), true)
            } else {
                originalBitmap
            }

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val byteArray = outputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e("GeminiReceiptParser", "Error converting image to base64", e)
            null
        }
    }

    private fun parseGeminiJsonResponse(responseJsonStr: String): ParsedReceipt {
        val rootObj = JSONObject(responseJsonStr)
        val candidates = rootObj.optJSONArray("candidates")
        val firstCandidate = candidates?.optJSONObject(0)
        val content = firstCandidate?.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        val firstPart = parts?.optJSONObject(0)
        val rawText = firstPart?.optString("text") ?: "{}"

        // Clean markdown backticks if any
        val cleanText = rawText.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val json = JSONObject(cleanText)

        val merchant = json.optString("merchantName", "Bilinmeyen Mağaza")
        val rawCat = json.optString("category", "Diğer")
        val (suggestedCategory, suggestedTag) = suggestCategoryAndTags(merchant, rawCat)
        val extractedTag = json.optString("tags", suggestedTag).ifBlank { suggestedTag }

        return ParsedReceipt(
            merchantName = merchant,
            date = json.optString("date", ""),
            receiptNumber = json.optString("receiptNumber", ""),
            category = suggestedCategory,
            subtotal = json.optDouble("subtotal", 0.0),
            taxAmount = json.optDouble("taxAmount", 0.0),
            taxRate = json.optDouble("taxRate", 20.0),
            totalAmount = json.optDouble("totalAmount", 0.0),
            currency = json.optString("currency", "₺").let { if (it.isBlank()) "₺" else it },
            paymentMethod = json.optString("paymentMethod", "Kredi Kartı"),
            itemsSummary = json.optString("itemsSummary", ""),
            notes = json.optString("notes", ""),
            tags = extractedTag
        )
    }
}
