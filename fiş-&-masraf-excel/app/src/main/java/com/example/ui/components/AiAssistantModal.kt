package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ReceiptEntity
import com.example.ui.theme.ExcelGreen

data class ChatMessage(
    val sender: String, // "AI" or "USER"
    val message: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantModal(
    receipts: List<ReceiptEntity>,
    onDismiss: () -> Unit,
    onAskAiQuestion: (question: String, callback: (String) -> Unit) -> Unit
) {
    var userPrompt by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }

    val chatMessages = remember {
        mutableStateListOf(
            ChatMessage(
                sender = "AI",
                message = "Merhaba! Ben sizin Akıllı Finans ve Masraf Yapay Zeka Asistanınızım. Toplam ${receipts.size} adet fişiniz analiz edilmeye hazır.\n\nAşağıdaki hızlı soruları sorabilir veya kendi sorunuzu yazabilirsiniz:"
            )
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ExcelGreen)
            ) {
                Text("Tamam", fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = ExcelGreen,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Gemini Yapay Zeka Danışmanı",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Masraf optimizasyonu, vergi ve bütçe tavsiyeleri",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
            ) {
                // Quick Suggestion Chips
                Text(
                    text = "HIZLI TAVSİYE İSTEKLERİ",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AssistChip(
                        onClick = {
                            val q = "Bu ay en çok nerede gereksiz harcama yaptım ve nasıl tasarruf edebilirim?"
                            userPrompt = q
                        },
                        label = { Text("Tasarruf İpuçları", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Savings, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )

                    AssistChip(
                        onClick = {
                            val q = "Giderlerimdeki KDV tutarları ve vergi indirimi potansiyeli nedir?"
                            userPrompt = q
                        },
                        label = { Text("Vergi & KDV Analizi", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Chat Stream
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(chatMessages) { msg ->
                            val isAi = msg.sender == "AI"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
                            ) {
                                Surface(
                                    color = if (isAi) MaterialTheme.colorScheme.surface else ExcelGreen,
                                    shape = RoundedCornerShape(
                                        topStart = 14.dp,
                                        topEnd = 14.dp,
                                        bottomStart = if (isAi) 2.dp else 14.dp,
                                        bottomEnd = if (isAi) 14.dp else 2.dp
                                    ),
                                    shadowElevation = if (isAi) 1.dp else 0.dp
                                ) {
                                    Text(
                                        text = msg.message,
                                        modifier = Modifier.padding(12.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isAi) MaterialTheme.colorScheme.onSurface else Color.White,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        if (isThinking) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = ExcelGreen,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Yapay Zeka düşünüyor...",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Question Input Field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = userPrompt,
                        onValueChange = { userPrompt = it },
                        placeholder = { Text("Sorunuzu yazın...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isThinking
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (userPrompt.isNotBlank() && !isThinking) {
                                val q = userPrompt.trim()
                                chatMessages.add(ChatMessage("USER", q))
                                userPrompt = ""
                                isThinking = true

                                onAskAiQuestion(q) { response ->
                                    isThinking = false
                                    chatMessages.add(ChatMessage("AI", response))
                                }
                            }
                        },
                        modifier = Modifier.background(ExcelGreen, RoundedCornerShape(12.dp)),
                        enabled = userPrompt.isNotBlank() && !isThinking
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Gönder", tint = Color.White)
                    }
                }
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
