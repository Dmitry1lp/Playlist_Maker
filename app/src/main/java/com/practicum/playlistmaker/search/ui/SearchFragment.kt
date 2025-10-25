package com.practicum.playlistmaker.search.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentSearchBinding
import com.practicum.playlistmaker.player.ui.PlayerFragment
import com.practicum.playlistmaker.search.domain.models.Track
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment: Fragment() {

    private val viewModel by viewModel<SearchViewModel>()
    private lateinit var binding: FragmentSearchBinding
    private lateinit var searchAdapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.observeSearchUiStateLiveData.observe(viewLifecycleOwner) { state ->
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

        viewModel.clearTextEvent.observe(viewLifecycleOwner) {
            binding.inputEditText.setText("")
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(binding.inputEditText.windowToken, 0)
            binding.inputEditText.clearFocus()
        }

        viewModel.observePlayerLiveData().observe(viewLifecycleOwner) { track ->
            track?.let {
                findNavController().navigate(R.id.action_searchFragment2_to_playerFragment,
                    PlayerFragment.createArgs(it))
                viewModel.observePlayerLiveData().value = null
            }
        }

        val onClickTrack: (Track) -> Unit = { track ->
            viewModel.onTrackClicked(track)
        }

        searchAdapter = TrackAdapter(emptyList(), onClickTrack)
        historyAdapter = TrackAdapter(emptyList(), onClickTrack)

        binding.recyclerView.adapter = searchAdapter
        binding.rvHistory.adapter = historyAdapter

        binding.inputEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                viewModel.updateHistoryVisibility(binding.inputEditText.text.isEmpty())
            }
        }
        binding.inputEditText.addTextChangedListener { text ->
            viewModel.onSearchTextChanged(text.toString())
        }

        binding.inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.performSearch(binding.inputEditText.text.toString())
                true
            } else {
                false
            }
        }

        binding.mbRefresh.setOnClickListener {
            val searchText = binding.inputEditText.text.toString()
            viewModel.performSearch(searchText)
        }

        binding.mbClear.setOnClickListener {
            viewModel.clearHistory()
        }

        binding.clearIcon.setOnClickListener {
            viewModel.onClearTextClicked()
        }

        binding.toolbarSearch.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        savedInstanceState?.getString(TEXT_SEARCH)?.let { text ->
            binding.inputEditText.setText(text)
            binding.inputEditText.setSelection(text.length)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::binding.isInitialized) {
            outState.putString(TEXT_SEARCH, binding.inputEditText.text.toString())
        }
    }

    companion object {
        private const val TEXT_SEARCH = "TEXT_SEARCH"
        private const val KEY = "KEY_TRACK"
    }
}
