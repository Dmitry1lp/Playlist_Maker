package com.practicum.playlistmaker.settings.data.repository

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
    }
}