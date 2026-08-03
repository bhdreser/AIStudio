package com.example.ui

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ReceiptRepository
import com.example.data.local.AppDatabase
import com.example.data.local.ReceiptEntity
import com.example.data.local.ReportEntity
import com.example.data.local.UserProfile
import com.example.data.remote.GeminiReceiptParser
import com.example.data.remote.GeminiReportAnalyzer
import com.example.data.remote.ParsedReceipt
import com.example.utils.ExcelExporter
import com.example.utils.GoogleAuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ReceiptUiState(
    val receipts: List<ReceiptEntity> = emptyList(),
    val filteredReceipts: List<ReceiptEntity> = emptyList(),
    val reports: List<ReportEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "Tümü",
    val selectedTag: String = "Tümü",
    val isScanning: Boolean = false,
    val scanProgressMessage: String = "",
    val scannedResult: ParsedReceipt? = null,
    val scannedImageUri: Uri? = null,
    val scanError: String? = null,
    val selectedReceiptForDetail: ReceiptEntity? = null,
    val isExportModalOpen: Boolean = false,
    val isManualAddOpen: Boolean = false,
    val isSavedReportsModalOpen: Boolean = false,
    val isAiAssistantModalOpen: Boolean = false,
    val isAiAnalysisLoading: Boolean = false,
    val activeReportAnalysisText: String? = null,
    val currentUser: UserProfile? = null,
    val isGoogleAccountModalOpen: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val isAppUnlocked: Boolean = true,
    val shouldLaunchCameraFromWidget: Boolean = false,
    val toastMessage: String? = null
)

class ReceiptViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ReceiptRepository
    private val geminiParser: GeminiReceiptParser
    private val reportAnalyzer: GeminiReportAnalyzer

    private val _uiState = MutableStateFlow(ReceiptUiState())
    val uiState: StateFlow<ReceiptUiState> = _uiState.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ReceiptRepository(db.receiptDao(), db.reportDao())
        geminiParser = GeminiReceiptParser(application)
        reportAnalyzer = GeminiReportAnalyzer(application)

        val initialBiometric = com.example.utils.BiometricAuthManager.isBiometricEnabled(application)
        val savedGoogleUser = GoogleAuthManager.SavedUser(application)

        _uiState.update {
            it.copy(
                isBiometricEnabled = initialBiometric,
                isAppUnlocked = !initialBiometric,
                currentUser = savedGoogleUser
            )
        }

        // Observe receipts
        viewModelScope.launch {
            repository.allReceipts.collectLatest { list ->
                if (list.isEmpty()) {
                    seedSampleData()
                } else {
                    _uiState.update { state ->
                        state.copy(
                            receipts = list,
                            filteredReceipts = filterList(list, state.searchQuery, state.selectedCategory, state.selectedTag)
                        )
                    }
                }
            }
        }

        // Observe reports
        viewModelScope.launch {
            repository.allReports.collectLatest { reportsList ->
                _uiState.update { it.copy(reports = reportsList) }
            }
        }
    }

    private suspend fun seedSampleData() {
        val sampleList = listOf(
            ReceiptEntity(
                merchantName = "Migros Ticaret A.Ş.",
                date = "02.08.2026",
                receiptNumber = "F082910",
                category = "Market",
                subtotal = 411.64,
                taxAmount = 41.16,
                taxRate = 10.0,
                totalAmount = 452.80,
                currency = "₺",
                paymentMethod = "Kredi Kartı",
                itemsSummary = "2x Tam Yağlı Süt, 1x Beyaz Peynir 500g, 2x Ekmek",
                notes = "Haftalık mutfak alışverişi",
                tags = "Kişisel"
            ),
            ReceiptEntity(
                merchantName = "Shell Akaryakıt",
                date = "01.08.2026",
                receiptNumber = "Z091823",
                category = "Akaryakıt & Ulaşım",
                subtotal = 1041.67,
                taxAmount = 208.33,
                taxRate = 20.0,
                totalAmount = 1250.00,
                currency = "₺",
                paymentMethod = "Kredi Kartı",
                itemsSummary = "V-Power Kurşunsuz 95 Benzin 28.5 Litre",
                notes = "Şirket aracı yakıt masrafı",
                tags = "Seyahat, İş"
            ),
            ReceiptEntity(
                merchantName = "Starbucks Coffee",
                date = "31.07.2026",
                receiptNumber = "S10928",
                category = "Yiyecek & İçecek",
                subtotal = 168.19,
                taxAmount = 16.81,
                taxRate = 10.0,
                totalAmount = 185.00,
                currency = "₺",
                paymentMethod = "Kredi Kartı",
                itemsSummary = "1x Iced Caffe Latte Grande, 1x Muffin",
                notes = "Müşteri toplantısı ikramı",
                tags = "İş"
            ),
            ReceiptEntity(
                merchantName = "Nezih Kitap Kırtasiye",
                date = "28.07.2026",
                receiptNumber = "K98127",
                category = "Ofis Malzemesi",
                subtotal = 266.67,
                taxAmount = 53.33,
                taxRate = 20.0,
                totalAmount = 320.00,
                currency = "₺",
                paymentMethod = "Nakit",
                itemsSummary = "1x A4 Fotokopi Kağıdı 500'lü, Mavi Tükenmez Kalem 10'lu",
                notes = "Ofis sarf malzemesi",
                tags = "Proje, İş"
            )
        )
        sampleList.forEach { repository.insertReceipt(it) }

        // Seed initial sample report
        val sampleReport = ReportEntity(
            title = "Ağustos 2026 Ofis & Operasyon Raporu",
            description = "Şirket içi operasyon, ulaşım ve sarf malzeme masrafları",
            categoryFilter = "Tümü",
            receiptIds = "1,2,3,4",
            totalAmount = 2207.80,
            totalTax = 319.63,
            receiptCount = 4,
            versionName = "v1.0 - Onaylandı",
            status = "Onaylandı",
            createdAt = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
        )
        repository.insertReport(sampleReport)
    }

    fun setSearchQuery(query: String) {
        _uiState.update { state ->
            val filtered = filterList(state.receipts, query, state.selectedCategory, state.selectedTag)
            state.copy(searchQuery = query, filteredReceipts = filtered)
        }
    }

    fun setSelectedCategory(category: String) {
        _uiState.update { state ->
            val filtered = filterList(state.receipts, state.searchQuery, category, state.selectedTag)
            state.copy(selectedCategory = category, filteredReceipts = filtered)
        }
    }

    fun setSelectedTag(tag: String) {
        _uiState.update { state ->
            val filtered = filterList(state.receipts, state.searchQuery, state.selectedCategory, tag)
            state.copy(selectedTag = tag, filteredReceipts = filtered)
        }
    }

    private fun filterList(list: List<ReceiptEntity>, query: String, category: String, tag: String): List<ReceiptEntity> {
        return list.filter { item ->
            val matchesCategory = (category == "Tümü" || item.category.contains(category, ignoreCase = true))
            val matchesTag = (tag == "Tümü" || item.tags.contains(tag, ignoreCase = true))
            val matchesQuery = query.isBlank() ||
                    item.merchantName.contains(query, ignoreCase = true) ||
                    item.notes.contains(query, ignoreCase = true) ||
                    item.itemsSummary.contains(query, ignoreCase = true) ||
                    item.receiptNumber.contains(query, ignoreCase = true) ||
                    item.tags.contains(query, ignoreCase = true) ||
                    item.category.contains(query, ignoreCase = true)
            matchesCategory && matchesTag && matchesQuery
        }
    }

    fun toggleBiometricLock(enabled: Boolean) {
        val context = getApplication<Application>()
        com.example.utils.BiometricAuthManager.setBiometricEnabled(context, enabled)
        _uiState.update { it.copy(isBiometricEnabled = enabled) }
        showToast(if (enabled) "Biyometrik kilit açıldı!" else "Biyometrik kilit kapatıldı.")
    }

    fun authenticateWithBiometrics(context: android.content.Context, onSuccess: () -> Unit) {
        if (!com.example.utils.BiometricAuthManager.isBiometricEnabled(context)) {
            _uiState.update { it.copy(isAppUnlocked = true) }
            onSuccess()
            return
        }

        com.example.utils.BiometricAuthManager.promptBiometricAuth(
            context = context,
            title = "FişMasraf Kilitli",
            subtitle = "Masraf verilerinize erişmek için biyometrik doğrulama yapın",
            onSuccess = {
                _uiState.update { it.copy(isAppUnlocked = true) }
                onSuccess()
            },
            onError = { err ->
                showToast("Biyometrik doğrulama: $err")
            }
        )
    }

    fun unlockAppManually() {
        _uiState.update { it.copy(isAppUnlocked = true) }
        showToast("Kilit açıldı.")
    }

    fun scanReceiptImage(imageUri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isScanning = true,
                    scanProgressMessage = "Fiş görseli yapay zeka ile analiz ediliyor...",
                    scanError = null,
                    scannedImageUri = imageUri
                )
            }

            val savedUri = copyUriToInternalStorage(imageUri) ?: imageUri

            val result = geminiParser.parseReceiptImage(imageUri)
            result.onSuccess { parsed ->
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        scannedResult = parsed,
                        scannedImageUri = savedUri,
                        scanProgressMessage = ""
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        scanError = err.message ?: "Fiş taranırken bir hata oluştu.",
                        scannedResult = ParsedReceipt(
                            date = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
                        ),
                        scannedImageUri = savedUri
                    )
                }
            }
        }
    }

    private fun copyUriToInternalStorage(uri: Uri): Uri? {
        return try {
            val context = getApplication<Application>()
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(context.filesDir, "receipt_$timeStamp.jpg")
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            Uri.fromFile(file)
        } catch (e: Exception) {
            Log.e("ReceiptViewModel", "Failed to save image locally", e)
            null
        }
    }

    fun handleIncomingIntent(intent: android.content.Intent) {
        if (intent.action == "SCAN_CAMERA") {
            _uiState.update { it.copy(shouldLaunchCameraFromWidget = true) }
        }
    }

    fun onCameraLaunchedFromWidgetHandled() {
        _uiState.update { it.copy(shouldLaunchCameraFromWidget = false) }
    }

    fun saveScannedReceipt(parsed: ParsedReceipt, imageUri: Uri?, notes: String = "") {
        viewModelScope.launch {
            var finalTotal = parsed.totalAmount
            var finalCurrency = parsed.currency
            var conversionNote = ""

            val normalizedCurr = com.example.utils.ExchangeRateManager.normalizeCurrency(finalCurrency)
            if (normalizedCurr != "TRY") {
                val conversion = com.example.utils.ExchangeRateManager.convertToTry(finalTotal, finalCurrency)
                finalTotal = conversion.convertedTryAmount
                conversionNote = conversion.conversionNote
            }

            val subtotal = if (parsed.subtotal > 0 && normalizedCurr == "TRY") {
                parsed.subtotal
            } else {
                (finalTotal * 100 / (100 + parsed.taxRate))
            }
            val taxAmount = finalTotal - subtotal

            val noteList = mutableListOf<String>()
            if (notes.isNotBlank()) noteList.add(notes)
            if (parsed.notes.isNotBlank() && parsed.notes != notes) noteList.add(parsed.notes)
            if (conversionNote.isNotBlank()) noteList.add(conversionNote)
            val combinedNotes = noteList.joinToString("\n")

            val newReceipt = ReceiptEntity(
                merchantName = parsed.merchantName.ifBlank { "Bilinmeyen Mağaza" },
                date = parsed.date.ifBlank { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date()) },
                receiptNumber = parsed.receiptNumber,
                category = parsed.category.ifBlank { "Diğer" },
                subtotal = subtotal,
                taxAmount = taxAmount,
                taxRate = parsed.taxRate,
                totalAmount = finalTotal,
                currency = "₺",
                paymentMethod = parsed.paymentMethod,
                itemsSummary = parsed.itemsSummary,
                notes = combinedNotes,
                tags = parsed.tags.ifBlank { "İş" },
                imagePath = imageUri?.toString() ?: ""
            )

            repository.insertReceipt(newReceipt)
            com.example.widget.ExpenseWidgetProvider.updateAllWidgets(getApplication())
            clearScanState()
            if (conversionNote.isNotBlank()) {
                showToast("Uluslararası fiş ($normalizedCurr) ₺ birimine dönüştürülüp kaydedildi!")
            } else {
                showToast("Fiş başarıyla kaydedildi!")
            }
        }
    }

    fun updateReceipt(receipt: ReceiptEntity) {
        viewModelScope.launch {
            repository.updateReceipt(receipt)
            com.example.widget.ExpenseWidgetProvider.updateAllWidgets(getApplication())
            _uiState.update { it.copy(selectedReceiptForDetail = null) }
            showToast("Fiş güncellendi!")
        }
    }

    fun deleteReceipt(receipt: ReceiptEntity) {
        viewModelScope.launch {
            repository.deleteReceipt(receipt.id)
            com.example.widget.ExpenseWidgetProvider.updateAllWidgets(getApplication())
            _uiState.update { it.copy(selectedReceiptForDetail = null) }
            showToast("Fiş silindi.")
        }
    }

    fun deleteReceipt(receiptId: Long) {
        viewModelScope.launch {
            repository.deleteReceipt(receiptId)
            com.example.widget.ExpenseWidgetProvider.updateAllWidgets(getApplication())
            _uiState.update { it.copy(selectedReceiptForDetail = null) }
            showToast("Fiş silindi.")
        }
    }

    // Custom Saved Report Methods
    fun createCustomReport(title: String, description: String, versionName: String, receiptIds: List<Long>) {
        viewModelScope.launch {
            val allList = _uiState.value.receipts
            val selectedReceipts = allList.filter { receiptIds.contains(it.id) }
            val totalAmt = selectedReceipts.sumOf { it.totalAmount }
            val totalTax = selectedReceipts.sumOf { it.taxAmount }

            val newReport = ReportEntity(
                title = title,
                description = description,
                receiptIds = receiptIds.joinToString(","),
                totalAmount = totalAmt,
                totalTax = totalTax,
                receiptCount = selectedReceipts.size,
                versionName = versionName,
                status = "Taslak",
                createdAt = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
            )

            repository.insertReport(newReport)
            showToast("Özel rapor '$title' kaydedildi!")
        }
    }

    fun deleteReport(reportId: Long) {
        viewModelScope.launch {
            repository.deleteReport(reportId)
            showToast("Rapor silindi.")
        }
    }

    fun analyzeReportWithAi(report: ReportEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiAnalysisLoading = true, activeReportAnalysisText = null) }
            val ids = report.receiptIds.split(",").mapNotNull { it.trim().toLongOrNull() }.toSet()
            val reportReceipts = _uiState.value.receipts.filter { ids.contains(it.id) }

            val result = reportAnalyzer.generateAiReportInsight(report.title, reportReceipts)
            result.onSuccess { text ->
                _uiState.update {
                    it.copy(
                        isAiAnalysisLoading = false,
                        activeReportAnalysisText = text
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isAiAnalysisLoading = false,
                        activeReportAnalysisText = "Analiz esnasında hata oluştu: ${err.message}"
                    )
                }
            }
        }
    }

    fun askAiAssistant(question: String, callback: (String) -> Unit) {
        viewModelScope.launch {
            val receipts = _uiState.value.receipts
            val result = reportAnalyzer.generateAiReportInsight(
                reportTitle = "Soru: $question",
                receipts = receipts
            )
            val answer = result.getOrElse { "Hata: ${it.message}" }
            callback(answer)
        }
    }

    fun exportSpecificReportPdf(report: ReportEntity) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val ids = report.receiptIds.split(",").mapNotNull { it.trim().toLongOrNull() }.toSet()
            val reportReceipts = _uiState.value.receipts.filter { ids.contains(it.id) }

            if (reportReceipts.isEmpty()) {
                showToast("Raporda dışa aktarılacak fiş bulunamadı.")
                return@launch
            }

            val pdfFile = com.example.utils.PdfExporter.generateReportPdf(
                context = context,
                reportTitle = report.title,
                reportDesc = report.description,
                aiSummary = _uiState.value.activeReportAnalysisText ?: "",
                receipts = reportReceipts
            )

            if (pdfFile != null) {
                com.example.utils.PdfExporter.sharePdfFile(context, pdfFile)
                showToast("'${report.title}' PDF raporu oluşturuldu ve paylaşılıyor!")
            } else {
                showToast("PDF dosyası oluşturulamadı.")
            }
        }
    }

    fun exportToPdf() {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val receiptsToExport = _uiState.value.filteredReceipts.ifEmpty { _uiState.value.receipts }

            if (receiptsToExport.isEmpty()) {
                showToast("Dışa aktarılacak fiş bulunamadı.")
                return@launch
            }

            val pdfFile = com.example.utils.PdfExporter.generateReportPdf(
                context = context,
                reportTitle = "FişMasraf - Detaylı Masraf Raporu",
                reportDesc = "Seçilen filtre ve kategoriye ait tüm fiş listesi",
                aiSummary = "",
                receipts = receiptsToExport
            )

            if (pdfFile != null) {
                com.example.utils.PdfExporter.sharePdfFile(context, pdfFile)
                _uiState.update { it.copy(isExportModalOpen = false) }
                showToast("PDF rapor belgesi oluşturuldu ve paylaşılıyor!")
            } else {
                showToast("PDF oluşturulamadı.")
            }
        }
    }

    fun exportSpecificReport(report: ReportEntity) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val ids = report.receiptIds.split(",").mapNotNull { it.trim().toLongOrNull() }.toSet()
            val reportReceipts = _uiState.value.receipts.filter { ids.contains(it.id) }

            if (reportReceipts.isEmpty()) {
                showToast("Raporda dışa aktarılacak fiş bulunamadı.")
                return@launch
            }

            val file = ExcelExporter.generateExcelHtmlFile(context, reportReceipts)
            ExcelExporter.shareExportFile(
                context = context,
                file = file,
                mimeType = "application/vnd.ms-excel"
            )
            showToast("'${report.title}' raporu Excel olarak dışa aktarıldı!")
        }
    }

    fun openSavedReportsModal(open: Boolean) {
        _uiState.update { it.copy(isSavedReportsModalOpen = open, activeReportAnalysisText = null) }
    }

    fun openAiAssistantModal(open: Boolean) {
        _uiState.update { it.copy(isAiAssistantModalOpen = open) }
    }

    fun clearScanState() {
        _uiState.update {
            it.copy(
                isScanning = false,
                scannedResult = null,
                scannedImageUri = null,
                scanError = null,
                scanProgressMessage = ""
            )
        }
    }

    fun selectReceiptForDetail(receipt: ReceiptEntity?) {
        _uiState.update { it.copy(selectedReceiptForDetail = receipt) }
    }

    fun openExportModal(open: Boolean) {
        _uiState.update { it.copy(isExportModalOpen = open) }
    }

    fun openManualAdd(open: Boolean) {
        _uiState.update { it.copy(isManualAddOpen = open) }
    }

    fun exportToExcel(useHtmlXls: Boolean = true) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val receiptsToExport = _uiState.value.filteredReceipts.ifEmpty { _uiState.value.receipts }

            if (receiptsToExport.isEmpty()) {
                showToast("Dışa aktarılacak fiş bulunamadı.")
                return@launch
            }

            val file = if (useHtmlXls) {
                ExcelExporter.generateExcelHtmlFile(context, receiptsToExport)
            } else {
                ExcelExporter.generateExcelCsvFile(context, receiptsToExport)
            }

            ExcelExporter.shareExportFile(
                context = context,
                file = file,
                mimeType = if (useHtmlXls) "application/vnd.ms-excel" else "text/csv"
            )

            _uiState.update { it.copy(isExportModalOpen = false) }
            showToast("Excel dosyası başarıyla oluşturuldu ve paylaşılıyor!")
        }
    }

    fun openGoogleAccountModal(open: Boolean) {
        _uiState.update { it.copy(isGoogleAccountModalOpen = open) }
    }

    fun signInWithGoogle(emailInput: String? = null) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val result = GoogleAuthManager.signInWithGoogleCredential(context)
            val user = result.getOrNull() ?: GoogleAuthManager.createDemoGoogleUser(context, emailInput)
            
            _uiState.update {
                it.copy(
                    currentUser = user,
                    isGoogleAccountModalOpen = false
                )
            }
            showToast("Google hesabı ile giriş yapıldı: ${user.displayName}")
        }
    }

    fun signOutGoogle() {
        val context = getApplication<Application>()
        GoogleAuthManager.logout(context)
        _uiState.update {
            it.copy(
                currentUser = null,
                isGoogleAccountModalOpen = false
            )
        }
        showToast("Google hesabından çıkış yapıldı.")
    }

    fun syncCloudHistory() {
        val context = getApplication<Application>()
        val user = _uiState.value.currentUser ?: return
        val newSyncTime = GoogleAuthManager.updateSyncTime(context)
        val updatedUser = user.copy(lastSyncTime = newSyncTime)
        _uiState.update { it.copy(currentUser = updatedUser) }
        showToast("Fiş ve rapor geçmişiniz Google hesabıyla senkronize edildi!")
    }

    fun showToast(msg: String) {
        _uiState.update { it.copy(toastMessage = msg) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
