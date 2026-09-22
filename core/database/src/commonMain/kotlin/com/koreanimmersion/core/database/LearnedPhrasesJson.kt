package com.koreanimmersion.core.database

/**
 * Минимальный кодек JSON для списка ID фраз (без внешних зависимостей в commonMain).
 * Python-скрипты могут читать тот же формат: [10101, 130104].
 */
object LearnedPhrasesJson {
    fun encode(phraseIds: List<Long>): String {
        if (phraseIds.isEmpty()) return "[]"
        return phraseIds.joinToString(prefix = "[", postfix = "]") { it.toString() }
    }

    fun decode(json: String): List<Long> {
        val trimmed = json.trim()
        if (trimmed == "[]" || trimmed.isEmpty()) return emptyList()
        return trimmed
            .removePrefix("[")
            .removeSuffix("]")
            .split(',')
            .mapNotNull { it.trim().toLongOrNull() }
    }
}
