package com.practicum.playlistmaker.settings.domain.impl


import androidx.appcompat.app.AppCompatDelegate
import com.practicum.playlistmaker.settings.domain.models.SettingsInteractor
import com.practicum.playlistmaker.settings.domain.models.SettingsRepository


class SettingsInteractorImpl(
    private val repository: SettingsRepository
) : SettingsInteractor {

    override fun getDarkTheme(): Boolean = repository.getDarkTheme()

    override fun setDarkTheme(isDark: Boolean) {
        repository.setDarkTheme(isDark)
    }

    override fun switchTheme(isDark: Boolean) {
        repository.setDarkTheme(isDark)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}