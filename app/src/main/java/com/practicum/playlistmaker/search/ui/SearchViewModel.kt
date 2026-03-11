package com.practicum.playlistmaker.search.ui

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.search.domain.interactor.SearchHistoryInteractor
import com.practicum.playlistmaker.search.domain.interactor.TracksInteractor
import com.practicum.playlistmaker.search.domain.models.SearchResult
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchViewModel(
    private val tracksInteractor: TracksInteractor,
    private val historyInteractor: SearchHistoryInteractor
) : ViewModel() {

    private var mainThreadHandler = Handler(Looper.getMainLooper())
    private var cachedHistory: List<Track> = emptyList()

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState = _uiState.asStateFlow()

    private val _clearTextEvent = MutableSharedFlow<Unit>()
    val clearTextEvent = _clearTextEvent.asSharedFlow()

    private val _playerEvent = MutableSharedFlow<Track>()
    val playerEvent = _playerEvent.asSharedFlow()

    private var latestSearchText: String? = null
    private var isClickAllowed = true
    private var searchJob: Job? = null

    val clickDebounceDelay get() = CLICK_DEBOUNCE_DELAY

    init {
        // Загружаем историю сразу при создании ViewModel
        viewModelScope.launch {
            cachedHistory = historyInteractor.getHistory()
            val showHistory = latestSearchText.isNullOrEmpty() && cachedHistory.isNotEmpty()
            _uiState.value =
                SearchUiState(
                    history = cachedHistory,
                    isHistoryVisible = showHistory
                )

        }
    }

    fun updateHistoryVisibility(isInputEmpty: Boolean) {
        if (cachedHistory.isNotEmpty()) {
            // Используем кэш, если он есть
            val showHistory = isInputEmpty && cachedHistory.isNotEmpty()
            _uiState.value =
                _uiState.value.copy(
                    history = cachedHistory,
                    isHistoryVisible = showHistory
                )

        } else {
            // Загружаем, если кэш пуст
            viewModelScope.launch {
                cachedHistory = historyInteractor.getHistory()
                val showHistory = isInputEmpty && cachedHistory.isNotEmpty()
                _uiState.value =
                    _uiState.value.copy(
                        history = cachedHistory,
                        isHistoryVisible = showHistory
                    )
            }
        }
    }
    fun performSearch(searchText: String) {
        if (searchText.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                tracksState = TracksState.Empty,
                isHistoryVisible = cachedHistory.isNotEmpty(),
                isLoading = false
            )
            return
        }
        _uiState.value = _uiState.value.copy(
            tracksState = TracksState.Loading,
            isLoading = true,
            isHistoryVisible = false
        )

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            tracksInteractor
                .searchTrack(searchText)
                .collect{ result ->
                    val newState = when (result) {
                        is SearchResult.Success -> TracksState.Content(result.tracks)
                        is SearchResult.NotFound -> TracksState.ErrorFound
                        is SearchResult.NetworkError -> TracksState.ErrorInternet
                    }
                    _uiState.value = _uiState.value.copy(
                        tracksState = newState,
                        isLoading = false,
                        isHistoryVisible = searchText.isEmpty() && cachedHistory.isNotEmpty()
                    )
                }
            }
        }



    fun searchDebounce(changedText: String) {
        if (latestSearchText == changedText) return
        this.latestSearchText = changedText

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (changedText.isNotEmpty()) {
                delay(SEARCH_DEBOUNCE_DELAY)
                performSearch(changedText)
            } else {
                _uiState.value = _uiState.value.copy(
                    tracksState = TracksState.Empty,
                    isLoading = false,
                    isHistoryVisible = cachedHistory.isNotEmpty()
                )
            }
            updateHistoryVisibility(changedText.isEmpty())
        }
    }

    fun onClearTextClicked() {

        searchJob?.cancel()
        latestSearchText = ""

        _uiState.update { state ->
            state.copy(
                searchText = "",
                isClearTextVisible = false,
                tracksState = TracksState.Empty,
                isHistoryVisible = state.history.isNotEmpty()
            )
        }

    }

    fun updateClearTextIconVisibility(hasText: Boolean) {
        _uiState.value = _uiState.value.copy(isClearTextVisible = hasText)
    }

    fun onTrackClicked(track: Track) {
        viewModelScope.launch(Dispatchers.IO) {
            historyInteractor.saveToHistory(track)
            val newHistory = historyInteractor.getHistory()
            withContext(Dispatchers.Main) {
                cachedHistory = newHistory
                _uiState.value = _uiState.value.copy(history = cachedHistory)
            }
        }

        if (clickDebounce()) {
            viewModelScope.launch {
                _playerEvent.emit(track)
            }
        }
    }
    fun onSearchTextChanged(text: String) {

        _uiState.value = _uiState.value.copy(
            searchText = text,
            isClearTextVisible = text.isNotEmpty()
        )

        searchDebounce(text)
        updateHistoryVisibility(text.isEmpty())
    }

    fun retrySearch() {
        latestSearchText?.let { performSearch(it) }
    }

    fun clearHistory() {
        historyInteractor.clearTrackHistory()
        cachedHistory = emptyList()
        _uiState.value = _uiState.value.copy(
            history = emptyList(),
            isHistoryVisible = false
        )
    }

    override fun onCleared() {
        super.onCleared()
        mainThreadHandler.removeCallbacksAndMessages(SEARCH_REQUEST_TOKEN)
    }

    fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false

            viewModelScope.launch {
                delay(CLICK_DEBOUNCE_DELAY)
                isClickAllowed = true
            }
        }
        return current
    }

//    fun onSearchTextChanged(text: String) {
//        updateClearTextIconVisibility(text.isNotEmpty())
//        searchDebounce(text)
//        updateHistoryVisibility(text.isEmpty())
//    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
        private val SEARCH_REQUEST_TOKEN = Any()
    }
}

