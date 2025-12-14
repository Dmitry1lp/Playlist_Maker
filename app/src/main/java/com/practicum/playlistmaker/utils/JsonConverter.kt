package com.practicum.playlistmaker.utils

import com.google.gson.Gson

object JsonConverter {
    private val gson = Gson()

    fun trackIdsToJson(trackIds: List<Long>): String {
        return gson.toJson(trackIds)
    }

    fun jsonToTrackIds(json: String?): List<Long> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            gson.fromJson(json, Array<Long>::class.java).toList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}