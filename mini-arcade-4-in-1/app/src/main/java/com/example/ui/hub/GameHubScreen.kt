package com.example.ui.hub

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.DailyMissionEntity
import com.example.data.GameHistoryRecord
import com.example.data.GameStat
import com.example.data.UserSettings
import com.example.data.UserWallet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.utils.SoundManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


data class GameInfo(
    val id: String,
    val titleTr: String,
    val titleEn: String,
    val subtitleTr: String,
    val subtitleEn: String,
    val descTr: String,
    val descEn: String,
    val bannerResId: Int,
    val accentColor: Color,
    val tagsTr: List<String>,
    val tagsEn: List<String>
)

val GAME_LIST = listOf(
    GameInfo(
        id = "block_blast",
        titleTr = "Blok Patlat",
        titleEn = "Block Blast",
        subtitleTr = "Block Blast Tarzı",
        subtitleEn = "Block Puzzle Style",
        descTr = "Izgaraya blok yerleştir, satır ve sütunları doldurarak patlat! Anında anlaşılır, offline bağımlılık.",
        descEn = "Place blocks on the grid, fill rows and columns to blast them! Fast-paced offline fun.",
        bannerResId = R.drawable.block_blast_banner_1785788371942,
        accentColor = NeonCyan,
        tagsTr = listOf("OFFLINE", "STRATEJİ", "ÇOK HIZLI"),
        tagsEn = listOf("OFFLINE", "STRATEGY", "FAST")
    ),
    GameInfo(
        id = "slice_it",
        titleTr = "Slice It All",
        titleEn = "Slice It All",
        subtitleTr = "Bıçakla Dilimleme",
        subtitleEn = "Knife Slicing Action",
        descTr = "Bıçakla nesneleri dilimle, tatmin edici yıkım hissi! Tek dokunuşla havada takla at.",
        descEn = "Slice objects with your knife! Flip in mid-air with satisfying physics and destruction.",
        bannerResId = R.drawable.slice_it_banner_1785788381580,
        accentColor = NeonMagenta,
        tagsTr = listOf("FİZİK", "TATMİNKAR", "VIRAL"),
        tagsEn = listOf("PHYSICS", "SATISFYING", "VIRAL")
    ),
    GameInfo(
        id = "pizza_ready",
        titleTr = "Pizza Ready",
        titleEn = "Pizza Ready",
        subtitleTr = "Hızlı Yönetim Mini-Simi",
        subtitleEn = "Fast Pizza Management",
        descTr = "Sipariş al → hamur aç → fırınla → servis et! Hız baskısı altında restoran büyüt.",
        descEn = "Take orders → roll dough → bake → serve! Expand your pizza shop under pressure.",
        bannerResId = R.drawable.pizza_ready_banner_1785788391861,
        accentColor = NeonGold,
        tagsTr = listOf("MİNİ-SİM", "RESTORAN", "ZAMAN YÖNETİMİ"),
        tagsEn = listOf("MINI-SIM", "RESTAURANT", "TIME MGMT")
    ),
    GameInfo(
        id = "fun_race",
        titleTr = "Fun Race 3D",
        titleEn = "Fun Race 3D",
        subtitleTr = "3D Parkur Yarışı",
        subtitleEn = "3D Obstacle Race",
        descTr = "Tek dokunuşlu parkur yarışı. Dönen çekiçlerden kaç, komik düşüşler ve rekabet!",
        descEn = "One-touch 3D obstacle race. Dodge rotating hammers, pushers, and win!",
        bannerResId = R.drawable.fun_race_banner_1785788404044,
        accentColor = NeonGreen,
        tagsTr = listOf("3D PARKUR", "ENGEL KAÇMA", "KOMİK"),
        tagsEn = listOf("3D RACER", "OBSTACLES", "FUN")
    )
)

