package com.practicum.playlistmaker.search.ui

import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentSearchBinding
import com.practicum.playlistmaker.player.ui.PlayerFragment
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.settings.ui.PlaylistMakerTheme
import com.practicum.playlistmaker.utils.BroadcastReceiverConnection
import com.practicum.playlistmaker.utils.debounce
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment: Fragment() {

    private val viewModel by viewModel<SearchViewModel>()
    private lateinit var binding: FragmentSearchBinding
    private lateinit var searchAdapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter
    private val connectionReceiver = BroadcastReceiverConnection()
    private lateinit var onTrackClickDebounce: (Track) -> Unit

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )

            setContent {
                PlaylistMakerTheme() {
                 SearchScreen(
                     viewModel = viewModel,
                     onTrackClick = { track ->
                         viewModel.onTrackClicked(track)
                         findNavController().navigate(
                             R.id.action_searchFragment2_to_playerFragment,
                             PlayerFragment.createArgs(track)
                         )
                     }
                 )
             }
            }
        }
    }




    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::binding.isInitialized) {
            outState.putString(TEXT_SEARCH, binding.inputEditText.text.toString())
        }
    }

    override fun onResume() {
        super.onResume()

        ContextCompat.registerReceiver(
            requireContext(),
            connectionReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(connectionReceiver)
    }

    companion object {
        private const val TEXT_SEARCH = "TEXT_SEARCH"
        private const val KEY = "KEY_TRACK"
    }
}
