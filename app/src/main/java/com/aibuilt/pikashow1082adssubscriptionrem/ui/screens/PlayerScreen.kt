@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package com.aibuilt.pikashow1082adssubscriptionrem.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.aibuilt.pikashow1082adssubscriptionrem.data.MediaCache
import com.aibuilt.pikashow1082adssubscriptionrem.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(
    key: String,
    contentId: Int,
    episodeId: Int,
    server: Int,
    isLive: Boolean,
    onBack: () -> Unit,
    viewModel: PlayerViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var position by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var playing by remember { mutableStateOf(true) }
    var qualityOpen by remember { mutableStateOf(false) }
    var speedOpen by remember { mutableStateOf(false) }

    LaunchedEffect(key, contentId, episodeId, server, isLive) {
        viewModel.bind(
            key = key,
            contentId = contentId,
            episodeId = episodeId,
            server = server,
            liveUrl = if (isLive) MediaCache.url(key) else null,
            imageUrl = null
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            val exo = viewModel.player
            position = exo?.currentPosition ?: 0L
            duration = exo?.duration?.takeIf { it > 0 } ?: 0L
            playing = exo?.isPlaying == true
        }
    }

    DisposableEffect(Unit) {
        onDispose { }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)) {
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    player = viewModel.player
                }
            },
            update = { view -> view.player = viewModel.player },
            modifier = Modifier.fillMaxSize()
        )

        if (state.error != null) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(state.error!!, color = Color.White)
                Button(onClick = { viewModel.replay() }, modifier = Modifier.padding(top = 10.dp)) {
                    Text("Retry")
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = state.title,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { viewModel.toggleSubtitles() }) {
                    Icon(
                        Icons.Filled.Subtitles,
                        contentDescription = "Subtitles",
                        tint = if (state.subtitlesOn) MaterialTheme.colorScheme.primary else Color.White
                    )
                }
                Box {
                    IconButton(onClick = { qualityOpen = true }) {
                        Text(state.selectedQuality, color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }
                    DropdownMenu(expanded = qualityOpen, onDismissRequest = { qualityOpen = false }) {
                        (listOf("Auto", "1080p", "720p", "480p", "360p")).forEach { quality ->
                            DropdownMenuItem(
                                text = { Text(quality) },
                                onClick = {
                                    viewModel.selectQuality(quality)
                                    qualityOpen = false
                                }
                            )
                        }
                    }
                }
                Box {
                    IconButton(onClick = { speedOpen = true }) {
                        Text("${state.speed}x", color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }
                    DropdownMenu(expanded = speedOpen, onDismissRequest = { speedOpen = false }) {
                        listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f).forEach { speed ->
                            DropdownMenuItem(
                                text = { Text("${speed}x") },
                                onClick = {
                                    viewModel.setSpeed(speed)
                                    speedOpen = false
                                }
                            )
                        }
                    }
                }
                IconButton(onClick = { viewModel.openExternally() }) {
                    Icon(Icons.Filled.OpenInNew, contentDescription = "External player", tint = Color.White)
                }
            }

            Box(Modifier.weight(1f))

            if (!isLive && state.servers.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.servers.forEachIndexed { index, name ->
                        FilterChip(
                            selected = state.selectedServer == index,
                            onClick = { viewModel.selectServer(index) },
                            label = { Text(name, color = Color.White) }
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                if (!isLive) {
                    Slider(
                        value = if (duration > 0) position.toFloat() / duration else 0f,
                        onValueChange = { fraction ->
                            val exo = viewModel.player ?: return@Slider
                            if (duration > 0) exo.seekTo((fraction * duration).toLong())
                        }
                    )
                    Text(
                        text = "${format(position)} / ${format(duration)}",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                } else {
                    Text("LIVE", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        val exo = viewModel.player ?: return@IconButton
                        exo.seekTo((exo.currentPosition - 10_000).coerceAtLeast(0))
                    }) {
                        Icon(Icons.Filled.Replay10, contentDescription = "Back 10s", tint = Color.White)
                    }
                    IconButton(
                        onClick = {
                            val exo = viewModel.player ?: return@IconButton
                            if (exo.isPlaying) exo.pause() else exo.play()
                        },
                        modifier = Modifier
                            .size(58.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
                    ) {
                        Icon(
                            if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Play or pause",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = {
                        val exo = viewModel.player ?: return@IconButton
                        exo.seekTo(exo.currentPosition + 10_000)
                    }) {
                        Icon(Icons.Filled.Forward10, contentDescription = "Forward 10s", tint = Color.White)
                    }
                }
            }
        }
    }
}

private fun format(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val hours = minutes / 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes % 60, seconds)
    else "%d:%02d".format(minutes, seconds)
}
