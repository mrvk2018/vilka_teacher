package com.koreanimmersion.domain.playback

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource

/** Простой плеер для коротких TTS-фраз (экзамен). */
class PhraseAudioPlayer(context: Context) {

    private val appContext = context.applicationContext
    private val player = ExoPlayer.Builder(appContext).build()

    fun play(url: String) {
        if (url.isBlank()) return
        val dataSourceFactory = DefaultDataSource.Factory(appContext)
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(Uri.parse(url)))
        player.setMediaSource(mediaSource)
        player.prepare()
        player.play()
    }

    fun stop() {
        player.stop()
        player.clearMediaItems()
    }

    fun release() {
        player.release()
    }
}
