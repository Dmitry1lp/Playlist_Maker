package com.practicum.playlistmaker.presentation.setting

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {

    var darkTheme = false

    override fun onCreate() {
        super.onCreate()

        darkTheme = getSharedPreferences(SETTINGS, MODE_PRIVATE)
            .getBoolean(KEY_DARK_THEME, false)

        AppCompatDelegate.setDefaultNightMode(
            if(darkTheme) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        darkTheme = darkThemeEnabled

        getSharedPreferences(SETTINGS, MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DARK_THEME, darkThemeEnabled)
            .apply()

        AppCompatDelegate.setDefaultNightMode(
            if(darkThemeEnabled){
                AppCompatDelegate.MODE_NIGHT_YES
            }else{
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
    companion object {
        private const val SETTINGS = "settings"
        private const val KEY_DARK_THEME = "dark_theme"
    }
}