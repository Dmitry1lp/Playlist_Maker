package com.practicum.playlistmaker.media.ui.playlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.practicum.playlistmaker.R
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.practicum.playlistmaker.databinding.FragmentMediaPlaylistsBinding
import com.practicum.playlistmaker.media.ui.FavoritesScreen
import com.practicum.playlistmaker.media.ui.PlaylistsScreen
import com.practicum.playlistmaker.player.ui.PlayerFragment
import com.practicum.playlistmaker.settings.ui.PlaylistMakerTheme
import org.koin.androidx.viewmodel.ext.android.viewModel


class PlaylistsFragment : Fragment() {

    private val viewModel by viewModel<PlaylistsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {

                PlaylistMakerTheme(){
                    PlaylistsScreen(
                        viewModel = viewModel,

                        onPlaylistClick = { playlistId ->
                            findNavController().navigate(
                                R.id.action_mediaFragment2_to_playlistFragment,
                                Bundle().apply {
                                    putLong(KEY_PLAYLIST_ID, playlistId)
                                }
                            )
                        },

                        onNewPlaylistClick = {
                            findNavController().navigate(
                                R.id.action_mediaFragment2_to_favoritesInfoFragment
                            )
                        }
                    )
                }
            }
        }
    }

    companion object {
        private const val KEY_PLAYLIST_ID = "KEY_PLAYLIST_ID"
        fun newInstance() = PlaylistsFragment()
    }
}