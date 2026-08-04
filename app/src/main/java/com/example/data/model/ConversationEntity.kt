package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val imagePath: String?,
    val firstQuestion: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val messageId: Long = 0,
    val conversationId: String,
    val sender: String, // "user" or "ai"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    // Embedded JSON strings for rich AI analysis fields
    val title: String? = null,
    val category: String? = null,
    val confidence: String? = null,
    val observationsJson: String? = null,
    val alternativesJson: String? = null,
    val warningsJson: String? = null,
    val originalText: String? = null,
    val translatedText: String? = null,
    val suggestedQuestionsJson: String? = null
)