@Composable
fun GameHubScreen(
    wallet: UserWallet,
    gameStats: List<GameStat>,
    userSettings: UserSettings,
    dailyMissions: List<DailyMissionEntity> = emptyList(),
    gameHistory: List<GameHistoryRecord> = emptyList(),
    onLaunchGame: (String) -> Unit,
    onClaimMissionReward: (String) -> Unit = {},
    onBuySkin: (String, String, Int) -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleMusic: (Boolean) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onSelectLanguage: (String) -> Unit,
    onLoginGoogle: (String, String) -> Unit = { _, _ -> },
    onLogoutGoogle: () -> Unit = {}
) {

    var isShopOpen by remember { mutableStateOf(false) }
    var isSettingsOpen by remember { mutableStateOf(false) }
    var isGoogleAuthOpen by remember { mutableStateOf(false) }
    var isHistoryOpen by remember { mutableStateOf(false) }

    val isTr = userSettings.language.uppercase() == "TR"

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Arcade Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = "Arcade Logo",
                        tint = NeonCyan,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "MINI ARCADE",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (isTr) "4'Ü 1 ARADA HİPER-KAZUAL HUB" else "4-IN-1 HYPERCASUAL HUB",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Google Profile / Sign In Pill
                    Surface(
                        onClick = { isGoogleAuthOpen = true },
                        color = if (userSettings.isLoggedIn) MaterialTheme.colorScheme.surface else NeonCyan.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = if (userSettings.isLoggedIn) NeonGreen else NeonCyan,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .testTag("google_auth_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (userSettings.isLoggedIn) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = NeonGreen.copy(alpha = 0.2f),
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = userSettings.googleName.take(1).uppercase().ifEmpty { "G" },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = NeonGreen
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isTr) "Giriş Yapıldı" else "Logged In",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonGreen
                                )
                            } else {
                                Text(
                                    text = "G",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NeonCyan
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isTr) "Giriş Yap" else "Sign In",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Game History Button
                    IconButton(
                        onClick = { isHistoryOpen = true },
                        modifier = Modifier.testTag("open_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Game History",
                            tint = NeonGold,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Coin Counter
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.border(1.dp, NeonGold, RoundedCornerShape(20.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🪙", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${wallet.coins}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeonGold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Settings Button
                    IconButton(
                        onClick = { isSettingsOpen = true },
                        modifier = Modifier.testTag("open_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Shop Button
                    IconButton(
                        onClick = { isShopOpen = true },
                        modifier = Modifier.testTag("open_shop_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = "Arcade Shop",
                            tint = NeonMagenta,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Game Cards List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Daily Missions Header Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, NeonGold.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "🎯", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isTr) "GÜNLÜK GÖREVLER" else "DAILY MISSIONS",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Surface(
                                    color = NeonGold.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (isTr) "HER GÜN YENİLENİR" else "RESETS DAILY",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = NeonGold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (dailyMissions.isEmpty()) {
                                Text(
                                    text = if (isTr) "Görevler yükleniyor..." else "Loading missions...",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    dailyMissions.forEach { mission ->
                                        DailyMissionItem(
                                            mission = mission,
                                            isTr = isTr,
                                            soundEnabled = userSettings.soundEnabled,
                                            onClaim = {
                                                SoundManager.playSuccess(userSettings.soundEnabled)
                                                onClaimMissionReward(mission.id)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                items(GAME_LIST) { game ->

                    val stat = gameStats.find { it.gameId == game.id }
                    val highScore = stat?.highScore ?: 0

                    val gameTitle = if (isTr) game.titleTr else game.titleEn
                    val gameSubtitle = if (isTr) game.subtitleTr else game.subtitleEn
                    val gameDesc = if (isTr) game.descTr else game.descEn
                    val gameTags = if (isTr) game.tagsTr else game.tagsEn

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
                            .clickable { onLaunchGame(game.id) }
                            .testTag("game_card_${game.id}")
                    ) {
                        Column {
                            // Banner Image
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = game.bannerResId),
                                    contentDescription = gameTitle,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Gradient Overlay
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, MaterialTheme.colorScheme.surface)
                                            )
                                        )
                                )

                                // Tags Pill
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    gameTags.forEach { tag ->
                                        Surface(
                                            color = Color.Black.copy(alpha = 0.75f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = tag,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = game.accentColor,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Card Content Body
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = gameTitle,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = gameSubtitle,
                                            fontSize = 12.sp,
                                            color = game.accentColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // High Score Badge
                                    Surface(
                                        color = MaterialTheme.colorScheme.background,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.EmojiEvents,
                                                contentDescription = "Best Score",
                                                tint = NeonGold,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${if (isTr) "EN İYİ" else "BEST"}: $highScore",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = gameDesc,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = { onLaunchGame(game.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = game.accentColor),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("play_button_${game.id}")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play",
                                            tint = Color.Black
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isTr) "HEMEN OYNA" else "PLAY NOW",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Settings Dialog Modal
        AnimatedVisibility(visible = isSettingsOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp)
                        .border(2.dp, NeonCyan, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = NeonCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTr) "AYARLAR ⚙️" else "SETTINGS ⚙️",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonCyan
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 1. Language Preference Toggle
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (isTr) "Uygulama Dili" else "App Language",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = if (isTr) "Türkçe / English" else "English / Türkçe",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = { onSelectLanguage("TR") },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isTr) NeonCyan else MaterialTheme.colorScheme.surface
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("lang_tr_button")
                                    ) {
                                        Text(
                                            text = "TR 🇹🇷",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isTr) Color.Black else MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Button(
                                        onClick = { onSelectLanguage("EN") },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (!isTr) NeonCyan else MaterialTheme.colorScheme.surface
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("lang_en_button")
                                    ) {
                                        Text(
                                            text = "EN 🇬🇧",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (!isTr) Color.Black else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Theme Preference Switch (Dark / Light Mode)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (userSettings.darkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                        contentDescription = "Theme Icon",
                                        tint = NeonGold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (isTr) "Karanlık Tema" else "Dark Mode",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = if (userSettings.darkMode)
                                                (if (isTr) "Karanlık Mod Açık" else "Dark Theme Active")
                                            else
                                                (if (isTr) "Aydınlık Mod Açık" else "Light Theme Active"),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Switch(
                                    checked = userSettings.darkMode,
                                    onCheckedChange = { onToggleDarkMode(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = NeonGold,
                                        checkedTrackColor = NeonCyan
                                    ),
                                    modifier = Modifier.testTag("dark_mode_switch")
                                )
                            }
                        }

                        // 3. Global Sound FX Switch
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (userSettings.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                        contentDescription = "Sound FX",
                                        tint = NeonGreen
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (isTr) "Ses Efektleri (SFX)" else "Sound Effects (SFX)",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = if (userSettings.soundEnabled)
                                                (if (isTr) "Sesler Açık" else "Sounds Enabled")
                                            else
                                                (if (isTr) "Sessiz Mod" else "Muted"),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Switch(
                                    checked = userSettings.soundEnabled,
                                    onCheckedChange = { onToggleSound(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = NeonGreen,
                                        checkedTrackColor = NeonCyan
                                    ),
                                    modifier = Modifier.testTag("sound_switch")
                                )
                            }
                        }

                        // 4. Global Music Switch
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (userSettings.musicEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff,
                                        contentDescription = "Background Music",
                                        tint = NeonMagenta
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (isTr) "Arka Plan Müzik" else "Background Music",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = if (userSettings.musicEnabled)
                                                (if (isTr) "Müzik Açık" else "Music Enabled")
                                            else
                                                (if (isTr) "Kapalı" else "Disabled"),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Switch(
                                    checked = userSettings.musicEnabled,
                                    onCheckedChange = { onToggleMusic(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = NeonMagenta,
                                        checkedTrackColor = NeonCyan
                                    ),
                                    modifier = Modifier.testTag("music_switch")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Google Account & Cloud Sync Tile in Settings
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (userSettings.isLoggedIn) NeonGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.background
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    isSettingsOpen = false
                                    isGoogleAuthOpen = true
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (userSettings.isLoggedIn) NeonGreen else Color(0xFF4285F4),
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = if (userSettings.isLoggedIn) "✓" else "G",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (userSettings.isLoggedIn)
                                                (if (isTr) "Google Hesabı Bağlı" else "Google Account Connected")
                                            else
                                                (if (isTr) "Google ile Giriş Yap" else "Sign In with Google"),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = if (userSettings.isLoggedIn) userSettings.googleEmail else (if (isTr) "Geçmişi ve skorları buluta kaydet" else "Save history & scores to cloud"),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Text(
                                    text = if (isTr) "YÖNET" else "MANAGE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan
                                )
                            }
                        }

                        // Game History Button in Settings
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    isSettingsOpen = false
                                    isHistoryOpen = true
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = "History",
                                        tint = NeonGold
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (isTr) "Oyun Geçmişi" else "Game History",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = if (isTr) "${gameHistory.size} kayıtlı seans" else "${gameHistory.size} saved sessions",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Text(
                                    text = if (isTr) "GÖR" else "VIEW",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonGold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { isSettingsOpen = false },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("close_settings_button")
                        ) {
                            Text(
                                text = if (isTr) "KAPAT" else "CLOSE",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Arcade Shop Dialog
        AnimatedVisibility(visible = isShopOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp)
                        .border(2.dp, NeonMagenta, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isTr) "ARCADE KOSTÜM DÜKKANI 🛍️" else "ARCADE SKIN SHOP 🛍️",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonMagenta
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "${if (isTr) "Mevcut Coin" else "Current Coins"}: ${wallet.coins} 🪙",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Skin 1
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (isTr) "Neon Blok Teması" else "Neon Block Theme",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = if (isTr) "Block Blast için parlayan bloklar" else "Glowing blocks for Block Blast",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Button(
                                    onClick = { onBuySkin("block", "neon", 100) },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                                ) {
                                    Text("100 🪙", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Skin 2
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (isTr) "Lazer Katana Bıçak" else "Laser Katana Blade",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = if (isTr) "Slice It için neon kılıç izi" else "Neon blade slice effect",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Button(
                                    onClick = { onBuySkin("blade", "laser", 150) },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta)
                                ) {
                                    Text("150 🪙", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { isShopOpen = false },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isTr) "KAPAT" else "CLOSE",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Google Auth Dialog Modal
        AnimatedVisibility(visible = isGoogleAuthOpen) {
            var inputEmail by remember { mutableStateOf("") }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(16.dp)
                        .border(2.dp, NeonCyan, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "G",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF4285F4)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isTr) "GOOGLE İLE HESAP" else "GOOGLE ACCOUNT",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (userSettings.isLoggedIn) {
                            // Already Logged In State
                            Surface(
                                color = NeonGreen.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDone,
                                        contentDescription = "Cloud Sync",
                                        tint = NeonGreen,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (isTr) "Bulut Senkronizasyonu Aktif ☁️" else "Cloud Sync Active ☁️",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonGreen
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = userSettings.googleName.ifEmpty { "Google Oyuncusu" },
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = userSettings.googleEmail,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    isGoogleAuthOpen = false
                                    isHistoryOpen = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGold),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = "History",
                                        tint = Color.Black
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isTr) "OYUN GEÇMİŞİNİ GÖR 📜" else "VIEW GAME HISTORY 📜",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    onLogoutGoogle()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Logout,
                                        contentDescription = "Logout",
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isTr) "ÇIKIŞ YAP" else "SIGN OUT",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        } else {
                            // Not Logged In State
                            Text(
                                text = if (isTr)
                                    "Oyun geçmişinizi, seviyelerinizi ve kazandığınız coinleri Google hesabınızla kaydedin."
                                else
                                    "Save your game history, levels, and earned coins to your Google Account.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Preset Accounts to Select
                            Text(
                                text = if (isTr) "HIZLI HESAP SEÇİN:" else "SELECT QUICK ACCOUNT:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Account Option 1 (kronsportapp@gmail.com)
                            Card(
                                onClick = {
                                    SoundManager.playBeep(userSettings.soundEnabled)
                                    onLoginGoogle("kronsportapp@gmail.com", "Kron Sport")
                                },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color(0xFF4285F4),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("K", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Kron Sport",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = "kronsportapp@gmail.com",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Account Option 2 (oyuncu.mini@gmail.com)
                            Card(
                                onClick = {
                                    SoundManager.playBeep(userSettings.soundEnabled)
                                    onLoginGoogle("oyuncu.mini@gmail.com", "Arcade Oyuncu")
                                },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color(0xFF34A853),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("A", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Arcade Oyuncu",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = "oyuncu.mini@gmail.com",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Custom Input
                            OutlinedTextField(
                                value = inputEmail,
                                onValueChange = { inputEmail = it },
                                label = { Text(if (isTr) "Veya E-posta Yazın" else "Or Type Email", fontSize = 11.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    val email = inputEmail.ifEmpty { "kronsportapp@gmail.com" }
                                    val name = if (email.contains("@")) email.substringBefore("@") else "Oyuncu"
                                    SoundManager.playSuccess(userSettings.soundEnabled)
                                    onLoginGoogle(email, name)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("google_login_action_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Login,
                                        contentDescription = "Login",
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isTr) "GOOGLE İLE GİRİŞ YAP" else "SIGN IN WITH GOOGLE",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { isGoogleAuthOpen = false },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isTr) "KAPAT" else "CLOSE",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Game History Modal
        AnimatedVisibility(visible = isHistoryOpen) {
            val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .fillMaxHeight(0.85f)
                        .padding(16.dp)
                        .border(2.dp, NeonGold, RoundedCornerShape(20.dp))
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
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "History",
                                    tint = NeonGold,
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isTr) "OYUN GEÇMİŞİ 📜" else "GAME HISTORY 📜",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NeonGold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Account status pill in history modal
                        Surface(
                            color = if (userSettings.isLoggedIn) NeonGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (userSettings.isLoggedIn) "☁️ Hesabınız:" else "👤 Misafir Modu:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (userSettings.isLoggedIn) NeonGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (userSettings.isLoggedIn) userSettings.googleEmail else (if (isTr) "Cihaz Kaydı" else "Local Device"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (gameHistory.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "🎮", fontSize = 42.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (isTr) "Henüz oyun geçmişi yok" else "No game history yet",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isTr)
                                            "Bir oyun oynayıp bitirdiğinizde skorlarınız buraya kaydedilir!"
                                        else
                                            "Play games to see your scores recorded here!",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(gameHistory) { record ->
                                    val gameIcon = when (record.gameId) {
                                        "block_blast" -> "🧱"
                                        "slice_it" -> "🗡️"
                                        "pizza_ready" -> "🍕"
                                        "fun_race" -> "🏃"
                                        else -> "🎮"
                                    }

                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(text = gameIcon, fontSize = 22.sp)
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = record.gameName,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onBackground
                                                    )
                                                    Text(
                                                        text = dateFormat.format(Date(record.timestamp)),
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "${record.score} pts",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = NeonCyan
                                                )
                                                Text(
                                                    text = "+${record.coinsEarned} 🪙",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = NeonGold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { isHistoryOpen = false },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isTr) "KAPAT" else "CLOSE",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyMissionItem(
    mission: DailyMissionEntity,
    isTr: Boolean,
    soundEnabled: Boolean,
    onClaim: () -> Unit
) {
    val title = if (isTr) mission.titleTr else mission.titleEn
    val progressFraction = if (mission.targetValue > 0) {
        (mission.currentValue.toFloat() / mission.targetValue.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_mission_${mission.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${mission.currentValue} / ${mission.targetValue}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Action / Reward Button
                if (mission.isClaimed) {
                    Surface(
                        color = NeonGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Claimed",
                                tint = NeonGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isTr) "ALINDI" else "CLAIMED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonGreen
                            )
                        }
                    }
                } else if (mission.isCompleted) {
                    Button(
                        onClick = onClaim,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGold),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("claim_mission_${mission.id}")
                    ) {
                        Text(
                            text = "${if (isTr) "AL" else "CLAIM"} +${mission.rewardCoins} 🪙",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                    }
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "+${mission.rewardCoins} 🪙",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (mission.isCompleted) NeonGreen else NeonCyan,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

