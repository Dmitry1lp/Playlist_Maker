package com.practicum.playlistmaker.player.domain

import com.practicum.playlistmaker.player.ui.PlayerState
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.StateFlow

interface AudioPlayerControl {
    fun getPlayerState(): StateFlow<PlayerState>
    fun startPlayer()
    fun pausePlayer()
    fun setTrack(track: Track)
    fun enterForeground()
    fun exitForeground()
}