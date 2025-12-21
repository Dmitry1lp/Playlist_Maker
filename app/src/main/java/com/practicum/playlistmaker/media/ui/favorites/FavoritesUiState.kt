package com.practicum.playlistmaker.media.ui.favorites

import com.practicum.playlistmaker.search.domain.models.Track

sealed interface FavoritesUiState {

    data class Content(
        val tracks: List<Track>
    ): FavoritesUiState

    object Empty : FavoritesUiState
}