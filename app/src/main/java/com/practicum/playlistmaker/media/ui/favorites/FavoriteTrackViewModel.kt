package com.practicum.playlistmaker.media.ui.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.media.domain.db.FavoriteTrackInteractor
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoriteTrackViewModel(
    private val favoriteTrackInteractor: FavoriteTrackInteractor
): ViewModel() {

    init {
        getFavoriteTracks()
    }

    private var removeJob: Job? = null

    val clickDebounceDelay get() = CLICK_DEBOUNCE_DELAY

    private val _favoritesUiState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Empty)
    val favoritesUiState: StateFlow<FavoritesUiState> = _favoritesUiState.asStateFlow()

    private fun getFavoriteTracks() {
        viewModelScope.launch {
            favoriteTrackInteractor.getFavoriteTrack().collect { tracks ->
                val tracksCopy = tracks.map { it.copy() }
                if (tracksCopy.isNullOrEmpty()) {
                    _favoritesUiState.value = FavoritesUiState.Empty
                } else {
                    _favoritesUiState.value = FavoritesUiState.Content(tracks)
                }
            }
        }
    }

    fun removeTrackFromPlaylist(track: Track) {
        if (removeJob?.isActive == true) return

        removeJob = viewModelScope.launch {
            try {
                favoriteTrackInteractor.deleteTrackIntoFavorite(track)
            } finally {
                removeJob = null
            }
        }
    }

    fun clearFavorites() {
        viewModelScope.launch {
            favoriteTrackInteractor.clearFavorites()
        }
    }

    companion object {
        const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}