package com.practicum.playlistmaker.media.ui.favorites

sealed interface CreatePlaylistState {

    object Empty : CreatePlaylistState
    object Loading : CreatePlaylistState
    object Success : CreatePlaylistState
    data class Error(val message: String) : CreatePlaylistState
}