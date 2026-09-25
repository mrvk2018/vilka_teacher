package com.koreanimmersion.data.config

/**
 * Прямые запросы к OpenRouter с устройства (без Python/FastAPI на ПК).
 */
object LlmBackendConfig {

    const val BASE_URL = "https://openrouter.ai"

    /** Универсальный free-роутер OpenRouter (устойчив к смене тарифов отдельных моделей). */
    const val DEFAULT_MODEL = "openrouter/free"

    fun chatCompletionsUrl(): String =
        "${BASE_URL.trimEnd('/')}/api/v1/chat/completions"
}
