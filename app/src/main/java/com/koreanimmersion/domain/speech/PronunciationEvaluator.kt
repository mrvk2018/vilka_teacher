package com.koreanimmersion.domain.speech

/**
 * Локальная оценка произношения (редакторское расстояние).
 * Серверная проверка может использовать тот же контракт результата.
 */
object PronunciationEvaluator {

    private const val DEFAULT_THRESHOLD_PERCENT = 80

    sealed interface Result {
        data object Success : Result
        data class TryAgain(val similarityPercent: Int) : Result
    }

    fun evaluate(
        referenceKoreanText: String,
        recognizedText: String,
        thresholdPercent: Int = DEFAULT_THRESHOLD_PERCENT
    ): Result {
        val reference = clean(referenceKoreanText)
        val recognized = clean(recognizedText)
        if (reference.isEmpty()) return Result.TryAgain(0)
        if (recognized.isEmpty()) return Result.TryAgain(0)

        val distance = levenshteinDistance(reference, recognized)
        val maxLen = maxOf(reference.length, recognized.length)
        val similarity = ((1.0 - distance.toDouble() / maxLen) * 100.0)
            .toInt()
            .coerceIn(0, 100)

        return if (similarity >= thresholdPercent) {
            Result.Success
        } else {
            Result.TryAgain(similarity)
        }
    }

    fun clean(text: String): String =
        text.lowercase()
            .replace(Regex("[\\s.,!?…~·\"'「」『』（）()\\[\\]{}]"), "")

    private fun levenshteinDistance(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length

        val prev = IntArray(b.length + 1) { it }
        val curr = IntArray(b.length + 1)

        for (i in a.indices) {
            curr[0] = i + 1
            for (j in b.indices) {
                val cost = if (a[i] == b[j]) 0 else 1
                curr[j + 1] = minOf(
                    curr[j] + 1,
                    prev[j + 1] + 1,
                    prev[j] + cost
                )
            }
            prev.indices.forEach { prev[it] = curr[it] }
        }
        return prev[b.length]
    }
}
