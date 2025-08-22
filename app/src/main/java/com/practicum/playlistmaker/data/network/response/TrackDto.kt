package com.practicum.playlistmaker.data.network.response

data class TrackDto(val trackId: Long,
                    val previewUrl: String?,
                    val collectionName: String?,
                    val releaseDate: String?,
                    val primaryGenreName: String?,
                    val country: String?,
                    val trackName: String?,
                    val artistName: String?,
                    val trackTimeMillis: Long,
                    val artworkUrl100: String?
)