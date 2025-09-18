package com.practicum.playlistmaker.search.domain.models

sealed interface SearchResult {
    data class Success(val tracks: List<Track>) : SearchResult
    object NotFound : SearchResult
    object NetworkError : SearchResult
}