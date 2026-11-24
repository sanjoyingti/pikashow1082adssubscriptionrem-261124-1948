package com.aibuilt.pikashow1082adssubscriptionrem.ui.screens

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
import androidx.compose.material.icons.filled.ArrowBack
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
import com.aibuilt.pikashow1082adssubscriptionrem.data.MediaCache
import com.aibuilt.pikashow1082adssubscriptionrem.data.isReady
import com.aibuilt.pikashow1082adssubscriptionrem.data.model.DownloadItem
import com.aibuilt.pikashow1082adssubscriptionrem.data.percent
import com.aibuilt.pikashow1082adssubscriptionrem.ui.components.EmptyState
import com.aibuilt.pikashow1082adssubscriptionrem.ui.components.Poster
import com.aibuilt.pikashow1082adssubscriptionrem.viewmodel.LibraryViewModel

@Composable
fun DownloadsScreen(
    onBack: () -> Unit,
    onPlay: (contentId: Int, episodeId: Int, key: String) -> Unit,
    viewModel: LibraryViewModel = viewModel()
) {
    val downloads by viewModel.downloads.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 12.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Downloads",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
        }

        if (downloads.isEmpty()) {
            EmptyState("No downloads yet", "Open a title and tap download to save it offline")
            return@Column
        }

        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            items(downloads, key = { "${it.id}-${it.episodeId}" }) { item ->
                DownloadRow(
                    item = item,
                    onPlay = { target ->
                        val key = if (target.episodeId == 0) "show-${target.id}" else "episode-${target.episodeId}"
                        MediaCache.put(key, target.name, target.path)
                        onPlay(target.id, target.episodeId, key)
                    },
                    onResume = { viewModel.resumeDownload(item) },
                    onCancel = { viewModel.cancelDownload(item) },
                    onDelete = { viewModel.deleteDownload(item) }
                )
            }
        }
    }
}

@Composable
private fun DownloadRow(
    item: DownloadItem,
    onPlay: (DownloadItem) -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
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
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = statusLabel(item),
                    style = MaterialTheme.typography.labelSmall,
                    color = when (item.status) {
                        "failed" -> MaterialTheme.colorScheme.primary
                        "completed" -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                if (!item.isReady()) {
                    LinearProgressIndicator(
                        progress = { item.percent() / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                    )
                }
            }
            when {
                item.isReady() -> {
                    IconButton(onClick = { onPlay(item) }) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = "Play offline",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }

                item.status == "failed" -> {
                    IconButton(onClick = onResume) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Retry")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }

                else -> {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Filled.Cancel, contentDescription = "Pause and remove")
                    }
                }
            }
        }
    }
}

private fun statusLabel(item: DownloadItem): String = when (item.status) {
    "completed" -> "Ready to play offline"
    "failed" -> "Download failed · tap retry"
    "queued" -> "Waiting for connection"
    else -> "Downloading ${item.percent()}%"
}
