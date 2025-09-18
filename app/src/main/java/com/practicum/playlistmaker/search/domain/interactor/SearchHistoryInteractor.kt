package com.practicum.playlistmaker.search.domain.interactor

import com.practicum.playlistmaker.search.domain.models.Track

interface SearchHistoryInteractor {
    fun clearTrackHistory()

    fun getHistory(consumer: HistoryConsumer)
    fun saveToHistory(t: Track)

    interface HistoryConsumer {
        fun consume(searchHistory: List<Track>?)
    }
}