package com.practicum.playlistmaker.media.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.media.domain.db.PlaylistInteractor
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaylistsViewModel(
    private val playlistInteractor: PlaylistInteractor
): ViewModel() {

    private val _playlistUiState = MutableStateFlow<PlaylistsUiState>(PlaylistsUiState.Empty)
    val playlistUiState: StateFlow<PlaylistsUiState> = _playlistUiState.asStateFlow()

    private val _openPlaylist = MutableSharedFlow<Long>()
    fun openPlaylist(): SharedFlow<Long> = _openPlaylist

    init {
        getPlaylists()
    }

    fun onPlaylistClicked(playlistId: Long) {
        viewModelScope.launch { _openPlaylist.emit(playlistId) }
    }

    fun getPlaylists() {
        viewModelScope.launch {
            playlistInteractor.getAllPlaylists().collect { playlist ->
                val playlistCopy = playlist.map { it.copy() }
                if (playlistCopy.isNullOrEmpty()) {
                    _playlistUiState.value = PlaylistsUiState.Empty
                } else {
                    _playlistUiState.value = PlaylistsUiState.Content(playlist)
                }
            }
        }
    }
}