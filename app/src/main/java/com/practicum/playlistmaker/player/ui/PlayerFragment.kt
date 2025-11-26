package com.practicum.playlistmaker.player.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentAudioplayerBinding
import com.practicum.playlistmaker.search.domain.models.Track
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PlayerFragment: Fragment() {

    private lateinit var binding: FragmentAudioplayerBinding

    private val track: Track by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getParcelable(KEY_TRACK, Track::class.java)
        } else {
            @Suppress("DEPRECATION")
            requireArguments().getParcelable<Track>(KEY_TRACK)
        } ?: throw IllegalArgumentException("Track is null")
    }

    private val viewModel: PlayerViewModel by viewModel {
        parametersOf(track)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAudioplayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.observePlayerUiState().observe(viewLifecycleOwner) { state ->
            binding.ibLikeButton.setImageResource(
                if(state.isLiked) R.drawable.like_red_button else R.drawable.like_button
            )
        }

        viewModel.observePlayerState().observe(viewLifecycleOwner) { state ->
            binding.tvTimeIndicator.text = state.progress

            // Меняем кнопку в зависимости от состояния
            when (state) {
                is PlayerState.Playing -> {
                    binding.ibPlayButton.setImageResource(R.drawable.paused_button)
                }
                is PlayerState.Paused,
                is PlayerState.Prepared,
                is PlayerState.Default -> {
                    binding.ibPlayButton.setImageResource(R.drawable.play_button)
                }
            }
            binding.ibPlayButton.isEnabled = state.isPlayButtonEnabled
        }

        binding.ivBackButton.setOnClickListener {
            findNavController().navigateUp()
        }
        // Обработка нажатия кнопки Play
        binding.ibPlayButton.setOnClickListener {
            viewModel.onPlayButtonClicked()
        }
        //Обработка нажатия кнопки Like
        binding.ibLikeButton.setOnClickListener {
            viewModel.onFavoriteClicked()
        }

        track.let {
            Glide.with(this)
                .load(it.artworkUrl100?.replace("100x100bb.jpg", "512x512bb.jpg"))
                .placeholder(R.drawable.ic_placeholder)
                .centerCrop()
                .into(binding.ivAlbum)

            binding.tvTrackName.text = it.trackName
            binding.tvGroupName.text = it.artistName
            binding.tvDuration.text = it.trackTime
            binding.tvAlbum.text = it.collectionName ?: ""
            binding.tvYear.text = it.releaseDate
            binding.tvGenre.text = it.primaryGenreName
            binding.tvCountry.text = it.country
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onDestroy()
    }

    companion object {
        private const val KEY_TRACK = "KEY_TRACK"

        fun createArgs(track: Track): Bundle =
            Bundle().apply {
                putParcelable(KEY_TRACK, track)
            }
    }
}