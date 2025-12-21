package com.practicum.playlistmaker.player.ui

import com.practicum.playlistmaker.media.domain.playlist.model.Playlist

sealed interface PlayerBottomSheetUiState {

    data class Content(
        val playlists: List<Playlist>
    ): PlayerBottomSheetUiState

    object Empty : PlayerBottomSheetUiState
}