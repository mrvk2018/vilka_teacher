package com.koreanimmersion.tts

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

/** Воспроизведение коротких TTS-файлов (надёжнее, чем TextToSpeech.speak() на Samsung). */
class TtsAudioPlayer(context: Context) {

    private val appContext = context.applicationContext
    private val player: ExoPlayer = ExoPlayer.Builder(appContext)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                .build(),
            true
        )
        .setHandleAudioBecomingNoisy(true)
        .build()

    suspend fun playFile(file: File): Boolean = withContext(Dispatchers.Main) {
        if (!file.exists() || file.length() == 0L) {
            Log.w(TAG, "TTS file missing or empty: ${file.absolutePath}")
            return@withContext false
        }
        suspendCancellableCoroutine { cont ->
            val listener = object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED && cont.isActive) {
                        player.removeListener(this)
                        cont.resume(true)
                    }
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    Log.e(TAG, "TTS playback error: ${error.message}")
                    if (cont.isActive) {
                        player.removeListener(this)
                        cont.resume(false)
                    }
                }
            }
            player.addListener(listener)
            cont.invokeOnCancellation { player.removeListener(listener) }
            player.stop()
            player.clearMediaItems()
            player.setMediaItem(MediaItem.fromUri(Uri.fromFile(file)))
            player.volume = 1f
            player.prepare()
            player.play()
        }
    }

    fun stop() {
        player.stop()
        player.clearMediaItems()
    }

    fun release() {
        player.release()
    }

    companion object {
        private const val TAG = "TtsAudioPlayer"
    }
}
