package com.practicum.playlistmaker.search.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.practicum.playlistmaker.search.domain.interactor.SearchHistoryInteractor
import com.practicum.playlistmaker.search.domain.interactor.TracksInteractor
import com.practicum.playlistmaker.search.domain.models.SearchResult
import com.practicum.playlistmaker.search.domain.models.Track

class SearchViewModel(private val tracksInteractor: TracksInteractor,
                      private val historyInteractor: SearchHistoryInteractor
): ViewModel() {

    private var mainThreadHandler = Handler(Looper.getMainLooper())
    private var clickHandler = Handler(Looper.getMainLooper())

    private val searchUiStateLiveData = MutableLiveData(SearchUiState())
    val observeSearchUiStateLiveData: LiveData<SearchUiState> = searchUiStateLiveData

    private val clearTextLiveData = MutableLiveData<Unit>()
    val clearTextEvent: LiveData<Unit> get() = clearTextLiveData

    private val playerLiveData = MutableLiveData<Track?>()
    fun observePlayerLiveData(): MutableLiveData<Track?> = playerLiveData

    init {
        loadHistory()
    }

    private var latestSearchText: String? = null
    private var isClickAllowed = true

    private fun loadHistory() {
        historyInteractor.getHistory(object : SearchHistoryInteractor.HistoryConsumer {
            override fun consume(searchHistory: List<Track>?) {
                val hasHistory = searchHistory?.isNotEmpty() == true
                val showHistory = latestSearchText.isNullOrEmpty() && hasHistory
                searchUiStateLiveData.postValue(
                    searchUiStateLiveData.value?.copy(
                        history = searchHistory ?: emptyList(),
                        isHistoryVisible = showHistory
                    ) ?: SearchUiState(
                        history = searchHistory ?: emptyList(),
                        isHistoryVisible = showHistory
                    )
                )
            }
        })
    }

    fun performSearch(searchText: String) {
        if (searchText.isEmpty()) {
            searchUiStateLiveData.value = searchUiStateLiveData.value?.copy(
                tracksState = TracksState.Empty,
                isHistoryVisible = true,
                isLoading = false
            )
            return
        }
        searchUiStateLiveData.value = searchUiStateLiveData.value?.copy(
            tracksState = TracksState.Loading,
            isLoading = true,
            isHistoryVisible = false
        )

        tracksInteractor.searchTrack(searchText, object : TracksInteractor.TracksConsumer {
            override fun consume(result: SearchResult) {
                mainThreadHandler.post {
                    val newState = when (result) {
                        is SearchResult.Success -> TracksState.Content(result.tracks)
                        is SearchResult.NotFound -> TracksState.ErrorFound
                        is SearchResult.NetworkError -> TracksState.ErrorInternet
                    }
                    searchUiStateLiveData.value = searchUiStateLiveData.value?.copy(
                        tracksState = newState,
                        isLoading = false,
                        isHistoryVisible = searchText.isEmpty()
                    )
                }
            }
        })
    }
    val searchRunnable = Runnable { latestSearchText?.let { performSearch(it) } }

    fun searchDebounce(changedText: String) {
        if (latestSearchText == changedText) return
        this.latestSearchText = changedText

        mainThreadHandler.removeCallbacks(searchRunnable)
        if (changedText.isNotEmpty()) {
            mainThreadHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
        } else {
            searchUiStateLiveData.value = searchUiStateLiveData.value?.copy(
                tracksState = TracksState.Empty,
                isLoading = false,
                isHistoryVisible = true
            )
        }
        updateHistoryVisibility(changedText.isEmpty())
    }

    fun onClearTextClicked() {
        searchUiStateLiveData.value = searchUiStateLiveData.value?.copy(isClearTextVisible = false)
        clearTextLiveData.value = Unit
    }

    fun updateClearTextIconVisibility(hasText: Boolean) {
        searchUiStateLiveData.value = searchUiStateLiveData.value?.copy(isClearTextVisible = hasText)
    }
    //    Клик по треку
    fun onTrackClicked(track: Track) {
        historyInteractor.saveToHistory(track)
        historyInteractor.getHistory(object : SearchHistoryInteractor.HistoryConsumer {
            override fun consume(searchHistory: List<Track>?) {
                searchUiStateLiveData.postValue(
                    searchUiStateLiveData.value?.copy(history = searchHistory ?: emptyList())
                        ?: SearchUiState(history = searchHistory ?: emptyList())
                )

            }
        })
        if (clickDebounce()) { playerLiveData.value = track }
    }
    //Очистка истории
    fun clearHistory() {
        historyInteractor.clearTrackHistory()
        searchUiStateLiveData.value = searchUiStateLiveData.value?.copy(
            history = emptyList(),
            isHistoryVisible = false
        )
    }

    override fun onCleared() {
        super.onCleared()
        mainThreadHandler.removeCallbacksAndMessages(SEARCH_REQUEST_TOKEN)
    }
    //Дебаунс кликов по треку
    fun clickDebounce() : Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            clickHandler?.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    fun updateHistoryVisibility(isInputEmpty: Boolean) {
        historyInteractor.getHistory(object : SearchHistoryInteractor.HistoryConsumer {
            override fun consume(searchHistory: List<Track>?) {
                val hasHistory = searchHistory?.isNotEmpty() == true
                val showHistory = isInputEmpty && hasHistory
                searchUiStateLiveData.postValue(
                    searchUiStateLiveData.value?.copy(isHistoryVisible = showHistory)
                        ?: SearchUiState(isHistoryVisible = showHistory)
                )
            }
        })
    }
    // Изменение текста
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

