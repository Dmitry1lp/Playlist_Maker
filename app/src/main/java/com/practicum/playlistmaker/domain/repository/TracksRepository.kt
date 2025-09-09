package com.practicum.playlistmaker.domain.repository

import com.practicum.playlistmaker.domain.models.Track

interface TracksRepository {
    fun searchTrack(expression: String): List<Track>
}