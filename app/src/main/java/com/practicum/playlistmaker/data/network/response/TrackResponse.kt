package com.practicum.playlistmaker.data.network.response

data class TrackResponse(val resultCount: Int,
                         val results: ArrayList<TrackDto>)
