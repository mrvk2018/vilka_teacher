package com.koreanimmersion.tts

import android.content.Context
import android.net.Uri
import android.util.Log
import com.koreanimmersion.core.database.dao.ContentPhraseDao
import com.koreanimmersion.data.local.dao.PhraseDao
import com.koreanimmersion.data.local.entity.PhraseEntity
import com.koreanimmersion.domain.playback.PlaybackSegment
import com.koreanimmersion.domain.playback.PlaybackSegmentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.util.Locale

/**
 * Кэширует TTS-озвучку: корейский (фразы) и русский (контекст, интро).
 */
class PhraseTtsCacheManager(
    context: Context,
    private val phraseDao: PhraseDao,
    private val coursePhraseDao: ContentPhraseDao,
    private val ttsEngine: LocaleTtsEngine
) {

    private val cacheRoot = File(context.filesDir, "tts_cache")

    private val _preloadState = MutableStateFlow(PreloadState())
    val preloadState: StateFlow<PreloadState> = _preloadState.asStateFlow()

    data class PreloadState(
        val isRunning: Boolean = false,
        val completed: Int = 0,
        val total: Int = 0,
        val failed: Int = 0,
        val isFinished: Boolean = false
    )

    suspend fun ensureCachedKo(phraseId: Long, koreanText: String): Uri? =
        ensureCached(koreanText, LocaleTtsEngine.LOCALE_KO, "p${phraseId}", koreanText)

    suspend fun ensureCachedRu(cacheKey: String, russianText: String): Uri? =
        ensureCached(russianText, LocaleTtsEngine.LOCALE_RU, cacheKey, russianText)

    private suspend fun ensureCached(
        text: String,
        locale: Locale,
        fileKey: String,
        hashSource: String
    ): Uri? = withContext(Dispatchers.IO) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return@withContext null

        val localeTag = locale.toLanguageTag().replace("-", "_")
        val dir = File(cacheRoot, localeTag)
        val file = File(dir, "${fileKey}_${textHash(hashSource)}.wav")

        if (file.exists() && file.length() > 0L) {
            return@withContext Uri.fromFile(file)
        }

        val success = ttsEngine.synthesizeToFile(trimmed, locale, file)
        if (success) Uri.fromFile(file) else null
    }

    suspend fun ensureCached(phrase: PhraseEntity): Uri? =
        ensureCachedKo(phrase.id, phrase.koreanText)

    suspend fun preloadAllPhrases() = withContext(Dispatchers.IO) {
        if (_preloadState.value.isRunning) return@withContext

        if (!ttsEngine.initialize()) {
            Log.w(TAG, "TTS engine not available")
            _preloadState.value = PreloadState(isFinished = true)
            return@withContext
        }

        val legacyPhrases = phraseDao.getAll()
        val coursePhrases = coursePhraseDao.getAll()
        val phrases = legacyPhrases
        val total = phrases.size * 2 + coursePhrases.size
        _preloadState.value = PreloadState(isRunning = true, total = total)

        var completed = 0
        var failed = 0

        phrases.forEach { phrase ->
            if (ensureCachedKo(phrase.id, phrase.koreanText) != null) completed++ else failed++
            _preloadState.value = PreloadState(isRunning = true, completed = completed + failed, total = total, failed = failed)

            if (ensureCachedRu("ctx${phrase.id}", phrase.russianContext) != null) completed++ else failed++
            _preloadState.value = PreloadState(isRunning = true, completed = completed + failed, total = total, failed = failed)
        }

        coursePhrases.forEach { phrase ->
            if (ensureCachedKo(phrase.id, phrase.koreanText) != null) completed++ else failed++
            _preloadState.value = PreloadState(isRunning = true, completed = completed + failed, total = total, failed = failed)
        }

        _preloadState.value = PreloadState(
            isRunning = false,
            completed = completed,
            total = total,
            failed = failed,
            isFinished = true
        )
        Log.i(TAG, "TTS preload: $completed ok, $failed failed / $total")
    }

    suspend fun resolvePlaybackSegments(
        segments: List<PlaybackSegment>,
        phrasesById: Map<Long, PhraseEntity>
    ): List<PlaybackSegment> {
        return segments.map { segment ->
            when {
                segment.type == PlaybackSegmentType.INTRO -> {
                    val text = segment.displayText ?: return@map segment.copy(audioUrl = null)
                    val uri = ensureCachedRu("intro_${textHash(text)}", text)
                    segment.copy(audioUrl = uri?.toString())
                }
                segment.type == PlaybackSegmentType.PHRASE_CONTEXT ||
                    segment.type == PlaybackSegmentType.SRS_REVIEW_CONTEXT -> {
                    val phraseId = segment.phraseId
                    val text = if (phraseId != null) {
                        phrasesById[phraseId]?.russianContext ?: segment.displayText
                    } else {
                        segment.displayText
                    } ?: return@map segment
                    val key = if (phraseId != null) "ctx$phraseId" else "ctx_${textHash(text)}"
                    val uri = ensureCachedRu(key, text)
                    segment.copy(audioUrl = uri?.toString())
                }
                segment.type == PlaybackSegmentType.PHRASE_AUDIO ||
                    segment.type == PlaybackSegmentType.SRS_REVIEW_AUDIO -> {
                    val phraseId = segment.phraseId ?: return@map segment
                    val koreanText = phrasesById[phraseId]?.koreanText
                        ?: segment.displayText
                        ?: return@map segment
                    val uri = ensureCachedKo(phraseId, koreanText)
                    segment.copy(audioUrl = uri?.toString())
                }
                else -> segment
            }
        }
    }

    suspend fun collectPhrasesForSegments(segments: List<PlaybackSegment>): Map<Long, PhraseEntity> {
        val ids = segments.mapNotNull { it.phraseId }.distinct()
        if (ids.isEmpty()) return emptyMap()
        return phraseDao.getByIds(ids).associateBy { it.id }
    }

    private fun textHash(text: String): String {
        val digest = MessageDigest.getInstance("MD5").digest(text.toByteArray(Charsets.UTF_8))
        return digest.take(6).joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val TAG = "PhraseTtsCacheManager"
    }
}
