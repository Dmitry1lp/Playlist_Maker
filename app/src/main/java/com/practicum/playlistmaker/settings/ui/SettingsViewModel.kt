package com.practicum.playlistmaker.settings.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.settings.domain.models.SettingsInteractor

class SettingsViewModel(private val settingsInteractor: SettingsInteractor) : ViewModel() {

    private val _isDarkTheme = MutableLiveData(settingsInteractor.getDarkTheme())
    val isDarkTheme: LiveData<Boolean> = _isDarkTheme

    private val sharedLiveData = SingleLiveEvent<String>()
    fun observeSharedLiveData(): LiveData<String> = sharedLiveData

    private val helpLiveData = SingleLiveEvent<String>()
    fun observeHelpLiveData(): LiveData<String> = helpLiveData

    private val agreementLiveData = SingleLiveEvent<String>()
    fun observeAgreementLiveData(): LiveData<String> = agreementLiveData

    init {
        settingsInteractor.setDarkTheme(_isDarkTheme.value ?: false)
    }

    fun onShareClicked() {
        sharedLiveData.value = "https://practicum.yandex.ru/profile/android-developer-plus"
    }

    fun onHelpClicked() {
        helpLiveData.value = "perovdv1@ya.ru"
    }

    fun onAgreementClicked() {
        agreementLiveData.value = "https://yandex.ru/legal/practicum_offer/"
    }

    fun switchTheme(isDark: Boolean) {
        settingsInteractor.setDarkTheme(isDark)
        _isDarkTheme.value = isDark
    }
}