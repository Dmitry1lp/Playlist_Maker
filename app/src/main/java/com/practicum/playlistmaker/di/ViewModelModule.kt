package com.practicum.playlistmaker.di

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

}