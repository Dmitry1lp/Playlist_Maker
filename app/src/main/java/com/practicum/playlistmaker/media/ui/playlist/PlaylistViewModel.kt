package com.practicum.playlistmaker.media.ui.playlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.media.domain.db.PlaylistInteractor
import com.practicum.playlistmaker.media.domain.playlist.model.Playlist
import com.practicum.playlistmaker.media.ui.favorites.FavoriteTrackViewModel.Companion.CLICK_DEBOUNCE_DELAY
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.settings.ui.SingleLiveEvent
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale


class PlaylistViewModel(
    private val playlistInteractor: PlaylistInteractor
): ViewModel() {

    val playlistLiveData = MutableLiveData<Playlist?>()
    fun observePlaylistLiveData(): MutableLiveData<Playlist?> = playlistLiveData

    private val playlistDuration = MutableLiveData<String>()
    fun observePlaylistDuration(): MutableLiveData<String> = playlistDuration

    private val playlistTracks = MutableLiveData<List<Track>>()
    fun observePlaylistTracks(): MutableLiveData<List<Track>> = playlistTracks

    private val sharedToast = SingleLiveEvent<Unit>()
    fun observeSharedToast(): LiveData<Unit> = sharedToast

    private val _sharedPlaylist = SingleLiveEvent<String>()
    fun observeSharedPlaylist(): LiveData<String> = _sharedPlaylist

    private val deletePlaylistLD = MutableLiveData<Unit>()
    fun observeDeletePlaylist(): MutableLiveData<Unit> = deletePlaylistLD

    val clickDebounceDelay get() = CLICK_DEBOUNCE_DELAY

    fun deletePlaylist() {
        viewModelScope.launch {
            val playlist = playlistLiveData.value ?: return@launch
            playlistInteractor.deletePlaylist(playlist.id)
            deletePlaylistLD.postValue(Unit)
        }
    }

    fun sharedPlaylist() {
        viewModelScope.launch {
            val currentPlaylist = playlistLiveData.value ?: return@launch

            if(currentPlaylist.trackCount == 0) {
                sharedToast.value = Unit
                return@launch
            }

            val tracks = playlistTracks.value ?: emptyList()

            val text = buildShareText(currentPlaylist, tracks)
            _sharedPlaylist.value = text
        }
    }

    private fun buildShareText(playlist: Playlist, tracks: List<Track>): String {
        val sb = StringBuilder()

        sb.append(playlist.name).append("\n")

        if (!playlist.description.isNullOrBlank()) {
            sb.append(playlist.description).append("\n")
        }

        sb.append("${playlist.trackCount} треков\n\n")

        tracks.forEachIndexed { index, track ->
            val duration = track.trackTime.orEmpty()
            sb.append("${index + 1}. ${track.artistName.orEmpty()} - ${track.trackName.orEmpty()} ($duration)\n")
        }
        return sb.toString()
    }

    fun removeTrackFromPlaylist(track: Track) {
        viewModelScope.launch {
            val playlist = playlistLiveData.value ?: return@launch
            playlistInteractor.removeTrackFromPlaylist(playlist.id,track.trackId)
            // обновляем локальную копию плейлиста
            val updatedTrackIds = playlist.trackId.filter { it != track.trackId }
            val updatedPlaylist = playlist.copy(
                trackId = updatedTrackIds,
                trackCount = updatedTrackIds.size
            )

            // загружаем треки по ID
            val tracks = playlistInteractor.getTracksByIds(playlist.trackId)
            playlistTracks.postValue(tracks)

            // сумма длительностей
            val durationSum = playlistInteractor.getPlaylistDuration(playlist.trackId)

            // формат
            val minutesString =
                SimpleDateFormat("mm", Locale.getDefault()).format(durationSum)

            // отображаем
            playlistDuration.postValue(minutesString)

            // обновление лд
            playlistLiveData.postValue(updatedPlaylist)

            // обновление треков
            val newTracks = playlistInteractor.getTracksByIds(updatedTrackIds)
            playlistTracks.postValue(newTracks)
        }
    }

    fun loadPlaylist(id: Long) {
        viewModelScope.launch {
            // получаем плейлист
            val playlist = playlistInteractor.getPlaylistByID(id)
            playlistLiveData.postValue(playlist)
            // if null то загрлушка
            if (playlist == null) {
                playlistDuration.postValue("0")
                playlistTracks.postValue(emptyList())
                return@launch
            }
            // загружаем треки по ID
            val tracks = playlistInteractor.getTracksByIds(playlist.trackId)
            playlistTracks.postValue(tracks)

            // сумма длительностей
            val durationSum = playlistInteractor.getPlaylistDuration(playlist.trackId)

            // формат
            val minutesString =
                SimpleDateFormat("mm", Locale.getDefault()).format(durationSum)

            // отображаем
            playlistDuration.postValue(minutesString)
        }
    }
}