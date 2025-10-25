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

class PlayerFragment: Fragment() {

    private lateinit var viewModel: PlayerViewModel
    private lateinit var binding: FragmentAudioplayerBinding

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

        val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getParcelable(KEY_TRACK, Track::class.java)
        } else {
            @Suppress("DEPRECATION")
            requireArguments().getParcelable<Track>(KEY_TRACK)
        } ?: return

        viewModel = ViewModelProvider(this, PlayerViewModel.getFactory(track))
            .get(PlayerViewModel::class.java)

        // Обработка кнопок проигрывания и лайка
        viewModel.observePlayerState().observe(viewLifecycleOwner) { state ->
            binding.ibPlayButton.setImageResource(
                if (state.isPlaying) R.drawable.paused_button else R.drawable.play_button
            )
            binding.ibLikeButton.setImageResource(
                if (state.isLiked) R.drawable.like_red_button else R.drawable.like_button
            )
            binding.tvTimeIndicator.text = state.currentTime
        }

        binding.ivBackButton.setOnClickListener {
            findNavController().navigateUp()
        }
        //Обработка нажатия кнопки Play
        binding.ibPlayButton.setOnClickListener {
            viewModel.onPlayClicked()
        }
        //Обработка нажатия кнопки Like
        binding.ibLikeButton.setOnClickListener {
            viewModel.onLikeClicked()
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