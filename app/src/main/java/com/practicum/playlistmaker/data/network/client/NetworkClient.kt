package com.practicum.playlistmaker.data.network.client

import com.practicum.playlistmaker.data.dto.Response

interface NetworkClient {
    fun doRequest(dto: Any): Response

}