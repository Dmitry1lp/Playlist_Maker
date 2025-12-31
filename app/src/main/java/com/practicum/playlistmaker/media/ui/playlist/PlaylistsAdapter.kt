package com.practicum.playlistmaker.media.ui.playlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.databinding.PlaylistViewBinding
import com.practicum.playlistmaker.media.domain.playlist.model.Playlist

class PlaylistsAdapter(
    private var playlists: List<Playlist>,
    private val onClick: (Playlist) -> Unit
): RecyclerView.Adapter<PlaylistsViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlaylistsViewHolder {
        val binding = PlaylistViewBinding.inflate(LayoutInflater.from(parent.context))
        return PlaylistsViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: PlaylistsViewHolder,
        position: Int
    ) {
        val playlist = playlists[position]
        holder.bind(playlist)
        holder.itemView.setOnClickListener {
            onClick(playlist)
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