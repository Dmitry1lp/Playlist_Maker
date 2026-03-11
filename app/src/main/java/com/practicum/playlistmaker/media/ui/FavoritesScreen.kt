package com.practicum.playlistmaker.media.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import androidx.compose.foundation.combinedClickable
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.media.ui.favorites.FavoriteTrackViewModel
import com.practicum.playlistmaker.media.ui.favorites.FavoritesUiState
import com.practicum.playlistmaker.search.domain.models.Track


@Composable
fun FavoritesScreen(
    viewModel: FavoriteTrackViewModel,
    onTrackClick: (Track) -> Unit
) {

    val state by viewModel.favoritesUiState.collectAsStateWithLifecycle()
    var trackToDelete by remember { mutableStateOf<Track?>(null) }

    when (val currentState = state) {

        is FavoritesUiState.Content -> {

            LazyColumn {
                items(currentState.tracks) { track ->

                    TrackItem(
                        track = track,
                        onClick = { onTrackClick(track) },
                        onLongClick = { trackToDelete = track }
                    )

                }
            }

        }

        FavoritesUiState.Empty -> {

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Image(
                    painter = painterResource(R.drawable.ic_not_found),
                    contentDescription = null
                )

                Text(
                    text = stringResource(R.string.media_null),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

            }

        }
    }

    trackToDelete?.let { track ->

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { trackToDelete = null },

            title = {
                Text(stringResource(R.string.delete_track_title))
            },

            confirmButton = {

                androidx.compose.material3.TextButton(
                    onClick = {
                        viewModel.removeTrackFromPlaylist(track)
                        trackToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.yes))
                }

            },

            dismissButton = {

                androidx.compose.material3.TextButton(
                    onClick = { trackToDelete = null }
                ) {
                    Text(stringResource(R.string.no))
                }

            }
        )
    }
}

@Composable
fun TrackItem(
    track: Track,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    // горизонтальная ориентация для всего блока с треком
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 13.dp, bottom = 8.dp, end = 20.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // обложка альбома
        AsyncImage(
            model = track.artworkUrl100,
            contentDescription = track.trackName,
            modifier = Modifier.size(45.dp),
            placeholder = painterResource(R.drawable.ic_placeholder),
            error = painterResource(R.drawable.ic_placeholder),
            contentScale = ContentScale.Crop
        )

        // вертикальный блок для инфы
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {

            // название трека
            Text(
                text = track.trackName ?: "",
                maxLines = 1,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primaryContainer,
                overflow = TextOverflow.Ellipsis
            )
            // опять горизонтальная ориентация для имени артиста элипса и длительности трека
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                // сам исполнитель
                Text(
                    text = track.artistName ?: "",
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(4.dp))

                // ellipse
                Image(
                    painter = painterResource(R.drawable.ellipse_1),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primaryContainer),
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(4.dp))

                // время трека
                Text(
                    text = track.trackTime ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                )
            }
        }

        // стрелка справа
        Image(
            painter = painterResource(R.drawable.agreement),
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp)
        )
    }
}