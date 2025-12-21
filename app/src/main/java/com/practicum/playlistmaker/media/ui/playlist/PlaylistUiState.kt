package com.practicum.playlistmaker.media.ui.playlist

import com.practicum.playlistmaker.media.domain.playlist.model.Playlist
import com.practicum.playlistmaker.search.domain.models.Track

sealed interface PlaylistUiState {

        data class Content(
            val playlists: List<Playlist>
        ): PlaylistUiState

        object Empty : PlaylistUiState

}