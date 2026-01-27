package com.practicum.playlistmaker.media.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.media.domain.playlist.model.Playlist
import com.practicum.playlistmaker.media.ui.extensions.loadPlaylistCover
import com.practicum.playlistmaker.media.ui.favorites.CreatePlaylistState
import com.practicum.playlistmaker.media.ui.favorites.FavoritesInfoFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class EditPlaylistFragment: FavoritesInfoFragment() {

    private var playlistId: Long? = null

    override val viewModel: EditPlaylistViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments
        val id = args?.getLong("playlistId", -1L) ?: -1L
        playlistId = if (id == -1L) null else id

        if (playlistId != null) {
            binding.btnCreate.text = getString(R.string.edit_playlist_save_button)
            binding.toolbarFavorites.title = getString(R.string.edit_playlist_title)
        } else {
            binding.btnCreate.text = getString(R.string.create)
            binding.toolbarFavorites.title = getString(R.string.new_playlist)
        }

        binding.ivBackButton.setOnClickListener {
            if (toSavedData() && playlistId == null) {
                toShowDialog()
            } else {
                findNavController().navigateUp()
            }
        }

        binding.ibImage.apply {
            outlineProvider = ViewOutlineProvider.BACKGROUND
            clipToOutline = true
        }

        viewModel.name.observe(viewLifecycleOwner) { name ->
            binding.etName.setText(name)
        }

        viewModel.description.observe(viewLifecycleOwner) { description ->
            binding.etDescription.setText(description)
        }

        viewModel.coverPath.observe(viewLifecycleOwner) { path ->
            binding.ibImage.loadPlaylistCover(path,
                resources.getDimensionPixelSize(R.dimen.radiusSize_16dp)
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is CreatePlaylistState.Success -> { findNavController().navigateUp() }
                    is CreatePlaylistState.Error -> { Toast.makeText(requireContext(),state.message, Toast.LENGTH_SHORT).show() }
                    is CreatePlaylistState.Editing -> {}
                    is CreatePlaylistState.Empty -> {}
                    is CreatePlaylistState.Loading -> {}
                }
            }
        }
    }

    override fun savePlaylist() {
        val name = binding.etName.text.toString().trim()
        val description = binding.etDescription.text?.toString()?.trim().orEmpty()
        val imagePath = selectedImageUri?.let { copyImage(it, name.replace(" ", "_")) }

        if (playlistId != null && viewModel.currentPlaylist.value != null) {
            viewModel.savePlaylist(
                name = name,
                description = description,
                coverPath = imagePath
            )
        } else {
            val playlist = Playlist(
                id = 0,
                name = name,
                description = description,
                filePath = imagePath,
                trackId = emptyList(),
                trackCount = 0
            )
            viewModel.createPlaylist(playlist)
        }
    }
}