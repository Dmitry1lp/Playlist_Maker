package com.practicum.playlistmaker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class TrackViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    val trackLayout: ConstraintLayout =  itemView.findViewById(R.id.trackLayout)
    val trackAlbum: ImageView = itemView.findViewById(R.id.album)
    val trackName: TextView = itemView.findViewById(R.id.trackName)
    val trackGroup: TextView = itemView.findViewById(R.id.trackGroup)
    val trackTime: TextView = itemView.findViewById(R.id.trackTime)

    fun bind(item: Track) {
        trackName.text = item.trackName
        trackGroup.text = item.artistName
        trackTime.text = item.trackTime

        Glide.with(itemView.context)
            .load(item.artworkUrl100)
            .placeholder(R.drawable.agreement)
            .centerCrop()
            .transform(RoundedCorners(2))
            .into(trackAlbum)
    }
}