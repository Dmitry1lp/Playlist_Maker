package com.practicum.playlistmaker.media.data

import com.practicum.playlistmaker.media.data.converters.TrackDbConvertor
import com.practicum.playlistmaker.media.data.db.AppDatabase
import com.practicum.playlistmaker.media.data.db.TrackEntity
import com.practicum.playlistmaker.media.domain.FavoriteTracksRepository
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class FavoriteTrackRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val trackDbConvertor: TrackDbConvertor
): FavoriteTracksRepository {

    override suspend fun addTrackToFavorite(track: Track) {
        track.isFavorite = true
        val trackEntity = convertFromTrack(track).copy(
            addedAt = System.currentTimeMillis()
        )
        appDatabase.trackDao().insertTrack(trackEntity)
    }

    override suspend fun deleteTrackIntoFavorite(track: Track) {
        track.isFavorite = false
        val trackEntity = convertFromTrack(track)
        appDatabase.trackDao().deleteTrackEntity(trackEntity)
    }

    override fun getFavoriteTrack(): Flow<List<Track>> =
        appDatabase.trackDao().getTracks().map { tracks ->
                convertFromTrackEntity(tracks)
    }

    private fun convertFromTrackEntity(tracks: List<TrackEntity>): List<Track> {
        return tracks.map { trackDbConvertor.map(it) }
    }

    private fun convertFromTrack(track: Track): TrackEntity {
        return trackDbConvertor.map(track)
    }

    override suspend fun clearFavorites() {
        appDatabase.trackDao().clearAll()
    }

}