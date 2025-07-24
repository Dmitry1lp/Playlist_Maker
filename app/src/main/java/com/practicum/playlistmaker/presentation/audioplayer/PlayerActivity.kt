package com.practicum.playlistmaker.presentation.audioplayer

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.data.network.request.Track

class PlayerActivity : AppCompatActivity() {

    private var isPlaying = false
    private var isLiked = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audioplayer)

        val playerBackButton = findViewById<ImageButton>(R.id.iv_back_button)
        val playerPlayButton = findViewById<ImageButton>(R.id.ib_play_button)
        val likeButton = findViewById<ImageButton>(R.id.ib_like_button)
        val audioAlbumImage = findViewById<ImageView>(R.id.iv_album)
        val audioTrackName = findViewById<TextView>(R.id.tv_trackName)
        val audioArtistName = findViewById<TextView>(R.id.tv_groupName)
        val audioTrackTime = findViewById<TextView>(R.id.tv_duration)
        val audioAlbumName = findViewById<TextView>(R.id.tv_album)
        val audioYear = findViewById<TextView>(R.id.tv_year)
        val audioGenre = findViewById<TextView>(R.id.tv_genre)
        val audioCountry = findViewById<TextView>(R.id.tv_country)
        val audioTimeIndicator = findViewById<TextView>(R.id.tv_time_indicator)

        val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("KEY_TRACK", Track::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<Track>("KEY_TRACK")
        }

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


        //Обработка нажатия кнопки Назад
        playerBackButton.setOnClickListener {
            finish()
        }
        //Обработка нажатия кнопки Play
        playerPlayButton.setOnClickListener {
            isPlaying = !isPlaying
            if (isPlaying) {
                playerPlayButton.setImageResource(R.drawable.paused_button)
            } else {
                playerPlayButton.setImageResource(R.drawable.play_button)
            }
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
    }
}