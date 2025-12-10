package com.practicum.playlistmaker.search.data.repository

import com.practicum.playlistmaker.media.data.db.AppDatabase
import com.practicum.playlistmaker.search.data.model.Resource
import com.practicum.playlistmaker.search.data.storage.StorageClient
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.search.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchHistoryRepositoryImpl(
    private val storage: StorageClient<ArrayList<Track>>,
    private val appDatabase: AppDatabase):
    SearchHistoryRepository {

    override suspend fun getHistory(): List<Track> = withContext(Dispatchers.IO) {
        val tracks = storage.getData() ?: listOf()

        val favoriteId = appDatabase.trackDao().getTracksId()
        tracks.forEach { track ->
            track.isFavorite = favoriteId.contains(track.trackId)
        }

        return@withContext tracks
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