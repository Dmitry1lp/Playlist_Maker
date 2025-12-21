package com.practicum.playlistmaker.media.data

import com.practicum.playlistmaker.media.data.converters.TrackDbConvertor
import com.practicum.playlistmaker.media.data.db.PlaylistDao
import com.practicum.playlistmaker.media.data.db.PlaylistEntity
import com.practicum.playlistmaker.media.data.db.TrackFavoriteDao
import com.practicum.playlistmaker.media.data.db.TrackFavoriteEntity
import com.practicum.playlistmaker.media.domain.PlaylistRepository
import com.practicum.playlistmaker.media.domain.playlist.model.Playlist
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.utils.JsonConverter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlaylistRepositoryImpl(
    private val playlistDao: PlaylistDao,
    private val trackFavoriteDao: TrackFavoriteDao,
    private val trackDbConvertor: TrackDbConvertor
): PlaylistRepository {
    override suspend fun createPlaylist(playlist: Playlist) {
        playlistDao.insertPlaylist(playlist.toPlaylistEntity())
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        playlistDao.updatePlaylist(playlist.toPlaylistEntity())
    }

    override suspend fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists()
            .map { list -> list.map { it.toPlaylist() } }
    }

    override suspend fun addTrackToPlaylist(
        playlist: Playlist,
        track: Track
    ) {
        trackFavoriteDao.insertTrack(trackDbConvertor.mapToFavorite(track))

        playlistDao.updatePlaylist(
            playlist.copy(
                trackId = playlist.trackId + track.trackId,
                trackCount = playlist.trackCount + 1
            ).toPlaylistEntity()
        )
    }

    fun Playlist.toPlaylistEntity(): PlaylistEntity {
        return PlaylistEntity(
            playlistId = this.id,
            name = this.name,
            description = this.description,
            filePath = this.filePath,
            tracksId = JsonConverter.trackIdsToJson(this.trackId),
            trackCount = this.trackCount
        )
    }

    fun PlaylistEntity.toPlaylist(): Playlist {
        return Playlist(
            id = this.playlistId,
            name = this.name,
            description = this.description,
            filePath = this.filePath,
            trackId = JsonConverter.jsonToTrackIds(this.tracksId),
            trackCount = this.trackCount
        )
    }
}