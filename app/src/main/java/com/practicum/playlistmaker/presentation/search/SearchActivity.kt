package com.practicum.playlistmaker.presentation.search

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.data.network.api.iTunesAPI
import com.practicum.playlistmaker.data.network.request.Track
import com.practicum.playlistmaker.data.network.response.TrackDto
import com.practicum.playlistmaker.data.network.response.TrackResponse
import com.practicum.playlistmaker.data.storage.SearchHistory
import com.practicum.playlistmaker.presentation.audioplayer.PlayerActivity
import com.practicum.playlistmaker.presentation.track.TrackAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class SearchActivity : AppCompatActivity() {

    // Базовый URL для API iTunes
    private val iTunesBaseUrl = "https://itunes.apple.com"

    // Инициализация Retrofit для сетевых запросов
    private val retrofit = Retrofit.Builder()
        .baseUrl(iTunesBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val iTunesService = retrofit.create(iTunesAPI::class.java)

    private val results = emptyList<TrackDto>()

    private var countValue: String = TEXT_DEF

    private lateinit var searchAdapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter

    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val sharedPrefs = getSharedPreferences("history_prefs", MODE_PRIVATE)
        val searchHistory = SearchHistory(sharedPrefs)

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

        val onClickTrack: (Track) -> Unit = { track ->
            searchHistory.addTrackHistory(track)
            historyAdapter.update(searchHistory.getHistory())
            val playerIntent = Intent(this, PlayerActivity::class.java)
            playerIntent.putExtra("KEY_TRACK", track)
            startActivity(playerIntent)
        }

        // Инициализируем адаптер с пустым списком и назначаем его RecyclerView
        searchAdapter = TrackAdapter(emptyList(), onClickTrack)

        historyAdapter = TrackAdapter(searchHistory.getHistory(), onClickTrack)
        searchHistoryLayout.visibility = if (searchHistory.getHistory().isNotEmpty()) View.VISIBLE else View.GONE

        recyclerView.adapter = searchAdapter
        recyclerViewHistory.adapter = historyAdapter

        // Поиск по введенному тексту
        fun performSearch(searchText: String) {
            iTunesService.search(searchText).enqueue(object: Callback<TrackResponse> {
                override fun onResponse(
                    call: Call<TrackResponse>,
                    response: Response<TrackResponse>
                ) {
                    val body = response.body()
                    if(response.isSuccessful && body != null){
                        val trackList = body.results

                        if (trackList.isNotEmpty()) {// Если треки найдены
                            recyclerView.visibility = View.VISIBLE
                            placeholderNotFound.visibility = View.GONE
                            placeholderNotInternet.visibility = View.GONE

                            val tracks = trackList.map{
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
                                    collectionName = it.collectionName,
                                    primaryGenreName = it.primaryGenreName,
                                    country = it.country,
                                    releaseDate = releaseDateTrack,
                                    trackName = it.trackName,
                                    artistName = it.artistName,
                                    trackTime = SimpleDateFormat("mm:ss", Locale.getDefault())
                                        .format(it.trackTimeMillis),
                                    artworkUrl100 = it.artworkUrl100
                                )
                            }
                            searchAdapter.update(tracks)
                            recyclerView.visibility = View.VISIBLE
                        }else{// Если трек не найден
                            placeholderNotFound.visibility = View.VISIBLE
                            placeholderNotInternet.visibility = View.GONE
                            recyclerView.visibility = View.GONE
                        }
                    }
                }

                override fun onFailure(call: Call<TrackResponse>, t: Throwable) {
                    placeholderNotFound.visibility = View.GONE
                    placeholderNotInternet.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }

            })
        }
        // Обработка фокуса
        inputEditText.setOnFocusChangeListener { view, hasFocus ->
            searchHistoryLayout.visibility = if (hasFocus && inputEditText.text.isEmpty() && searchHistory.getHistory().isNotEmpty()) View.VISIBLE else View.GONE
        }
        inputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchHistoryLayout.visibility = if (searchHistory.getHistory().isNotEmpty() && inputEditText.hasFocus() && p0?.isEmpty() == true) View.VISIBLE else View.GONE
            }

            override fun afterTextChanged(p0: Editable?) {
            }
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
            searchHistory.clearTrackHistory()
            historyAdapter.update(emptyList())
            searchHistoryLayout.visibility = View.GONE
        }
        // Обработка нажатия на кнопку "Очистки текста"
        clearButton.setOnClickListener {
            inputEditText.setText("")
            clearButton.visibility = View.GONE
            placeholderNotInternet.visibility = View.GONE
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

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            // Показываем кнопку очистки, если текст не пустой, иначе скрываем
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNullOrEmpty()) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
                clearButton.visibility = clearButtonVisibility(s)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }
        inputEditText.addTextChangedListener(simpleTextWatcher)
    }
    // Сохраняем состояние поиска при повороте экрана
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(TEXT_SEARCH, countValue)
    }
    // Восстанавливаем состояние поиска
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        countValue = savedInstanceState.getString(TEXT_SEARCH, TEXT_DEF)
    }
    // Видимость кнопки очистки
    private fun clearButtonVisibility(s: CharSequence?): Int {
        return if (s.isNullOrEmpty()) View.GONE  else  View.VISIBLE
    }

    companion object {
        private const val TEXT_SEARCH = "TEXT_SEARCH"
        private const val TEXT_DEF = ""
    }
}