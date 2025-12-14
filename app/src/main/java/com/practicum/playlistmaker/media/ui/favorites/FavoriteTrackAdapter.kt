package com.practicum.playlistmaker.media.ui.favorites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.search.domain.models.Track

class FavoriteTrackAdapter(
    private var tracks: List<Track>,
    private val onItemClick: (Track) -> Unit
) : RecyclerView.Adapter<FavoritesTrackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesTrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.favorite_track, parent, false)
        return FavoritesTrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoritesTrackViewHolder, position: Int) {
        val track = tracks[position]
        holder.bind(track)
        holder.itemView.setOnClickListener {
            onItemClick(track)
        }
    }

    override fun getItemCount(): Int {
        return tracks.size
    }

    fun update(newTracks: List<Track>) {
        tracks = newTracks
        notifyDataSetChanged()
    }
}