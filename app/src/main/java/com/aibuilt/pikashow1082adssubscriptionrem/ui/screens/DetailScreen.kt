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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aibuilt.pikashow1082adssubscriptionrem.data.MediaCache
import com.aibuilt.pikashow1082adssubscriptionrem.data.StreamSources
import com.aibuilt.pikashow1082adssubscriptionrem.data.isReady
import com.aibuilt.pikashow1082adssubscriptionrem.data.percent
import com.aibuilt.pikashow1082adssubscriptionrem.ui.components.ErrorState
import com.aibuilt.pikashow1082adssubscriptionrem.ui.components.LoadingState
import com.aibuilt.pikashow1082adssubscriptionrem.ui.components.Poster
import com.aibuilt.pikashow1082adssubscriptionrem.viewmodel.DetailViewModel

@Composable
fun DetailScreen(
    id: Int,
    onBack: () -> Unit,
    onPlay: (contentId: Int, episodeId: Int, server: Int, key: String) -> Unit,
    onOpenDownloads: () -> Unit,
    viewModel: DetailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val downloads by viewModel.downloads.collectAsState()

    LaunchedEffect(id) { viewModel.load(id) }

    val show = state.show

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.loading && show == null -> LoadingState()
            state.error != null && show == null -> ErrorState(state.error!!, onRetry = { viewModel.load(id) })
            show != null -> LazyColumn(
                contentPadding = PaddingValues(bottom = 28.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Box {
                        Poster(
                            url = show.image,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 11f),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, MaterialTheme.colorScheme.background)
                                    )
                                )
                        )
                        IconButton(onClick = onBack, modifier = Modifier.padding(6.dp)) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = show.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        val meta = buildList {
                            show.rating?.takeIf { it > 0 }?.let { add("★ ${"%.1f".format(it)}") }
                            show.premiered?.take(4)?.let { add(it) }
                            show.runtime?.let { add("$it min") }
                            show.network?.let { add(it) }
                        }
                        if (meta.isNotEmpty()) {
                            Text(
                                text = meta.joinToString("  ·  "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        if (show.genres.isNotEmpty()) {
                            Text(
                                text = show.genres.joinToString(" • "),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                        if (show.summary.isNotBlank()) {
                            Text(
                                text = show.summary,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 6,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 10.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.padding(top = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { onPlay(show.id, 0, state.selectedServer, "show-${show.id}") },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                Text("  Play")
                            }
                            IconButton(onClick = { viewModel.toggleFavourite() }) {
                                Icon(
                                    if (state.favourite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = "Favourite",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = { viewModel.toggleWatchLater() }) {
                                Icon(
                                    if (state.watchLater) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                    contentDescription = "Watch later"
                                )
                            }
                            IconButton(onClick = { viewModel.toggleSubtitles() }) {
                                Icon(
                                    Icons.Filled.Subtitles,
                                    contentDescription = "Subtitles",
                                    tint = if (state.subtitles) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "Servers",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp)
                        ) {
                            StreamSources.servers(show.id, 0).forEachIndexed { index, server ->
                                FilterChip(
                                    selected = state.selectedServer == index,
                                    onClick = { viewModel.selectServer(index) },
                                    label = { Text(server.name) }
                                )
                            }
                        }

                        Text(
                            text = "Quality",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StreamSources.qualities.forEach { (label, _) ->
                                FilterChip(
                                    selected = state.quality == label,
                                    onClick = { viewModel.selectQuality(label) },
                                    label = { Text(label) }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.padding(top = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val entry = downloads.firstOrNull { it.id == show.id && it.episodeId == 0 }
                            OutlinedButton(
                                onClick = {
                                    if (entry?.isReady() == true) {
                                        val key = "show-${show.id}"
                                        MediaCache.put(key, show.name, viewModel.localPath(show.id, 0))
                                        onPlay(show.id, 0, -1, key)
                                    } else if (entry == null) {
                                        viewModel.startDownload(show.id, 0, show.name, show.image)
                                        onOpenDownloads()
                                    }
                                }
                            ) {
                                Icon(
                                    if (entry?.isReady() == true) Icons.Filled.PlayArrow else Icons.Filled.Download,
                                    contentDescription = null
                                )
                                Text(if (entry?.isReady() == true) "  Play offline" else "  Download")
                            }
                            if (entry != null && !entry.isReady()) {
                                AssistChip(
                                    onClick = { viewModel.cancelDownload(show.id, 0) },
                                    label = { Text("${entry.percent()}% · cancel") },
                                    leadingIcon = { Icon(Icons.Filled.Cancel, contentDescription = null) }
                                )
                            }
                        }

                        if (state.episodes.isNotEmpty()) {
                            Text(
                                text = "Episodes",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 20.dp, bottom = 6.dp)
                            )
                        }
                    }
                }

                items(state.episodes, key = { it.id }) { episode ->
                    val label = "S${episode.season}·E${episode.number ?: 0}"
                    val entry = downloads.firstOrNull { it.id == show.id && it.episodeId == episode.id }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val key = "episode-${episode.id}"
                                if (entry?.isReady() == true) {
                                    MediaCache.put(key, episode.name, viewModel.localPath(show.id, episode.id))
                                    onPlay(show.id, episode.id, -1, key)
                                } else {
                                    onPlay(show.id, episode.id, state.selectedServer, key)
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Poster(
                                url = episode.image,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 10.dp)
                            ) {
                                Text(
                                    text = "$label  ${episode.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                episode.runtime?.let {
                                    Text(
                                        text = "$it min",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (entry != null) {
                                    LinearProgressIndicator(
                                        progress = { entry.percent() / 100f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp)
                                            .height(3.dp)
                                    )
                                }
                            }
                            IconButton(onClick = {
                                when {
                                    entry == null -> viewModel.startDownload(show.id, episode.id, episode.name, episode.image)
                                    entry.isReady() -> {
                                        val key = "episode-${episode.id}"
                                        MediaCache.put(key, episode.name, viewModel.localPath(show.id, episode.id))
                                        onPlay(show.id, episode.id, -1, key)
                                    }
                                    else -> viewModel.cancelDownload(show.id, episode.id)
                                }
                            }) {
                                Icon(
                                    when {
                                        entry?.isReady() == true -> Icons.Filled.PlayArrow
                                        entry != null -> Icons.Filled.Cancel
                                        else -> Icons.Filled.Download
                                    },
                                    contentDescription = "Download episode",
                                    tint = if (entry?.isReady() == true) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
