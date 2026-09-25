package com.koreanimmersion.data.remote

import android.util.Log
import com.koreanimmersion.data.config.LlmBackendConfig
import com.koreanimmersion.data.remote.dto.LlmChatRequestDto
import com.koreanimmersion.data.remote.dto.LlmChatResponseDto
import com.koreanimmersion.domain.llm.TeacherPromptGenerator
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class KtorLlmChatApiService(
    private val apiKey: String,
    private val httpClient: HttpClient = createDefaultClient()
) : LlmChatApiService {

    override suspend fun sendMessage(request: LlmChatRequestDto): LlmChatResponseDto {
        require(apiKey.isNotBlank()) {
            "OPENROUTER_API_KEY is empty. Add openrouter.api.key to local.properties and rebuild."
        }

        val systemPrompt = TeacherPromptGenerator.generate(
            nativeLanguage = request.nativeLanguage,
            currentTopicKey = request.currentTopicKey,
            learnedPhrases = request.learnedPhrases
        )
        val apiMessages = buildList {
            add(OpenRouterMessage(role = "system", content = systemPrompt))
            request.history.forEach { item ->
                add(OpenRouterMessage(role = item.role, content = item.content))
            }
            add(OpenRouterMessage(role = "user", content = request.userMessage))
        }
        val payload = OpenRouterChatRequest(
            model = LlmBackendConfig.DEFAULT_MODEL,
            messages = apiMessages
        )

        val url = LlmBackendConfig.chatCompletionsUrl()
        Log.d(TAG, "POST $url model=${LlmBackendConfig.DEFAULT_MODEL}")

        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $apiKey")
            header("HTTP-Referer", "https://localhost")
            header("X-Title", "Korean Learning App")
            setBody(payload)
        }

        if (!response.status.isSuccess()) {
            val raw = response.bodyAsText()
            throw LlmApiException(response.status.value, raw.take(400))
        }

        val body: OpenRouterChatResponse = response.body()
        val text = body.choices.firstOrNull()?.message?.content?.trim().orEmpty()
        if (text.isEmpty()) {
            throw LlmApiException(502, "OpenRouter returned empty response")
        }
        return LlmChatResponseDto(
            assistantMessage = text,
            model = body.model ?: LlmBackendConfig.DEFAULT_MODEL
        )
    }

    @Serializable
    private data class OpenRouterChatRequest(
        val model: String,
        val messages: List<OpenRouterMessage>
    )

    @Serializable
    private data class OpenRouterMessage(
        val role: String,
        val content: String
    )

    @Serializable
    private data class OpenRouterChatResponse(
        val model: String? = null,
        val choices: List<OpenRouterChoice> = emptyList()
    )

    @Serializable
    private data class OpenRouterChoice(
        val message: OpenRouterMessage? = null
    )

    companion object {
        private const val TAG = "KtorLlmChat"

        fun createDefaultClient(): HttpClient = HttpClient(Android) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        encodeDefaults = true
                    }
                )
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 130_000
                connectTimeoutMillis = 20_000
                socketTimeoutMillis = 130_000
            }
            defaultRequest {
                contentType(ContentType.Application.Json)
            }
        }
    }
}

class LlmApiException(val httpCode: Int, message: String) : Exception(message)
