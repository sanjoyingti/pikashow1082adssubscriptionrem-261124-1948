package com.aibuilt.pikashow1082adssubscriptionrem.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aibuilt.pikashow1082adssubscriptionrem.data.isReady
import com.aibuilt.pikashow1082adssubscriptionrem.data.percent
import com.aibuilt.pikashow1082adssubscriptionrem.ui.components.EmptyState
import com.aibuilt.pikashow1082adssubscriptionrem.ui.components.Poster
import com.aibuilt.pikashow1082adssubscriptionrem.viewmodel.LibraryViewModel

@Composable
fun DownloadsScreen(
    onOpenShow: (Int) -> Unit,
    onPlayLocal: (contentId: Int, episodeId: Int, name: String, path: String) -> Unit,
    viewModel: LibraryViewModel = viewModel()
) {
    val downloads by viewModel.downloads.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Downloads",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${downloads.count { it.isReady() }} saved",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (downloads.isEmpty()) {
            EmptyState("No downloads yet", "Open a title and tap Download to save it offline")
            return@Column
        }

        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            items(downloads, key = { "${it.id}-${it.episodeId}" }) { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (item.isReady()) onPlayLocal(item.id, item.episodeId, item.name, item.path)
                            else onOpenShow(item.id)
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
                            Text(item.name, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(
                                text = when (item.status) {
                                    "completed" -> "Saved · ready to play"
                                    "downloading" -> "${item.percent()}% downloading"
                                    "queued" -> "Waiting for connection"
                                    "failed" -> "Failed · tap retry"
                                    else -> item.status
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (item.isReady()) {
                            IconButton(onClick = { onPlayLocal(item.id, item.episodeId, item.name, item.path) }) {
                                Icon(
                                    Icons.Filled.PlayArrow,
                                    contentDescription = "Play offline",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            IconButton(onClick = { viewModel.cancelDownload(item) }) {
                                Icon(Icons.Filled.Cancel, contentDescription = "Cancel")
                            }
                            if (item.status == "failed" || item.status == "queued") {
                                IconButton(onClick = { viewModel.resumeDownload(item) }) {
                                    Icon(Icons.Filled.Refresh, contentDescription = "Retry")
                                }
                            }
                        }
                        IconButton(onClick = { viewModel.deleteDownload(item) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    }
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
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Saved videos live on this device and play offline",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun DownloadsHint(onOpen: () -> Unit) {
    TextButton(onClick = onOpen) { Text("Open downloads") }
}
