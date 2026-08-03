package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.local.ReceiptEntity
import com.example.data.local.ReportEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    fun generateReportPdf(
        context: Context,
        reportTitle: String,
        reportDesc: String,
        aiSummary: String = "",
        receipts: List<ReceiptEntity>
    ): File? {
        return try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // A4 width in points (1/72 inch)
            val pageHeight = 842 // A4 height in points

            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            val paint = Paint().apply { isAntiAlias = true }
            val textPaint = Paint().apply {
                isAntiAlias = true
                textSize = 10f
                color = Color.BLACK
            }

            var currentY = 0f

            // 1. Header Banner
            val headerHeight = 60f
            paint.color = Color.parseColor("#0D5C46")
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), headerHeight, paint)

            // Header Title
            paint.color = Color.WHITE
            paint.textSize = 18f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("FişMasraf - Bireysel & Kurumsal Masraf Raporu", 20f, 36f, paint)

            paint.textSize = 10f
            paint.typeface = Typeface.DEFAULT
            val currentDateStr = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr", "TR")).format(Date())
            canvas.drawText("Oluşturulma Tarihi: $currentDateStr", pageWidth - 180f, 36f, paint)

            currentY = headerHeight + 25f

            // 2. Report Information Card
            val cardPadding = 15f
            paint.color = Color.parseColor("#F1F8F5")
            canvas.drawRoundRect(20f, currentY, pageWidth - 20f, currentY + 70f, 8f, 8f, paint)

            paint.color = Color.parseColor("#0D5C46")
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(reportTitle, 32f, currentY + 22f, paint)

            paint.color = Color.parseColor("#444444")
            paint.textSize = 10f
            paint.typeface = Typeface.DEFAULT
            val descText = if (reportDesc.isNotBlank()) reportDesc else "Detaylı fiş ve masraf dökümü"
            canvas.drawText(descText, 32f, currentY + 38f, paint)

            val totalSpend = receipts.sumOf { it.totalAmount }
            val totalTax = receipts.sumOf { it.taxAmount }

            paint.color = Color.parseColor("#0D5C46")
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val totalText = "${String.format(Locale("tr", "TR"), "%.2f", totalSpend)} ₺"
            canvas.drawText("Toplam: $totalText", pageWidth - 160f, currentY + 25f, paint)

            paint.color = Color.parseColor("#666666")
            paint.textSize = 9f
            paint.typeface = Typeface.DEFAULT
            canvas.drawText("KDV: ${String.format(Locale("tr", "TR"), "%.2f", totalTax)} ₺ | ${receipts.size} Adet Fiş", pageWidth - 160f, currentY + 42f, paint)

            currentY += 85f

            // 3. AI Executive Summary (if provided)
            if (aiSummary.isNotBlank()) {
                val summaryBoxHeight = 65f
                paint.color = Color.parseColor("#EBF3FE")
                canvas.drawRoundRect(20f, currentY, pageWidth - 20f, currentY + summaryBoxHeight, 8f, 8f, paint)

                paint.color = Color.parseColor("#1565C0")
                paint.textSize = 10f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("🤖 Yapay Zeka (AI) Rapor Analizi & Vergi İpuçları:", 30f, currentY + 18f, paint)

                paint.color = Color.parseColor("#222222")
                paint.textSize = 8.5f
                paint.typeface = Typeface.DEFAULT

                // Wrap summary text into 2 lines
                val maxChars = 110
                val line1 = if (aiSummary.length > maxChars) aiSummary.substring(0, maxChars) + "..." else aiSummary
                canvas.drawText(line1, 30f, currentY + 34f, paint)
                if (aiSummary.length > maxChars) {
                    val remaining = aiSummary.substring(maxChars)
                    val line2 = if (remaining.length > maxChars) remaining.substring(0, maxChars) + "..." else remaining
                    canvas.drawText(line2, 30f, currentY + 48f, paint)
                }

                currentY += summaryBoxHeight + 15f
            }

            // 4. Receipts Table Header
            paint.color = Color.parseColor("#107C41")
            canvas.drawRect(20f, currentY, pageWidth - 20f, currentY + 22f, paint)

            paint.color = Color.WHITE
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            canvas.drawText("#", 25f, currentY + 15f, paint)
            canvas.drawText("Tarih", 45f, currentY + 15f, paint)
            canvas.drawText("Mağaza / Firma", 110f, currentY + 15f, paint)
            canvas.drawText("Kategori", 250f, currentY + 15f, paint)
            canvas.drawText("Etiketler", 340f, currentY + 15f, paint)
            canvas.drawText("Fiş No", 430f, currentY + 15f, paint)
            canvas.drawText("Tutar", 510f, currentY + 15f, paint)

            currentY += 22f

            // 5. Table Rows
            paint.typeface = Typeface.DEFAULT
            paint.textSize = 8.5f

            receipts.forEachIndexed { index, r ->
                // Check page height overflow
                if (currentY > pageHeight - 60f) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas

                    // Draw continuation table header
                    currentY = 30f
                    paint.color = Color.parseColor("#107C41")
                    canvas.drawRect(20f, currentY, pageWidth - 20f, currentY + 22f, paint)
                    paint.color = Color.WHITE
                    paint.textSize = 9.5f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

                    canvas.drawText("#", 25f, currentY + 15f, paint)
                    canvas.drawText("Tarih", 45f, currentY + 15f, paint)
                    canvas.drawText("Mağaza / Firma", 110f, currentY + 15f, paint)
                    canvas.drawText("Kategori", 250f, currentY + 15f, paint)
                    canvas.drawText("Etiketler", 340f, currentY + 15f, paint)
                    canvas.drawText("Fiş No", 430f, currentY + 15f, paint)
                    canvas.drawText("Tutar", 510f, currentY + 15f, paint)

                    currentY += 22f
                    paint.typeface = Typeface.DEFAULT
                    paint.textSize = 8.5f
                }

                // Row zebra striping
                if (index % 2 == 1) {
                    paint.color = Color.parseColor("#F9FAF9")
                    canvas.drawRect(20f, currentY, pageWidth - 20f, currentY + 20f, paint)
                }

                // Row border bottom
                paint.color = Color.parseColor("#E0E0E0")
                canvas.drawLine(20f, currentY + 20f, pageWidth - 20f, currentY + 20f, paint)

                // Row text
                paint.color = Color.parseColor("#333333")
                canvas.drawText("${index + 1}", 25f, currentY + 14f, paint)
                canvas.drawText(r.date.take(10), 45f, currentY + 14f, paint)

                val merchantStr = if (r.merchantName.length > 22) r.merchantName.take(20) + ".." else r.merchantName
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(merchantStr, 110f, currentY + 14f, paint)
                paint.typeface = Typeface.DEFAULT

                canvas.drawText(r.category.take(14), 250f, currentY + 14f, paint)

                val tagStr = if (r.tags.isBlank()) "—" else r.tags.take(12)
                canvas.drawText(tagStr, 340f, currentY + 14f, paint)

                canvas.drawText(if (r.receiptNumber.isBlank()) "—" else r.receiptNumber.take(10), 430f, currentY + 14f, paint)

                val amtStr = "${String.format(Locale("tr", "TR"), "%.2f", r.totalAmount)} ${r.currency}"
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = Color.parseColor("#0D5C46")
                canvas.drawText(amtStr, 510f, currentY + 14f, paint)
                paint.typeface = Typeface.DEFAULT

                currentY += 20f
            }

            // Summary Total Row
            currentY += 10f
            paint.color = Color.parseColor("#0D5C46")
            canvas.drawRect(20f, currentY, pageWidth - 20f, currentY + 25f, paint)

            paint.color = Color.WHITE
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("GENEL TOPLAM MASRAF", 30f, currentY + 16f, paint)

            val grandTotalStr = "${String.format(Locale("tr", "TR"), "%.2f", totalSpend)} ₺ (KDV Dahil)"
            canvas.drawText(grandTotalStr, pageWidth - 220f, currentY + 16f, paint)

            // Finish document
            pdfDocument.finishPage(page)

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val pdfFile = File(context.cacheDir, "Rapor_$timeStamp.pdf")
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()

            pdfFile
        } catch (e: Exception) {
            Log.e("PdfExporter", "Error generating PDF report", e)
            null
        }
    }

    fun sharePdfFile(context: Context, pdfFile: File) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val contentUri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "FişMasraf - PDF Masraf Raporu")
                putExtra(Intent.EXTRA_TEXT, "FişMasraf uygulamasından oluşturulan PDF masraf raporu ektedir.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(intent, "PDF Raporunu Paylaş veya Görüntüle")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            Log.e("PdfExporter", "Error sharing PDF", e)
        }
    }
}
