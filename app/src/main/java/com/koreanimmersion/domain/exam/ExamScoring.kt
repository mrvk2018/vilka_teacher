package com.koreanimmersion.domain.exam

import org.json.JSONArray
import org.json.JSONObject

data class McQuestion(
    val phraseId: Long,
    /** Романизация — без хангыля, для подсказки после ответа (опционально). */
    val koreanRomanization: String,
    /** Локальный TTS-файл корейской фразы. */
    val audioUrl: String?,
    val options: List<String>,
    val correctIndex: Int
)

data class ExamAnswerRecord(
    val phraseId: Long,
    val selectedIndex: Int,
    val correct: Boolean
)

object ExamScoring {
    /** Оценка 1–5 по доле правильных ответов multiple choice. */
    fun scoreFromMcAnswers(correctCount: Int, totalCount: Int): Int {
        if (totalCount == 0) return 1
        val ratio = correctCount.toFloat() / totalCount
        return when {
            ratio >= 0.95f -> 5
            ratio >= 0.80f -> 4
            ratio >= 0.60f -> 3
            ratio >= 0.40f -> 2
            else -> 1
        }
    }

    fun answersToJson(answers: List<ExamAnswerRecord>): String {
        val arr = JSONArray()
        answers.forEach { a ->
            arr.put(
                JSONObject().apply {
                    put("phraseId", a.phraseId)
                    put("selectedIndex", a.selectedIndex)
                    put("correct", a.correct)
                }
            )
        }
        return arr.toString()
    }
}
