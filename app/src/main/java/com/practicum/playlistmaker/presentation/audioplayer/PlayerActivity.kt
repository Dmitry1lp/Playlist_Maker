package com.practicum.playlistmaker.presentation.audioplayer

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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.data.network.request.Track
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class PlayerActivity : AppCompatActivity() {


    private var isLiked = false
    private var timerHandler: Handler? = null
    private var playerState = STATE_DEFAULT
    private var urlTrack : String? = null
    private lateinit var audioTimeIndicator : TextView
    private lateinit var playerPlayButton: ImageButton
    private var mediaPlayer = MediaPlayer()
    private var durationMillis: Long = 0L
    private var currentPosition: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audioplayer)

        audioTimeIndicator = findViewById(R.id.tv_time_indicator)
        playerPlayButton = findViewById(R.id.ib_play_button)
        val playerBackButton = findViewById<ImageButton>(R.id.iv_back_button)
        val likeButton = findViewById<ImageButton>(R.id.ib_like_button)
        val audioAlbumImage = findViewById<ImageView>(R.id.iv_album)
        val audioTrackName = findViewById<TextView>(R.id.tv_trackName)
        val audioArtistName = findViewById<TextView>(R.id.tv_groupName)
        val audioTrackTime = findViewById<TextView>(R.id.tv_duration)
        val audioAlbumName = findViewById<TextView>(R.id.tv_album)
        val audioYear = findViewById<TextView>(R.id.tv_year)
        val audioGenre = findViewById<TextView>(R.id.tv_genre)
        val audioCountry = findViewById<TextView>(R.id.tv_country)


        timerHandler = Handler(Looper.getMainLooper())

        val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("KEY_TRACK", Track::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<Track>("KEY_TRACK")
        }

        urlTrack = track?.previewUrl
        audioCountry.text = track?.country
        audioGenre.text = track?.primaryGenreName
        audioYear.text = track?.releaseDate
        audioAlbumName.text = if (track?.collectionName.isNullOrBlank()) "" else track?.collectionName
        audioTrackTime.text = track?.trackTime
        audioArtistName.text = track?.artistName
        audioTrackName.text = track?.trackName
        audioTimeIndicator.text = track?.trackTime

        fun dpToPx(dp: Float, context: Context): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.resources.displayMetrics).toInt()
        }

        val radiusView = dpToPx(2f, applicationContext)
        Glide.with(applicationContext)
            .load(track?.artworkUrl100?.replace("100x100bb.jpg", "512x512bb.jpg"))
            .placeholder(R.drawable.ic_placeholder)
            .centerCrop()
            .transform(RoundedCorners(radiusView))
            .into(audioAlbumImage)

        //Переменная с длительностью
        val trackTimeString = track?.trackTime
        val parts = trackTimeString?.split(":")
        val minutes = parts?.getOrNull(0)?.toLongOrNull() ?: 0L
        val seconds = parts?.getOrNull(1)?.toLongOrNull() ?: 0L
        durationMillis = (minutes * 60 + seconds) * 1000

        //Обработка нажатия кнопки Назад
        playerBackButton.setOnClickListener {
            finish()
        }
        //Обработка нажатия кнопки Play
        playerPlayButton.setOnClickListener {
            playbackControl()
        }
        //Обработка нажатия кнопки Like
        likeButton.setOnClickListener {
            isLiked = !isLiked
            if(isLiked) {
                likeButton.setImageResource(R.drawable.like_red_button)
            } else {
                likeButton.setImageResource(R.drawable.like_button)
            }
        }
        preparePlayer()
    }
    override fun onPause() {
        super.onPause()
        pausePlayer()
        timerHandler?.removeCallbacksAndMessages(null)
    }
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        timerHandler?.removeCallbacksAndMessages(null)
    }
    private fun playbackControl() {
        when(playerState) {
            STATE_PLAYING -> {
                pausePlayer()
            }
            STATE_PREPARED, STATE_PAUSED -> {
                startPlayer()
            }
        }
    }
    private fun startPlayer() {
        mediaPlayer.start()
        startTimeIndicator()
        if (currentPosition > 0) {
            mediaPlayer.seekTo(currentPosition.toInt())
        }
        playerPlayButton.setImageResource(R.drawable.paused_button)
        playerState = STATE_PLAYING
    }

    private fun pausePlayer() {
        mediaPlayer.pause()
        playerPlayButton.setImageResource(R.drawable.play_button)
        playerState = STATE_PAUSED
        currentPosition = mediaPlayer?.currentPosition?.toLong() ?: 0L
        timerHandler?.removeCallbacksAndMessages(null)
    }

    private fun preparePlayer() {
        mediaPlayer.setDataSource(urlTrack)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            playerPlayButton.isEnabled = true
            playerState = STATE_PREPARED
        }
        mediaPlayer.setOnCompletionListener {
            playerState = STATE_PREPARED
            val sdf = SimpleDateFormat("m:ss", Locale.getDefault())
            val date = Date(0)
            audioTimeIndicator?.text = sdf.format(date)
            currentPosition = 0L
            timerHandler?.removeCallbacksAndMessages(null)
            playerPlayButton.setImageResource(R.drawable.play_button)
        }
    }

    private fun startTimeIndicator() {
        timerHandler?.post(updateTimeIndicator())
    }

    private fun updateTimeIndicator(): Runnable {
        return object : Runnable {
            override fun run() {
                // Сколько прошло времени с момента запуска
                val elapsedTime = mediaPlayer.currentPosition.toLong()
                // Сколько осталось до конца
                val remainingTime = durationMillis - elapsedTime

                if(remainingTime > 0) {
                    val seconds = elapsedTime / REFRESH_DELAY_MILLIS
                    audioTimeIndicator?.text = String.format("%02d:%02d", seconds / 60, seconds % 60)
                    timerHandler?.postDelayed(this,REFRESH_DELAY_MILLIS)
                } else {
                    val sdf = SimpleDateFormat("m:ss", Locale.getDefault())
                    val date = Date(0)
                    audioTimeIndicator?.text = sdf.format(date)
                    playerState = STATE_PREPARED
                    playerPlayButton.setImageResource(R.drawable.play_button)
                }
            }
        }
    }

    companion object {
        private const val REFRESH_DELAY_MILLIS = 1000L
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
    }
}