package com.practicum.playlistmaker.domain.interactor

import com.practicum.playlistmaker.domain.repository.TracksRepository
import java.util.concurrent.Executors

class TracksInteractorImpl(private val repository: TracksRepository): TracksInteractor {

    private val executor = Executors.newCachedThreadPool()

    override fun searchTrack(expression: String, consumer: TracksInteractor.TracksConsumer) {
        executor.execute{
            consumer.consume(repository.searchTrack(expression))
        }
    }
}