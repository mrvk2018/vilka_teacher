package com.koreanimmersion.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume

/**
 * Offline Android TTS с поддержкой нескольких локалей (ko-KR, ru-RU).
 */
class LocaleTtsEngine(context: Context) {

    private val appContext = context.applicationContext
    private val synthesizeMutex = Mutex()

    private var tts: TextToSpeech? = null
    private var isEngineReady = false
    private var activeLocale: Locale? = null
    private val availableLocales = mutableSetOf<Locale>()

    suspend fun initialize(): Boolean = withContext(Dispatchers.Main) {
        if (isEngineReady) return@withContext true

        suspendCancellableCoroutine { cont ->
            tts = TextToSpeech(appContext) { status ->
                if (status != TextToSpeech.SUCCESS) {
                    Log.e(TAG, "TTS engine init failed: status=$status")
                    cont.resume(false)
                    return@TextToSpeech
                }
                isEngineReady = true
                cont.resume(true)
            }
        }
    }

    suspend fun isLocaleAvailable(locale: Locale): Boolean {
        if (!initialize()) return false
        val engine = tts ?: return false
        if (locale in availableLocales) return true
        val result = withContext(Dispatchers.Main) { engine.isLanguageAvailable(locale) }
        val ok = result != TextToSpeech.LANG_MISSING_DATA &&
            result != TextToSpeech.LANG_NOT_SUPPORTED
        if (ok) availableLocales.add(locale)
        return ok
    }

    suspend fun synthesizeToFile(text: String, locale: Locale, outputFile: File): Boolean {
        if (!initialize()) return false
        if (!isLocaleAvailable(locale)) {
            Log.w(TAG, "TTS locale not available: $locale")
            return false
        }

        outputFile.parentFile?.mkdirs()

        return synthesizeMutex.withLock {
            withContext(Dispatchers.Main) {
                val engine = tts ?: return@withContext false

                if (activeLocale != locale) {
                    engine.setLanguage(locale)
                    activeLocale = locale
                }

                val utteranceId = UUID.randomUUID().toString()

                suspendCancellableCoroutine { cont ->
                    engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {}

                        override fun onDone(id: String?) {
                            if (id == utteranceId && cont.isActive) {
                                cont.resume(outputFile.exists() && outputFile.length() > 0L)
                            }
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(id: String?) {
                            if (id == utteranceId && cont.isActive) cont.resume(false)
                        }

                        override fun onError(id: String?, errorCode: Int) {
                            if (id == utteranceId && cont.isActive) cont.resume(false)
                        }
                    })

                    val params = Bundle().apply {
                        putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
                    }
                    val result = engine.synthesizeToFile(text, params, outputFile, utteranceId)
                    if (result != TextToSpeech.SUCCESS && cont.isActive) {
                        cont.resume(false)
                    }
                }
            }
        }
    }

    /** Озвучивание текста вслух (например, ответ учителя в чате). */
    suspend fun speakAloud(text: String, locale: Locale = LOCALE_KO): Boolean {
        if (text.isBlank()) return false
        if (!initialize()) return false
        if (!isLocaleAvailable(locale)) return false
        return withContext(Dispatchers.Main) {
            val engine = tts ?: return@withContext false
            if (activeLocale != locale) {
                engine.setLanguage(locale)
                activeLocale = locale
            }
            val utteranceId = UUID.randomUUID().toString()
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId) == TextToSpeech.SUCCESS
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isEngineReady = false
        activeLocale = null
        availableLocales.clear()
    }

    companion object {
        private const val TAG = "LocaleTtsEngine"
        val LOCALE_KO = Locale.forLanguageTag("ko-KR")
        val LOCALE_RU = Locale.forLanguageTag("ru-RU")
    }
}
