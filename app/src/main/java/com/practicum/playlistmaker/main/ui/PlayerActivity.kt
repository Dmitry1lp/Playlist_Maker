package com.practicum.playlistmaker.main.ui

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.ActivityAudioplayerBinding
import com.practicum.playlistmaker.player.ui.PlayerViewModel
import com.practicum.playlistmaker.search.domain.models.Track


class PlayerActivity : AppCompatActivity() {

    private var timerHandler: Handler? = null
    private lateinit var audioTimeIndicator: TextView
    private lateinit var playerPlayButton: ImageButton
    lateinit var viewModel: PlayerViewModel

    private lateinit var binding: ActivityAudioplayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAudioplayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById<android.view.View>(android.R.id.content)) { view, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = statusBar.top)
            insets
        }

        audioTimeIndicator = findViewById(R.id.tv_time_indicator)
        playerPlayButton = findViewById(R.id.ib_play_button)
        val playerBackButton = findViewById<ImageButton>(R.id.iv_back_button)
        val likeButton = findViewById<ImageButton>(R.id.ib_like_button)

        timerHandler = Handler(Looper.getMainLooper())

        val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(KEY_TRACK, Track::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<Track>(KEY_TRACK)
        } ?: return finish()

        viewModel = ViewModelProvider(this, PlayerViewModel.getFactory(track))
            .get(PlayerViewModel::class.java)
        // Обработка кнопок проигрывания и лайка
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

    }
}