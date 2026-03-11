package com.practicum.playlistmaker.media.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import androidx.compose.ui.res.pluralStringResource
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.media.domain.playlist.model.Playlist
import com.practicum.playlistmaker.media.ui.playlist.PlaylistsUiState
import com.practicum.playlistmaker.media.ui.playlist.PlaylistsViewModel
import java.io.File


@Composable
fun PlaylistsScreen(
    viewModel: PlaylistsViewModel,
    onPlaylistClick: (Long) -> Unit,
    onNewPlaylistClick: () -> Unit
) {
    val state by viewModel.playlistUiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.getPlaylists()
    }

    Column {
        Button(
            onClick = onNewPlaylistClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .wrapContentWidth(Alignment.CenterHorizontally)
        ) {
            Text(text = stringResource(R.string.new_playlist))
        }

        when (val currentState = state) {

            is PlaylistsUiState.Content -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(currentState.playlists) { playlist ->
                    PlaylistItem(playlist) { onPlaylistClick(playlist.id) }
                }

            }
            is PlaylistsUiState.Empty -> Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_not_found),
                    contentDescription = null,
                    modifier = Modifier.padding(top = 46.dp)
                )
                Text(
                    text = stringResource(R.string.playlists_null),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                    )
            }
        }
    }
}

@Composable
fun PlaylistItem(playlist: Playlist, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        val imageFile = playlist.filePath?.let { File(it) }

        if (imageFile != null && imageFile.exists()) {
            AsyncImage(
                model = imageFile,
                contentDescription = playlist.name,
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center
            )
        } else {
            Image(
                painter = painterResource(R.drawable.ic_placeholder),
                contentDescription = playlist.name,
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center
            )
        }

        Text(
            text = playlist.name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = pluralStringResource(
                id = R.plurals.tracks_count,
                count = playlist.trackCount,
                playlist.trackCount
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}