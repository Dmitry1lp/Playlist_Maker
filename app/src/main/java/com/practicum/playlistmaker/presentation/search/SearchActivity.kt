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

    private val results = ArrayList<TrackDto>()

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

        Log.d("WeatherActivity", "1 вызван для:")
        trackAdapter = TrackAdapter(emptyList())
        recyclerView.adapter = trackAdapter


        Log.d("WeatherActivity", "2 вызван для:")
        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val searchText = inputEditText.text.toString()
                Log.d("WeatherActivity", "3 вызван для:")
                iTunesService.search(searchText).enqueue(object: Callback<TrackResponse> {
                    override fun onResponse(
                        call: Call<TrackResponse>,
                        response: Response<TrackResponse>
                    ) { Log.d("WeatherActivity", "4 вызван для:")
                        if(response.isSuccessful && response.body() != null){
                            val trackList = response.body()!!.results

                            if (trackList.isNotEmpty()) {

                            Log.d("WeatherActivity", "5 вызван для:${response.body()}")
                            recyclerView.visibility = View.VISIBLE
                            placeholderNotFound.visibility = View.GONE
                            placeholderNotInternet.visibility = View.GONE

                            Log.d("WeatherActivity", "6 вызван для:")
                            val tracks = trackList.map{
                                Log.d("WeatherActivity", "7 вызван для:")
                                Track(
                                trackName = it.trackName,
                                artistName = it.artistName,
                                trackTime = SimpleDateFormat("mm:ss", Locale.getDefault())
                                    .format(it.trackTimeMillis),
                                artworkUrl100 = it.artworkUrl100
                            )
                            }
                            Log.d("WeatherActivity", "8 вызван для:")
                            trackAdapter.update(tracks)
                            Log.d("WeatherActivity", "9 вызван для:")
                            recyclerView.visibility = View.VISIBLE
                                }else{
                                Log.d("WeatherActivity", "10 вызван для:")
                                placeholderNotFound.visibility = View.VISIBLE
                                placeholderNotInternet.visibility = View.GONE
                                recyclerView.visibility = View.GONE
                            }
                        }
                    }

                    override fun onFailure(call: Call<TrackResponse>, t: Throwable) {
                        Log.d("WeatherActivity", "11 вызван для:")
                        placeholderNotFound.visibility = View.GONE
                        placeholderNotInternet.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    }

                }
                )
                true
            }
            false
        }

        clearButton.setOnClickListener {
            inputEditText.setText("")
            clearButton.visibility = View.GONE

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
        const val TEXT_SEARCH = "TEXT_SEARCH"
        const val TEXT_DEF = ""
    }
}