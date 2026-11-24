package com.aibuilt.pikashow1082adssubscriptionrem.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aibuilt.pikashow1082adssubscriptionrem.data.isReady
import com.aibuilt.pikashow1082adssubscriptionrem.data.percent
import com.aibuilt.pikashow1082adssubscriptionrem.ui.components.EmptyState
import com.aibuilt.pikashow1082adssubscriptionrem.ui.components.Poster
import com.aibuilt.pikashow1082adssubscriptionrem.viewmodel.LibraryViewModel

@Composable
fun DownloadsScreen(
    onPlayOffline: (contentId: Int, episodeId: Int, name: String, path: String) -> Unit,
    viewModel: LibraryViewModel = viewModel()
) {
    val downloads by viewModel.downloads.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Downloads",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 6.dp)
        )
        Text(
            text = "Saved videos play offline with no ads and no subscription check.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
        )

        if (downloads.isEmpty()) {
            EmptyState("No downloads yet", "Tap download on any title or episode")
            return@Column
        }

        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp, top = 6.dp)) {
            items(downloads, key = { "d-${it.id}-${it.episodeId}" }) { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (item.isReady()) onPlayOffline(item.id, item.episodeId, item.name, item.path)
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Poster(
                            url = item.image,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = item.name,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = when (item.status) {
                                    "completed" -> "Ready offline"
                                    "failed" -> "Failed - tap retry"
                                    "queued" -> "Waiting for network"
                                    else -> "${item.percent()}%"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!item.isReady()) {
                                LinearProgressIndicator(
                                    progress = { item.percent() / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                )
                            }
                        }
                        when {
                            item.isReady() -> IconButton(onClick = {
                                onPlayOffline(item.id, item.episodeId, item.name, item.path)
                            }) {
                                Icon(
                                    Icons.Filled.PlayArrow,
                                    contentDescription = "Play offline",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            item.status == "failed" -> IconButton(onClick = { viewModel.resumeDownload(item) }) {
                                Icon(Icons.Filled.Refresh, contentDescription = "Retry")
                            }

                            else -> IconButton(onClick = { viewModel.cancelDownload(item) }) {
                                Icon(Icons.Filled.Cancel, contentDescription = "Cancel")
                            }
                        }
                        IconButton(onClick = { viewModel.deleteDownload(item) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
    }
}
