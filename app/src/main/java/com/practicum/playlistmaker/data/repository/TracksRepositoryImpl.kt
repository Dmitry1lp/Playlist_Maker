package com.practicum.playlistmaker.data.repository

import com.practicum.playlistmaker.data.dto.TracksSearchRequest
import com.practicum.playlistmaker.data.dto.TracksSearchResponse
import com.practicum.playlistmaker.data.network.client.NetworkClient
import com.practicum.playlistmaker.domain.repository.TracksRepository
import com.practicum.playlistmaker.domain.models.Track
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class TracksRepositoryImpl (private val networkClient: NetworkClient) : TracksRepository {

    override fun searchTrack(expression: String): List<Track> {
        val response = networkClient.doRequest(TracksSearchRequest(expression))
        if (response.resultCode == 200) {
            return (response as TracksSearchResponse).results.map {
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
            return emptyList()
        }
    }

    private fun formatTrackTime(trackTimeMillis: Long): String {
        return SimpleDateFormat("mm:ss", Locale.getDefault()).format(trackTimeMillis)
    }
}