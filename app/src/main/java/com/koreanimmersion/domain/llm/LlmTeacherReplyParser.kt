package com.koreanimmersion.domain.llm

object LlmTeacherReplyParser {

    const val MARKER_RU = "[RU]"
    const val MARKER_EN = "[EN]"

    private val MARKER_RU_REGEX = Regex("""\[\s*ru\s*]""", RegexOption.IGNORE_CASE)
    private val MARKER_EN_REGEX = Regex("""\[\s*en\s*]""", RegexOption.IGNORE_CASE)

    /** Удаляет кириллицу и латиницу — только хангыль, цифры, пунктуация и пробелы для KO TTS. */
    private val CYRILLIC_REGEX = Regex("[а-яА-ЯёЁ]")
    private val LATIN_REGEX = Regex("[a-zA-Z]")

    fun markerForNativeLanguage(nativeLanguage: String): String =
        if (nativeLanguage.equals("en", ignoreCase = true)) MARKER_EN else MARKER_RU

    private fun markerRegexForNativeLanguage(nativeLanguage: String): Regex =
        if (nativeLanguage.equals("en", ignoreCase = true)) MARKER_EN_REGEX else MARKER_RU_REGEX

    data class ParsedReply(
        val koreanPart: String,
        val explanationPart: String,
        val displayText: String
    )

    fun parse(raw: String, nativeLanguage: String): ParsedReply {
        val markerRegex = markerRegexForNativeLanguage(nativeLanguage)
        val match = markerRegex.find(raw)
        if (match == null) {
            val trimmed = raw.trim()
            return ParsedReply(
                koreanPart = trimmed,
                explanationPart = "",
                displayText = trimmed
            )
        }
        val splitAt = match.range.first
        val afterMarker = match.range.last + 1
        val korean = raw.substring(0, splitAt).trim()
        val explanation = raw.substring(afterMarker).trim()
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

    /**
     * Строка для корейского TTS: без кириллицы и латиницы (модель не сможет «прочитать» русский/EN).
     */
    fun sanitizeForKoreanTts(text: String): String {
        return text
            .replace(CYRILLIC_REGEX, "")
            .replace(LATIN_REGEX, "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
