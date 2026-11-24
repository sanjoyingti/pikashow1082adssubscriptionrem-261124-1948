package com.aibuilt.pikashow1082adssubscriptionrem.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aibuilt.pikashow1082adssubscriptionrem.data.model.Show
import com.aibuilt.pikashow1082adssubscriptionrem.ui.components.ErrorState
import com.aibuilt.pikashow1082adssubscriptionrem.ui.components.Poster
import com.aibuilt.pikashow1082adssubscriptionrem.ui.components.ShowCard
import com.aibuilt.pikashow1082adssubscriptionrem.ui.components.ShowGridCard
import com.aibuilt.pikashow1082adssubscriptionrem.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onOpenShow: (Int) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLive: () -> Unit,
    onOpenDownloads: () -> Unit,
    onResume: (Int, Int, String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val genres by viewModel.genres.collectAsState()
    val continueWatching by viewModel.continueWatching.collectAsState()

    val visible = remember(state.shows, state.genre) {
        val genre = state.genre
        if (genre == null) state.shows else state.shows.filter { it.genres.contains(genre) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 6.dp, top = 10.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PikaShow",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "  PRO",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            )
            Box(Modifier.weight(1f))
            IconButton(onClick = onOpenLive) {
                Icon(Icons.Filled.LiveTv, contentDescription = "Live TV", tint = MaterialTheme.colorScheme.onBackground)
            }
            IconButton(onClick = onOpenDownloads) {
                Icon(Icons.Filled.Download, contentDescription = "Downloads", tint = MaterialTheme.colorScheme.onBackground)
            }
            IconButton(onClick = onOpenSearch) {
                Icon(Icons.Filled.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onBackground)
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onBackground)
            }
        }

        if (state.loading && state.shows.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        if (state.error != null && state.shows.isEmpty()) {
            ErrorState(message = state.error!!, onRetry = { viewModel.load(force = true) })
            return@Column
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(104.dp),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (continueWatching.isNotEmpty() && state.genre == null) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column {
                        Text(
                            text = "Continue watching",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(continueWatching, key = { it.key }) { progress ->
                                Column(
                                    modifier = Modifier
                                        .width(150.dp)
                                        .clickable { onResume(progress.id, progress.episodeId, progress.name) }
                                ) {
                                    Poster(
                                        url = progress.image,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(16f / 9f)
                                    )
                                    Text(
                                        text = progress.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                    LinearProgressIndicator(
                                        progress = { progress.fraction },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 3.dp)
                                            .height(3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (genres.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = state.genre == null,
                                onClick = { viewModel.selectGenre(null) },
                                label = { Text("All") },
                                colors = FilterChipDefaults.filterChipColors()
                            )
                        }
                        items(genres) { genre ->
                            FilterChip(
                                selected = state.genre == genre,
                                onClick = { viewModel.selectGenre(genre) },
                                label = { Text(genre, maxLines = 1) }
                            )
                        }
                    }
                }
            }

            items(visible, key = { it.id }) { show ->
                ShowGridCard(show = show, onClick = { onOpenShow(show.id) })
            }
        }
    }
}

@Composable
fun ShowRowItem(show: Show, onClick: () -> Unit) {
    ShowCard(show = show, onClick = onClick)
}

@Composable
fun ShowPosterBox(show: Show) {
    Poster(url = show.image, modifier = Modifier.size(96.dp), contentScale = ContentScale.Crop)
}
