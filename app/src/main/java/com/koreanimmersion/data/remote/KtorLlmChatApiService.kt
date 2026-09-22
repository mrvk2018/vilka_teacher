package com.koreanimmersion.data.remote

import com.koreanimmersion.data.config.LlmBackendConfig
import com.koreanimmersion.data.remote.dto.LlmChatRequestDto
import com.koreanimmersion.data.remote.dto.LlmChatResponseDto
import com.koreanimmersion.data.remote.dto.LlmErrorResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class KtorLlmChatApiService(
    private val httpClient: HttpClient = createDefaultClient()
) : LlmChatApiService {

    override suspend fun sendMessage(request: LlmChatRequestDto): LlmChatResponseDto {
        val response = httpClient.post(LlmBackendConfig.chatMessageUrl()) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (!response.status.isSuccess()) {
            val raw = response.bodyAsText()
            val detail = runCatching {
                Json { ignoreUnknownKeys = true }.decodeFromString<LlmErrorResponseDto>(raw).detail
            }.getOrNull() ?: raw.take(300)
            throw LlmApiException(response.status.value, detail)
        }
        return response.body()
    }

    companion object {
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
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 130_000
            }
            defaultRequest {
                contentType(ContentType.Application.Json)
            }
        }
    }
}

class LlmApiException(val httpCode: Int, message: String) : Exception(message)
