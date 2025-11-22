package com.practicum.playlistmaker.search.domain.interactor

import com.practicum.playlistmaker.search.domain.models.SearchResult
import com.practicum.playlistmaker.search.domain.repository.TracksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.util.concurrent.Executors

class TracksInteractorImpl(private val repository: TracksRepository): TracksInteractor {

    override fun searchTrack(expression: String): Flow<SearchResult> {
        return repository.searchTrack(expression).map { tracks ->
            if (tracks.isEmpty()) (SearchResult.NotFound)
            else  (SearchResult.Success(tracks))
        } .catch { emit(SearchResult.NetworkError) }
    }
}