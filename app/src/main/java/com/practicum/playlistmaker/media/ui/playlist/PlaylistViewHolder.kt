package com.practicum.playlistmaker.media.ui.playlist

import android.util.TypedValue
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.PlaylistViewBinding
import com.practicum.playlistmaker.media.domain.playlist.model.Playlist
import java.io.File

class PlaylistViewHolder(private val binding: PlaylistViewBinding): RecyclerView.ViewHolder(binding.root) {

    private val cornerRadius by lazy {
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            8f,
            itemView.resources.displayMetrics
        ).toInt()
    }

    fun bind(item: Playlist) {
        binding.tvPlaylistName.text = item.name
        binding.tvTrackCount.text = item.trackCount.toString()

        Glide.with(itemView.context)
            .load(item.filePath?.let { File(it) })
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_placeholder)
            .centerCrop()
            .transform(RoundedCorners(cornerRadius))
            .into(binding.ivPlaylistImage)

    }
}