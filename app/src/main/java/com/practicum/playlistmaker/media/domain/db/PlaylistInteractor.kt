package com.practicum.playlistmaker.media.domain.db

import com.practicum.playlistmaker.media.data.db.TrackEntity
import com.practicum.playlistmaker.media.domain.playlist.model.Playlist
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistInteractor {

    suspend fun createPlaylist(playlist: Playlist)

    suspend fun deletePlaylist(playlistId: Long)

    suspend fun updatePlaylist(playlist: Playlist)

    suspend fun deleteTrackEntity(track: Track)

    suspend fun getAllPlaylists(): Flow<List<Playlist>>

    suspend fun getPlaylistByID(id: Long): Playlist

    suspend fun getTracksByIds(trackIds: List<Long>): List<Track>

    suspend fun getPlaylistDuration(trackIds: List<Long>): Long

    suspend fun removeTrackFromPlaylist(
        playlistId: Long,
        trackId: Long
    )

    suspend fun addTrackToPlaylist(
        playlist: Playlist,
        track: Track
    )
}