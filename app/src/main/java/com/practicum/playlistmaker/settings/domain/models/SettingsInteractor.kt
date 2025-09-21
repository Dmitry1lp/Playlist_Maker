package com.practicum.playlistmaker.settings.domain.models

interface SettingsInteractor {
    fun getDarkTheme(): Boolean
    fun setDarkTheme(isDark: Boolean)
    fun switchTheme(darkThemeEnabled: Boolean)
}