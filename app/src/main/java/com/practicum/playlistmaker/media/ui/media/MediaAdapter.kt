package com.practicum.playlistmaker.media.ui.media

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.databinding.MediaPlaylistViewBinding
import com.practicum.playlistmaker.media.domain.playlist.model.Playlist

class MediaAdapter(
    private var playlists: List<Playlist>,
    private val onPlaylistClick: (Playlist) -> Unit
): RecyclerView.Adapter<MediaViewHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): MediaViewHolder {
            val binding = MediaPlaylistViewBinding.inflate(LayoutInflater.from(parent.context))
            return MediaViewHolder(binding)
        }

        override fun onBindViewHolder(
            holder: MediaViewHolder,
            position: Int
        ) {
            val playlist = playlists[position]
            holder.bind(playlist)
            holder.itemView.setOnClickListener {
                onPlaylistClick(playlist)
            }
        }

        override fun getItemCount(): Int {
            return playlists.size
        }

        fun update(newPlaylists: List<Playlist>) {
            playlists = newPlaylists
            notifyDataSetChanged()
        }
}