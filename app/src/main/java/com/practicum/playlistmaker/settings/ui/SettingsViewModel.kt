package com.practicum.playlistmaker.settings.ui

import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.settings.domain.models.SettingsInteractor
import com.practicum.playlistmaker.sharing.domain.models.SharingInteractor

class SettingsViewModel(
    private val sharingInteractor: SharingInteractor,
    private val settingsInteractor: SettingsInteractor,
) : ViewModel() {
    // Основной код
}