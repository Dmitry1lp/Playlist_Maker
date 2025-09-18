package com.practicum.playlistmaker.search.domain.repository

import com.practicum.playlistmaker.search.data.model.Resource
import com.practicum.playlistmaker.search.domain.models.Track

interface SearchHistoryRepository {
    fun saveToHistory(t: Track)
    fun getHistory(): Resource<List<Track>>
    fun clearHistory()
}