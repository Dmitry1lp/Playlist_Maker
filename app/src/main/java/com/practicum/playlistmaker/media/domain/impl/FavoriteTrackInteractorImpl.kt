package com.practicum.playlistmaker.media.domain.impl

import com.practicum.playlistmaker.media.domain.FavoriteTracksRepository
import com.practicum.playlistmaker.media.domain.db.FavoriteTrackInteractor
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow

class FavoriteTrackInteractorImpl(
    private val favoriteTracksRepository: FavoriteTracksRepository
): FavoriteTrackInteractor {
    override suspend fun addTrackToFavorite(track: Track) {
        favoriteTracksRepository.addTrackToFavorite(track)
    }

    override suspend fun deleteTrackIntoFavorite(track: Track) {
        favoriteTracksRepository.deleteTrackIntoFavorite(track)
    }

    override fun getFavoriteTrack(): Flow<List<Track>> {
        return favoriteTracksRepository.getFavoriteTrack()
    }

    override suspend fun clearFavorites() {
        favoriteTracksRepository.clearFavorites()
    }
}