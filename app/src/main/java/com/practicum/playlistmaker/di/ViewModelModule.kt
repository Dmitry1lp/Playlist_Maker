package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.media.ui.EditPlaylistViewModel
import com.practicum.playlistmaker.media.ui.favorites.FavoriteTrackViewModel
import com.practicum.playlistmaker.media.ui.favorites.FavoritesInfoViewModel
import com.practicum.playlistmaker.media.ui.playlist.PlaylistViewModel
import com.practicum.playlistmaker.media.ui.playlist.PlaylistsViewModel
import com.practicum.playlistmaker.player.ui.PlayerViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import androidx.lifecycle.SavedStateHandle
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
        PlayerViewModel(get(), get(), get())
    }

    viewModel {
        FavoriteTrackViewModel(get())
    }

    viewModel {
        PlaylistsViewModel(get())
    }

    viewModel {
        FavoritesInfoViewModel(get())
    }

    viewModel {
        PlaylistViewModel(get())
    }

    viewModel {
        EditPlaylistViewModel(get(), get())
    }

    viewModel { (savedStateHandle: SavedStateHandle) ->
        EditPlaylistViewModel(get(), savedStateHandle)
    }


}