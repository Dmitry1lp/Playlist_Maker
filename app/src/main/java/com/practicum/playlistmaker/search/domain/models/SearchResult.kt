package com.practicum.playlistmaker.search.domain.models

sealed class SearchResult {
    data class Success(val tracks: List<Track>) : SearchResult()
    object NotFound : SearchResult()
    object NetworkError : SearchResult()
}