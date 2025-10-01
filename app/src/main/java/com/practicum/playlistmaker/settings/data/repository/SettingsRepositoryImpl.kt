package com.practicum.playlistmaker.settings.data.repository

import androidx.appcompat.app.AppCompatDelegate
import com.practicum.playlistmaker.search.data.storage.StorageClient
import com.practicum.playlistmaker.settings.domain.models.SettingsRepository

class SettingsRepositoryImpl(
    private val storage: StorageClient<Boolean>
) : SettingsRepository {

    override fun getDarkTheme(): Boolean {
        return storage.getData() ?: false
    }

    override fun setDarkTheme(isDark: Boolean) {
        storage.storeData(isDark)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}