package com.practicum.playlistmaker.media.ui.playlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.practicum.playlistmaker.R
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.practicum.playlistmaker.databinding.FragmentMediaPlaylistsBinding
import org.koin.androidx.viewmodel.ext.android.viewModel


class PlaylistsFragment : Fragment() {

    private lateinit var playlistAdapter: PlaylistsAdapter
    private val viewModel by viewModel<PlaylistsViewModel>()

    private var _binding: FragmentMediaPlaylistsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMediaPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getPlaylists()

        playlistAdapter = PlaylistsAdapter(emptyList()) { playlist ->
            viewModel.onPlaylistClicked(playlist.id)
        }

        binding.rvRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvRecyclerView.adapter = playlistAdapter

        binding.mbNewPlaylist.setOnClickListener {
            findNavController().navigate(
                R.id.action_mediaFragment2_to_favoritesInfoFragment
            )
        }

        viewModel.observeOpenPlaylist().observe(viewLifecycleOwner) { playlistId ->
            playlistId?.let {
                findNavController().navigate(
                    R.id.action_mediaFragment2_to_playlistFragment,
                    Bundle().apply {
                        putLong(KEY_PLAYLIST_ID, it)
                    }
                )
                viewModel.observeOpenPlaylist().value = null
            }
        }

        viewModel.observePlaylistUiState.observe(viewLifecycleOwner){ state ->
            when(state) {
                is PlaylistsUiState.Content -> {
                    playlistAdapter.update(state.playlists)
                    binding.rvRecyclerView.visibility = View.VISIBLE
                    binding.ivPlaceholderMedia.visibility = View.GONE
                    binding.tvPlaylistPlaceholder.visibility = View.GONE
                }
                is PlaylistsUiState.Empty -> {
                    binding.rvRecyclerView.visibility = View.GONE
                    binding.ivPlaceholderMedia.visibility = View.VISIBLE
                    binding.tvPlaylistPlaceholder.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val KEY_PLAYLIST_ID = "KEY_PLAYLIST_ID"
        fun newInstance() = PlaylistsFragment()
    }
}