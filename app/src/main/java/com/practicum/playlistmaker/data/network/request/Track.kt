package com.practicum.playlistmaker.data.network.request

data class Track (
    val trackId: Long,
    val trackName: String, // Название композиции
    val artistName: String, // Имя исполнителя
    val trackTime: String, // Продолжительность трека
    val artworkUrl100: String // Ссылка на изображение обложки
)