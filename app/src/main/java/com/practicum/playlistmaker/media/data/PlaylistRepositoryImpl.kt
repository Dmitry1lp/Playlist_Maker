package com.practicum.playlistmaker.media.data

import com.practicum.playlistmaker.media.data.converters.TrackDbConvertor
import com.practicum.playlistmaker.media.data.db.PlaylistDao
import com.practicum.playlistmaker.media.data.db.PlaylistEntity
import com.practicum.playlistmaker.media.data.db.TrackDao
import com.practicum.playlistmaker.media.data.db.TrackFavoriteDao
import com.practicum.playlistmaker.media.data.db.TrackFavoriteEntity
import com.practicum.playlistmaker.media.domain.PlaylistRepository
import com.practicum.playlistmaker.media.domain.playlist.model.Playlist
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.utils.JsonConverter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.any
import kotlinx.coroutines.flow.map

class PlaylistRepositoryImpl(
    private val playlistDao: PlaylistDao,
    private val trackDao: TrackDao,
    private val trackFavoriteDao: TrackFavoriteDao,
    private val trackDbConvertor: TrackDbConvertor
): PlaylistRepository {
    override suspend fun createPlaylist(playlist: Playlist) {
        playlistDao.insertPlaylist(playlist.toPlaylistEntity())
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        val playlist = playlistDao.getPlaylistById(playlistId)

        playlistDao.deletePlaylist(playlistId)

        val trackId = JsonConverter.jsonToTrackIds(playlist.tracksId)
        removeTrackInPlaylist(trackId)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        playlistDao.updatePlaylist(playlist.toPlaylistEntity())
    }

    override suspend fun deleteTrackEntity(track: Track) {
        trackDao.deleteTrackEntity(trackDbConvertor.map(track))
    }

    override suspend fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists()
            .map { list -> list.map { it.toPlaylist() } }
    }

    override suspend fun getPlaylistById(id: Long): Playlist {
        return playlistDao.getPlaylistById(id).toPlaylist()
    }

    override suspend fun getTracksByIds(trackIds: List<Long>): List<Track> {
        if (trackIds.isEmpty()) return emptyList()
        val trackEntities = trackDao.getTracksByIds(trackIds)
        return trackEntities.map { trackDbConvertor.map(it)}
    }

    override suspend fun getPlaylistDuration(trackIds: List<Long>): Long {
        val tracks = trackDao.getTracksByIds(trackIds)
        return tracks.sumOf { it.trackTimeMillis }
    }

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        // получаем плейлист
        val playlistEntity = playlistDao.getPlaylistById(playlistId)
        // получаем треки
        val trackIds = JsonConverter.jsonToTrackIds(playlistEntity.tracksId)
        //удаляем нужный айди
        val newTrackIds = trackIds.filter { it != trackId }

        //новый плейлист
        playlistDao.updatePlaylist(
            playlistEntity.copy(
                tracksId = JsonConverter.trackIdsToJson(newTrackIds),
                trackCount = newTrackIds.size)
        )
        //удаление
        removeIntoTrackTable(trackId)
    }

    private suspend fun removeIntoTrackTable(trackId: Long) {
        val playlists = playlistDao.getAllPlaylistsOnce()
        val isUse = playlists.any { playlist ->
            JsonConverter
                .jsonToTrackIds(playlist.tracksId)
                .contains(trackId)
        }
        if (!isUse) {
            trackDao.deleteTrackById(trackId)
        }
    }

    private suspend fun removeTrackInPlaylist(trackIds: List<Long>) {
        if (trackIds.isEmpty()) return

        val playlists = playlistDao.getAllPlaylistsOnce()
        trackIds.forEach { trackId ->
            val isUse = playlists.any { pl ->
                val currentId = JsonConverter.jsonToTrackIds(pl.tracksId)
                currentId.contains(trackId)
            }
            if (!isUse) {
                trackDao.deleteTrackById(trackId)
            }
        }
    }

    override suspend fun addTrackToPlaylist(
        playlist: Playlist,
        track: Track
    ) {
        // сохранение трека
        trackDao.insertTrack(trackDbConvertor.map(track))

        // актуальный плейлист
        val playlistEntity = playlistDao.getPlaylistById(playlist.id)

        // список айди
        val currentTrackIds =
            JsonConverter.jsonToTrackIds(playlistEntity.tracksId)

        // проверка наличия трека в списке
        if (currentTrackIds.contains(track.trackId)) return

        // добавляем айди
        val updatedTrackIds = currentTrackIds + track.trackId

        playlistDao.updatePlaylist(
            playlistEntity.copy(
                tracksId = JsonConverter.trackIdsToJson(updatedTrackIds),
                trackCount = updatedTrackIds.size)
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