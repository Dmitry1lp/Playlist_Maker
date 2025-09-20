package com.practicum.playlistmaker.search.domain.interactor

import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.search.domain.repository.SearchHistoryRepository

class SearchHistoryInteractorImpl(
    private val repository: SearchHistoryRepository
) : SearchHistoryInteractor {

    override fun clearTrackHistory() {
        repository.clearHistory()
    }

    override fun getHistory(consumer: SearchHistoryInteractor.HistoryConsumer) {
        val data = repository.getHistory().data
        consumer.consume(data)
    }

    override fun saveToHistory(t: Track) {
        repository.saveToHistory(t)
    }
}