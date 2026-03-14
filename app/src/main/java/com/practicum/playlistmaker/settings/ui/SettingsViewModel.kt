package com.practicum.playlistmaker.settings.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.settings.domain.models.SettingsInteractor
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsInteractor: SettingsInteractor) : ViewModel() {

    private val _isDarkTheme = MutableStateFlow(settingsInteractor.getDarkTheme())
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _shareEvent = MutableSharedFlow<String>()
    val shareEvent: SharedFlow<String> = _shareEvent.asSharedFlow()

    private val _helpEvent = MutableSharedFlow<String>()
    val helpEvent: SharedFlow<String> = _helpEvent.asSharedFlow()

    private val _agreementEvent = MutableSharedFlow<String>()
    val agreementEvent: SharedFlow<String> = _agreementEvent.asSharedFlow()

    init {
        settingsInteractor.setDarkTheme(_isDarkTheme.value)
    }

    fun onShareClicked() {
        viewModelScope.launch {
            _shareEvent.emit("https://practicum.yandex.ru/profile/android-developer-plus")
        }
    }

    fun onHelpClicked() {
        viewModelScope.launch {
            _helpEvent.emit("perovdv1@ya.ru")
        }
    }

    fun onAgreementClicked() {
        viewModelScope.launch {
            _agreementEvent.emit("https://yandex.ru/legal/practicum_offer/")
        }
    }

    fun switchTheme(isDark: Boolean) {
        settingsInteractor.setDarkTheme(isDark)
        _isDarkTheme.value = isDark
    }
}