package com.practicum.playlistmaker.player.ui

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.practicum.playlistmaker.search.domain.models.Track

class PlayerViewModel(track: Track) : ViewModel() {

    private val playerStateLiveData = MutableLiveData<PlayerUiState>()
    fun observePlayerState(): LiveData<PlayerUiState> = playerStateLiveData

    private var mediaPlayer = MediaPlayer()
    private var durationMillis: Long = 0L
    private var currentPosition: Long = 0L
    private var isLiked = false
    private var playerState = STATE_DEFAULT
    private var currentTrack: Track? = track
    private var urlTrack: String? = track.previewUrl

    private val handler = Handler(Looper.getMainLooper())

    init {
        preparePlayer()
    }

    fun onPlayClicked() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            stopTimer()
            playerStateLiveData.value = playerStateLiveData.value?.copy(isPlaying = false)
        } else {
            mediaPlayer.start()
            startTimer()
            playerStateLiveData.value = playerStateLiveData.value?.copy(isPlaying = true)
        }
    }

    fun onLikeClicked() {
        val liked = !(playerStateLiveData.value?.isLiked ?: false)
        playerStateLiveData.value = playerStateLiveData.value?.copy(isLiked = liked)
    }

    private fun preparePlayer() {
        val track = currentTrack ?: return
        track.previewUrl?.let { url ->
            try {
                mediaPlayer.reset()
                mediaPlayer.setDataSource(url)
                mediaPlayer.prepareAsync()
                mediaPlayer.setOnPreparedListener {
                    playerStateLiveData.value = PlayerUiState(
                        track = track,
                        isPlaying = false,
                        currentTime = track.trackTime,
                        isLiked = false
                    )
                }
                mediaPlayer.setOnCompletionListener {
                    stopTimer()
                    playerStateLiveData.value = playerStateLiveData.value?.copy(
                        isPlaying = false,
                        currentTime = "00:00"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (mediaPlayer.isPlaying) {
                val elapsedTime = mediaPlayer.currentPosition
                val minutes = (elapsedTime / 1000) / 60
                val seconds = (elapsedTime / 1000) % 60
                val time = String.format("%02d:%02d", minutes, seconds)
                playerStateLiveData.value = playerStateLiveData.value?.copy(currentTime = time)
                handler.postDelayed(this, REFRESH_DELAY_MILLIS)
            }
        }
    }

    private fun startTimer() = handler.post(timerRunnable)
    private fun stopTimer() = handler.removeCallbacks(timerRunnable)

    fun setTrack(track: Track) {
        currentTrack = track
        preparePlayer()
    }

    fun onPause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            playerStateLiveData.value = playerStateLiveData.value?.copy(isPlaying = false)
            handler.removeCallbacksAndMessages(null)
        }
    }
    fun onDestroy() {
        mediaPlayer.release()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer.release()
        handler.removeCallbacks(timerRunnable)
    }

    companion object {
        private const val REFRESH_DELAY_MILLIS = 1000L
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3

        fun getFactory(track: Track): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PlayerViewModel(track)
            }
        }
    }
}