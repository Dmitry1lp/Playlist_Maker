package com.practicum.playlistmaker.search.data.repository

import com.practicum.playlistmaker.search.data.model.Resource
import com.practicum.playlistmaker.search.data.storage.StorageClient
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.search.domain.repository.SearchHistoryRepository

class SearchHistoryRepositoryImpl(private val storage: StorageClient<ArrayList<Track>>):
    SearchHistoryRepository {

    override fun getHistory(): Resource<List<Track>> {
        val track = storage.getData() ?: listOf()
        return Resource.Success(track)
    }

    override fun clearHistory() {
        storage.storeData(arrayListOf())
    }

    override fun saveToHistory(t: Track){
        val history = storage.getData() ?: arrayListOf()
        history.removeAll { it.trackId == t.trackId }
        history.add(0, t)
        if (history.size > MAX_SIZE) history.removeAt(history.lastIndex)
        storage.storeData(history)
    }

    companion object {
        private const val MAX_SIZE = 10
    }
}