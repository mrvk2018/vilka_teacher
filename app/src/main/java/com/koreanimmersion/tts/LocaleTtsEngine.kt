package com.koreanimmersion.tts

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume

/**
 * Offline Android TTS (ko-KR, ru-RU). Озвучка вслух — через синтез в WAV + ExoPlayer.
 */
class LocaleTtsEngine(context: Context) {

    private val appContext = context.applicationContext
    private val ttsMutex = Mutex()
    private val audioPlayer = TtsAudioPlayer(appContext)

    private var tts: TextToSpeech? = null
    private var isEngineReady = false
    private var activeLocale: Locale? = null
    private val availableLocales = mutableSetOf<Locale>()

    suspend fun initialize(): Boolean = ttsMutex.withLock { initializeLocked() }

    suspend fun isLocaleAvailable(locale: Locale): Boolean = ttsMutex.withLock {
        initializeLocked() && resolveLocaleLocked(locale) != null
    }

    suspend fun synthesizeToFile(text: String, locale: Locale, outputFile: File): Boolean =
        ttsMutex.withLock {
            synthesizeToFileLocked(text, locale, outputFile)
        }

    /** Озвучивание: синтез в cache + воспроизведение через ExoPlayer. */
    suspend fun speakAloud(text: String, locale: Locale = LOCALE_KO): Boolean {
        if (text.isBlank()) return false
        val cacheFile = File(appContext.cacheDir, "tts_aloud").apply { mkdirs() }
        val out = File(cacheFile, "utterance_${textHash(text)}.wav")
        val synthesized = ttsMutex.withLock {
            val resolved = resolveLocaleLocked(locale) ?: return@withLock false
            synthesizeToFileLocked(text, resolved, out)
        }
        if (!synthesized) {
            Log.w(TAG, "speakAloud: synthesize failed for '$text'")
            return false
        }
        return audioPlayer.playFile(out)
    }

    private suspend fun synthesizeToFileLocked(
        text: String,
        locale: Locale,
        outputFile: File
    ): Boolean {
        if (!initializeLocked()) return false
        val resolved = resolveLocaleLocked(locale) ?: return false

        outputFile.parentFile?.mkdirs()

        return withContext(Dispatchers.Main) {
            val engine = tts ?: return@withContext false

            if (activeLocale != resolved) {
                engine.setLanguage(resolved)
                activeLocale = resolved
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

    private suspend fun initializeLocked(): Boolean {
        if (isEngineReady) return true
        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { cont ->
                tts = TextToSpeech(appContext, { status ->
                    if (status != TextToSpeech.SUCCESS) {
                        cont.resume(false)
                        return@TextToSpeech
                    }
                    val engine = tts
                    if (engine != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        engine.setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .build()
                        )
                    }
                    isEngineReady = true
                    cont.resume(true)
                }, GOOGLE_TTS_ENGINE)
            }
        }
    }

    private suspend fun resolveLocaleLocked(preferred: Locale): Locale? {
        if (!initializeLocked()) return null
        val engine = tts ?: return null
        val candidates = when (preferred.language) {
            "ko" -> listOf(LOCALE_KO, Locale.KOREAN, preferred)
            "ru" -> listOf(LOCALE_RU, Locale.forLanguageTag("ru-RU"), preferred)
            else -> listOf(preferred)
        }
        for (candidate in candidates) {
            if (candidate in availableLocales) return candidate
            val result = withContext(Dispatchers.Main) { engine.isLanguageAvailable(candidate) }
            val ok = result != TextToSpeech.LANG_MISSING_DATA &&
                result != TextToSpeech.LANG_NOT_SUPPORTED
            if (ok) {
                availableLocales.add(candidate)
                return candidate
            }
        }
        return null
    }

    fun shutdown() {
        audioPlayer.release()
        tts?.stop()
        tts?.shutdown()
        tts = null
        isEngineReady = false
        activeLocale = null
        availableLocales.clear()
    }

    private fun textHash(text: String): String =
        text.hashCode().toUInt().toString(16)

    companion object {
        private const val TAG = "LocaleTtsEngine"
        private const val GOOGLE_TTS_ENGINE = "com.google.android.tts"
        val LOCALE_KO = Locale.forLanguageTag("ko-KR")
        val LOCALE_RU = Locale.forLanguageTag("ru-RU")
    }
}
