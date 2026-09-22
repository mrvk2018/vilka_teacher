package com.koreanimmersion.data.repository

import com.koreanimmersion.data.remote.LlmApiException
import com.koreanimmersion.data.remote.LlmChatApiService
import com.koreanimmersion.data.remote.dto.LlmChatRequestDto
import com.koreanimmersion.data.remote.dto.LlmChatResponseDto
import java.io.IOException

sealed class LlmChatResult {
    data class Success(val assistantMessage: String, val model: String?) : LlmChatResult()
    data class Failure(val message: String) : LlmChatResult()
}

class LlmChatRepositoryImpl(
    private val api: LlmChatApiService
) {

    suspend fun sendUserMessage(
        userId: String,
        userMessage: String,
        currentTopicKey: String,
        nativeLanguage: String,
        learnedPhrases: List<String>
    ): LlmChatResult {
        val request = LlmChatRequestDto(
            userId = userId,
            userMessage = userMessage.trim(),
            currentTopicKey = currentTopicKey,
            nativeLanguage = nativeLanguage,
            learnedPhrases = learnedPhrases
        )
        if (request.userMessage.isEmpty()) {
            return LlmChatResult.Failure("empty_message")
        }
        return try {
            val response: LlmChatResponseDto = api.sendMessage(request)
            LlmChatResult.Success(
                assistantMessage = response.assistantMessage,
                model = response.model
            )
        } catch (e: LlmApiException) {
            LlmChatResult.Failure(e.message ?: "api_error")
        } catch (e: IOException) {
            LlmChatResult.Failure(e.message ?: "network_error")
        } catch (e: Exception) {
            LlmChatResult.Failure(e.message ?: "unknown_error")
        }
    }
}
