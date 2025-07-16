package com.practicum.playlistmaker.data.network.response

data class TrackDto(val trackId: Long,
                    val trackName: String,
                    val artistName: String,
                    val trackTimeMillis: Long,
                    val artworkUrl100: String
)