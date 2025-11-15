package com.practicum.playlistmaker.search.data.repository

import com.practicum.playlistmaker.search.data.network.dto.TracksSearchRequest
import com.practicum.playlistmaker.search.data.network.dto.TracksSearchResponse
import com.practicum.playlistmaker.search.data.network.client.NetworkClient
import com.practicum.playlistmaker.search.domain.repository.TracksRepository
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class TracksRepositoryImpl (private val networkClient: NetworkClient) : TracksRepository {

    override fun searchTrack(expression: String): Flow<List<Track>> = flow {
        val response = networkClient.doRequest(TracksSearchRequest(expression))
        val tracks = if (response.resultCode == 200) {
             (response as TracksSearchResponse).results.map {
                val releaseDateTrack = try {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                    val date = dateFormat.parse(it.releaseDate ?: "")
                    val formatter = SimpleDateFormat("yyyy", Locale.getDefault())
                    formatter.format(date ?: "")
                } catch (e: Exception) {
                    ""
                }

                Track(
                    trackId = it.trackId,
                    previewUrl = it.previewUrl,
                    collectionName = it.collectionName,
                    releaseDate = releaseDateTrack,
                    primaryGenreName = it.primaryGenreName,
                    country = it.country,
                    trackName = it.trackName,
                    artistName = it.artistName,
                    trackTime = formatTrackTime(it.trackTimeMillis),
                    artworkUrl100 = it.artworkUrl100
                ) }
        } else {
            emptyList()
        }
        emit(tracks)
    }

    private fun formatTrackTime(trackTimeMillis: Long): String {
        return SimpleDateFormat("mm:ss", Locale.getDefault()).format(trackTimeMillis)
    }
}