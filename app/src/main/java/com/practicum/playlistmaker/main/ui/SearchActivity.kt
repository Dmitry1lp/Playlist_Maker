package com.practicum.playlistmaker.main.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.addTextChangedListener
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.ActivitySearchBinding
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.search.ui.SearchViewModel
import com.practicum.playlistmaker.search.ui.TrackAdapter
import com.practicum.playlistmaker.search.ui.TracksState
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchActivity : AppCompatActivity() {

    private val viewModel by viewModel<SearchViewModel>()
    private var searchText: String = ""

    private lateinit var binding: ActivitySearchBinding
    private lateinit var searchAdapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById<android.view.View>(android.R.id.content)) { view, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = statusBar.top)
            insets
        }

        viewModel?.observeSearchUiStateLiveData?.observe(this) { state ->
            binding.recyclerView.visibility = View.GONE
            binding.placeholderNotInternet.visibility = View.GONE
            binding.placeholderNotFound.visibility = View.GONE
            binding.pbProgressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

            // Контент поиска
            when (state.tracksState) {
                is TracksState.Content -> {
                    searchAdapter.update(state.tracksState.tracks)
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.placeholderNotInternet.visibility = View.GONE
                    binding.placeholderNotFound.visibility = View.GONE
                }
                is TracksState.ErrorInternet -> {
                    binding.recyclerView.visibility = View.GONE
                    binding.placeholderNotInternet.visibility = View.VISIBLE
                    binding.placeholderNotFound.visibility = View.GONE
                }
                is TracksState.ErrorFound -> {
                    binding.recyclerView.visibility = View.GONE
                    binding.placeholderNotInternet.visibility = View.GONE
                    binding.placeholderNotFound.visibility = View.VISIBLE
                }
                is TracksState.Empty -> {
                    binding.recyclerView.visibility = View.GONE
                    binding.placeholderNotInternet.visibility = View.GONE
                    binding.placeholderNotFound.visibility = View.GONE
                }
                is TracksState.Loading -> {}
            }

            // История
            historyAdapter.update(state.history)
            binding.searchHistory.visibility = if (state.isHistoryVisible && state.history.isNotEmpty()) View.VISIBLE else View.GONE
            binding.clearIcon.visibility = if (state.isClearTextVisible) View.VISIBLE else View.GONE
        }

        viewModel?.clearTextEvent?.observe(this) {
            binding.inputEditText.setText("")
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(binding.inputEditText.windowToken, 0)
            binding.inputEditText.clearFocus()
        }

        viewModel?.observePlayerLiveData()?.observe(this) { track ->
            track?.let {
                val playerIntent = Intent(this, PlayerActivity::class.java)
                playerIntent.putExtra(KEY, it)
                startActivity(playerIntent)
            }
        }

        val onClickTrack: (Track) -> Unit = { track ->
            viewModel?.onTrackClicked(track)
        }

        searchAdapter = TrackAdapter(emptyList(), onClickTrack)
        historyAdapter = TrackAdapter(emptyList(), onClickTrack)

        binding.recyclerView.adapter = searchAdapter
        binding.rvHistory.adapter = historyAdapter

        binding.inputEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                viewModel?.updateHistoryVisibility(binding.inputEditText.text.isEmpty())
            }
        }
        binding.inputEditText.addTextChangedListener { text ->
            viewModel?.onSearchTextChanged(text.toString())
        }

        binding.inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel?.performSearch(binding.inputEditText.text.toString())
                true
            } else {
                false
            }
        }

        binding.mbRefresh.setOnClickListener {
            val searchText = binding.inputEditText.text.toString()
            viewModel?.performSearch(searchText)
        }

        binding.mbClear.setOnClickListener {
            viewModel?.clearHistory()
        }

        binding.clearIcon.setOnClickListener {
            viewModel?.onClearTextClicked()
        }

        binding.toolbarSearch.setNavigationOnClickListener {
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(TEXT_SEARCH, searchText)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchText = savedInstanceState.getString(TEXT_SEARCH, "")
        findViewById<EditText>(R.id.inputEditText).setText(searchText)
    }

    companion object {
        private const val TEXT_SEARCH = "TEXT_SEARCH"
        private const val KEY = "KEY_TRACK"
    }
}