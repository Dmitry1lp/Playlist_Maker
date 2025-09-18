package com.practicum.playlistmaker.main.ui

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.ActivityAudioplayerBinding
import com.practicum.playlistmaker.databinding.ActivitySearchBinding
import com.practicum.playlistmaker.player.ui.PlayerViewModel
import com.practicum.playlistmaker.search.domain.models.Track
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class PlayerActivity : AppCompatActivity() {

    private var timerHandler: Handler? = null
    private var urlTrack: String? = null
    private lateinit var audioTimeIndicator: TextView
    private lateinit var playerPlayButton: ImageButton

    private lateinit var binding: ActivityAudioplayerBinding
    private val viewModel: PlayerViewModel by viewModels {
        PlayerViewModel.getFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioplayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        audioTimeIndicator = findViewById(R.id.tv_time_indicator)
        playerPlayButton = findViewById(R.id.ib_play_button)
        val playerBackButton = findViewById<ImageButton>(R.id.iv_back_button)
        val likeButton = findViewById<ImageButton>(R.id.ib_like_button)



        timerHandler = Handler(Looper.getMainLooper())

        val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("KEY_TRACK", Track::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<Track>("KEY_TRACK")
        }

        track?.let { viewModel.setTrack(it) }

        viewModel?.observePlayerState()?.observe(this) { state ->
            binding.ibPlayButton.setImageResource(
                if (state.isPlaying) R.drawable.paused_button else R.drawable.play_button
            )
            binding.ibLikeButton.setImageResource(
                if (state.isLiked) R.drawable.like_red_button else R.drawable.like_button
            )
            binding.tvTimeIndicator.text = state.currentTime
        }

        //Обработка нажатия кнопки Назад
        playerBackButton.setOnClickListener {
            finish()
        }
        //Обработка нажатия кнопки Play
        playerPlayButton.setOnClickListener {
            viewModel.onPlayClicked()
        }
        //Обработка нажатия кнопки Like
        likeButton.setOnClickListener {
            viewModel.onLikeClicked()
        }

        track?.let {
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


        fun onPause() {
            super.onPause()
            viewModel.onPause()
        }

        fun onDestroy() {
            super.onDestroy()
            viewModel.onDestroy()
        }
    }
}