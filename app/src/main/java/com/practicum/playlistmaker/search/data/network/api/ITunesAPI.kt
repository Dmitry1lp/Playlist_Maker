package com.practicum.playlistmaker.search.data.network.api

import com.practicum.playlistmaker.search.data.network.dto.TracksSearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface iTunesAPI {
    @GET ("/search?entity=song")
    suspend fun search(@Query("term") text: String): TracksSearchResponse
}