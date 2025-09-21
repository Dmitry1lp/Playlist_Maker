package com.practicum.playlistmaker.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.practicum.playlistmaker.search.data.network.api.iTunesAPI
import com.practicum.playlistmaker.search.data.network.client.NetworkClient
import com.practicum.playlistmaker.search.data.network.client.RetrofitNetworkClient
import com.practicum.playlistmaker.search.data.storage.PrefsStorageClient
import com.practicum.playlistmaker.search.data.storage.StorageClient
import com.practicum.playlistmaker.search.domain.models.Track
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


val dataModule = module {

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

    single<StorageClient<Boolean>> {
        PrefsStorageClient(
            prefs = androidContext().getSharedPreferences("settings", Context.MODE_PRIVATE),
            gson = get(),
            dataKey = "dark_theme",
            type = object : TypeToken<Boolean>() {}.type
        )
    }

    single<StorageClient<List<Track>>> {
        PrefsStorageClient(
            prefs = androidContext().getSharedPreferences("search_history", Context.MODE_PRIVATE),
            gson = get(),
            dataKey = "search_history",
            type = object : TypeToken<List<Track>>() {}.type
        )
    }
}