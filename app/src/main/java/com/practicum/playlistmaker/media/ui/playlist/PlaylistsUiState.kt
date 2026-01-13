package com.practicum.playlistmaker.media.ui.playlist

import com.practicum.playlistmaker.media.domain.playlist.model.Playlist

sealed interface PlaylistsUiState {

        data class Content(
            val playlists: List<Playlist>
        ): PlaylistsUiState

        object Empty : PlaylistsUiState

}