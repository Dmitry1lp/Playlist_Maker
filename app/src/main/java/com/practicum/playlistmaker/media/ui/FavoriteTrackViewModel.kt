package com.practicum.playlistmaker.media.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.media.domain.db.FavoriteTrackInteractor
import kotlinx.coroutines.launch

class FavoriteTrackViewModel(
    private val favoriteTrackInteractor: FavoriteTrackInteractor
): ViewModel() {

    init {
        getFavoriteTracks()
    }

    val clickDebounceDelay get() = CLICK_DEBOUNCE_DELAY

    private val _favoritesUiState = MutableLiveData<FavoritesUiState>()
    val observeFavoritesUiState: LiveData<FavoritesUiState> = _favoritesUiState

    private fun getFavoriteTracks() {
        viewModelScope.launch {
            favoriteTrackInteractor.getFavoriteTrack().collect { tracks ->
                val tracksCopy = tracks.map { it.copy() }
                if (tracksCopy.isNullOrEmpty()) {
                    _favoritesUiState.postValue(FavoritesUiState.Empty)
                } else {
                    _favoritesUiState.postValue(FavoritesUiState.Content(tracks))
                }
            }
        }
    }

    fun clearFavorites() {
        viewModelScope.launch {
            favoriteTrackInteractor.clearFavorites()
        }
    }

    companion object {
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}