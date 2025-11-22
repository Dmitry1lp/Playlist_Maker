package com.practicum.playlistmaker.player.ui

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerViewModel(track: Track) : ViewModel() {

//    private val playerStateLiveData = MutableLiveData<PlayerUiState>()
//    fun observePlayerState(): LiveData<PlayerUiState> = playerStateLiveData

    private val playerState = MutableLiveData<PlayerState>(PlayerState.Default())
    fun observePlayerState(): LiveData<PlayerState> = playerState

    private var mediaPlayer = MediaPlayer()
    private var currentTrack: Track? = track
    private var timerJob: Job? = null

    init {
        preparePlayer()
    }

    fun onPlayButtonClicked() {
        when(playerState.value) {
            is PlayerState.Playing -> {
                pausePlayer()
            }
            is PlayerState.Prepared, is PlayerState.Paused -> {
                startPlayer()
            }
            else -> { }
        }
    }

//    fun onPlayClicked() {
//        if (mediaPlayer.isPlaying) {
//            mediaPlayer.pause()
//            stopTimer()
//            playerStateLiveData.value = playerStateLiveData.value?.copy(isPlaying = false)
//        } else {
//            mediaPlayer.start()
//            startTimer()
//            playerStateLiveData.value = playerStateLiveData.value?.copy(isPlaying = true)
//        }
//    }
//
//    fun onLikeClicked() {
//        val liked = !(playerStateLiveData.value?.isLiked ?: false)
//        playerStateLiveData.value = playerStateLiveData.value?.copy(isLiked = liked)
//    }

    private fun preparePlayer() {
        val track = currentTrack ?: return
        track.previewUrl?.let { url ->
            try {
                mediaPlayer.reset()
                mediaPlayer.setDataSource(url)
                mediaPlayer.prepareAsync()
                mediaPlayer.setOnPreparedListener {
                    playerState.postValue(PlayerState.Prepared())
                }
                mediaPlayer.setOnCompletionListener {
                    playerState.postValue(PlayerState.Prepared())
                    timerJob?.cancel()
                    playerState.postValue(PlayerState.Prepared())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

//    private val timerRunnable = object : Runnable {
//        override fun run() {
//            if (mediaPlayer.isPlaying) {
//                val elapsedTime = mediaPlayer.currentPosition
//                val minutes = (elapsedTime / 1000) / 60
//                val seconds = (elapsedTime / 1000) % 60
//                val time = String.format("%02d:%02d", minutes, seconds)
//                playerStateLiveData.value = playerStateLiveData.value?.copy(currentTime = time)
//                handler.postDelayed(this, REFRESH_DELAY_MILLIS)
//            }
//        }
//    }
//    private fun stopTimer() = handler.removeCallbacks(timerRunnable)

    fun setTrack(track: Track) {
        currentTrack = track
        preparePlayer()
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (mediaPlayer.isPlaying) {
                delay(300L)
                playerState.postValue(PlayerState.Playing(getCurrentPlayerPosition()))
            }
        }
    }

    private fun startPlayer() {
        mediaPlayer.start()
        playerState.postValue(PlayerState.Playing(getCurrentPlayerPosition()))
        startTimer()
    }

    private fun pausePlayer() {
        mediaPlayer.pause()
        timerJob?.cancel()
        playerState.postValue(PlayerState.Paused(getCurrentPlayerPosition()))
    }

    private fun releasePlayer() {
        if (mediaPlayer.isPlaying) mediaPlayer.stop()
        mediaPlayer.release()
        playerState.value = PlayerState.Default()
    }

    fun onLikeClicked() {
        val current = playerState.value
        if (current != null) {
            val newState = when (current) {
                is PlayerState.Playing -> PlayerState.Playing(current.progress)
                is PlayerState.Paused -> PlayerState.Paused(current.progress)
                is PlayerState.Prepared -> PlayerState.Prepared()
                else -> current
            }
            playerState.postValue(newState)
        }
    }

    private fun getCurrentPlayerPosition(): String {
        return SimpleDateFormat("mm:ss", Locale.getDefault()).format(mediaPlayer.currentPosition) ?: "00:00"
    }

    fun onPause() {
        pausePlayer()
    }
    //        if (mediaPlayer.isPlaying) {
//            mediaPlayer.pause()
//            playerStateLiveData.value = playerStateLiveData.value?.copy(isPlaying = false)
//            handler.removeCallbacksAndMessages(null)
//        }


    fun onDestroy() {
        mediaPlayer.release()
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
//        mediaPlayer.release()
//        handler.removeCallbacks(timerRunnable)
    }

    companion object {

        fun getFactory(track: Track): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PlayerViewModel(track)
            }
        }
    }
}