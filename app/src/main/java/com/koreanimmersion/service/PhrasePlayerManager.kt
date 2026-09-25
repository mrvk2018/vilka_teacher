package com.koreanimmersion.service

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Очередь фраз подэтапа «Ознакомление» и управление ExoPlayer (одна фраза за раз).
 */
class PhrasePlayerManager(
    private val appContext: Context,
    private val player: ExoPlayer,
    private val scope: CoroutineScope,
    private val resolveAudioUrl: suspend (PhrasePlaybackItemParcelable) -> String?,
    private val onIndexChanged: (Int, PhrasePlaybackItemParcelable) -> Unit,
    private val onQueueFinished: () -> Unit
) {

    private var queue: List<PhrasePlaybackItemParcelable> = emptyList()
    private var index = 0
    private var suppressEnded = false

    val currentItem: PhrasePlaybackItemParcelable?
        get() = queue.getOrNull(index)

    val queueSize: Int get() = queue.size

    val currentIndex: Int get() = index

    fun attachPlayerListener() {
        player.addListener(
            object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED && !suppressEnded) {
                        skipNext(autoFromEnd = true)
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    Log.e(TAG, "Playback error: ${error.message}", error)
                    skipNext(autoFromEnd = true)
                }
            }
        )
    }

    fun setQueue(items: List<PhrasePlaybackItemParcelable>) {
        queue = items
        index = 0
    }

    fun playFromStart() {
        if (queue.isEmpty()) return
        index = 0
        playCurrent()
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            if (player.playbackState == Player.STATE_ENDED || player.mediaItemCount == 0) {
                playCurrent()
            } else {
                player.play()
            }
        }
    }

    fun skipPrevious() {
        if (queue.isEmpty()) return
        index = (index - 1).coerceAtLeast(0)
        playCurrent()
    }

    fun skipNext(autoFromEnd: Boolean = false) {
        if (queue.isEmpty()) return
        if (index >= queue.lastIndex) {
            if (autoFromEnd) {
                onQueueFinished()
            }
            return
        }
        index++
        playCurrent()
    }

    fun stop() {
        suppressEnded = true
        try {
            player.stop()
            player.clearMediaItems()
        } finally {
            suppressEnded = false
        }
        queue = emptyList()
        index = 0
    }

    private fun playCurrent() {
        val item = queue.getOrNull(index) ?: return
        onIndexChanged(index, item)
        scope.launch {
            val url = resolveAudioUrl(item)
            if (url.isNullOrBlank()) {
                Log.w(TAG, "No audio for phrase ${item.phraseId}, skipping")
                skipNext(autoFromEnd = true)
                return@launch
            }
            playUrl(url, item)
        }
    }

    private fun playUrl(url: String, item: PhrasePlaybackItemParcelable) {
        val dataSourceFactory = DefaultDataSource.Factory(appContext)
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(
                MediaItem.Builder()
                    .setUri(url)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(item.koreanText)
                            .setArtist(item.russianTranslation)
                            .build()
                    )
                    .build()
            )
        suppressEnded = true
        try {
            player.stop()
            player.clearMediaItems()
        } finally {
            suppressEnded = false
        }
        player.setMediaSource(mediaSource)
        player.prepare()
        player.play()
    }

    companion object {
        private const val TAG = "PhrasePlayerManager"
    }
}
