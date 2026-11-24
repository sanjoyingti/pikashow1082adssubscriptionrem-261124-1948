package com.aibuilt.pikashow1082adssubscriptionrem.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChip
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
    onPlay: (key: String, contentId: Int, episodeId: Int) -> Unit,
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
            text = "Saved to this device · playable without internet",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
        )

        if (downloads.isEmpty()) {
            EmptyState("Nothing downloaded yet", "Open a title and tap Download")
            return@Column
        }

        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            items(downloads, key = { "${it.id}-${it.episodeId}" }) { item ->
                DownloadRow(
                    item = item,
                    onPlay = {
                        val key = "dl-${item.id}-${item.episodeId}"
                        MediaCache.put(key, item.name, item.path)
                        onPlay(key, item.id, item.episodeId)
                    },
                    onCancel = { viewModel.cancelDownload(item) },
                    onResume = { viewModel.resumeDownload(item) },
                    onDelete = { viewModel.deleteDownload(item) }
                )
            }
        }
    }
}

@Composable
private fun DownloadRow(
    item: DownloadItem,
    onPlay: () -> Unit,
    onCancel: () -> Unit,
    onResume: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = item.isReady(), onClick = onPlay)
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
                    text = statusLabel(item),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            when {
                item.isReady() -> IconButton(onClick = onPlay) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = "Play offline",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                item.status == "failed" -> IconButton(onClick = onResume) {
                    Icon(Icons.Filled.Download, contentDescription = "Retry download")
                }

                else -> IconButton(onClick = onCancel) {
                    Icon(Icons.Filled.Cancel, contentDescription = "Cancel download")
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete file")
            }
        }
        if (!item.isReady() && item.status != "failed") {
            LinearProgressIndicator(
                progress = { item.percent() / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .height(3.dp)
            )
        }
        if (item.status == "failed") {
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = onResume,
                    label = { Text("Retry") },
                    leadingIcon = { Icon(Icons.Filled.Download, contentDescription = null) }
                )
            }
        }
    }
}

private fun statusLabel(item: DownloadItem): String = when {
    item.isReady() -> "Ready · ${(item.bytes / (1024 * 1024))} MB"
    item.status == "failed" -> "Failed · tap retry"
    item.status == "completed" -> "Finished"
    else -> "Downloading ${item.percent()}%"
}
