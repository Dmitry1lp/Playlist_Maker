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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel(
    private val tracksInteractor: TracksInteractor,
    private val historyInteractor: SearchHistoryInteractor
) : ViewModel() {

    private var mainThreadHandler = Handler(Looper.getMainLooper())
    private var clickHandler = Handler(Looper.getMainLooper())
    private var cachedHistory: List<Track> = emptyList()

    private val searchUiStateLiveData = MutableLiveData(SearchUiState())
    val observeSearchUiStateLiveData: LiveData<SearchUiState> = searchUiStateLiveData

    private val clearTextLiveData = MutableLiveData<Unit>()
    val clearTextEvent: LiveData<Unit> get() = clearTextLiveData

    private val playerLiveData = MutableLiveData<Track?>()
    fun observePlayerLiveData(): MutableLiveData<Track?> = playerLiveData

    private var latestSearchText: String? = null
    private var isClickAllowed = true
    private var searchJob: Job? = null

    val clickDebounceDelay get() = CLICK_DEBOUNCE_DELAY

    init {
        // Загружаем историю сразу при создании ViewModel
        historyInteractor.getHistory(object : SearchHistoryInteractor.HistoryConsumer {
            override fun consume(searchHistory: List<Track>?) {
                cachedHistory = searchHistory ?: emptyList()
                val showHistory = latestSearchText.isNullOrEmpty() && cachedHistory.isNotEmpty()
                searchUiStateLiveData.postValue(
                    SearchUiState(
                        history = cachedHistory,
                        isHistoryVisible = showHistory
                    )
                )
            }
        })
    }

    fun updateHistoryVisibility(isInputEmpty: Boolean) {
        if (cachedHistory.isNotEmpty()) {
            // Используем кэш, если он есть
            val showHistory = isInputEmpty && cachedHistory.isNotEmpty()
            searchUiStateLiveData.postValue(
                searchUiStateLiveData.value?.copy(
                    history = cachedHistory,
                    isHistoryVisible = showHistory
                ) ?: SearchUiState(
                    history = cachedHistory,
                    isHistoryVisible = showHistory
                )
            )
        } else {
            // Загружаем, если кэш пуст
            historyInteractor.getHistory(object : SearchHistoryInteractor.HistoryConsumer {
                override fun consume(searchHistory: List<Track>?) {
                    cachedHistory = searchHistory ?: emptyList()
                    val showHistory = isInputEmpty && cachedHistory.isNotEmpty()
                    searchUiStateLiveData.postValue(
                        searchUiStateLiveData.value?.copy(
                            history = cachedHistory,
                            isHistoryVisible = showHistory
                        ) ?: SearchUiState(
                            history = cachedHistory,
                            isHistoryVisible = showHistory
                        )
                    )
                }
            })
        }
    }

    fun performSearch(searchText: String) {
        if (searchText.isEmpty()) {
            searchUiStateLiveData.value = searchUiStateLiveData.value?.copy(
                tracksState = TracksState.Empty,
                isHistoryVisible = cachedHistory.isNotEmpty(),
                isLoading = false
            )
            return
        }
        searchUiStateLiveData.value = searchUiStateLiveData.value?.copy(
            tracksState = TracksState.Loading,
            isLoading = true,
            isHistoryVisible = false
        )

        viewModelScope.launch {
            tracksInteractor
                .searchTrack(searchText)
                .collect{ result ->
                    val newState = when (result) {
                        is SearchResult.Success -> TracksState.Content(result.tracks)
                        is SearchResult.NotFound -> TracksState.ErrorFound
                        is SearchResult.NetworkError -> TracksState.ErrorInternet
                    }
                    searchUiStateLiveData.value = searchUiStateLiveData.value?.copy(
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
                searchUiStateLiveData.value = searchUiStateLiveData.value?.copy(
                    tracksState = TracksState.Empty,
                    isLoading = false,
                    isHistoryVisible = cachedHistory.isNotEmpty()
                )
            }
            updateHistoryVisibility(changedText.isEmpty())
        }
    }

    fun onClearTextClicked() {
        searchUiStateLiveData.value = searchUiStateLiveData.value?.copy(isClearTextVisible = false)
        clearTextLiveData.value = Unit
    }

    fun updateClearTextIconVisibility(hasText: Boolean) {
        searchUiStateLiveData.value = searchUiStateLiveData.value?.copy(isClearTextVisible = hasText)
    }

    fun onTrackClicked(track: Track) {
        historyInteractor.saveToHistory(track)
        historyInteractor.getHistory(object : SearchHistoryInteractor.HistoryConsumer {
            override fun consume(searchHistory: List<Track>?) {
                cachedHistory = searchHistory ?: emptyList()
                searchUiStateLiveData.postValue(
                    searchUiStateLiveData.value?.copy(history = cachedHistory)
                        ?: SearchUiState(history = cachedHistory)
                )
                if (clickDebounce()) { playerLiveData.value = track }
            }
        })
    }

    fun clearHistory() {
        historyInteractor.clearTrackHistory()
        cachedHistory = emptyList()
        searchUiStateLiveData.value = searchUiStateLiveData.value?.copy(
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

    fun onSearchTextChanged(text: String) {
        updateClearTextIconVisibility(text.isNotEmpty())
        searchDebounce(text)
        updateHistoryVisibility(text.isEmpty())
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
        private val SEARCH_REQUEST_TOKEN = Any()
    }
}

