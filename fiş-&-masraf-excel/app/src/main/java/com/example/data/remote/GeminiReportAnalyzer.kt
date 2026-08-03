package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.local.ReceiptEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiReportAnalyzer(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .build()

    suspend fun generateAiReportInsight(
        reportTitle: String,
        receipts: List<ReceiptEntity>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext Result.failure(
                    IllegalStateException("Gemini API anahtarı ayarlanmamış. Lütfen Secrets panelinden ekleyin.")
                )
            }

            if (receipts.isEmpty()) {
                return@withContext Result.success("Bu raporda henüz analiz edilecek fiş bulunmuyor.")
            }

            val totalAmount = receipts.sumOf { it.totalAmount }
            val totalTax = receipts.sumOf { it.taxAmount }
            val categories = receipts.groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.totalAmount } }

            val receiptListFormatted = receipts.take(30).joinToString("\n") { r ->
                "- ${r.date} | ${r.merchantName} | Kat: ${r.category} | Toplam: ${r.totalAmount} ${r.currency} (KDV: ${r.taxAmount} ₺)"
            }

            val promptText = """
                Sen üst düzey bir Şirket Finans Direktörü ve Yeminli Mali Müşavir Yapay Zeka Danışmanısın.
                Aşağıdaki harcama/masraf raporunu detaylıca analiz et ve Türkçe dilinde profesyonel, anlaşılır, eyleme dönüştürülebilir bir Yönetici Özeti (Executive Summary) ve Finansal Analiz sun.

                Rapor Adı: $reportTitle
                Toplam Fiş Sayısı: ${receipts.size}
                Toplam Masraf Tutar: $totalAmount ₺
                Toplam Hesaplanan KDV: $totalTax ₺
                Kategori Dağılımı: $categories

                Fiş Listesi Örneği:
                $receiptListFormatted

                Lütfen çıktıda şu bölümleri Markdown formatında düzenli bir şekilde yaz:
                📊 **1. Genel Değerlendirme & Özet**: Harcamaların yoğunlaştığı alanlar ve bütçe durumu.
                💡 **2. Tasarruf & Maliyet İyileştirme Fırsatları**: Şirket veya birey için nerede tasarruf sağlanabilir?
                🛡️ **3. KDV & Vergi Avantajı Analizi**: Gider gösterilebilecek KDV ve muhtemel vergi indirimleri.
                🔍 **4. Anormallik & Risk Kontrolü**: Tekrarlayan fişler veya yüksek tutarlı şüpheli harcamalar var mı?

                Samimi, doğrudan, profesyonel ve cesaret verici bir dille yanıt ver.
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().put("text", promptText))
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.3)
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
                return@withContext Result.failure(Exception("Gemini API Hatası (${response.code})"))
            }

            val rootObj = JSONObject(responseString)
            val candidates = rootObj.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val firstPart = parts?.optJSONObject(0)
            val rawText = firstPart?.optString("text") ?: "Analiz oluşturulamadı."

            Result.success(rawText.trim())
        } catch (e: Exception) {
            Log.e("GeminiReportAnalyzer", "Report analysis failed", e)
            Result.failure(e)
        }
    }
}
