package com.practicum.playlistmaker.search.data.network.client

import com.practicum.playlistmaker.search.data.network.dto.Response
import com.practicum.playlistmaker.search.data.network.dto.TracksSearchRequest
import com.practicum.playlistmaker.search.data.network.api.iTunesAPI
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitNetworkClient(private val iTunesService: iTunesAPI): NetworkClient {
    // Инициализация Retrofit для сетевых запросов
    private val retrofit = Retrofit.Builder()
        .baseUrl(ITUNES_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()


    override fun doRequest(dto: Any): Response {
        if (dto is TracksSearchRequest) {
            val resp = iTunesService.search(dto.expression).execute()

            val body = resp.body() ?: Response()
            return body.apply { resultCode = resp.code() }
        } else {
            return Response().apply { resultCode = 400 }
        }
    }
    companion object {
        private const val ITUNES_BASE_URL = "https://itunes.apple.com"
    }
}