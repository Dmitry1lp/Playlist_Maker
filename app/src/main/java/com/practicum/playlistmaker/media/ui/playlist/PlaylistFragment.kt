package com.practicum.playlistmaker.media.ui.playlist

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentPlaylistBinding
import com.practicum.playlistmaker.media.ui.favorites.FavoriteTrackAdapter
import com.practicum.playlistmaker.player.ui.PlayerFragment
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.utils.debounce
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File


class PlaylistFragment: Fragment() {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!

    private lateinit var favoriteAdapter: FavoriteTrackAdapter

    private lateinit var onTrackClickDebounce: (Track) -> Unit

    private lateinit var menuBottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    private val viewModel by viewModel<PlaylistViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
            // менюшка
        menuBottomSheetBehavior = BottomSheetBehavior.from(binding.menuBottomSheet)
        menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        menuBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {

                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        binding.overlay.visibility = View.GONE
                    }
                    else -> {
                        binding.overlay.visibility = View.VISIBLE
                    }
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
        //обрабатываем нажатие
        binding.ibMenu.setOnClickListener {
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            binding.overlay.visibility = View.VISIBLE
        }
        //обработчик нажатия на оверлей
        binding.overlay.setOnClickListener {
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            binding.overlay.visibility = View.GONE
        }

        binding.ivBackButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.ibShare.setOnClickListener {
            viewModel.sharedPlaylist()
        }

        binding.btnShared.setOnClickListener {
            viewModel.sharedPlaylist()
        }

        binding.btnEdit.setOnClickListener {
            val playlistId = viewModel.playlistLiveData.value?.id ?: return@setOnClickListener

            val action =
                PlaylistFragmentDirections
                    .actionPlaylistFragmentToEditPlaylistFragment(
                        playlistId = playlistId
                    )

            findNavController().navigate(action)
        }

        binding.btnDelete.setOnClickListener {
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            binding.overlay.visibility = View.GONE
            showDeletePlaylistDialog()
        }

        favoriteAdapter = FavoriteTrackAdapter(
            tracks = emptyList(),
            onItemClick = { track ->
                onTrackClickDebounce(track)
            },
            onLongClick = { track ->
                showDeleteTrackDialog(track)
            }
        )

        binding.rvAudio.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAudio.adapter = favoriteAdapter

        onTrackClickDebounce = debounce<Track>(
            delayMillis = viewModel.clickDebounceDelay,
            scope = viewLifecycleOwner.lifecycleScope
        ) { track ->
            findNavController().navigate(
                R.id.action_playlistFragment_to_playerFragment,
                PlayerFragment.createArgs(track)
            )
        }

        val playlistId = arguments?.getLong(KEY_PLAYLIST_ID) ?: return

        viewModel.loadPlaylist(playlistId)


        viewModel.observeSharedPlaylist().observe(viewLifecycleOwner) { text ->
            text?.let {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, it)
                }
                startActivity(Intent.createChooser(sendIntent, null))
            }
        }

        viewModel.observeSharedToast().observe(viewLifecycleOwner) {
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            binding.overlay.visibility = View.GONE
            Toast.makeText(requireContext(),"В этом плейлисте нет списка треков, которым можно поделиться", Toast.LENGTH_SHORT)
                .show()
        }

        viewModel.observePlaylistLiveData().observe(viewLifecycleOwner) { playlist ->
            playlist?.let { pl ->
                binding.tvPlaylistName.text = pl.name
                binding.tvDescription.text = pl.description
                binding.tvCountTrack.text = resources.getQuantityString(R.plurals.tracks_count,pl.trackCount,pl.trackCount)

                val coverPath = pl.filePath
                if (coverPath != null) {
                    val coverFile = File(coverPath)
                    if (coverFile.exists()) {
                        Glide.with(this@PlaylistFragment)
                            .load(coverFile)
                            .placeholder(R.drawable.ic_placeholder)
                            .error(R.drawable.ic_placeholder)
                            .centerCrop()
                            .apply(
                                RequestOptions().transform(
                                RoundedCorners(
                                    resources.getDimensionPixelSize(R.dimen.radiusSize_16dp)
                                )))
                            .into(binding.ivAlbum)
                    } else {
                        Glide.with(this@PlaylistFragment)
                            .load(R.drawable.ic_placeholder)
                            .into(binding.ivAlbum)
                    }
                } else {
                    Glide.with(this@PlaylistFragment)
                        .load(R.drawable.ic_placeholder)
                        .into(binding.ivAlbum)
                }

                binding.tvMenuTitle.text = pl.name
                binding.tvMenuCount.text = resources.getQuantityString(R.plurals.tracks_count,pl.trackCount,pl.trackCount)

                val menuPath = pl.filePath
                if (menuPath != null) {
                    val menuFile = File(menuPath)
                    if(menuFile.exists()) {
                        Glide.with(this@PlaylistFragment)
                            .load(menuFile)
                            .placeholder(R.drawable.ic_placeholder)
                            .error(R.drawable.ic_placeholder)
                            .centerCrop()
                            .apply(
                                RequestOptions().transform(
                                    RoundedCorners(
                                        resources.getDimensionPixelSize(R.dimen.radiusSize_16dp)
                                    )))
                            .into(binding.ivMenuCover)
                    } else {
                        binding.ivMenuCover.setImageResource(R.drawable.ic_placeholder)
                    }
                } else {
                    binding.ivMenuCover.setImageResource(R.drawable.ic_placeholder)
                }
            }
        }

        viewModel.observePlaylistTracks().observe(viewLifecycleOwner) { tracks ->
            favoriteAdapter.update(tracks)
            if (tracks.isEmpty()) {
                binding.rvAudio.visibility = View.GONE
            } else {
                binding.rvAudio.visibility = View.VISIBLE
            }
        }

        viewModel.observePlaylistDuration().observe(viewLifecycleOwner) { minutesString ->
            val minutes = minutesString.toInt()

            binding.tvDuration.text = resources.getQuantityString(R.plurals.tracks_duration,minutes,minutes)
        }

        viewModel.observeDeletePlaylist().observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }
    }

    private fun showDeletePlaylistDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_playlist_title)
            .setMessage(R.string.delete_playlist_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete_btn) { _, _ ->
                viewModel.deletePlaylist()
            }
            .show()
    }

    private fun showDeleteTrackDialog(track: Track) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_track_title)
            .setNegativeButton(R.string.no, null)
            .setPositiveButton(R.string.yes) { _, _ ->
                viewModel.removeTrackFromPlaylist(track)
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val KEY_PLAYLIST_ID = "KEY_PLAYLIST_ID"
    }

}