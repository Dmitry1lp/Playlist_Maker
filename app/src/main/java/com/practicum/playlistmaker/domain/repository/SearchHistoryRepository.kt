package com.practicum.playlistmaker.domain.repository

import com.practicum.playlistmaker.domain.models.Track

interface SearchHistoryRepository {
    fun addTrackHistory(track: Track)
    fun getHistory(): List<Track>
    fun clearTrackHistory()
}