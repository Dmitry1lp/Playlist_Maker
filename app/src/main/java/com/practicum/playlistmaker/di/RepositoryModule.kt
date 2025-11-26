package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.media.data.FavoriteTrackRepositoryImpl
import com.practicum.playlistmaker.media.data.converters.TrackDbConvertor
import com.practicum.playlistmaker.media.domain.FavoriteTracksRepository
import com.practicum.playlistmaker.search.data.repository.SearchHistoryRepositoryImpl
import com.practicum.playlistmaker.search.data.repository.TracksRepositoryImpl
import com.practicum.playlistmaker.search.data.storage.StorageClient
import com.practicum.playlistmaker.search.domain.repository.SearchHistoryRepository
import com.practicum.playlistmaker.search.domain.repository.TracksRepository
import com.practicum.playlistmaker.settings.data.repository.SettingsRepositoryImpl
import com.practicum.playlistmaker.settings.domain.models.SettingsRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

val repositoryModule = module {

    single<TracksRepository> {
        TracksRepositoryImpl(get(), get(), get())
    }

    single<SearchHistoryRepository> {
        SearchHistoryRepositoryImpl(get(qualifier = named("search_history")), get())
    }

    single<SettingsRepository> {
        SettingsRepositoryImpl(get<StorageClient<Boolean>>(qualifier = named("dark_theme")))
    }

    single<FavoriteTracksRepository> {
        FavoriteTrackRepositoryImpl(get(), get())
    }

    factory { TrackDbConvertor() }
}