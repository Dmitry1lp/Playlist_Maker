package com.practicum.playlistmaker.player.ui

import com.practicum.playlistmaker.search.domain.models.Track

data class PlayerUiState(
    val track: Track,
    val isPlaying: Boolean,
    val currentTime: String,
    val isLiked: Boolean
)