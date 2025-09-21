package com.practicum.playlistmaker.search.data.storage

import android.content.SharedPreferences
import com.google.gson.Gson
import java.lang.reflect.Type

class PrefsStorageClient<T>(
    private val prefs: SharedPreferences,
    private val gson: Gson,
    private val dataKey: String,
    private val type: Type) : StorageClient<T> {

    override fun storeData(data: T) {
        prefs.edit().putString(dataKey, gson.toJson(data, type)).apply()
    }

    override fun getData(): T? {
        val dataJson = prefs.getString(dataKey, null)
        return if (dataJson == null) {
            null
        } else {
            try {
                val result = gson.fromJson<T>(dataJson, type)
                result
            } catch (e: Exception) {
                null
            }
        }
    }
}