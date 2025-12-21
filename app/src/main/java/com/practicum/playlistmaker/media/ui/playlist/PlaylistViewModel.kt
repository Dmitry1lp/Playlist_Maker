package com.practicum.playlistmaker.media.ui.playlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.media.domain.db.PlaylistInteractor
import kotlinx.coroutines.launch

class PlaylistViewModel(
    private val playlistInteractor: PlaylistInteractor
): ViewModel() {

    init {
        getPlaylists()
    }

    private val _playlistUiState = MutableLiveData<PlaylistUiState>()
    val observePlaylistUiState: LiveData<PlaylistUiState> = _playlistUiState

    private fun getPlaylists() {
        viewModelScope.launch {
            playlistInteractor.getAllPlaylists().collect { playlist ->
                val playlistCopy = playlist.map { it.copy() }
                if (playlistCopy.isNullOrEmpty()) {
                    _playlistUiState.postValue(PlaylistUiState.Empty)
                } else {
                    _playlistUiState.postValue(PlaylistUiState.Content(playlist))
                }
            }
        }
    }
}