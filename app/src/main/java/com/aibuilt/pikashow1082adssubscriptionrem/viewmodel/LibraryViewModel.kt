package com.aibuilt.pikashow1082adssubscriptionrem.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aibuilt.pikashow1082adssubscriptionrem.PikaApp
import com.aibuilt.pikashow1082adssubscriptionrem.data.model.DownloadItem
import com.aibuilt.pikashow1082adssubscriptionrem.data.model.ProgressItem
import com.aibuilt.pikashow1082adssubscriptionrem.data.model.SavedItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as PikaApp).container

    val favourites: StateFlow<List<SavedItem>> = container.library.state
        .map { it.favourites }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val watchLater: StateFlow<List<SavedItem>> = container.library.state
        .map { it.watchLater }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val progress: StateFlow<List<ProgressItem>> = container.library.state
        .map { it.progress.sortedByDescending { entry -> entry.updatedAt } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val downloads: StateFlow<List<DownloadItem>> = container.library.state
        .map { it.downloads }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val searchHistory: StateFlow<List<String>> = container.library.state
        .map { it.searchHistory }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun removeFavourite(item: SavedItem) = container.library.toggleFavourite(item)

    fun removeWatchLater(item: SavedItem) = container.library.toggleWatchLater(item)

    fun removeProgress(item: ProgressItem) = container.library.removeProgress(item.id, item.episodeId)

    fun clearProgress() = container.library.clearProgress()

    fun clearSearchHistory() = container.library.clearSearchHistory()

    fun deleteDownload(item: DownloadItem) {
        viewModelScope.launch { container.downloads.delete(item.id, item.episodeId) }
    }

    fun cancelDownload(item: DownloadItem) {
        viewModelScope.launch { container.downloads.cancel(item.id, item.episodeId) }
    }

    fun resumeDownload(item: DownloadItem) {
        viewModelScope.launch {
            container.downloads.start(item.id, item.episodeId, item.name, item.image, item.url)
        }
    }
}
