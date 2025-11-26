package com.practicum.playlistmaker.search.domain.interactor

import com.practicum.playlistmaker.search.domain.models.Track

interface SearchHistoryInteractor {
    fun clearTrackHistory()
    suspend fun getHistory(): List<Track>
    fun saveToHistory(t: Track)

}