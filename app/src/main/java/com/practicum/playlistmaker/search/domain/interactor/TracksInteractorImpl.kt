package com.practicum.playlistmaker.search.domain.interactor

import com.practicum.playlistmaker.search.domain.models.SearchResult
import com.practicum.playlistmaker.search.domain.repository.TracksRepository
import java.util.concurrent.Executors

class TracksInteractorImpl(private val repository: TracksRepository): TracksInteractor {

    private val executor = Executors.newCachedThreadPool()

    override fun searchTrack(expression: String, consumer: TracksInteractor.TracksConsumer) {
        executor.execute{
            try {
                val tracks = repository.searchTrack(expression)
                if (tracks.isEmpty()) {
                    consumer.consume(SearchResult.NotFound)
                } else {
                    consumer.consume(SearchResult.Success(tracks))
                }
            } catch (e: Exception) {
                consumer.consume(SearchResult.NetworkError)
            }
        }
    }
}