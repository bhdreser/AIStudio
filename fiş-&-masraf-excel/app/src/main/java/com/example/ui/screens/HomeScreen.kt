package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.local.ReceiptEntity
import com.example.ui.ReceiptUiState
import com.example.ui.ReceiptViewModel
import com.example.ui.components.*
import com.example.ui.theme.ExcelGreen
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val CATEGORY_FILTERS = listOf("Tümü", "Market", "Yiyecek & İçecek", "Akaryakıt & Ulaşım", "Ofis Malzemesi", "Konaklama & Seyahat", "Diğer")
val TAG_FILTERS = listOf("Tümü", "İş", "Kişisel", "Seyahat", "Proje", "Acil")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: ReceiptViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var showScanSourceOptions by remember { mutableStateOf(false) }
    var showCameraXDialog by remember { mutableStateOf(false) }
    var showAnalyticsModal by remember { mutableStateOf(false) }
    var showExchangeRatesModal by remember { mutableStateOf(false) }

    // Quick camera launch from widget listener
    LaunchedEffect(uiState.shouldLaunchCameraFromWidget) {
        if (uiState.shouldLaunchCameraFromWidget) {
            showCameraXDialog = true
            viewModel.onCameraLaunchedFromWidgetHandled()
        }
    }

    // Toast message listener
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // Biometric Lock Overlay Screen
    if (uiState.isBiometricEnabled && !uiState.isAppUnlocked) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(32.dp)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = ExcelGreen.copy(alpha = 0.15f),
                        modifier = Modifier.size(100.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "Biyometrik Kilit",
                                tint = ExcelGreen,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "FişMasraf Kilitli",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Masraf verilerinize erişmek için parmak izi veya yüz tanıma ile kimliğinizi doğrulayın.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = { viewModel.authenticateWithBiometrics(context) {} },
                        colors = ButtonDefaults.buttonColors(containerColor = ExcelGreen),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(0.8f).height(50.dp)
                    ) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Biyometrik Doğrula", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(onClick = { viewModel.unlockAppManually() }) {
                        Text("Manuel Kilit Aç (Yedek)")
                    }
                }
            }
        }
        return
    }

    // Gallery Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.scanReceiptImage(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = ExcelGreen,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "FişMasraf",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "AI Fiş & Özel Rapor Yönetimi",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Google Account Login / Profile
                    IconButton(onClick = { viewModel.openGoogleAccountModal(true) }) {
                        val user = uiState.currentUser
                        if (user != null && user.isLoggedIn) {
                            if (!user.photoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = user.photoUrl,
                                    contentDescription = "Google Hesabı",
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Icon(Icons.Default.AccountCircle, contentDescription = "Google Hesabı", tint = ExcelGreen)
                            }
                        } else {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Google Girişi", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Biometric Security Toggle
                    IconButton(onClick = { viewModel.toggleBiometricLock(!uiState.isBiometricEnabled) }) {
                        Icon(
                            imageVector = if (uiState.isBiometricEnabled) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "Biyometrik Güvenlik",
                            tint = if (uiState.isBiometricEnabled) ExcelGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Live Currency Rates / Exchange Rate Calculator
                    IconButton(onClick = { showExchangeRatesModal = true }) {
                        Icon(Icons.Default.CurrencyExchange, contentDescription = "Canlı Döviz Kurları", tint = ExcelGreen)
                    }

                    // AI Assistant
                    IconButton(onClick = { viewModel.openAiAssistantModal(true) }) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Danışman", tint = ExcelGreen)
                    }

                    // Saved Custom Reports
                    IconButton(onClick = { viewModel.openSavedReportsModal(true) }) {
                        BadgedBox(
                            badge = {
                                if (uiState.reports.isNotEmpty()) {
                                    Badge(containerColor = ExcelGreen) {
                                        Text("${uiState.reports.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.FolderSpecial, contentDescription = "Raporlar", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    // Analytics Bar Chart
                    IconButton(onClick = { showAnalyticsModal = true }) {
                        Icon(Icons.Default.BarChart, contentDescription = "Grafikler", tint = MaterialTheme.colorScheme.onSurface)
                    }

                    // Manual Add
                    IconButton(onClick = { viewModel.openManualAdd(true) }) {
                        Icon(Icons.Default.Add, contentDescription = "Manuel Ekle")
                    }

                    // Export Button
                    Button(
                        onClick = { viewModel.openExportModal(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = ExcelGreen),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.padding(end = 8.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Excel", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showScanSourceOptions = true },
                icon = { Icon(Icons.Default.PhotoCamera, contentDescription = null) },
                text = { Text("Fiş Tara", fontWeight = FontWeight.Bold) },
                containerColor = ExcelGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Quick Feature Access Bar (Reports & AI)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.openSavedReportsModal(true) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.FolderSpecial, contentDescription = null, tint = ExcelGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Özel Raporlar", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text("${uiState.reports.size} kayıtlı rapor", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                OutlinedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.openAiAssistantModal(true) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("AI Danışmanı", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text("Tasarruf & Vergi", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Google Account & Cloud Sync Banner
            GoogleAccountBannerCard(
                currentUser = uiState.currentUser,
                onOpenAccountModal = { viewModel.openGoogleAccountModal(true) }
            )

            // Stats Summary Dashboard
            StatsHeaderCard(
                receipts = uiState.receipts,
                filteredCount = uiState.filteredReceipts.size,
                onExportClick = { viewModel.openExportModal(true) },
                onAnalyticsClick = { showAnalyticsModal = true }
            )

            // Month-over-Month Line Chart Dashboard Card
            MonthOverMonthLineChartCard(
                receipts = uiState.receipts
            )

            // Search Bar & Filters
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Mağaza, fiş no, not veya #etiket ara...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Temizle")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category Chips Filter
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    items(CATEGORY_FILTERS) { category ->
                        val isSelected = uiState.selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setSelectedCategory(category) },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ExcelGreen,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Tag Chips Filter
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    item {
                        Text(
                            text = "Etiket:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                    items(TAG_FILTERS) { tag ->
                        val isSelected = uiState.selectedTag == tag
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setSelectedTag(tag) },
                            label = { Text(if (tag == "Tümü") "Tüm Etiketler" else "#$tag") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // Receipts List or Empty View
            if (uiState.filteredReceipts.isEmpty()) {
                EmptyReceiptsView(
                    onScanClick = { showScanSourceOptions = true },
                    onManualClick = { viewModel.openManualAdd(true) }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.filteredReceipts, key = { it.id }) { receipt ->
                        DismissibleReceiptItem(
                            receipt = receipt,
                            onClick = { viewModel.selectReceiptForDetail(receipt) },
                            onDelete = { viewModel.deleteReceipt(receipt.id) }
                        )
                    }
                }
            }
        }
    }

    // CameraX Full Capture View Dialog
    if (showCameraXDialog) {
        CameraXCaptureDialog(
            onDismiss = { showCameraXDialog = false },
            onImageCaptured = { uri ->
                showCameraXDialog = false
                viewModel.scanReceiptImage(uri)
            }
        )
    }

    // Camera vs Gallery Dialog Selection
    if (showScanSourceOptions) {
        AlertDialog(
            onDismissRequest = { showScanSourceOptions = false },
            title = { Text("Fiş Fotoğrafı Ekle", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("CameraX in-app kamerayla fişinizi çekin veya galerinizden seçin.")

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showScanSourceOptions = false
                                showCameraXDialog = true
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("CameraX ile Fotoğraf Çek", fontWeight = FontWeight.Bold)
                                Text("Uygulama içi kamera ile hiza çerçeveli tarama", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showScanSourceOptions = false
                                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Galeriden Seç", fontWeight = FontWeight.Bold)
                                Text("Cihazındaki kaydetilmiş fişlerden yükle", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showScanSourceOptions = false }) {
                    Text("Kapat")
                }
            }
        )
    }

    // Saved Custom Reports Modal
    if (uiState.isSavedReportsModalOpen) {
        SavedReportsModal(
            reports = uiState.reports,
            allReceipts = uiState.receipts,
            onDismiss = { viewModel.openSavedReportsModal(false) },
            onCreateReport = { title, desc, ver, receiptIds ->
                viewModel.createCustomReport(title, desc, ver, receiptIds)
            },
            onDeleteReport = { id -> viewModel.deleteReport(id) },
            onAnalyzeReport = { report -> viewModel.analyzeReportWithAi(report) },
            onExportReport = { report -> viewModel.exportSpecificReport(report) },
            onExportReportPdf = { report -> viewModel.exportSpecificReportPdf(report) },
            aiAnalysisLoading = uiState.isAiAnalysisLoading,
            selectedReportAnalysis = uiState.activeReportAnalysisText
        )
    }

    // AI Assistant Modal
    if (uiState.isAiAssistantModalOpen) {
        AiAssistantModal(
            receipts = uiState.receipts,
            onDismiss = { viewModel.openAiAssistantModal(false) },
            onAskAiQuestion = { q, callback ->
                viewModel.askAiAssistant(q, callback)
            }
        )
    }

    // Google Account Modal
    if (uiState.isGoogleAccountModalOpen) {
        GoogleAccountModal(
            currentUser = uiState.currentUser,
            totalReceiptsCount = uiState.receipts.size,
            onDismiss = { viewModel.openGoogleAccountModal(false) },
            onSignInGoogle = { email -> viewModel.signInWithGoogle(email) },
            onSignOutGoogle = { viewModel.signOutGoogle() },
            onSyncNow = { viewModel.syncCloudHistory() }
        )
    }

    // Live Exchange Rates Modal
    if (showExchangeRatesModal) {
        com.example.ui.components.ExchangeRatesModal(
            onDismiss = { showExchangeRatesModal = false }
        )
    }

    // Expense Analytics Bar Chart Modal
    if (showAnalyticsModal) {
        ExpenseAnalyticsModal(
            receipts = uiState.receipts,
            onDismiss = { showAnalyticsModal = false },
            onCategorySelected = { cat ->
                viewModel.setSelectedCategory(cat)
            }
        )
    }

    // Scan Modal Dialog
    if (uiState.isScanning || uiState.scannedResult != null || uiState.scanError != null) {
        ScanReceiptModal(
            isScanning = uiState.isScanning,
            progressMessage = uiState.scanProgressMessage,
            scanError = uiState.scanError,
            parsedReceipt = uiState.scannedResult,
            imageUri = uiState.scannedImageUri,
            onDismiss = { viewModel.clearScanState() },
            onSave = { parsed, uri, notes ->
                viewModel.saveScannedReceipt(parsed, uri, notes)
            }
        )
    }

    // Detail & Edit Modal
    if (uiState.selectedReceiptForDetail != null) {
        ReceiptDetailModal(
            receipt = uiState.selectedReceiptForDetail,
            onDismiss = { viewModel.selectReceiptForDetail(null) },
            onDelete = { viewModel.deleteReceipt(it) },
            onUpdate = { viewModel.updateReceipt(it) }
        )
    }

    // Export Modal
    if (uiState.isExportModalOpen) {
        ExcelExportModal(
            receiptsCount = uiState.filteredReceipts.size,
            totalSpend = uiState.filteredReceipts.sumOf { it.totalAmount },
            onDismiss = { viewModel.openExportModal(false) },
            onConfirmExport = { useHtmlXls ->
                viewModel.exportToExcel(useHtmlXls)
            },
            onConfirmExportPdf = {
                viewModel.exportToPdf()
            }
        )
    }

    // Manual Add Modal
    if (uiState.isManualAddOpen) {
        ManualAddModal(
            onDismiss = { viewModel.openManualAdd(false) },
            onSave = { parsed, notes ->
                viewModel.saveScannedReceipt(parsed, null, notes)
                viewModel.openManualAdd(false)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DismissibleReceiptItem(
    receipt: ReceiptEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart || value == SwipeToDismissBoxValue.StartToEnd) {
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val isDismissing = dismissState.targetValue != SwipeToDismissBoxValue.Settled
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isDismissing) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .padding(horizontal = 20.dp),
                contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Sil",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sil",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        },
        content = {
            ReceiptItemCard(receipt = receipt, onClick = onClick)
        }
    )
}

@Composable
private fun StatsHeaderCard(
    receipts: List<ReceiptEntity>,
    filteredCount: Int,
    onExportClick: () -> Unit,
    onAnalyticsClick: () -> Unit
) {
    val totalSpend = receipts.sumOf { it.totalAmount }
    val totalTax = receipts.sumOf { it.taxAmount }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ExcelGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TOPLAM MASRAF HESABI",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${String.format(Locale("tr", "TR"), "%.2f", totalSpend)} ₺",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        IconButton(onClick = onAnalyticsClick) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "Grafikler",
                                tint = Color.White
                            )
                        }
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        IconButton(onClick = onExportClick) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Excel'e Aktar",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.25f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Fiş Sayısı",
                    value = "${receipts.size} Adet",
                    icon = Icons.Default.Receipt
                )
                StatItem(
                    label = "Hesaplanan KDV",
                    value = "${String.format(Locale("tr", "TR"), "%.2f", totalTax)} ₺",
                    icon = Icons.Default.Calculate
                )
                StatItem(
                    label = "Listelenen",
                    value = "$filteredCount Kayıt",
                    icon = Icons.Default.FilterList
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.75f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun ReceiptItemCard(receipt: ReceiptEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Receipt Thumbnail or Category Icon
            if (receipt.imagePath.isNotBlank()) {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(56.dp)
                ) {
                    AsyncImage(
                        model = receipt.imagePath,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = getCategoryColor(receipt.category).copy(alpha = 0.15f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = getCategoryIcon(receipt.category),
                            contentDescription = null,
                            tint = getCategoryColor(receipt.category),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = receipt.merchantName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${String.format("%.2f", receipt.totalAmount)} ${receipt.currency}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = ExcelGreen
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (receipt.itemsSummary.isNotBlank()) receipt.itemsSummary else receipt.notes.ifBlank { "Fiş detayı" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = receipt.date,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    CategoryPill(category = receipt.category)
                    if (receipt.tags.isNotBlank()) {
                        receipt.tags.split(",").forEach { tagStr ->
                            val cleanTag = tagStr.trim()
                            if (cleanTag.isNotEmpty()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "#$cleanTag",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                    if (receipt.receiptNumber.isNotBlank()) {
                        Text(
                            text = "#${receipt.receiptNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryPill(category: String) {
    Surface(
        color = getCategoryColor(category).copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = category,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = getCategoryColor(category),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EmptyReceiptsView(onScanClick: () -> Unit, onManualClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = ExcelGreen.copy(alpha = 0.1f),
                modifier = Modifier.size(90.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = ExcelGreen,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Henüz Fiş Eklenmedi",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Fişlerinizin fotoğraflarını çekip yapay zeka ile otomatik listeleyin ve Excel çıktısı alın.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onScanClick,
                colors = ButtonDefaults.buttonColors(containerColor = ExcelGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Fiş Fotoğrafı Tara", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onManualClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Manuel Fiş Gir")
            }
        }
    }
}

private fun getCategoryIcon(category: String): ImageVector {
    return when {
        category.contains("Market", ignoreCase = true) -> Icons.Default.ShoppingCart
        category.contains("Yiyecek", ignoreCase = true) -> Icons.Default.Restaurant
        category.contains("Akaryakıt", ignoreCase = true) || category.contains("Ulaşım", ignoreCase = true) -> Icons.Default.LocalGasStation
        category.contains("Ofis", ignoreCase = true) -> Icons.Default.Work
        category.contains("Konaklama", ignoreCase = true) -> Icons.Default.Hotel
        category.contains("Sağlık", ignoreCase = true) -> Icons.Default.MedicalServices
        else -> Icons.Default.Receipt
    }
}

private fun getCategoryColor(category: String): Color {
    return when {
        category.contains("Market", ignoreCase = true) -> Color(0xFF2E7D32)
        category.contains("Yiyecek", ignoreCase = true) -> Color(0xFFE65100)
        category.contains("Akaryakıt", ignoreCase = true) -> Color(0xFF0277BD)
        category.contains("Ofis", ignoreCase = true) -> Color(0xFF6A1B9A)
        category.contains("Konaklama", ignoreCase = true) -> Color(0xFFC2185B)
        else -> Color(0xFF455A64)
    }
}

@Composable
fun GoogleAccountBannerCard(
    currentUser: com.example.data.local.UserProfile?,
    onOpenAccountModal: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onOpenAccountModal() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (currentUser?.isLoggedIn == true) {
                ExcelGreen.copy(alpha = 0.12f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                if (currentUser?.isLoggedIn == true) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = ExcelGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Google: ${currentUser.displayName}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Geçmiş fişleriniz bulutta senkronize",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Google Hesabı ile Giriş Yapın",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Fiş & masraf geçmişinizi güvenle yedekleyin",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (currentUser?.isLoggedIn == true) ExcelGreen else MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = if (currentUser?.isLoggedIn == true) "Hesabım" else "Giriş Yap",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}
