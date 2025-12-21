package com.practicum.playlistmaker.media.ui.media

import android.util.TypedValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.MediaPlaylistViewBinding
import com.practicum.playlistmaker.databinding.PlaylistViewBinding
import com.practicum.playlistmaker.media.domain.db.PlaylistInteractor
import com.practicum.playlistmaker.media.domain.playlist.model.Playlist
import com.practicum.playlistmaker.media.ui.playlist.PlaylistUiState
import kotlinx.coroutines.launch
import java.io.File

class MediaViewHolder(private val binding: MediaPlaylistViewBinding): RecyclerView.ViewHolder(binding.root) {

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