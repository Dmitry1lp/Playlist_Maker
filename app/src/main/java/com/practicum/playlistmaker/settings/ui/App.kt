package com.practicum.playlistmaker.settings.ui

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import com.practicum.playlistmaker.di.dataModule
import com.practicum.playlistmaker.di.interactorModule
import com.practicum.playlistmaker.di.repositoryModule
import com.practicum.playlistmaker.di.viewModelModule
import com.practicum.playlistmaker.media.data.db.AppDatabase
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class App : Application() {

    lateinit var database: AppDatabase
    private set

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(dataModule, repositoryModule, interactorModule, viewModelModule)
        }

        val settingsRepository = getKoin().get<com.practicum.playlistmaker.settings.domain.models.SettingsRepository>()
        settingsRepository.setDarkTheme(settingsRepository.getDarkTheme())

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "database.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    }
