package com.practicum.playlistmaker.media.data

import com.practicum.playlistmaker.media.data.converters.TrackDbConvertor
import com.practicum.playlistmaker.media.data.db.AppDatabase
import com.practicum.playlistmaker.media.data.db.TrackDao
import com.practicum.playlistmaker.media.data.db.TrackEntity
import com.practicum.playlistmaker.media.data.db.TrackFavoriteDao
import com.practicum.playlistmaker.media.domain.FavoriteTracksRepository
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class FavoriteTrackRepositoryImpl(
    private val trackFavoriteDao: TrackFavoriteDao,
    private val trackDbConvertor: TrackDbConvertor
): FavoriteTracksRepository {

    override suspend fun addTrackToFavorite(track: Track) {
        track.isFavorite = true
        val trackEntity = trackDbConvertor.mapToFavorite(track).copy(
            addedAt = System.currentTimeMillis()
        )
        trackFavoriteDao.insertTrack(trackEntity)
    }

    override suspend fun deleteTrackIntoFavorite(track: Track) {
        track.isFavorite = false
        trackFavoriteDao.deleteTrackEntity(track.trackId)
    }

    override fun getFavoriteTrack(): Flow<List<Track>> =
        trackFavoriteDao.getFavoriteTracks().map { favoriteEntities ->
            favoriteEntities.map { trackDbConvertor.mapFromFavorite(it) }
        }

    override suspend fun clearFavorites() {
        trackFavoriteDao.clearAll()
    }

}