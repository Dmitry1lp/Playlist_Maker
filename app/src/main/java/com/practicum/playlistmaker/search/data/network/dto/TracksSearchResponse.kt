package com.practicum.playlistmaker.search.data.network.dto

data class TracksSearchResponse(val resultCount: Int,
                                val results: List<TrackDto>): Response()
