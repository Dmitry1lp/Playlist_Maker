package com.practicum.playlistmaker.presentation.setting

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {

    var darkTheme = false

    override fun onCreate() {
        super.onCreate()

        darkTheme = getSharedPreferences("settings", MODE_PRIVATE)
            .getBoolean("dark_theme", false)

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

        getSharedPreferences("settings", MODE_PRIVATE)
            .edit()
            .putBoolean("dark_theme", darkThemeEnabled)
            .apply()

        AppCompatDelegate.setDefaultNightMode(
            if(darkThemeEnabled){
                AppCompatDelegate.MODE_NIGHT_YES
            }else{
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}