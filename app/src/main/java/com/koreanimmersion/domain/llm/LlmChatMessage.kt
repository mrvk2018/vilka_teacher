package com.koreanimmersion.domain.llm

/** Сообщение в LLM-диалоге (UI + история для OpenRouter). */
sealed interface LlmChatMessage {
    data class User(val text: String) : LlmChatMessage
    data class Teacher(val text: String) : LlmChatMessage
}
