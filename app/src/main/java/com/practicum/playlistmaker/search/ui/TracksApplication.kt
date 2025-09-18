package com.practicum.playlistmaker.search.ui

import android.app.Application
import com.google.gson.reflect.TypeToken
import com.practicum.playlistmaker.search.data.network.client.NetworkClient
import com.practicum.playlistmaker.search.data.network.client.RetrofitNetworkClient
import com.practicum.playlistmaker.search.data.repository.SearchHistoryRepositoryImpl
import com.practicum.playlistmaker.search.data.repository.TracksRepositoryImpl
import com.practicum.playlistmaker.search.data.storage.PrefsStorageClient
import com.practicum.playlistmaker.search.domain.interactor.SearchHistoryInteractor
import com.practicum.playlistmaker.search.domain.interactor.SearchHistoryInteractorImpl
import com.practicum.playlistmaker.search.domain.interactor.TracksInteractor
import com.practicum.playlistmaker.search.domain.interactor.TracksInteractorImpl
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.search.domain.repository.SearchHistoryRepository
import com.practicum.playlistmaker.search.domain.repository.TracksRepository

class TracksApplication: Application() {

    lateinit var networkClient: NetworkClient
    lateinit var tracksRepository: TracksRepository
    lateinit var tracksInteractor: TracksInteractor
    lateinit var searchHistoryRepository: SearchHistoryRepository
    lateinit var searchHistoryInteractor: SearchHistoryInteractor

    override fun onCreate() {
        super.onCreate()

        networkClient = RetrofitNetworkClient()
        tracksRepository = TracksRepositoryImpl(networkClient)
        tracksInteractor = TracksInteractorImpl(tracksRepository)
        val type = object : TypeToken<ArrayList<Track>>() {}.type
        val storage = PrefsStorageClient<ArrayList<Track>>(this, "search_history", type)
        searchHistoryRepository = SearchHistoryRepositoryImpl(storage)
        searchHistoryInteractor = SearchHistoryInteractorImpl(searchHistoryRepository)
    }
}