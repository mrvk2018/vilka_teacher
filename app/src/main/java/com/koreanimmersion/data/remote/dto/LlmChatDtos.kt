package com.koreanimmersion.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LlmChatHistoryItemDto(
    val role: String,
    val content: String
)

@Serializable
data class LlmChatRequestDto(
    val userId: String,
    val userMessage: String,
    val currentTopicKey: String,
    val nativeLanguage: String,
    val learnedPhrases: List<String> = emptyList(),
    val history: List<LlmChatHistoryItemDto> = emptyList()
)

@Serializable
data class LlmChatResponseDto(
    val assistantMessage: String,
    val model: String? = null
)

@Serializable
data class LlmErrorResponseDto(
    val detail: String? = null
)
