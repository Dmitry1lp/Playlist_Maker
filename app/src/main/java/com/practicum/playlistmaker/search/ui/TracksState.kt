package com.practicum.playlistmaker.search.ui

import com.practicum.playlistmaker.search.domain.models.Track

sealed interface TracksState {

    object Loading : TracksState
    object ErrorInternet : TracksState
    object ErrorFound : TracksState
    object Empty : TracksState

    data class Content(
        val tracks: List<Track>
    ) : TracksState


}