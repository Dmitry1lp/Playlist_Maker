package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.media.ui.FavoriteTrackViewModel
import com.practicum.playlistmaker.player.ui.PlayerViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import com.practicum.playlistmaker.search.ui.SearchViewModel
import com.practicum.playlistmaker.settings.ui.SettingsViewModel

val viewModelModule = module {

    viewModel {
        SearchViewModel(tracksInteractor = get(), historyInteractor = get())
    }

    viewModel {
        SettingsViewModel(get())
    }

    viewModel {
        PlayerViewModel(get(), get())
    }

    viewModel {
        FavoriteTrackViewModel(get())
    }

}