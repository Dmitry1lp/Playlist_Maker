package com.practicum.playlistmaker.search.ui

import com.practicum.playlistmaker.search.domain.models.Track

data class SearchUiState(
    val tracksState: TracksState = TracksState.Empty,
    val history: List<Track> = emptyList(),
    val isHistoryVisible: Boolean = false,
    val isClearTextVisible: Boolean = false,
    val isLoading: Boolean = false
)