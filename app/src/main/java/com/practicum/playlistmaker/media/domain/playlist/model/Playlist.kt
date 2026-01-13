package com.practicum.playlistmaker.media.domain.playlist.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Playlist(
    val id: Long = 0,
    val name: String,
    val description: String?,
    val filePath: String?,
    val trackId: List<Long>,
    val trackCount: Int
): Parcelable
