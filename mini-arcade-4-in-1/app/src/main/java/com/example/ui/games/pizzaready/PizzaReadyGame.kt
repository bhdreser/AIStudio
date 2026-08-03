package com.example.ui.games.pizzaready

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import kotlinx.coroutines.delay
import kotlin.random.Random

data class CustomerOrder(
    val id: Int,
    val name: String,
    val avatarEmoji: String,
    val pizzasNeeded: Int,
    var pizzasDelivered: Int = 0,
    var patience: Float = 1.0f,
    val maxPatience: Float = 100f
)

@Composable
fun PizzaReadyGame(
    highScore: Int,
    onBack: () -> Unit,
    onGameOver: (score: Int, coins: Int) -> Unit
) {
    var level by remember { mutableIntStateOf(1) }
    var totalEarnings by remember { mutableIntStateOf(0) }
    var coinsEarned by remember { mutableIntStateOf(0) }
    var doughCount by remember { mutableIntStateOf(8) }
    var pizzasInOven by remember { mutableIntStateOf(0) }
    var bakedPizzas by remember { mutableIntStateOf(3) }

    // Upgrades
    var ovenLevel by remember { mutableIntStateOf(1) } // 1: Standard, 2: Double Electric, 3: Stone Brick, 4: Conveyor
    var tableCapacity by remember { mutableIntStateOf(3) } // Queue size
    var hasAutoChef by remember { mutableStateOf(false) }
    var pizzaPriceMultiplier by remember { mutableIntStateOf(1) }

    var isShiftActive by remember { mutableStateOf(true) }
    var isShiftFinished by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("HAMUR YURUR, FIRINA AT, MÜŞTERİLERE SERVİS ET!") }

    val customers = remember { mutableStateListOf<CustomerOrder>() }
    var nextCustomerId by remember { mutableIntStateOf(1) }
    var ovenProgress by remember { mutableFloatStateOf(0f) }

    fun getLevelTarget(): Int = 80 + (level * 70)
    fun getOvenCapacity(): Int = when (ovenLevel) {
        1 -> 2
        2 -> 4
        3 -> 6
        else -> 10
    }

    fun getOvenSpeed(): Float = when (ovenLevel) {
        1 -> 0.25f
        2 -> 0.45f
        3 -> 0.70f
        else -> 1.10f
    }

    fun getOvenName(): String = when (ovenLevel) {
        1 -> "Küçük Standart Fırın 🍕"
        2 -> "Çift Katlı Elektrikli Fırın ⚡"
        3 -> "İtalyan Taş Tuğla Fırın 🧱"
        else -> "Endüstriyel Konveyör Fırın 🏭"
    }

    fun spawnCustomer() {
        if (customers.size < tableCapacity) {
            val names = listOf("Ahmet", "Elif", "Can", "Zeynep", "Mert", "Selin", "Ege", "Gamze")
            val avatars = listOf("👨‍🦱", "👩‍🦰", "🧔", "👩‍💼", "👨‍🍳", "👧", "👦")
            val maxNeeded = minOf(1 + level, 4)
            customers.add(
                CustomerOrder(
                    id = nextCustomerId++,
                    name = names.random(),
                    avatarEmoji = avatars.random(),
                    pizzasNeeded = Random.nextInt(1, maxNeeded + 1),
                    maxPatience = 100f
                )
            )
        }
    }

    fun resetGame() {
        level = 1
        totalEarnings = 0
        coinsEarned = 0
        doughCount = 8
        pizzasInOven = 0
        bakedPizzas = 3
        ovenLevel = 1
        tableCapacity = 3
        hasAutoChef = false
        pizzaPriceMultiplier = 1
        isShiftActive = true
        isShiftFinished = false
        statusText = "HAMUR YOĞUR, FIRINA AT, MÜŞTERİLERE SERVİS ET!"
        customers.clear()
        spawnCustomer()
        spawnCustomer()
    }

    LaunchedEffect(Unit) {
        spawnCustomer()
        spawnCustomer()
    }

    // Main Game Engine Loop
    LaunchedEffect(isShiftActive, isShiftFinished) {
        while (isShiftActive && !isShiftFinished) {
            delay(500)

            // Spawn customer periodically
            if (Random.nextFloat() < 0.35f + (level * 0.05f)) {
                spawnCustomer()
            }

            // Bake pizzas in oven
            if (pizzasInOven > 0) {
                ovenProgress += getOvenSpeed()
                if (ovenProgress >= 1.0f) {
                    bakedPizzas += pizzasInOven
                    pizzasInOven = 0
                    ovenProgress = 0f
                }
            }

            // Customer patience drop
            val iterator = customers.iterator()
            while (iterator.hasNext()) {
                val customer = iterator.next()
                customer.patience -= (1.5f + (level * 0.5f))
                if (customer.patience <= 0f) {
                    iterator.remove()
                    statusText = "${customer.name} SABRI TÜKENİP RESTORANDAN AYRILDI! ❌"
                }
            }

            // Auto Chef Assistant Helper
            if (hasAutoChef) {
                if (doughCount < 5) doughCount++
                if (bakedPizzas > 0 && customers.isNotEmpty()) {
                    val first = customers.first()
                    first.pizzasDelivered++
                    bakedPizzas--
                    if (first.pizzasDelivered >= first.pizzasNeeded) {
                        val income = (first.pizzasNeeded * 18 * pizzaPriceMultiplier) + 10
                        totalEarnings += income
                        coinsEarned += (income / 5)
                        customers.remove(first)
                        statusText = "OTOMATİK ŞEF ${first.name}'A SERVİS YAPTI! +$$income 💵"
                    }
                }
            }

            // Level Up Check
            if (totalEarnings >= getLevelTarget()) {
                level++
                statusText = "TEBRİKLER! SEVİYE $level RESTORANINA YÜKSELDİN! 🎉"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(12.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "PIZZA READY TYCOON 🍕",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = NeonGold
                )
                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = { resetGame() },
                modifier = Modifier.testTag("reset_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = NeonCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Level Target Revenue Bar
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("SEVİYE $level HEDEFİ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                    Text("$$totalEarnings / $$${getLevelTarget()}", fontSize = 12.sp, fontWeight = FontWeight.Black, color = NeonGold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (totalEarnings.toFloat() / getLevelTarget().toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = NeonGold,
                    trackColor = Color(0xFF334155)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Kitchen Counters & Oven Visualization
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Dough Prep Station
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { doughCount += 2 }
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🥐 HAMUR İSTASYONU", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text("$doughCount Top", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { doughCount += 2 },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("+ HAMUR YOĞUR", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }

            // Oven Station
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1.2f)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(getOvenName(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeonGold)
                    Text(if (pizzasInOven > 0) "🔥 PIZZALAR PIŞIYOR..." else "✅ FIRIN BOŞ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)

                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { ovenProgress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFFF97316),
                        trackColor = Color(0xFF334155)
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = {
                            if (pizzasInOven == 0 && doughCount >= getOvenCapacity()) {
                                doughCount -= getOvenCapacity()
                                pizzasInOven = getOvenCapacity()
                                ovenProgress = 0f
                            }
                        },
                        enabled = pizzasInOven == 0 && doughCount >= getOvenCapacity(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("FIRINA AT (${getOvenCapacity()} 🍕)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Serving Counter Station
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📦 SERVİS TEZGÂHI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text("$bakedPizzas Pizza Hazır", fontSize = 14.sp, fontWeight = FontWeight.Black, color = NeonGreen)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Upgrades Row Bar
        Text("🏪 RESTORAN YATIRIMLARI & YÜKSELTMELER", fontSize = 11.sp, fontWeight = FontWeight.Black, color = NeonCyan)
        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Upgrade Oven Button
            Button(
                onClick = {
                    val cost = ovenLevel * 60
                    if (totalEarnings >= cost) {
                        totalEarnings -= cost
                        ovenLevel++
                        statusText = "FIRIN BÜYÜTÜLDÜ! YENİ KAPASİTE: ${getOvenCapacity()} 🍕"
                    }
                },
                enabled = ovenLevel < 4 && totalEarnings >= (ovenLevel * 60),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("🔥 FIRIN BÜYÜT\n($${ovenLevel * 60})", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NeonGold)
            }

            // Hire Auto Chef Helper Button
            Button(
                onClick = {
                    if (totalEarnings >= 120 && !hasAutoChef) {
                        totalEarnings -= 120
                        hasAutoChef = true
                        statusText = "OTOMATİK ŞEF YARDIMCISI İŞE ALINDI! 🧑‍🍳"
                    }
                },
                enabled = !hasAutoChef && totalEarnings >= 120,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(if (hasAutoChef) "✅ OTO ŞEF AKTİF" else "🧑‍🍳 OTO ŞEF AL\n($120)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
            }

            // Expand Tables Capacity
            Button(
                onClick = {
                    val cost = tableCapacity * 40
                    if (totalEarnings >= cost) {
                        totalEarnings -= cost
                        tableCapacity++
                        statusText = "KUYRUK KAPASİTESİ ARTTIRILDI: $tableCapacity MÜŞTERİ"
                    }
                },
                enabled = tableCapacity < 6 && totalEarnings >= (tableCapacity * 40),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("🪑 MASALARI BÜYÜT\n($${tableCapacity * 40})", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Customers Queue Header
        Text("👥 BEKLEYEN MÜŞTERİLER KUYRUĞU (${customers.size} / $tableCapacity)", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
        Spacer(modifier = Modifier.height(4.dp))

        if (customers.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Müşteriler kapıda sıraya giriyor... 🚪", fontSize = 12.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(customers) { customer ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(customer.avatarEmoji, fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(customer.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Sipariş: 🍕 x${customer.pizzasNeeded} (Teslim: ${customer.pizzasDelivered})", fontSize = 11.sp, color = NeonGold)

                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { (customer.patience / customer.maxPatience).coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .width(120.dp)
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        color = if (customer.patience > 40f) NeonGreen else Color(0xFFEF4444),
                                        trackColor = Color(0xFF334155)
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (bakedPizzas > 0) {
                                        bakedPizzas--
                                        customer.pizzasDelivered++
                                        if (customer.pizzasDelivered >= customer.pizzasNeeded) {
                                            val income = (customer.pizzasNeeded * 18 * pizzaPriceMultiplier) + 12
                                            totalEarnings += income
                                            coinsEarned += (income / 4)
                                            customers.remove(customer)
                                            statusText = "${customer.name} MUTLU AYRILDI! +$$income 💵"
                                        }
                                    }
                                },
                                enabled = bakedPizzas > 0,
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("SERVİS ET 🍕", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}
