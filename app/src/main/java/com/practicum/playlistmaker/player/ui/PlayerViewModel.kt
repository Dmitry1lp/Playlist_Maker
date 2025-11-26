package com.practicum.playlistmaker.player.ui

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.practicum.playlistmaker.media.domain.db.FavoriteTrackInteractor
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerViewModel(
    track: Track,
    private val favoriteTrackInteractor: FavoriteTrackInteractor
) : ViewModel() {

    private val playerState = MutableLiveData<PlayerState>(PlayerState.Default())
    fun observePlayerState(): LiveData<PlayerState> = playerState

    private val playerStateLiveData = MutableLiveData<PlayerUiState>()
    fun observePlayerUiState(): LiveData<PlayerUiState> = playerStateLiveData


    private var mediaPlayer = MediaPlayer()
    private val currentTrack: Track? = track
    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            val favorites = favoriteTrackInteractor.getFavoriteTrack().first()
            val isFav = favorites.any { it.trackId == track.trackId }
            val updatedTrack = track.copy(isFavorite = isFav)
            playerStateLiveData.postValue(PlayerUiState(track = updatedTrack, isLiked = isFav))
        }
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

    fun onFavoriteClicked() {
        val track = currentTrack ?: return
        viewModelScope.launch {

            val newFavorite = !track.isFavorite
            track.isFavorite = newFavorite

            val newState = playerStateLiveData.value?.copy(track = track, isLiked = track.isFavorite)
                ?: PlayerUiState(track = track,
                    isLiked = track.isFavorite)

            playerStateLiveData.postValue(newState)

            if (track.isFavorite) {
                favoriteTrackInteractor.addTrackToFavorite(track)
            } else {
                favoriteTrackInteractor.deleteTrackIntoFavorite(track)
            }
        }
    }

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

    private fun getCurrentPlayerPosition(): String {
        return SimpleDateFormat("mm:ss", Locale.getDefault()).format(mediaPlayer.currentPosition) ?: "00:00"
    }

    fun onPause() {
        pausePlayer()
    }

    fun onDestroy() {
        mediaPlayer.release()
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }

    companion object {

        fun getFactory(track: Track, favoriteTrackInteractor: FavoriteTrackInteractor): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PlayerViewModel(track, favoriteTrackInteractor)
            }
        }
    }
}