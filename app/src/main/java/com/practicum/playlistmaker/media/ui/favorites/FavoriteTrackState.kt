package com.practicum.playlistmaker.media.ui.favorites

import com.practicum.playlistmaker.search.domain.models.Track

sealed interface FavoriteTrackState {

    object Loading : FavoriteTrackState

    data class Content(
        val movies: List<Track>
    ) : FavoriteTrackState

    data class Empty(
        val message: String
    ) : FavoriteTrackState
}