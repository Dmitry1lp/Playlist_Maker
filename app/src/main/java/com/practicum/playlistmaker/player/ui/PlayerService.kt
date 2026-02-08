package com.practicum.playlistmaker.player.ui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.player.domain.AudioPlayerControl
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

internal class PlayerService: Service(), AudioPlayerControl {

    private var timerJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null
    private val binder = PlayerServiceBinder()

    private var currentTrack: Track? = null

    private val _playerState = MutableStateFlow<PlayerState>(PlayerState.Default())
    private val playerState = _playerState.asStateFlow()

    private var isAppForeground = true

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()
        createNotificationChannel()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    private fun createNotificationChannel() {
        // Создание каналов доступно только с Android 8.0
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel = NotificationChannel(
            /* id= */ NOTIFICATION_CHANNEL_ID,
            /* name= */ getString(R.string.music_service),
            /* importance= */ NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = getString(R.string.music_service_description)

        // Регистрируем канал уведомлений
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createServiceNotification(): Notification {

        val contentText = if (currentTrack != null) {
            "${currentTrack!!.artistName} — ${currentTrack!!.trackName}"
        } else {
            getString(R.string.load_service)
        }
            return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(contentText)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build()
    }

    private fun getForegroundServiceTypeConstant(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        } else {
            0
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = CoroutineScope(Dispatchers.Default).launch {
            while (mediaPlayer?.isPlaying == true) {
                delay(DELAY_TIMER)
                _playerState.update { PlayerState.Playing(getCurrentPlayerPosition()) }
            }
        }
    }

    override fun startPlayer() {
        mediaPlayer?.start()
        _playerState.update { PlayerState.Playing(getCurrentPlayerPosition())  }
        startTimer()
        updateNotification()
    }

    override fun pausePlayer() {
        mediaPlayer?.pause()
        timerJob?.cancel()
        _playerState.update{ PlayerState.Paused(getCurrentPlayerPosition()) }
        updateNotification()
    }

    override fun setTrack(track: Track) {
        currentTrack = track

        mediaPlayer?.reset()
        mediaPlayer?.setDataSource(track.previewUrl)
        mediaPlayer?.prepareAsync()
        mediaPlayer?.setOnPreparedListener {
            _playerState.update{ PlayerState.Prepared() }
        }
        mediaPlayer?.setOnCompletionListener {
            timerJob?.cancel()
            _playerState.update{ PlayerState.Prepared() }
            exitForeground()
        }
    }

    override fun enterForeground() {
        ServiceCompat.startForeground(
            /* service = */ this,
            /* id = */ SERVICE_NOTIFICATION_ID,
            /* notification = */ createServiceNotification(),
            /* foregroundServiceType = */ getForegroundServiceTypeConstant()
        )
    }

    override fun exitForeground() {
        stopForeground(true)
    }

    override fun getPlayerState(): StateFlow<PlayerState> {
        return playerState
    }

    private fun releasePlayer() {
        if (mediaPlayer?.isPlaying == false) mediaPlayer?.stop()
        timerJob?.cancel()
        mediaPlayer?.release()
        _playerState.update { PlayerState.Default() }
    }

    fun setAppForeground(isForeground: Boolean) {
        isAppForeground = isForeground
        updateNotification()
    }

    private fun updateNotification() {
        if (!isAppForeground && mediaPlayer?.isPlaying == true) {
            enterForeground()
        } else {
            exitForeground()
        }
    }

    private fun getCurrentPlayerPosition(): String {
        return SimpleDateFormat("mm:ss", Locale.getDefault()).format(mediaPlayer?.currentPosition) ?: "00:00"
    }

    inner class PlayerServiceBinder : Binder() {
        fun getService(): PlayerService = this@PlayerService
    }

    private companion object {
        const val DELAY_TIMER = 300L
        const val NOTIFICATION_CHANNEL_ID = "player_service_channel"
        const val SERVICE_NOTIFICATION_ID = 100
    }

}