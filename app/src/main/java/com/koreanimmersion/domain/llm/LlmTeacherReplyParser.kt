package com.koreanimmersion.domain.llm

object LlmTeacherReplyParser {

    const val MARKER_RU = "[RU]"
    const val MARKER_EN = "[EN]"

    fun markerForNativeLanguage(nativeLanguage: String): String =
        if (nativeLanguage.equals("en", ignoreCase = true)) MARKER_EN else MARKER_RU

    data class ParsedReply(
        val koreanPart: String,
        val explanationPart: String,
        val displayText: String
    )

    fun parse(raw: String, nativeLanguage: String): ParsedReply {
        val marker = markerForNativeLanguage(nativeLanguage)
        val idx = raw.indexOf(marker)
        if (idx < 0) {
            val trimmed = raw.trim()
            return ParsedReply(
                koreanPart = trimmed,
                explanationPart = "",
                displayText = trimmed
            )
        }
        val korean = raw.substring(0, idx).trim()
        val explanation = raw.substring(idx + marker.length).trim()
        val display = if (explanation.isEmpty()) {
            korean
        } else {
            "$korean\n\n$explanation"
        }
        return ParsedReply(
            koreanPart = korean,
            explanationPart = explanation,
            displayText = display
        )
    }
}
