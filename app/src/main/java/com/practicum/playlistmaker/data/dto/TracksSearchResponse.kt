package com.practicum.playlistmaker.data.dto

data class TracksSearchResponse(val resultCount: Int,
                                val results: List<TrackDto>): Response()
