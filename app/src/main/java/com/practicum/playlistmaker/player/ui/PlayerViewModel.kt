package com.practicum.playlistmaker.player.ui

import android.content.Context
import android.content.Intent
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
import com.practicum.playlistmaker.player.domain.AudioPlayerControl
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

    private var audioPlayerControl: AudioPlayerControl? = null

    private var currentTrack: Track = track


    init {
        viewModelScope.launch {
            val favorites = favoriteTrackInteractor.getFavoriteTrack().first()
            val isFav = favorites.any { it.trackId == track.trackId }
            val updatedTrack = track.copy(isFavorite = isFav)
            currentTrack = updatedTrack
            playerStateLiveData.postValue(PlayerUiState(track = updatedTrack, isLiked = isFav))
        }
        getPlaylists()
    }

    fun onPlayButtonClicked() {
        when(playerState.value) {
            is PlayerState.Playing -> {
                audioPlayerControl?.pausePlayer()
            }
            is PlayerState.Prepared, is PlayerState.Paused -> {
                audioPlayerControl?.startPlayer()
            }
            else -> { }
        }
    }

    fun setAudioPlayerControl(audioPlayerControl: AudioPlayerControl) {
        this.audioPlayerControl = audioPlayerControl
        audioPlayerControl.setTrack(currentTrack)

        viewModelScope.launch {
            audioPlayerControl.getPlayerState().collect {
                playerState.postValue(it)
            }
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

    override fun onCleared() {
        super.onCleared()
        audioPlayerControl = null
    }

    fun removeAudioPlayerControl() {
        audioPlayerControl = null
    }

    companion object {


    }
}