package com.practicum.playlistmaker.search.domain.repository

import com.practicum.playlistmaker.search.domain.models.Track

interface TracksRepository {
    fun searchTrack(expression: String): List<Track>
}