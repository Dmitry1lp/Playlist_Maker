package com.practicum.playlistmaker.media.ui.playlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.media.domain.db.PlaylistInteractor
import com.practicum.playlistmaker.media.domain.playlist.model.Playlist
import kotlinx.coroutines.launch

class PlaylistsViewModel(
    private val playlistInteractor: PlaylistInteractor
): ViewModel() {

    init {
        getPlaylists()
    }

    private val _playlistUiState = MutableLiveData<PlaylistsUiState>()
    val observePlaylistUiState: LiveData<PlaylistsUiState> = _playlistUiState

    private val openPlaylistLiveData = MutableLiveData<Long>()
    fun observeOpenPlaylist(): MutableLiveData<Long> = openPlaylistLiveData

    fun onPlaylistClicked(playlistId: Long) {
        openPlaylistLiveData.value = playlistId
    }

    private fun getPlaylists() {
        viewModelScope.launch {
            playlistInteractor.getAllPlaylists().collect { playlist ->
                val playlistCopy = playlist.map { it.copy() }
                if (playlistCopy.isNullOrEmpty()) {
                    _playlistUiState.postValue(PlaylistsUiState.Empty)
                } else {
                    _playlistUiState.postValue(PlaylistsUiState.Content(playlist))
                }
            }
        }
    }
}