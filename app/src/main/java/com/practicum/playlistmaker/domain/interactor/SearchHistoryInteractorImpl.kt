package com.practicum.playlistmaker.domain.interactor

import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.domain.repository.SearchHistoryRepository

class SearchHistoryInteractorImpl(
    private val repository: SearchHistoryRepository
) : SearchHistoryInteractor {
    override fun addTrackHistory(track: Track) {
        repository.addTrackHistory(track)
    }

    override fun getHistory(): List<Track> {
        return repository.getHistory()
    }

    override fun clearTrackHistory() {
        repository.clearTrackHistory()
    }
}