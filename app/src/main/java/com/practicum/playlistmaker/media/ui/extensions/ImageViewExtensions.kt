package com.practicum.playlistmaker.media.ui.extensions

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.practicum.playlistmaker.R
import java.io.File

fun ImageView.loadPlaylistCover(
    filePath: String?,
    cornerRadius: Int
) {
    val source = if (filePath != null) {
        val file = File(filePath)
        if (file.exists()) file else R.drawable.ic_placeholder
    } else {
        R.drawable.ic_placeholder
    }

    Glide.with(this)
        .load(source)
        .placeholder(R.drawable.ic_placeholder)
        .error(R.drawable.ic_placeholder)
        .centerCrop()
        .apply(
            RequestOptions().transform(
                RoundedCorners(cornerRadius)
            )
        )
        .into(this)
}
