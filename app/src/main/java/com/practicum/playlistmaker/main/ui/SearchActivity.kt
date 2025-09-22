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
import androidx.lifecycle.ViewModelProvider
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
            // Прогресс
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
            binding.searchHistory.visibility = if (state.isHistoryVisible) View.VISIBLE else View.GONE
            // Иконка очистки
            binding.clearIcon.visibility = if (state.isClearTextVisible) View.VISIBLE else View.GONE
        }

        viewModel?.clearTextEvent?.observe(this) {
            binding.inputEditText.setText("")
            // Прячем клавиатуру и убираем фокус
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(binding.inputEditText.windowToken, 0)
            binding.inputEditText.clearFocus()
        }
        // Переход на другую активити
        viewModel?.observePlayerLiveData()?.observe(this) { track ->
            track?.let {
                val playerIntent = Intent(this, PlayerActivity::class.java)
                playerIntent.putExtra(KEY, it)
                startActivity(playerIntent)
            }
        }
        //Клик по треку
        val onClickTrack: (Track) -> Unit = { track ->
            viewModel?.onTrackClicked(track)
        }
        // Инициализируем адаптер с пустым списком и назначаем его RecyclerView
        searchAdapter = TrackAdapter(emptyList(), onClickTrack)
        historyAdapter = TrackAdapter(emptyList(), onClickTrack)

        binding.recyclerView.adapter = searchAdapter
        binding.rvHistory.adapter = historyAdapter

        // Обработка фокуса
        binding.inputEditText.setOnFocusChangeListener { view, hasFocus ->
            viewModel?.updateHistoryVisibility(binding.inputEditText.text.isEmpty())
        }
        binding.inputEditText.addTextChangedListener{ text ->
            viewModel?.onSearchTextChanged(text.toString())
        }
        // Обработка нажатия на кнопку "Ввод"
        binding.inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel?.performSearch(binding.inputEditText.text.toString())
                true
            } else {
                false
            }
        }
        // Обработка нажатия на кнопку "Обновить"
        binding.mbRefresh.setOnClickListener {
            val searchText = binding.inputEditText.text.toString()
            viewModel?.performSearch(searchText)
        }
        // Обработка нажатия на кнопку "Очистить историю"
        binding.mbClear.setOnClickListener {
            viewModel?.clearHistory()
        }
        // Обработка нажатия на кнопку "Очистки текста"
        binding.clearIcon.setOnClickListener {
            viewModel?.onClearTextClicked()
            // Прячем клавиатуру и убираем фокус

        }
        // Обработка нажатия на кнопку "Назад"
        binding.toolbarSearch.setNavigationOnClickListener {
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

    companion object {
        private const val TEXT_SEARCH = "TEXT_SEARCH"
        private const val KEY = "KEY_TRACK"
    }
}