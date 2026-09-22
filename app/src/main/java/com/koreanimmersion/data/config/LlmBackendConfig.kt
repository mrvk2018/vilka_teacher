package com.koreanimmersion.data.config

/**
 * Базовый URL Python/FastAPI сервера.
 * Эмулятор: `10.0.2.2` — хост машины. На реальном устройстве укажите LAN IP ПК.
 */
object LlmBackendConfig {
    var baseUrl: String = DEFAULT_BASE_URL

    const val DEFAULT_BASE_URL = "http://10.0.2.2:8000"

    fun chatMessageUrl(): String = "${baseUrl.trimEnd('/')}/api/chat/v1/message"
}
