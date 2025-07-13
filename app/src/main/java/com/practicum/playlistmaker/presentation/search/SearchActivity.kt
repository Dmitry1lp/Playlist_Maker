package com.practicum.playlistmaker.presentation.search

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.data.network.api.iTunesAPI
import com.practicum.playlistmaker.data.network.request.Track
import com.practicum.playlistmaker.data.network.response.TrackDto
import com.practicum.playlistmaker.data.network.response.TrackResponse
import com.practicum.playlistmaker.presentation.track.TrackAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Locale

class SearchActivity : AppCompatActivity() {

    private val iTunesBaseUrl = "https://itunes.apple.com"

    private val retrofit = Retrofit.Builder()
        .baseUrl(iTunesBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val iTunesService = retrofit.create(iTunesAPI::class.java)

    private val results = emptyList<TrackDto>()

    private var countValue: String = TEXT_DEF

    private lateinit var trackAdapter: TrackAdapter


    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val backButton = findViewById<MaterialToolbar>(R.id.toolbarSearch)
        val clearButton = findViewById<ImageView>(R.id.clearIcon)
        val inputEditText = findViewById<EditText>(R.id.inputEditText)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val placeholderNotFound = findViewById<LinearLayout>(R.id.placeholderNotFound)
        val placeholderNotInternet = findViewById<LinearLayout>(R.id.placeholderNotInternet)
        val refreshButton = findViewById<MaterialButton>(R.id.mb_refresh)

        trackAdapter = TrackAdapter(emptyList())
        recyclerView.adapter = trackAdapter

        fun performSearch(searchText: String) {
            iTunesService.search(searchText).enqueue(object: Callback<TrackResponse> {
                override fun onResponse(
                    call: Call<TrackResponse>,
                    response: Response<TrackResponse>
                ) {
                    val body = response.body()
                    if(response.isSuccessful && body != null){
                        val trackList = body.results

                        if (trackList.isNotEmpty()) {

                            recyclerView.visibility = View.VISIBLE
                            placeholderNotFound.visibility = View.GONE
                            placeholderNotInternet.visibility = View.GONE

                            val tracks = trackList.map{
                                Track(
                                    trackName = it.trackName,
                                    artistName = it.artistName,
                                    trackTime = SimpleDateFormat("mm:ss", Locale.getDefault())
                                        .format(it.trackTimeMillis),
                                    artworkUrl100 = it.artworkUrl100
                                )
                            }
                            trackAdapter.update(tracks)
                            recyclerView.visibility = View.VISIBLE
                        }else{
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

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val searchText = inputEditText.text.toString()

                performSearch(searchText)
                true
            } else {
                false
            }
        }

        refreshButton.setOnClickListener {
            val searchText = inputEditText.text.toString()
            performSearch(searchText)
        }

        clearButton.setOnClickListener {
            inputEditText.setText("")
            clearButton.visibility = View.GONE
            recyclerView.visibility = View.GONE

            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(inputEditText.windowToken, 0)
            inputEditText.clearFocus()
        }

        backButton.setNavigationOnClickListener {
            finish()
        }

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // empty
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNullOrEmpty()) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
                clearButton.visibility = clearButtonVisibility(s)
            }

            override fun afterTextChanged(s: Editable?) {
                // empty
            }
        }
        inputEditText.addTextChangedListener(simpleTextWatcher)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(TEXT_SEARCH, countValue)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        countValue = savedInstanceState.getString(TEXT_SEARCH, TEXT_DEF)
    }

    private fun clearButtonVisibility(s: CharSequence?): Int {
        return if (s.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    companion object {
        private const val TEXT_SEARCH = "TEXT_SEARCH"
        private const val TEXT_DEF = ""
    }
}