package com.practicum.playlistmaker.settings.domain.models

interface SettingsRepository {
    fun getDarkTheme(): Boolean
    fun setDarkTheme(isDark: Boolean)
}