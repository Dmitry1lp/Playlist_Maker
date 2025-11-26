package com.practicum.playlistmaker.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.practicum.playlistmaker.media.data.db.AppDatabase
import com.practicum.playlistmaker.search.data.network.api.iTunesAPI
import com.practicum.playlistmaker.search.data.network.client.NetworkClient
import com.practicum.playlistmaker.search.data.network.client.RetrofitNetworkClient
import com.practicum.playlistmaker.search.data.storage.PrefsStorageClient
import com.practicum.playlistmaker.search.data.storage.StorageClient
import com.practicum.playlistmaker.search.domain.models.Track
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


val dataModule = module {

    single(qualifier = named("settings")) {
        androidContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
    }

    single(qualifier = named("search_history")) {
        androidContext().getSharedPreferences("search_history", Context.MODE_PRIVATE)
    }

    single {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single <iTunesAPI> {
        get<Retrofit>().create(iTunesAPI::class.java)
    }

    factory { Gson() }

    single<NetworkClient> {
        RetrofitNetworkClient(get())
    }

    single<StorageClient<Boolean>>(qualifier = named("dark_theme")) {
        PrefsStorageClient(
            prefs = get(qualifier = named("settings")),
            gson = get(),
            dataKey = "dark_theme",
            type = Boolean::class.java
        )
    }

    single<StorageClient<List<Track>>>(qualifier = named("search_history")) {
        PrefsStorageClient(
            prefs = get(qualifier = named("search_history")),
            gson = get(),
            dataKey = "search_history",
            type = object : TypeToken<List<Track>>() {}.type
        )
    }

    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "database.db")
            .build()
    }
}