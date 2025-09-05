package com.practicum.playlistmaker.presentation.search

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.creator.Creator
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.domain.interactor.TracksInteractor
import com.practicum.playlistmaker.presentation.track.TrackAdapter
import com.practicum.playlistmaker.ui.PlayerActivity

class SearchActivity : AppCompatActivity() {

    private var mainThreadHandler: Handler? = null
    private var clickHandler: Handler? = null
    private var searchText: String = ""
    private var isClickAllowed = true
    private val getTracksInteractor = Creator.provideTracksInteractor()

    private lateinit var searchAdapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter

    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val getSearchHistoryInteractor = Creator.provideSearchHistoryInteractor(getSharedPreferences("history_prefs", MODE_PRIVATE))

        mainThreadHandler = Handler(Looper.getMainLooper())
        clickHandler = Handler(Looper.getMainLooper())

        val backButton = findViewById<MaterialToolbar>(R.id.toolbarSearch)
        val clearButton = findViewById<ImageView>(R.id.clearIcon)
        val inputEditText = findViewById<EditText>(R.id.inputEditText)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val recyclerViewHistory = findViewById<RecyclerView>(R.id.rv_history)
        val placeholderNotFound = findViewById<LinearLayout>(R.id.placeholderNotFound)
        val searchHistoryLayout = findViewById<NestedScrollView>(R.id.search_history)
        val placeholderNotInternet = findViewById<LinearLayout>(R.id.placeholderNotInternet)
        val refreshButton = findViewById<MaterialButton>(R.id.mb_refresh)
        val clearHistoryButton = findViewById<MaterialButton>(R.id.mb_clear)
        val progressBar = findViewById<ProgressBar>(R.id.pb_progressBar)
        //Клик по треку
        val onClickTrack: (Track) -> Unit = { track ->
            getSearchHistoryInteractor.addTrackHistory(track)
            historyAdapter.update(getSearchHistoryInteractor.getHistory())
            if(clickDebounce()) {
                val playerIntent = Intent(this, PlayerActivity::class.java)
                playerIntent.putExtra(KEY, track)
                startActivity(playerIntent)
            }
        }
        // Инициализируем адаптер с пустым списком и назначаем его RecyclerView
        searchAdapter = TrackAdapter(emptyList(), onClickTrack)

        historyAdapter = TrackAdapter(getSearchHistoryInteractor.getHistory(), onClickTrack)
        searchHistoryLayout.visibility = if (getSearchHistoryInteractor.getHistory().isNotEmpty()) View.VISIBLE else View.GONE

        recyclerView.adapter = searchAdapter
        recyclerViewHistory.adapter = historyAdapter

        // Поиск по введенному тексту
        fun performSearch(searchText: String) {
            if (searchText.isEmpty()) {
                progressBar.visibility = View.GONE
                recyclerView.visibility = View.GONE
                placeholderNotFound.visibility = View.GONE
                placeholderNotInternet.visibility = View.GONE
                return
            }
            progressBar.visibility = View.VISIBLE
            getTracksInteractor.searchTrack(searchText, object : TracksInteractor.TracksConsumer {
                override fun consume(foundTracks: List<Track>) {
                    mainThreadHandler?.post {
                        progressBar.visibility = View.GONE
                        if (foundTracks.isNotEmpty()) {
                            recyclerView.visibility = View.VISIBLE
                            placeholderNotFound.visibility = View.GONE
                            placeholderNotInternet.visibility = View.GONE
                            searchAdapter.update(foundTracks)
                        } else {
                            placeholderNotFound.visibility = View.VISIBLE
                            placeholderNotInternet.visibility = View.GONE
                            recyclerView.visibility = View.GONE
                        }
                    }
                }
            })
        }
        val searchRunnable = Runnable { performSearch(searchText) }

        fun searchDebounce() {
            mainThreadHandler?.removeCallbacks(searchRunnable)
            if(searchText.isNotEmpty()) {
                mainThreadHandler?.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
            } else {
                recyclerView.visibility = View.GONE
                placeholderNotFound.visibility = View.GONE
                placeholderNotInternet.visibility = View.GONE
            }
        }
        // Обработка фокуса
        inputEditText.setOnFocusChangeListener { view, hasFocus ->
            searchHistoryLayout.visibility = if (hasFocus && inputEditText.text.isEmpty() && getSearchHistoryInteractor.getHistory().isNotEmpty()) View.VISIBLE else View.GONE
        }
        inputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchText = s.toString()
                clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                searchHistoryLayout.visibility = if (getSearchHistoryInteractor.getHistory().isNotEmpty() && inputEditText.hasFocus() && s?.isEmpty() == true) View.VISIBLE else View.GONE
                searchDebounce()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        // Обработка нажатия на кнопку "Ввод"
        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val searchText = inputEditText.text.toString()
                performSearch(searchText)
                true
            } else {
                false
            }
        }
        // Обработка нажатия на кнопку "Обновить"
        refreshButton.setOnClickListener {
            val searchText = inputEditText.text.toString()
            performSearch(searchText)
        }
        // Обработка нажатия на кнопку "Очистить историю"
        clearHistoryButton.setOnClickListener {
            getSearchHistoryInteractor.clearTrackHistory()
            historyAdapter.update(emptyList())
            searchHistoryLayout.visibility = View.GONE
        }
        // Обработка нажатия на кнопку "Очистки текста"
        clearButton.setOnClickListener {
            inputEditText.setText("")
            clearButton.visibility = View.GONE
            placeholderNotInternet.visibility = View.GONE
            placeholderNotFound.visibility = View.GONE
            recyclerView.visibility = View.GONE
            // Прячем клавиатуру и убираем фокус
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(inputEditText.windowToken, 0)
            inputEditText.clearFocus()
        }
        // Обработка нажатия на кнопку "Назад"
        backButton.setNavigationOnClickListener {
            finish()
        }
    }
    // Сохраняем состояние поиска при повороте экрана
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(TEXT_SEARCH, searchText)
    }
    // Восстанавливаем состояние поиска
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchText = savedInstanceState.getString(TEXT_SEARCH, "")
        findViewById<EditText>(R.id.inputEditText).setText(searchText)
    }
    // Видимость кнопки очистки
    private fun clearButtonVisibility(s: CharSequence?): Int {
        return if (s.isNullOrEmpty()) View.GONE  else  View.VISIBLE
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
    companion object {
        private const val TEXT_SEARCH = "TEXT_SEARCH"
        private const val KEY = "KEY_TRACK"
        private const val CLICK_DEBOUNCE_DELAY = 1000L
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }
}