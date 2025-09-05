package com.practicum.playlistmaker.creator

import android.content.Context
import android.content.SharedPreferences
import com.practicum.playlistmaker.data.repository.TracksRepositoryImpl
import com.practicum.playlistmaker.data.network.client.RetrofitNetworkClient
import com.practicum.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import com.practicum.playlistmaker.domain.interactor.SearchHistoryInteractor
import com.practicum.playlistmaker.domain.interactor.SearchHistoryInteractorImpl
import com.practicum.playlistmaker.domain.interactor.TracksInteractor
import com.practicum.playlistmaker.domain.repository.TracksRepository
import com.practicum.playlistmaker.domain.interactor.TracksInteractorImpl
import com.practicum.playlistmaker.domain.repository.SearchHistoryRepository

object Creator {
    private fun getTracksRepository(): TracksRepository {
        return TracksRepositoryImpl(RetrofitNetworkClient())
    }

    private fun getSearchHistoryRepository(sharedPrefs: SharedPreferences): SearchHistoryRepository {
        return SearchHistoryRepositoryImpl(sharedPrefs)
    }

    fun provideTracksInteractor(): TracksInteractor {
        return TracksInteractorImpl(getTracksRepository())
    }

    fun provideSearchHistoryInteractor(context: Context): SearchHistoryInteractor {
        val sharedPreferences = context.getSharedPreferences("history_prefs", Context.MODE_PRIVATE)
        return SearchHistoryInteractorImpl(getSearchHistoryRepository(sharedPreferences))
    }

}