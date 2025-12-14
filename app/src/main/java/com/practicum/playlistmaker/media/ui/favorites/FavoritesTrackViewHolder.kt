package com.practicum.playlistmaker.media.ui.favorites

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.search.domain.models.Track

class FavoritesTrackViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    val trackAlbum: ImageView = itemView.findViewById(R.id.album)
    val trackName: TextView = itemView.findViewById(R.id.trackName)
    val trackGroup: TextView = itemView.findViewById(R.id.trackGroup)
    val trackTime: TextView = itemView.findViewById(R.id.trackTime)

    fun bind(item: Track) {
        trackName.text = item.trackName
        trackGroup.text = item.artistName
        trackTime.text = item.trackTime


        fun dpToPx(dp: Float, context: Context): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.resources.displayMetrics).toInt()
        }

        val radiusView = dpToPx(2f, itemView.context)

        Glide.with(itemView.context)
            .load(item.artworkUrl100)
            .placeholder(R.drawable.ic_placeholder)
            .centerCrop()
            .transform(RoundedCorners(radiusView))
            .into(trackAlbum)
    }
}