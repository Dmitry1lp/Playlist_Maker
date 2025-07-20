package com.practicum.playlistmaker.data.storage

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.practicum.playlistmaker.data.network.request.Track

class SearchHistory(private val sharedPrefs: SharedPreferences) {

    val gson = Gson()

    fun saveHistory(track: List<Track>) {
        val json = gson.toJson(track)
        sharedPrefs.edit()
            .putString(KEY_SEARCH_HISTORY,json)
            .apply()
    }

    fun getHistory(): List<Track> {
        val json = sharedPrefs.getString(KEY_SEARCH_HISTORY, null) ?: return emptyList()
        val type = object: TypeToken<List<Track>>() {}.type
        return gson.fromJson(json, type)
    }

    fun clearTrackHistory() {
        sharedPrefs.edit()
            .remove(KEY_SEARCH_HISTORY)
            .apply()
    }

    @SuppressLint("NewApi")
    fun addTrackHistory(track: Track){
        val historyList = getHistory().toMutableList().apply() {

            removeAll { it.trackId == track.trackId }
            add(0,track)
            if (size > MAX_SIZE) {
                removeLast()
            }
        }
        saveHistory(historyList)
    }
    companion object {
        private const val KEY_SEARCH_HISTORY = "search_history"
        private const val MAX_SIZE = 10
    }
}