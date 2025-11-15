package com.practicum.playlistmaker.search.data.network.client

import com.practicum.playlistmaker.search.data.network.dto.Response

interface NetworkClient {
    suspend fun doRequest(dto: Any): Response

}