package com.practicum.playlistmaker.data.network.api

import com.practicum.playlistmaker.data.network.response.TrackResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface iTunesAPI {
    @GET ("/search?entity=song")
    fun search(@Query("term") text: String): Call<TrackResponse>
}