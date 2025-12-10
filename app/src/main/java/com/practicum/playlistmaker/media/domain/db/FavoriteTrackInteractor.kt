package com.practicum.playlistmaker.media.domain.db

import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface FavoriteTrackInteractor {

    suspend fun addTrackToFavorite(track: Track)

    suspend fun deleteTrackIntoFavorite(track: Track)

    fun getFavoriteTrack(): Flow<List<Track>>

    suspend fun clearFavorites()
}