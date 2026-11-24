package com.aibuilt.pikashow1082adssubscriptionrem.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aibuilt.pikashow1082adssubscriptionrem.data.isReady
import com.aibuilt.pikashow1082adssubscriptionrem.data.model.DownloadItem
import com.aibuilt.pikashow1082adssubscriptionrem.data.percent
import com.aibuilt.pikashow1082adssubscriptionrem.ui.components.EmptyState
import com.aibuilt.pikashow1082adssubscriptionrem.ui.components.Poster
import com.aibuilt.pikashow1082adssubscriptionrem.viewmodel.LibraryViewModel

private fun megabytes(bytes: Long): String = "%.1f MB".format(bytes / (1024.0 * 1024.0))

@Composable
fun DownloadsScreen(
    onOpenShow: (Int) -> Unit,
    onPlayLocal: (contentId: Int, episodeId: Int, name: String, path: String) -> Unit,
    viewModel: LibraryViewModel = viewModel()
) {
    val downloads by viewModel.downloads.collectAsState()
    val used = downloads.sumOf { it.bytes }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp)) {
            Text(
                text = "Downloads",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (downloads.isEmpty()) "Nothing saved yet"
                else "${downloads.size} video(s) · ${megabytes(used)} stored",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        if (downloads.isEmpty()) {
            EmptyState(
                title = "No downloads yet",
                subtitle = "Tap the download icon on any movie or episode to save it for offline playback"
            )
            return@Column
        }

        LazyColumn(
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(downloads, key = { "${it.id}-${it.episodeId}" }) { item ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    DownloadRow(
                        item = item,
                        onOpen = { onOpenShow(item.id) },
                        onPlay = {
                            if (item.isReady()) onPlayLocal(item.id, item.episodeId, item.name, item.path)
                        },
                        onCancel = { viewModel.cancelDownload(item) },
                        onResume = { viewModel.resumeDownload(item) },
                        onDelete = { viewModel.deleteDownload(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadRow(
    item: DownloadItem,
    onOpen: () -> Unit,
    onPlay: () -> Unit,
    onCancel: () -> Unit,
    onResume: () -> Unit,
    onDelete: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Poster(
            url = item.image,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onOpen)
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
                text = when {
                    item.isReady() -> "Saved · ${megabytes(item.bytes)}"
                    item.status == "failed" -> "Failed · tap retry"
                    item.total > 0 -> "${item.percent()}% · ${megabytes(item.bytes)} of ${megabytes(item.total)}"
                    else -> "Waiting for network"
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (item.status == "failed") MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
            if (!item.isReady()) {
                LinearProgressIndicator(
                    progress = { item.percent() / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .height(3.dp)
                )
            }
        }
        IconButton(
            onClick = {
                when {
                    item.isReady() -> onPlay()
                    item.status == "failed" -> onResume()
                    else -> onCancel()
                }
            }
        ) {
            Icon(
                imageVector = when {
                    item.isReady() -> Icons.Filled.PlayArrow
                    item.status == "failed" -> Icons.Filled.Refresh
                    else -> Icons.Filled.Cancel
                },
                contentDescription = when {
                    item.isReady() -> "Play offline"
                    item.status == "failed" -> "Retry download"
                    else -> "Cancel download"
                },
                tint = if (item.isReady()) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = "Delete download",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
