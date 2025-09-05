package com.practicum.playlistmaker.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Track (
    val trackId: Long, //ID
    val previewUrl: String?, // Ссылка превью
    val collectionName: String?, //Название альбома
    val releaseDate: String?, // Год
    val primaryGenreName: String?, //Жанр
    val country: String?, //Страна
    val trackName: String?, // Название композиции
    val artistName: String?, // Имя исполнителя
    val trackTime: String, // Продолжительность трека
    val artworkUrl100: String? // Ссылка на изображение обложки
): Parcelable