package com.koreanimmersion.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.koreanimmersion.KoreanImmersionApp
import com.koreanimmersion.MainActivity
import com.koreanimmersion.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Foreground MediaSessionService для подэтапа 1 (ознакомление): очередь фраз темы.
 */
class PhraseIntroductionPlaybackService : MediaSessionService() {

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var phraseManager: PhrasePlayerManager? = null

    private var topicId: Long = -1L
    private var displayTranslation: String = ""
    private var displayKorean: String = ""
    private var isPlayingUi = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val exoPlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
        player = exoPlayer
        mediaSession = MediaSession.Builder(this, exoPlayer).build()

        val manager = PhrasePlayerManager(
            appContext = applicationContext,
            player = exoPlayer,
            scope = serviceScope,
            resolveAudioUrl = { item ->
                val path = item.audioUrlOrPath
                if (!path.isNullOrBlank()) return@PhrasePlayerManager path
                KoreanImmersionApp.instance.phraseTtsCacheManager
                    .ensureCachedKo(item.phraseId, item.koreanText)
                    ?.toString()
            },
            onIndexChanged = { _, item ->
                displayKorean = item.koreanText
                displayTranslation = item.russianTranslation
                updateNotification(isPlayingUi)
            },
            onQueueFinished = { stopSelfAndCleanup() }
        )
        manager.attachPlayerListener()
        phraseManager = manager
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                topicId = intent.getLongExtra(EXTRA_TOPIC_ID, -1L)
                val items = intent.getParcelableArrayListExtra<PhrasePlaybackItemParcelable>(EXTRA_PHRASES)
                    ?: emptyList()
                phraseManager?.setQueue(items)
                isPlayingUi = true
                startForeground(NOTIFICATION_ID, buildNotification(isPlaying = true))
                phraseManager?.playFromStart()
            }
            ACTION_TOGGLE_PAUSE -> {
                phraseManager?.togglePlayPause()
                isPlayingUi = player?.isPlaying == true
                updateNotification(isPlayingUi)
            }
            ACTION_PLAY -> {
                player?.play()
                isPlayingUi = true
                updateNotification(true)
            }
            ACTION_SKIP_PREVIOUS -> {
                phraseManager?.skipPrevious()
                isPlayingUi = true
                updateNotification(true)
            }
            ACTION_SKIP_NEXT -> {
                phraseManager?.skipNext()
                isPlayingUi = true
                updateNotification(true)
            }
            ACTION_STOP -> stopSelfAndCleanup()
        }
        return START_STICKY
    }

    private fun stopSelfAndCleanup() {
        phraseManager?.stop()
        isPlayingUi = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(isPlaying: Boolean): Notification {
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val prevIntent = servicePendingIntent(ACTION_SKIP_PREVIOUS, 1)
        val toggleIntent = servicePendingIntent(
            if (isPlaying) ACTION_TOGGLE_PAUSE else ACTION_TOGGLE_PAUSE,
            2
        )
        val nextIntent = servicePendingIntent(ACTION_SKIP_NEXT, 3)
        val stopIntent = servicePendingIntent(ACTION_STOP, 4)

        val title = displayKorean.ifBlank { getString(R.string.intro_notification_title_default) }
        val text = displayTranslation.ifBlank { getString(R.string.intro_notification_text_default) }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSubText(getString(R.string.intro_notification_subtext))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(contentIntent)
            .setOngoing(isPlaying)
            .addAction(
                R.drawable.ic_launcher_foreground,
                getString(R.string.action_previous),
                prevIntent
            )
            .addAction(
                R.drawable.ic_launcher_foreground,
                getString(if (isPlaying) R.string.action_pause else R.string.action_play),
                toggleIntent
            )
            .addAction(
                R.drawable.ic_launcher_foreground,
                getString(R.string.action_next),
                nextIntent
            )
            .addAction(
                R.drawable.ic_launcher_foreground,
                getString(R.string.action_stop),
                stopIntent
            )
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession?.sessionCompatToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .build()
    }

    private fun servicePendingIntent(action: String, requestCode: Int): PendingIntent =
        PendingIntent.getService(
            this,
            requestCode,
            Intent(this, PhraseIntroductionPlaybackService::class.java).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun updateNotification(isPlaying: Boolean) {
        isPlayingUi = isPlaying
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, buildNotification(isPlaying))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.channel_intro_playback_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_intro_playback_desc)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        phraseManager?.stop()
        mediaSession?.release()
        player?.release()
        player = null
        mediaSession = null
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "intro_phrase_playback"
        const val NOTIFICATION_ID = 1002
        const val ACTION_START = "com.koreanimmersion.intro.action.START"
        const val ACTION_TOGGLE_PAUSE = "com.koreanimmersion.intro.action.TOGGLE_PAUSE"
        const val ACTION_PLAY = "com.koreanimmersion.intro.action.PLAY"
        const val ACTION_SKIP_PREVIOUS = "com.koreanimmersion.intro.action.SKIP_PREVIOUS"
        const val ACTION_SKIP_NEXT = "com.koreanimmersion.intro.action.SKIP_NEXT"
        const val ACTION_STOP = "com.koreanimmersion.intro.action.STOP"
        const val EXTRA_TOPIC_ID = "extra_topic_id"
        const val EXTRA_PHRASES = "extra_phrases"
    }
}
