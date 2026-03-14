package com.practicum.playlistmaker.media.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.media.ui.favorites.FavoriteTrackViewModel
import com.practicum.playlistmaker.media.ui.playlist.PlaylistsViewModel
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaScreen(
    favoriteViewModel: FavoriteTrackViewModel,
    playlistsViewModel: PlaylistsViewModel,
    onTrackClick: (Track) -> Unit,
    onPlaylistClick: (Long) -> Unit,
    onNewPlaylistClick: () -> Unit,
    onBackClick: () -> Unit
) {

    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {

        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.media),
                    style = MaterialTheme.typography.titleLarge
                )
            },
        )

        TabRow(selectedTabIndex = pagerState.currentPage) {

            Tab(
                selected = pagerState.currentPage == 0,
                onClick = {
                    coroutineScope.launch { pagerState.animateScrollToPage(0) }
                },
                text = { Text(
                    text = stringResource(R.string.favorites),
                    style = MaterialTheme.typography.titleSmall
                    ) }
            )

            Tab(
                selected = pagerState.currentPage == 1,
                onClick = {
                    coroutineScope.launch { pagerState.animateScrollToPage(1) }
                },
                text = { Text(
                    text = stringResource(R.string.playlists),
                    style = MaterialTheme.typography.titleSmall
                ) }
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->

            when (page) {

                0 -> FavoritesScreen(
                    viewModel = favoriteViewModel,
                    onTrackClick = onTrackClick
                )

                1 -> PlaylistsScreen(
                    viewModel = playlistsViewModel,
                    onPlaylistClick = onPlaylistClick,
                    onNewPlaylistClick = onNewPlaylistClick
                )
            }
        }
    }
}