package com.practicum.playlistmaker.media.domain

import com.practicum.playlistmaker.media.domain.playlist.model.Playlist
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {

    suspend fun createPlaylist(playlist: Playlist)

    suspend fun updatePlaylist(playlist: Playlist)

    suspend fun getAllPlaylists(): Flow<List<Playlist>>

    suspend fun addTrackToPlaylist(
        playlist: Playlist,
        track: Track
    )
}