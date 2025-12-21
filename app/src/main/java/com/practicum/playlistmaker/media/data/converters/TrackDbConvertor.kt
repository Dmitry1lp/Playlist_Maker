package com.practicum.playlistmaker.media.data.converters

import com.practicum.playlistmaker.media.data.db.TrackEntity
import com.practicum.playlistmaker.media.data.db.TrackFavoriteEntity
import com.practicum.playlistmaker.search.data.network.dto.TrackDto
import com.practicum.playlistmaker.search.domain.models.Track

class TrackDbConvertor {

    fun map(track: TrackDto): TrackEntity {
        return TrackEntity(track.trackId, track.previewUrl, track.collectionName, track.releaseDate, track.primaryGenreName, track.country, track.trackName, track.artistName, track.trackTimeMillis, track.artworkUrl100, System.currentTimeMillis() )
    }

    fun map(track: TrackEntity): Track {
        return Track(track.trackId, track.previewUrl, track.collectionName, track.releaseDate, track.primaryGenreName, track.country, track.trackName, track.artistName, formatTrackTime(track.trackTimeMillis), track.artworkUrl100)
    }

    fun map(track: Track): TrackEntity {
        return TrackEntity(track.trackId, track.previewUrl, track.collectionName, track.releaseDate, track.primaryGenreName, track.country, track.trackName, track.artistName, parseMillis(track.trackTime), track.artworkUrl100, System.currentTimeMillis() )
    }

    fun mapToFavorite(track: Track): TrackFavoriteEntity {
        return TrackFavoriteEntity(
            trackId = track.trackId,
            previewUrl = track.previewUrl,
            collectionName = track.collectionName,
            releaseDate = track.releaseDate,
            primaryGenreName = track.primaryGenreName,
            country = track.country,
            trackName = track.trackName,
            artistName = track.artistName,
            trackTimeMillis = parseMillis(track.trackTime),
            artworkUrl100 = track.artworkUrl100,
            addedAt = System.currentTimeMillis()
        )
    }

    fun formatTrackTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }

    fun parseMillis(trackTime: String): Long {
        val parts = trackTime.split(":")
        val minutes = parts[0].toLong()
        val seconds = parts[1].toLong()
        return (minutes * 60 + seconds) * 1000
    }

}