package com.koreanimmersion.core.database.model

/**
 * Подэтап обучения внутри темы.
 * 1 — ознакомление (прослушивание), 2 — говорение, 3 — диалог с LLM.
 */
enum class LearningSubStage(val code: Int) {
    INTRODUCTION(1),
    SPEAKING(2),
    LLM_DIALOG(3);

    companion object {
        fun fromCode(code: Int): LearningSubStage =
            entries.firstOrNull { it.code == code } ?: INTRODUCTION
    }
}
