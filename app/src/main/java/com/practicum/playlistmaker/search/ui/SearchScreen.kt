package com.practicum.playlistmaker.search.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.settings.ui.AppTypography
import com.practicum.playlistmaker.settings.ui.BlackLight
import com.practicum.playlistmaker.settings.ui.DarkColors
import com.practicum.playlistmaker.settings.ui.LightColors
import com.practicum.playlistmaker.settings.ui.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onTrackClick: (Track) -> Unit
) {

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.search),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    windowInsets = WindowInsets(0.dp)
                )
            }
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                // поиск
                SearchBar(text = state.searchText,
                    isClearVisible = state.isClearTextVisible,
                    onTextChange = viewModel::onSearchTextChanged,
                    onClearClick = viewModel::onClearTextClicked)

                // управление историей
                if (state.isHistoryVisible) {

                    SearchHistory(
                        tracks = state.history,
                        onTrackClick = onTrackClick,
                        onClearHistory = { viewModel.clearHistory() }
                    )

                } else { when (val tracksState = state.tracksState) {

                    is TracksState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is TracksState.Content -> {
                        TracksList(
                            tracks = tracksState.tracks,
                            onTrackClick = onTrackClick
                        )
                    }

                    is TracksState.ErrorInternet -> {
                        NoInternetPlaceholder(
                            onRefreshClick = { viewModel.retrySearch() }
                        )
                    }

                    is TracksState.ErrorFound -> {
                        NotFoundPlaceholder()
                    }

                    else -> {}
                }
                }
            }
        }
}

@Composable
fun SearchBar(
    text: String,
    isClearVisible: Boolean,
    onTextChange: (String) -> Unit,
    onClearClick: () -> Unit
) {
    //управление клавиатурой
    val keyboardController = LocalSoftwareKeyboardController.current

    //сам поисковик
    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        placeholder = { Text(
            text = stringResource(R.string.search),
            style = MaterialTheme.typography.bodyLarge,
            color =MaterialTheme.colorScheme.secondaryContainer
        ) },
        singleLine = true,
        // лупа
        leadingIcon = {
            Icon(
                painterResource(R.drawable.search_icon),
                tint = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.padding(start = 14.dp),
                contentDescription = null
            )
        },
        //кнопка очистки текста
        trailingIcon = {
            if (isClearVisible) {
                IconButton(onClick = {
                    onClearClick()
                    keyboardController?.hide()
                }) {
                    Icon(
                        painterResource(R.drawable.clear_button),
                        tint = MaterialTheme.colorScheme.secondaryContainer,
                        contentDescription = null
                    )
                }
            }
        },
        shape = RoundedCornerShape(12.dp),

        colors = TextFieldDefaults.colors(

            // фон
            focusedContainerColor = MaterialTheme.colorScheme.onBackground,
            unfocusedContainerColor = MaterialTheme.colorScheme.onBackground,

            // курсор
            cursorColor = MaterialTheme.colorScheme.tertiary,

            // бордер
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,

            focusedTextColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

@Composable
fun SearchHistory(
    tracks: List<Track>,
    onTrackClick: (Track) -> Unit,
    onClearHistory: () -> Unit
) {

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Text(
            text = stringResource(R.string.history),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp, bottom = 8.dp)
        )
        // история
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(tracks, key = { it.trackId }) { track ->
                TrackItem(track = track) { onTrackClick(track) }
            }

            // кнопка очистки
            if (tracks.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = onClearHistory,
                            shape = RoundedCornerShape(54.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.clear_history),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TracksList(
    tracks: List<Track>,
    onTrackClick: (Track) -> Unit
) {

    LazyColumn {
        items(
            items = tracks,
            key = { it.trackId }
        ) { track ->
            TrackItem(track) { onTrackClick(track) }
        }
    }
}

@Composable
fun TrackItem(
    track: Track,
    onClick: () -> Unit,
) {
    // горизонтальная ориентация для всего блока с треком
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 13.dp, bottom = 8.dp, end = 20.dp)
            .clickable { onClick() },
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

@Composable
fun NoInternetPlaceholder(onRefreshClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_not_internet),
            contentDescription = stringResource(R.string.not_internet)
        )

        Text(
            text = stringResource(R.string.not_internet),
            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
        )

        Button(onClick = onRefreshClick) {
            Text(text = stringResource(R.string.refresh))
        }
    }
}

@Composable
fun NotFoundPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_not_found),
            contentDescription = stringResource(R.string.not_found)
        )

        Text(
            text = stringResource(R.string.not_found),
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}