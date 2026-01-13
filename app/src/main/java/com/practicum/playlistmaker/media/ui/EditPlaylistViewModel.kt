package com.practicum.playlistmaker.media.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.media.domain.db.PlaylistInteractor
import com.practicum.playlistmaker.media.domain.playlist.model.Playlist
import com.practicum.playlistmaker.media.ui.favorites.CreatePlaylistState
import com.practicum.playlistmaker.media.ui.favorites.FavoritesInfoViewModel
import kotlinx.coroutines.launch

class EditPlaylistViewModel(
    private val playlistInteractor: PlaylistInteractor,
    savedStateHandle: SavedStateHandle
) : FavoritesInfoViewModel(playlistInteractor) {

    private val playlistId: Long? = savedStateHandle.get<Long?>("playlistId")

    private val _name = MutableLiveData<String>()
    val name: LiveData<String> = _name

    private val _description = MutableLiveData<String>()
    val description: LiveData<String> = _description

    private val _coverPath = MutableLiveData<String?>()
    val coverPath: LiveData<String?> = _coverPath

    private val _currentPlaylist = MutableLiveData<Playlist?>()
    val currentPlaylist: LiveData<Playlist?> = _currentPlaylist

    init {
        playlistId?.let { loadPlaylist(it) }
    }

    private fun loadPlaylist(id: Long) {
        viewModelScope.launch {
            _state.value = CreatePlaylistState.Loading

            try{
                val playlist = playlistInteractor.getPlaylistByID(id)
                if (playlist == null) {
                    _state.value = CreatePlaylistState.Error("Плейлист не найден")
                    return@launch
                }

                _currentPlaylist.postValue(playlist)
                _name.postValue(playlist.name)
                _description.postValue(playlist.description.orEmpty())
                _coverPath.postValue(playlist.filePath)

                _state.value = CreatePlaylistState.Editing(
                    name = playlist.name,
                    description = playlist.description.orEmpty(),
                    coverPath = playlist.filePath
                )
            } catch (e: Exception) {
                _state.value = CreatePlaylistState.Error("Ошибка загрузки")
            }
        }
    }

    fun savePlaylist(
        name: String,
        description: String,
        coverPath: String?
    ) {
        viewModelScope.launch {
            _state.value = CreatePlaylistState.Loading

            val current = _currentPlaylist.value
                ?: return@launch

            val updated = current.copy(
                name = name,
                description = description,
                filePath = coverPath ?: current.filePath
            )

            try {
                playlistInteractor.updatePlaylist(updated)
                _state.value = CreatePlaylistState.Success
            } catch (e: Exception) {
                _state.value = CreatePlaylistState.Error("Ошибка сохранения")
            }
        }
    }
}