package com.practicum.playlistmaker.domain.interactor

import com.practicum.playlistmaker.domain.models.Track

interface SearchHistoryInteractor {
    fun addTrackHistory(track: Track)
    fun getHistory(): List<Track>
    fun clearTrackHistory()
}