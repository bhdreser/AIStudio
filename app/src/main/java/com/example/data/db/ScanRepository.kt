package com.example.data.db

import com.example.data.model.AiAnalysisResult
import com.example.data.model.ConversationEntity
import com.example.data.model.LanguageCode
import com.example.data.model.MessageEntity
import com.example.data.model.TranslatedText
import com.example.service.GeminiAiService
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray

class ScanRepository(private val scanDao: ScanDao) {
    val allConversations: Flow<List<ConversationWithMessages>> = scanDao.getAllConversations()

    suspend fun enforceMaxLimit(maxCount: Int = 20) {
        val list = scanDao.getConversationsList()
        if (list.size > maxCount) {
            val toDelete = list.drop(maxCount)
            toDelete.forEach { item ->
                scanDao.deleteMessagesByConversationId(item.conversation.id)
                scanDao.deleteConversation(item.conversation.id)
            }
        }
        scanDao.deleteOrphanedMessages()
    }

    suspend fun createScanConversation(
        id: String,
        imagePath: String?,
        question: String,
        result: AiAnalysisResult
    ) {
        val conversation = ConversationEntity(
            id = id,
            title = result.title.ifBlank { result.summary.take(30) },
            category = result.category,
            imagePath = imagePath,
            firstQuestion = question,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        scanDao.insertConversation(conversation)

        if (question.isNotBlank()) {
            val userMessage = MessageEntity(
                conversationId = id,
                sender = "user",
                text = question,
                timestamp = System.currentTimeMillis()
            )
            scanDao.insertMessage(userMessage)
        }

        val firstMessage = MessageEntity(
            conversationId = id,
            sender = "ai",
            text = result.summary,
            timestamp = System.currentTimeMillis() + 1,
            title = result.title,
            category = result.category,
            confidence = result.confidence,
            observationsJson = JSONArray(result.observations).toString(),
            alternativesJson = JSONArray(result.alternatives).toString(),
            warningsJson = JSONArray(result.warnings).toString(),
            originalText = result.translatedText?.originalText,
            translatedText = result.translatedText?.translation,
            suggestedQuestionsJson = JSONArray(result.suggestedQuestions).toString()
        )
        scanDao.insertMessage(firstMessage)

        enforceMaxLimit(20)
    }

    private suspend fun ensureConversationExists(
        conversationId: String,
        initialTitle: String = "Sohbet",
        imagePath: String? = null,
        questionText: String = ""
    ) {
        val existing = scanDao.getConversationById(conversationId)
        if (existing == null) {
            val conversation = ConversationEntity(
                id = conversationId,
                title = initialTitle.ifBlank { "Sohbet" }.take(35),
                category = "chat",
                imagePath = imagePath,
                firstQuestion = questionText,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            scanDao.insertConversation(conversation)
        } else {
            val updated = existing.conversation.copy(
                updatedAt = System.currentTimeMillis(),
                imagePath = existing.conversation.imagePath ?: imagePath
            )
            scanDao.insertConversation(updated)
        }
    }

    suspend fun addUserMessage(conversationId: String, text: String, imagePath: String? = null) {
        ensureConversationExists(conversationId, initialTitle = text, imagePath = imagePath, questionText = text)
        val userMsg = MessageEntity(
            conversationId = conversationId,
            sender = "user",
            text = text,
            timestamp = System.currentTimeMillis()
        )
        scanDao.insertMessage(userMsg)
    }

    suspend fun addAiMessage(conversationId: String, result: AiAnalysisResult) {
        ensureConversationExists(conversationId, initialTitle = result.title)
        val aiMsg = MessageEntity(
            conversationId = conversationId,
            sender = "ai",
            text = result.summary,
            timestamp = System.currentTimeMillis(),
            title = result.title,
            category = result.category,
            confidence = result.confidence,
            observationsJson = JSONArray(result.observations).toString(),
            alternativesJson = JSONArray(result.alternatives).toString(),
            warningsJson = JSONArray(result.warnings).toString(),
            originalText = result.translatedText?.originalText,
            translatedText = result.translatedText?.translation,
            suggestedQuestionsJson = JSONArray(result.suggestedQuestions).toString()
        )
        scanDao.insertMessage(aiMsg)
        enforceMaxLimit(20)
    }

    suspend fun deleteConversation(id: String) {
        scanDao.deleteMessagesByConversationId(id)
        scanDao.deleteConversation(id)
    }

    suspend fun clearAllHistory() {
        scanDao.deleteAllMessages()
        scanDao.deleteAllConversations()
    }

    suspend fun autoDeleteOldHistory(days: Int) {
        if (days <= 0) return
        val cutoff = System.currentTimeMillis() - (days.toLong() * 24 * 60 * 60 * 1000)
        scanDao.deleteOlderThan(cutoff)
    }

    suspend fun translateAllHistoryToLanguage(
        targetLanguage: LanguageCode,
        geminiService: GeminiAiService
    ) {
        val conversationsWithMsgs = scanDao.getConversationsList()
        for (item in conversationsWithMsgs) {
            val conv = item.conversation
            val newTitle = if (conv.title.isNotBlank()) {
                geminiService.translateText(conv.title, targetLanguage)
            } else conv.title

            val updatedConv = conv.copy(title = newTitle)
            scanDao.insertConversation(updatedConv)

            for (msg in item.messages) {
                if (msg.sender == "ai") {
                    val newText = geminiService.translateText(msg.text, targetLanguage)
                    val newMsgTitle = if (!msg.title.isNullOrBlank()) {
                        geminiService.translateText(msg.title, targetLanguage)
                    } else msg.title

                    val newObsJson = translateJsonArray(msg.observationsJson, targetLanguage, geminiService)
                    val newAltJson = translateJsonArray(msg.alternativesJson, targetLanguage, geminiService)
                    val newWarnJson = translateJsonArray(msg.warningsJson, targetLanguage, geminiService)
                    val newSqJson = translateJsonArray(msg.suggestedQuestionsJson, targetLanguage, geminiService)

                    val updatedMsg = msg.copy(
                        text = newText,
                        title = newMsgTitle,
                        observationsJson = newObsJson,
                        alternativesJson = newAltJson,
                        warningsJson = newWarnJson,
                        suggestedQuestionsJson = newSqJson
                    )
                    scanDao.insertMessage(updatedMsg)
                }
            }
        }
    }

    private suspend fun translateJsonArray(
        jsonStr: String?,
        targetLanguage: LanguageCode,
        geminiService: GeminiAiService
    ): String? {
        if (jsonStr.isNullOrBlank()) return jsonStr
        return try {
            val arr = JSONArray(jsonStr)
            if (arr.length() == 0) return jsonStr
            val newArr = JSONArray()
            for (i in 0 until arr.length()) {
                val original = arr.getString(i)
                val translated = geminiService.translateText(original, targetLanguage)
                newArr.put(translated)
            }
            newArr.toString()
        } catch (e: Exception) {
            jsonStr
        }
    }
}
