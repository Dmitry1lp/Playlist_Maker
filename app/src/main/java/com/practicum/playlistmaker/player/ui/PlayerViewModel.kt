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
import com.practicum.playlistmaker.media.domain.db.PlaylistInteractor
import com.practicum.playlistmaker.media.domain.playlist.model.Playlist
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerViewModel(
    track: Track,
    private val favoriteTrackInteractor: FavoriteTrackInteractor,
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val playerState = MutableLiveData<PlayerState>(PlayerState.Default())
    fun observePlayerState(): LiveData<PlayerState> = playerState

    private val playerStateLiveData = MutableLiveData<PlayerUiState>()
    fun observePlayerUiState(): LiveData<PlayerUiState> = playerStateLiveData

    private val _playerBottomSheetUiState = MutableLiveData<PlayerBottomSheetUiState>()
    val observePlayerBottomSheetUiState: LiveData<PlayerBottomSheetUiState> = _playerBottomSheetUiState

    private val _addTrack = MutableLiveData<AddTrack>()
    val observeAddTrack: LiveData<AddTrack> = _addTrack


    private var mediaPlayer = MediaPlayer()
    private var currentTrack: Track = track
    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            val favorites = favoriteTrackInteractor.getFavoriteTrack().first()
            val isFav = favorites.any { it.trackId == track.trackId }
            val updatedTrack = track.copy(isFavorite = isFav)
            playerStateLiveData.postValue(PlayerUiState(track = updatedTrack, isLiked = isFav))
        }
        preparePlayer()
        getPlaylists()
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

    private fun getPlaylists() {
        viewModelScope.launch {
            playlistInteractor.getAllPlaylists().collect { playlist ->
                val playlistCopy = playlist.map { it.copy() }
                if (playlistCopy.isNullOrEmpty()) {
                    _playerBottomSheetUiState.postValue(PlayerBottomSheetUiState.Empty)
                } else {
                    _playerBottomSheetUiState.postValue(PlayerBottomSheetUiState.Content(playlist))
                }
            }
        }
    }

    fun onFavoriteClicked() {
        viewModelScope.launch {

            val newFavorite = !currentTrack.isFavorite
            currentTrack = currentTrack.copy(isFavorite = newFavorite)

            val newState = PlayerUiState(
                track = currentTrack,
                isLiked = newFavorite
            )
            playerStateLiveData.postValue(newState)

            if (newFavorite) {
                favoriteTrackInteractor.addTrackToFavorite(currentTrack)
            } else {
                favoriteTrackInteractor.deleteTrackIntoFavorite(currentTrack)
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

    fun onAddTrackToPlaylistClicked(playlist: Playlist) {
        if (playlist.trackId.contains(currentTrack.trackId)) {
            _addTrack.postValue(AddTrack.NotAdded)
            return
        } else {
            viewModelScope.launch {
                playlistInteractor.addTrackToPlaylist(playlist,currentTrack)
                _addTrack.postValue(AddTrack.Added)
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

        fun getFactory(track: Track, favoriteTrackInteractor: FavoriteTrackInteractor, playlistInteractor: PlaylistInteractor): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PlayerViewModel(track, favoriteTrackInteractor, playlistInteractor)
            }
        }
    }
}