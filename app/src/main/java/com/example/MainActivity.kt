package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.core.content.ContextCompat
import com.example.data.db.AppDatabase
import com.example.data.db.ConversationWithMessages
import com.example.data.db.ScanRepository
import com.example.data.model.AiAnalysisResult
import com.example.data.model.LanguageCode
import com.example.data.model.MessageEntity
import com.example.service.GeminiAiService
import com.example.service.SpeechToTextManager
import com.example.service.TextToSpeechManager
import com.example.ui.components.LanguageSelectorDialog
import com.example.ui.components.PermissionDialog
import com.example.ui.screens.AnalysisLoadingScreen
import com.example.ui.screens.CameraScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ImagePreviewScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.WhatsThisTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

enum class AppScreen {
    ONBOARDING,
    HOME,
    CAMERA,
    PREVIEW,
    LOADING,
    CHAT,
    HISTORY,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(applicationContext)
        val repository = ScanRepository(db.scanDao())
        val geminiService = GeminiAiService(applicationContext)

        setContent {
            WhatsThisApp(
                repository = repository,
                geminiService = geminiService
            )
        }
    }
}

@Composable
fun WhatsThisApp(
    repository: ScanRepository,
    geminiService: GeminiAiService
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // SharedPreferences for settings
    val prefs = remember { context.getSharedPreferences("whats_this_prefs", Context.MODE_PRIVATE) }

    var currentLanguage by remember {
        val savedCode = prefs.getString("app_language", null)
        val defaultCode = savedCode ?: java.util.Locale.getDefault().language
        mutableStateOf(LanguageCode.fromCode(defaultCode))
    }
    var isFirstRun by remember {
        mutableStateOf(prefs.getBoolean("first_run", true))
    }
    var isMockMode by remember {
        mutableStateOf(prefs.getBoolean("mock_mode", false))
    }
    var saveHistory by remember {
        mutableStateOf(prefs.getBoolean("save_history", true))
    }
    var detailLevel by remember {
        mutableStateOf(prefs.getString("detail_level", "balanced") ?: "balanced")
    }
    var speechRate by remember {
        mutableFloatStateOf(prefs.getFloat("speech_rate", 1.0f))
    }
    var autoDeleteDays by remember {
        mutableIntStateOf(prefs.getInt("auto_delete_days", 0))
    }

    var currentScreen by remember {
        mutableStateOf(if (isFirstRun) AppScreen.ONBOARDING else AppScreen.HOME)
    }

    // Active scan state
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var capturedImagePath by remember { mutableStateOf<String?>(null) }
    var pendingQuestion by remember { mutableStateOf("") }
    var activeConversationId by remember { mutableStateOf<String?>(null) }

    // Dialog & Permission States
    var showLanguageDialog by remember { mutableStateOf(false) }
    var permissionNeeded by remember { mutableStateOf<String?>(null) }

    // Managers
    val speechToTextManager = remember { SpeechToTextManager(context) }
    val textToSpeechManager = remember { TextToSpeechManager(context) }

    val speechState by speechToTextManager.speechState.collectAsState()
    val spokenText by speechToTextManager.spokenText.collectAsState()
    val isSpeaking by textToSpeechManager.isSpeaking.collectAsState()

    // Database conversations
    val allConversations by repository.allConversations.collectAsState(initial = emptyList())
    val activeConversationWithMessages = remember(allConversations, activeConversationId) {
        allConversations.find { it.conversation.id == activeConversationId }
    }

    // Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            currentScreen = AppScreen.CAMERA
        }
    }

    // Audio Permission Launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            speechToTextManager.startListening(currentLanguage)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechToTextManager.stopListening()
            textToSpeechManager.shutdown()
        }
    }

    LaunchedEffect(currentLanguage) {
        repository.enforceMaxLimit(20)
        repository.translateAllHistoryToLanguage(currentLanguage, geminiService)
    }

    CompositionLocalProvider(LocalLayoutDirection provides currentLanguage.direction) {
        WhatsThisTheme {
            // Intercept system back press when in sub-screens (Camera, Preview, Chat, History, Settings)
            BackHandler(enabled = currentScreen != AppScreen.HOME && currentScreen != AppScreen.ONBOARDING) {
                capturedBitmap = null
                capturedImagePath = null
                currentScreen = AppScreen.HOME
            }

            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Box(modifier = Modifier.fillMaxSize()) {
                    Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
                        when (screen) {
                            AppScreen.ONBOARDING -> {
                                OnboardingScreen(
                                    language = currentLanguage,
                                    onFinish = {
                                        prefs.edit().putBoolean("first_run", false).apply()
                                        isFirstRun = false
                                        currentScreen = AppScreen.HOME
                                    }
                                )
                            }

                            AppScreen.HOME -> {
                                HomeScreen(
                                    selectedLanguage = currentLanguage,
                                    recentScans = allConversations,
                                    onOpenLanguageDialog = { showLanguageDialog = true },
                                    onOpenCamera = {
                                        pendingQuestion = ""
                                        val hasPermission = ContextCompat.checkSelfPermission(
                                            context, Manifest.permission.CAMERA
                                        ) == PackageManager.PERMISSION_GRANTED

                                        if (hasPermission) {
                                            currentScreen = AppScreen.CAMERA
                                        } else {
                                            permissionNeeded = "camera"
                                        }
                                    },
                                    onPhotoSelected = { uri ->
                                        pendingQuestion = ""
                                        scope.launch {
                                            val loaded = loadBitmapFromUri(context, uri)
                                            if (loaded != null) {
                                                capturedBitmap = loaded
                                                capturedImagePath = saveTempBitmap(context, loaded)
                                                currentScreen = AppScreen.PREVIEW
                                            }
                                        }
                                    },
                                    onAskWithoutPhoto = {
                                        capturedBitmap = null
                                        capturedImagePath = null
                                        val newId = UUID.randomUUID().toString()
                                        activeConversationId = newId
                                        currentScreen = AppScreen.CHAT
                                    },
                                    onExampleClicked = { prompt ->
                                        pendingQuestion = prompt
                                        val hasPermission = ContextCompat.checkSelfPermission(
                                            context, Manifest.permission.CAMERA
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (hasPermission) {
                                            currentScreen = AppScreen.CAMERA
                                        } else {
                                            permissionNeeded = "camera"
                                        }
                                    },
                                    onRecentScanClicked = { convId ->
                                        capturedBitmap = null
                                        capturedImagePath = null
                                        activeConversationId = convId
                                        currentScreen = AppScreen.CHAT
                                    },
                                    onHistoryClick = { currentScreen = AppScreen.HISTORY },
                                    onSettingsClick = { currentScreen = AppScreen.SETTINGS }
                                )
                            }

                            AppScreen.CAMERA -> {
                                CameraScreen(
                                    language = currentLanguage,
                                    onPhotoCaptured = { bitmap, path ->
                                        capturedBitmap = bitmap
                                        capturedImagePath = path
                                        currentScreen = AppScreen.PREVIEW
                                    },
                                    onCancel = { currentScreen = AppScreen.HOME }
                                )
                            }

                            AppScreen.PREVIEW -> {
                                capturedBitmap?.let { bmp ->
                                    ImagePreviewScreen(
                                        bitmap = bmp,
                                        language = currentLanguage,
                                        initialQuestion = pendingQuestion,
                                        onRetake = { currentScreen = AppScreen.CAMERA },
                                        onChooseAnother = { currentScreen = AppScreen.HOME },
                                        onAnalyze = { finalBmp, qText ->
                                            pendingQuestion = qText
                                            currentScreen = AppScreen.LOADING

                                            scope.launch {
                                                val result = geminiService.analyzeVisualContent(
                                                    bitmap = finalBmp,
                                                    question = qText,
                                                    language = currentLanguage,
                                                    detailLevel = detailLevel,
                                                    isMockMode = isMockMode
                                                )

                                                val newId = UUID.randomUUID().toString()
                                                activeConversationId = newId

                                                if (saveHistory) {
                                                    repository.createScanConversation(
                                                        id = newId,
                                                        imagePath = capturedImagePath,
                                                        question = qText,
                                                        result = result
                                                    )
                                                }
                                                currentScreen = AppScreen.CHAT
                                            }
                                        },
                                        onMicClick = {
                                            val hasMicPerm = ContextCompat.checkSelfPermission(
                                                context, Manifest.permission.RECORD_AUDIO
                                            ) == PackageManager.PERMISSION_GRANTED

                                            if (hasMicPerm) {
                                                speechToTextManager.startListening(currentLanguage)
                                            } else {
                                                permissionNeeded = "microphone"
                                            }
                                        }
                                    )
                                } ?: LaunchedEffect(Unit) { currentScreen = AppScreen.HOME }
                            }

                            AppScreen.LOADING -> {
                                AnalysisLoadingScreen(
                                    language = currentLanguage,
                                    onCancel = { currentScreen = AppScreen.PREVIEW }
                                )
                            }

                            AppScreen.CHAT -> {
                                val conversationId = activeConversationId ?: UUID.randomUUID().toString()
                                val messagesList = activeConversationWithMessages?.messages ?: emptyList()

                                ChatScreen(
                                    conversationId = conversationId,
                                    imagePath = capturedImagePath ?: activeConversationWithMessages?.conversation?.imagePath,
                                    bitmap = capturedBitmap,
                                    messages = messagesList,
                                    language = currentLanguage,
                                    speechState = speechState,
                                    spokenText = spokenText,
                                    isSpeaking = isSpeaking,
                                    onSendMessage = { userMsg ->
                                        scope.launch {
                                            val imagePathToUse = capturedImagePath ?: activeConversationWithMessages?.conversation?.imagePath
                                            repository.addUserMessage(conversationId, userMsg, imagePathToUse)

                                            val bitmapToUse = capturedBitmap ?: imagePathToUse?.let { path ->
                                                loadBitmapFromFile(path)
                                            }

                                            val currentMsgs = activeConversationWithMessages?.messages ?: messagesList
                                            val historyList = currentMsgs.map { Pair(it.sender, it.text) } + Pair("user", userMsg)

                                            val aiResult = geminiService.analyzeVisualContent(
                                                bitmap = bitmapToUse,
                                                question = userMsg,
                                                language = currentLanguage,
                                                detailLevel = detailLevel,
                                                isMockMode = isMockMode,
                                                conversationHistory = historyList
                                            )

                                            repository.addAiMessage(conversationId, aiResult)
                                        }
                                    },
                                    onStartVoiceInput = {
                                        val hasMicPerm = ContextCompat.checkSelfPermission(
                                            context, Manifest.permission.RECORD_AUDIO
                                        ) == PackageManager.PERMISSION_GRANTED

                                        if (hasMicPerm) {
                                            speechToTextManager.startListening(currentLanguage)
                                        } else {
                                            permissionNeeded = "microphone"
                                        }
                                    },
                                    onStopVoiceInput = { speechToTextManager.stopListening() },
                                    onSpeakText = { text -> textToSpeechManager.speak(text, currentLanguage, speechRate) },
                                    onStopSpeech = { textToSpeechManager.stop() },
                                    onBack = {
                                        capturedBitmap = null
                                        capturedImagePath = null
                                        currentScreen = AppScreen.HOME
                                    }
                                )
                            }

                            AppScreen.HISTORY -> {
                                HistoryScreen(
                                    conversations = allConversations,
                                    language = currentLanguage,
                                    onConversationClick = { id ->
                                        capturedBitmap = null
                                        capturedImagePath = null
                                        activeConversationId = id
                                        currentScreen = AppScreen.CHAT
                                    },
                                    onDeleteConversation = { id ->
                                        scope.launch { repository.deleteConversation(id) }
                                    },
                                    onClearAllHistory = {
                                        scope.launch { repository.clearAllHistory() }
                                    },
                                    onBack = { currentScreen = AppScreen.HOME }
                                )
                            }

                            AppScreen.SETTINGS -> {
                                SettingsScreen(
                                    language = currentLanguage,
                                    isMockMode = isMockMode,
                                    saveHistory = saveHistory,
                                    detailLevel = detailLevel,
                                    speechRate = speechRate,
                                    autoDeleteDays = autoDeleteDays,
                                    onOpenLanguageDialog = { showLanguageDialog = true },
                                    onMockModeChange = {
                                        isMockMode = it
                                        prefs.edit().putBoolean("mock_mode", it).apply()
                                    },
                                    onSaveHistoryChange = {
                                        saveHistory = it
                                        prefs.edit().putBoolean("save_history", it).apply()
                                    },
                                    onDetailLevelChange = {
                                        detailLevel = it
                                        prefs.edit().putString("detail_level", it).apply()
                                    },
                                    onSpeechRateChange = {
                                        speechRate = it
                                        prefs.edit().putFloat("speech_rate", it).apply()
                                    },
                                    onAutoDeleteDaysChange = {
                                        autoDeleteDays = it
                                        prefs.edit().putInt("auto_delete_days", it).apply()
                                        if (it > 0) {
                                            scope.launch { repository.autoDeleteOldHistory(it) }
                                        }
                                    },
                                    onBack = { currentScreen = AppScreen.HOME }
                                )
                            }
                        }
                    }

                    // Dialog Overlays
                    if (showLanguageDialog) {
                        LanguageSelectorDialog(
                            currentLanguage = currentLanguage,
                            onLanguageSelected = { newLang ->
                                if (newLang != currentLanguage) {
                                    currentLanguage = newLang
                                    prefs.edit().putString("app_language", newLang.code).apply()
                                    scope.launch {
                                        repository.translateAllHistoryToLanguage(newLang, geminiService)
                                    }
                                }
                                showLanguageDialog = false
                            },
                            onDismiss = { showLanguageDialog = false }
                        )
                    }

                    permissionNeeded?.let { type ->
                        PermissionDialog(
                            permissionType = type,
                            language = currentLanguage,
                            onGrant = {
                                permissionNeeded = null
                                if (type == "camera") {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                } else {
                                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            onDismiss = { permissionNeeded = null }
                        )
                    }
                }
            }
        }
    }
}

private suspend fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
    try {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        }
    } catch (e: Exception) {
        null
    }
}

private suspend fun loadBitmapFromFile(path: String): Bitmap? = withContext(Dispatchers.IO) {
    try {
        val file = File(path)
        if (file.exists()) {
            BitmapFactory.decodeFile(path)
        } else null
    } catch (e: Exception) {
        null
    }
}

private suspend fun saveTempBitmap(context: Context, bitmap: Bitmap): String = withContext(Dispatchers.IO) {
    val file = File(context.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
    }
    file.absolutePath
}
