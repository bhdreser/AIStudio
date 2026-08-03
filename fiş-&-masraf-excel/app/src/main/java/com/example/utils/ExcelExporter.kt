package com.example.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.ReceiptEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExcelExporter {

    fun generateExcelHtmlFile(context: Context, receipts: List<ReceiptEntity>): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Masraf_Listesi_$timeStamp.xls"
        val file = File(context.cacheDir, fileName)

        val totalSpend = receipts.sumOf { it.totalAmount }
        val totalTax = receipts.sumOf { it.taxAmount }

        val htmlContent = StringBuilder().apply {
            append("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta http-equiv="content-type" content="text/html; charset=utf-8">
                    <style>
                        body { font-family: Arial, sans-serif; background-color: #f8f9fa; }
                        table { border-collapse: collapse; width: 100%; margin-top: 15px; font-size: 13px; }
                        th { background-color: #107C41; color: #ffffff; font-weight: bold; padding: 10px; border: 1px solid #005a2b; text-align: left; }
                        td { padding: 8px; border: 1px solid #cccccc; color: #333333; }
                        tr:nth-child(even) { background-color: #f2f7f4; }
                        .number { text-align: right; }
                        .center { text-align: center; }
                        .total-row { background-color: #E2F0D9; font-weight: bold; }
                        .header-box { background-color: #0D5C46; color: white; padding: 16px; border-radius: 6px; margin-bottom: 20px; }
                    </style>
                </head>
                <body>
                    <div class="header-box">
                        <h2>FİŞ VE MASRAF RAPORU</h2>
                        <p>Tarih: ${SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())}</p>
                        <p>Toplam Kayıt: ${receipts.size} adet | Toplam Tutar: ${String.format(Locale("tr", "TR"), "%.2f ₺", totalSpend)}</p>
                    </div>

                    <table>
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Tarih</th>
                                <th>Mağaza / Firma Adı</th>
                                <th>Fiş No</th>
                                <th>Kategori</th>
                                <th>Ödeme Şekli</th>
                                <th>Ürün / Hizmet Özeti</th>
                                <th class="number">KDV Oranı</th>
                                <th class="number">KDV Tutarı</th>
                                <th class="number">Matrah (KDV Hariç)</th>
                                <th class="number">Toplam Tutar</th>
                                <th>Açıklama</th>
                            </tr>
                        </thead>
                        <tbody>
            """.trimIndent())

            receipts.forEachIndexed { index, receipt ->
                append("""
                    <tr>
                        <td class="center">${index + 1}</td>
                        <td class="center">${escapeHtml(receipt.date)}</td>
                        <td><strong>${escapeHtml(receipt.merchantName)}</strong></td>
                        <td class="center">${escapeHtml(receipt.receiptNumber)}</td>
                        <td>${escapeHtml(receipt.category)}</td>
                        <td>${escapeHtml(receipt.paymentMethod)}</td>
                        <td>${escapeHtml(receipt.itemsSummary)}</td>
                        <td class="number">%${String.format(Locale.US, "%.1f", receipt.taxRate)}</td>
                        <td class="number">${String.format(Locale.US, "%.2f", receipt.taxAmount)} ₺</td>
                        <td class="number">${String.format(Locale.US, "%.2f", receipt.subtotal)} ₺</td>
                        <td class="number"><strong>${String.format(Locale.US, "%.2f", receipt.totalAmount)} ${receipt.currency}</strong></td>
                        <td>${escapeHtml(receipt.notes)}</td>
                    </tr>
                """.trimIndent())
            }

            append("""
                        <tr class="total-row">
                            <td colspan="8" class="number"><strong>GENEL TOPLAM:</strong></td>
                            <td class="number"><strong>${String.format(Locale.US, "%.2f", totalTax)} ₺</strong></td>
                            <td class="number"><strong>${String.format(Locale.US, "%.2f", totalSpend - totalTax)} ₺</strong></td>
                            <td class="number"><strong>${String.format(Locale.US, "%.2f", totalSpend)} ₺</strong></td>
                            <td></td>
                        </tr>
                    </tbody>
                </table>
            </body>
            </html>
            """.trimIndent())
        }.toString()

        FileOutputStream(file).use { out ->
            out.write(htmlContent.toByteArray(Charsets.UTF_8))
        }

        return file
    }

    fun generateExcelCsvFile(context: Context, receipts: List<ReceiptEntity>): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Masraf_Listesi_$timeStamp.csv"
        val file = File(context.cacheDir, fileName)

        val csvBuilder = StringBuilder().apply {
            // Write UTF-8 BOM so Microsoft Excel automatically recognizes encoding
            append('\uFEFF')

            // Header row (using ';' semicolon delimiter for Turkish locale Excel)
            append("Sıra No;Tarih;Mağaza Adı;Fiş No;Kategori;Ödeme Şekli;Ürün Özeti;KDV Oranı;KDV Tutarı;Matrah (Ara Toplam);Toplam Tutar;Para Birimi;Açıklama\n")

            receipts.forEachIndexed { index, r ->
                val row = listOf(
                    (index + 1).toString(),
                    escapeCsv(r.date),
                    escapeCsv(r.merchantName),
                    escapeCsv(r.receiptNumber),
                    escapeCsv(r.category),
                    escapeCsv(r.paymentMethod),
                    escapeCsv(r.itemsSummary),
                    String.format(Locale.US, "%.1f", r.taxRate),
                    String.format(Locale.US, "%.2f", r.taxAmount),
                    String.format(Locale.US, "%.2f", r.subtotal),
                    String.format(Locale.US, "%.2f", r.totalAmount),
                    escapeCsv(r.currency),
                    escapeCsv(r.notes)
                ).joinToString(";")

                append(row).append("\n")
            }
        }

        FileOutputStream(file).use { out ->
            out.write(csvBuilder.toString().toByteArray(Charsets.UTF_8))
        }

        return file
    }

    fun shareExportFile(context: Context, file: File, mimeType: String = "application/vnd.ms-excel") {
        try {
            val authority = "${context.packageName}.fileprovider"
            val contentUri: Uri = FileProvider.getUriForFile(context, authority, file)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "FişMasraf - Excel Masraf Raporu")
                putExtra(Intent.EXTRA_TEXT, "FişMasraf uygulamasından oluşturulan masraf listesi ektedir.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(intent, "Excel Raporunu Paylaş veya Aç")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun escapeCsv(value: String): String {
        val clean = value.replace("\"", "\"\"").replace("\n", " ").replace("\r", " ")
        return "\"$clean\""
    }

    private fun escapeHtml(value: String): String {
        return value.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
    }
}
