package com.practicum.playlistmaker.media.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentMediaFavoritesBinding
import com.practicum.playlistmaker.media.ui.FavoritesScreen
import com.practicum.playlistmaker.player.ui.PlayerFragment
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.settings.ui.PlaylistMakerTheme
import com.practicum.playlistmaker.utils.debounce
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoritesFragment : Fragment() {

    private val viewModel by viewModel<FavoriteTrackViewModel>()

    private lateinit var favoriteAdapter: FavoriteTrackAdapter

    private lateinit var onTrackClickDebounce: (Track) -> Unit

    private var _binding: FragmentMediaFavoritesBinding? = null
    private val binding get() = _binding!!

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
                    FavoritesScreen(
                        viewModel = viewModel,
                        onTrackClick = { track ->

                            findNavController().navigate(
                                R.id.action_mediaFragment2_to_playerFragment,
                                PlayerFragment.createArgs(track)
                            )

                        }
                    )
                }
            }
        }
    }

    companion object {
        fun newInstance() = FavoritesFragment()
    }
}