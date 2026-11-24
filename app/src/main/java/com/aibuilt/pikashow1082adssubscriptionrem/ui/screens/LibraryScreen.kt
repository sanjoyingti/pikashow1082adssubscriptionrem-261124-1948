package com.aibuilt.pikashow1082adssubscriptionrem.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aibuilt.pikashow1082adssubscriptionrem.data.model.ProgressItem
import com.aibuilt.pikashow1082adssubscriptionrem.data.model.SavedItem
import com.aibuilt.pikashow1082adssubscriptionrem.ui.components.EmptyState
import com.aibuilt.pikashow1082adssubscriptionrem.ui.components.Poster
import com.aibuilt.pikashow1082adssubscriptionrem.viewmodel.LibraryViewModel

@Composable
fun LibraryScreen(
    onOpenShow: (Int) -> Unit,
    onResume: (Int, Int, String) -> Unit,
    viewModel: LibraryViewModel = viewModel()
) {
    val favourites by viewModel.favourites.collectAsState()
    val watchLater by viewModel.watchLater.collectAsState()
    val progress by viewModel.progress.collectAsState()

    var tab by remember { mutableIntStateOf(0) }
    val titles = listOf("Favourites", "Watch later", "Continue")

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "My library",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 6.dp)
        )
        TabRow(selectedTabIndex = tab) {
            titles.forEachIndexed { index, title ->
                Tab(
                    selected = tab == index,
                    onClick = { tab = index },
                    text = { Text(title) }
                )
            }
        }

        when (tab) {
            0 -> SavedList(
                items = favourites,
                emptyTitle = "Nothing saved yet",
                emptySubtitle = "Tap the heart on a title",
                onOpen = onOpenShow,
                onRemove = { viewModel.removeFavourite(it) }
            )

            1 -> SavedList(
                items = watchLater,
                emptyTitle = "Watch later is empty",
                emptySubtitle = "Tap the bookmark on a title",
                onOpen = onOpenShow,
                onRemove = { viewModel.removeWatchLater(it) }
            )

            else -> ProgressList(
                items = progress,
                onResume = onResume,
                onRemove = { viewModel.removeProgress(it) },
                onClear = { viewModel.clearProgress() }
            )
        }
    }
}

@Composable
private fun SavedList(
    items: List<SavedItem>,
    emptyTitle: String,
    emptySubtitle: String,
    onOpen: (Int) -> Unit,
    onRemove: (SavedItem) -> Unit
) {
    if (items.isEmpty()) {
        EmptyState(emptyTitle, emptySubtitle)
        return
    }
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        items(items, key = { it.id }) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpen(item.id) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Poster(
                    url = item.image,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = item.name,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                )
                IconButton(onClick = { onRemove(item) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remove")
                }
            }
        }
    }
}

@Composable
private fun ProgressList(
    items: List<ProgressItem>,
    onResume: (Int, Int, String) -> Unit,
    onRemove: (ProgressItem) -> Unit,
    onClear: () -> Unit
) {
    if (items.isEmpty()) {
        EmptyState("No resume points", "Start watching something")
        return
    }
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Continue watching",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onClear) { Text("Clear all") }
            }
        }
        items(items, key = { it.key }) { item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onResume(item.id, item.episodeId, item.name) }
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
                    Text(
                        text = item.name,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    )
                    IconButton(onClick = { onRemove(item) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Remove")
                    }
                }
                LinearProgressIndicator(
                    progress = { item.fraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                )
            }
        }
    }
}
