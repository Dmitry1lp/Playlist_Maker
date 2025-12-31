package com.practicum.playlistmaker.media.domain.impl

import com.practicum.playlistmaker.media.domain.PlaylistRepository
import com.practicum.playlistmaker.media.domain.db.PlaylistInteractor
import com.practicum.playlistmaker.media.domain.playlist.model.Playlist
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow

class PlaylistInteractorImpl(
    private val playlistRepository: PlaylistRepository
): PlaylistInteractor {
    override suspend fun createPlaylist(playlist: Playlist) {
        playlistRepository.createPlaylist(playlist)
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        playlistRepository.deletePlaylist(playlistId)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        playlistRepository.updatePlaylist(playlist)
    }

    override suspend fun deleteTrackEntity(track: Track) {
        return playlistRepository.deleteTrackEntity(track)
    }

    override suspend fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistRepository.getAllPlaylists()
    }

    override suspend fun getPlaylistByID(id: Long): Playlist {
        return playlistRepository.getPlaylistById(id)
    }

    override suspend fun getTracksByIds(trackIds: List<Long>): List<Track> {
        return playlistRepository.getTracksByIds(trackIds)
    }

    override suspend fun getPlaylistDuration(trackIds: List<Long>): Long {
        return playlistRepository.getPlaylistDuration(trackIds)
    }

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        return playlistRepository.removeTrackFromPlaylist(playlistId, trackId)
    }

    override suspend fun addTrackToPlaylist(
        playlist: Playlist,
        track: Track
    ) {
        playlistRepository.addTrackToPlaylist(playlist, track)
    }
}