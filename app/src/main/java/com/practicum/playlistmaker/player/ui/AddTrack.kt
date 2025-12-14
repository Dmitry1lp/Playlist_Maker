package com.practicum.playlistmaker.player.ui

sealed interface AddTrack {

    object Added: AddTrack
    object NotAdded: AddTrack
}