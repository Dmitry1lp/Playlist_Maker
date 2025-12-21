package com.practicum.playlistmaker.media.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentMediaFavoritesBinding
import com.practicum.playlistmaker.player.ui.PlayerFragment
import com.practicum.playlistmaker.search.domain.models.Track
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
        _binding = FragmentMediaFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favoriteAdapter = FavoriteTrackAdapter(emptyList()) { track ->
            onTrackClickDebounce(track)
        }

        binding.rvFavorites.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFavorites.adapter = favoriteAdapter

        onTrackClickDebounce = debounce<Track>(
            delayMillis = viewModel.clickDebounceDelay,
            scope = viewLifecycleOwner.lifecycleScope
        ) { track ->
            findNavController().navigate(
                R.id.action_mediaFragment2_to_playerFragment,
                PlayerFragment.createArgs(track)
            )
        }

        viewModel.observeFavoritesUiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FavoritesUiState.Content -> {
                    binding.rvFavorites.visibility = View.VISIBLE
                    binding.ivPlaceholder.visibility = View.GONE
                    binding.tvPlaceholder.visibility = View.GONE
                    favoriteAdapter.update(state.tracks)
                }

                is FavoritesUiState.Empty -> {
                    binding.rvFavorites.visibility = View.GONE
                    binding.ivPlaceholder.visibility = View.VISIBLE
                    binding.tvPlaceholder.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        fun newInstance() = FavoritesFragment()

    }
}