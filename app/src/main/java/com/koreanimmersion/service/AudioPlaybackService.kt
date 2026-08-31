package com.koreanimmersion.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.koreanimmersion.MainActivity
import com.koreanimmersion.R
import com.koreanimmersion.domain.playback.PlaybackSegment
import com.koreanimmersion.domain.playback.PlaybackSegmentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AudioPlaybackService : MediaSessionService() {

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var segments: List<PlaybackSegment> = emptyList()
    private var segmentIndex = 0
    private var isReplayLoop = false
    private var pauseJob: Job? = null
    private var lessonId: Long? = null

    private val _playbackState = MutableStateFlow(ServicePlaybackState())
    val playbackState: StateFlow<ServicePlaybackState> = _playbackState.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                onSegmentFinished()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
            .also { it.addListener(playerListener) }

        mediaSession = MediaSession.Builder(this, player!!).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val newSegments = intent.getParcelableArrayListExtra<PlaybackSegmentParcelable>(EXTRA_SEGMENTS)
                    ?.map { it.toDomain() } ?: emptyList()
                lessonId = intent.getLongExtra(EXTRA_LESSON_ID, -1L).takeIf { it > 0 }
                isReplayLoop = intent.getBooleanExtra(EXTRA_REPLAY_LOOP, false)
                startPlayback(newSegments)
            }
            ACTION_PAUSE -> pause()
            ACTION_PLAY -> resume()
            ACTION_STOP -> stopPlayback(userInitiated = true)
        }
        return START_STICKY
    }

    private fun startPlayback(newSegments: List<PlaybackSegment>) {
        segments = newSegments
        segmentIndex = 0
        _playbackState.value = ServicePlaybackState(
            isPlaying = true,
            lessonId = lessonId,
            isReplayLoop = isReplayLoop,
            currentSegmentIndex = 0,
            totalSegments = segments.size
        )
        startForeground(NOTIFICATION_ID, buildNotification(isPlaying = true))
        playCurrentSegment()
    }

    private fun playCurrentSegment() {
        pauseJob?.cancel()
        if (segmentIndex >= segments.size) {
            onLessonCycleCompleted()
            return
        }
        val segment = segments[segmentIndex]
        updateStateSegment(segment)

        when (segment.type) {
            PlaybackSegmentType.PHRASE_USER_PAUSE,
            PlaybackSegmentType.SRS_USER_PAUSE -> {
                pauseJob = serviceScope.launch {
                    delay(segment.pauseDurationMs)
                    advanceSegment()
                }
            }
            PlaybackSegmentType.PHRASE_CONTEXT,
            PlaybackSegmentType.SRS_REVIEW_CONTEXT -> {
                pauseJob = serviceScope.launch {
                    delay(2_000L)
                    advanceSegment()
                }
            }
            else -> {
                val url = segment.audioUrl
                if (url.isNullOrBlank()) {
                    advanceSegment()
                } else {
                    val mediaItem = MediaItem.fromUri(Uri.parse(url))
                    player?.setMediaItem(mediaItem)
                    player?.prepare()
                    player?.play()
                }
            }
        }
    }

    private fun onSegmentFinished() {
        advanceSegment()
    }

    private fun advanceSegment() {
        segmentIndex++
        _playbackState.value = _playbackState.value.copy(currentSegmentIndex = segmentIndex)
        playCurrentSegment()
    }

    private fun onLessonCycleCompleted() {
        val cycles = _playbackState.value.fullCyclesCompleted + 1
        _playbackState.value = _playbackState.value.copy(
            fullCyclesCompleted = cycles,
            hasCompletedFullCycle = true
        )
        sendBroadcast(Intent(ACTION_CYCLE_COMPLETED).apply {
            setPackage(packageName)
            putExtra(EXTRA_LESSON_ID, lessonId ?: -1L)
        })
        if (isReplayLoop) {
            segmentIndex = 0
            playCurrentSegment()
        } else {
            stopPlayback(userInitiated = false)
        }
    }

    private fun pause() {
        player?.pause()
        pauseJob?.cancel()
        _playbackState.value = _playbackState.value.copy(isPlaying = false)
        updateNotification(isPlaying = false)
    }

    private fun resume() {
        val segment = segments.getOrNull(segmentIndex)
        if (segment?.type == PlaybackSegmentType.PHRASE_USER_PAUSE ||
            segment?.type == PlaybackSegmentType.SRS_USER_PAUSE ||
            segment?.type == PlaybackSegmentType.PHRASE_CONTEXT ||
            segment?.type == PlaybackSegmentType.SRS_REVIEW_CONTEXT
        ) {
            playCurrentSegment()
        } else {
            player?.play()
        }
        _playbackState.value = _playbackState.value.copy(isPlaying = true)
        updateNotification(isPlaying = true)
    }

    private fun stopPlayback(userInitiated: Boolean) {
        pauseJob?.cancel()
        player?.stop()
        player?.clearMediaItems()
        if (userInitiated) {
            sendBroadcast(Intent(ACTION_USER_STOP).apply {
                setPackage(packageName)
                putExtra(EXTRA_LESSON_ID, lessonId ?: -1L)
            })
        }
        _playbackState.value = ServicePlaybackState()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun updateStateSegment(segment: PlaybackSegment) {
        _playbackState.value = _playbackState.value.copy(
            currentDisplayText = segment.displayText,
            currentPhraseId = segment.phraseId,
            currentSegmentType = segment.type.name
        )
    }

    private fun buildNotification(isPlaying: Boolean): Notification {
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val pauseIntent = PendingIntent.getService(
            this, 1,
            Intent(this, AudioPlaybackService::class.java).setAction(ACTION_PAUSE),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val playIntent = PendingIntent.getService(
            this, 2,
            Intent(this, AudioPlaybackService::class.java).setAction(ACTION_PLAY),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this, 3,
            Intent(this, AudioPlaybackService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(
                _playbackState.value.currentDisplayText
                    ?: getString(if (isPlaying) R.string.notification_playing else R.string.notification_paused)
            )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(contentIntent)
            .setOngoing(isPlaying)
            .addAction(
                if (isPlaying) R.drawable.ic_launcher_foreground else R.drawable.ic_launcher_foreground,
                getString(if (isPlaying) R.string.action_pause else R.string.action_play),
                if (isPlaying) pauseIntent else playIntent
            )
            .addAction(R.drawable.ic_launcher_foreground, getString(R.string.action_stop), stopIntent)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession?.sessionCompatToken)
            )
            .build()
    }

    private fun updateNotification(isPlaying: Boolean) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, buildNotification(isPlaying))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.channel_playback_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_playback_desc)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        pauseJob?.cancel()
        mediaSession?.release()
        player?.removeListener(playerListener)
        player?.release()
        player = null
        mediaSession = null
        super.onDestroy()
    }

    data class ServicePlaybackState(
        val isPlaying: Boolean = false,
        val lessonId: Long? = null,
        val isReplayLoop: Boolean = false,
        val currentSegmentIndex: Int = 0,
        val totalSegments: Int = 0,
        val currentDisplayText: String? = null,
        val currentPhraseId: Long? = null,
        val currentSegmentType: String? = null,
        val hasCompletedFullCycle: Boolean = false,
        val fullCyclesCompleted: Int = 0
    )

    companion object {
        const val CHANNEL_ID = "lesson_playback"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.koreanimmersion.action.START"
        const val ACTION_PAUSE = "com.koreanimmersion.action.PAUSE"
        const val ACTION_PLAY = "com.koreanimmersion.action.PLAY"
        const val ACTION_STOP = "com.koreanimmersion.action.STOP"
        const val ACTION_CYCLE_COMPLETED = "com.koreanimmersion.action.CYCLE_COMPLETED"
        const val ACTION_USER_STOP = "com.koreanimmersion.action.USER_STOP"
        const val EXTRA_SEGMENTS = "extra_segments"
        const val EXTRA_LESSON_ID = "extra_lesson_id"
        const val EXTRA_REPLAY_LOOP = "extra_replay_loop"
    }
}
