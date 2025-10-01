package com.practicum.playlistmaker.search.data.network.client

import com.practicum.playlistmaker.search.data.network.dto.Response
import com.practicum.playlistmaker.search.data.network.dto.TracksSearchRequest
import com.practicum.playlistmaker.search.data.network.api.iTunesAPI
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitNetworkClient(private val iTunesService: iTunesAPI): NetworkClient {

    override fun doRequest(dto: Any): Response {
        if (dto is TracksSearchRequest) {
            val resp = iTunesService.search(dto.expression).execute()

            val body = resp.body() ?: Response()
            return body.apply { resultCode = resp.code() }
        } else {
            return Response().apply { resultCode = 400 }
        }
    }
}