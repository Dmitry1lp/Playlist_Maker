package com.practicum.playlistmaker.media.domain

import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface FavoriteTracksRepository {

    suspend fun addTrackToFavorite(track: Track)

    suspend fun deleteTrackIntoFavorite(track: Track)

    fun getFavoriteTrack(): Flow<List<Track>>

    suspend fun clearFavorites()
}