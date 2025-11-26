package com.practicum.playlistmaker.media.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "track_table")
data class TrackEntity(
    @PrimaryKey
    val trackId: Long,
    val previewUrl: String?,
    val collectionName: String?,
    val releaseDate: String?,
    val primaryGenreName: String?,
    val country: String?,
    val trackName: String?,
    val artistName: String?,
    val trackTimeMillis: Long,
    val artworkUrl100: String?,
    val addedAt: Long
)
