package com.example.data.model

data class TranslatedText(
    val detectedLanguage: String = "",
    val originalText: String = "",
    val translation: String = ""
)

data class AiAnalysisResult(
    val title: String = "",
    val category: String = "other", // insect | plant | food | object | text | document | product | animal | other
    val summary: String = "",
    val confidence: String = "high", // high | medium | low
    val observations: List<String> = emptyList(),
    val alternatives: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val translatedText: TranslatedText? = null,
    val suggestedQuestions: List<String> = emptyList()
) {
    fun getConfidenceFormatted(lang: LanguageCode): String {
        val confidencePrefix = when (confidence.lowercase()) {
            "high" -> Strings.get("confidence_high", lang)
            "medium" -> Strings.get("confidence_medium", lang)
            else -> Strings.get("confidence_low", lang)
        }
        return "$confidencePrefix $title"
    }
}
