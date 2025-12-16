package com.practicum.playlistmaker.media.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.media.domain.db.PlaylistInteractor
import com.practicum.playlistmaker.media.domain.playlist.model.Playlist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FavoritesInfoViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _state = MutableStateFlow<CreatePlaylistState>(CreatePlaylistState.Empty)
    val state: StateFlow<CreatePlaylistState> = _state

    fun createPlaylist(playlist: Playlist) {
        viewModelScope.launch {
            _state.value = CreatePlaylistState.Loading

            try {
                playlistInteractor.createPlaylist(playlist)
               _state.value = CreatePlaylistState.Success
            } catch (e: Exception) {
                _state.value = CreatePlaylistState.Error(
                    e.message ?: "Ошибка"
                )
            }
        }
    }
}