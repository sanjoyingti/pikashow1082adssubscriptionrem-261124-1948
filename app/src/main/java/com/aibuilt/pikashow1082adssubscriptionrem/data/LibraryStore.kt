package com.aibuilt.pikashow1082adssubscriptionrem.data

import com.aibuilt.pikashow1082adssubscriptionrem.data.model.DownloadItem
import com.aibuilt.pikashow1082adssubscriptionrem.data.model.ProgressItem
import com.aibuilt.pikashow1082adssubscriptionrem.data.model.SavedItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class LibraryState(
    val favourites: List<SavedItem> = emptyList(),
    val watchLater: List<SavedItem> = emptyList(),
    val progress: List<ProgressItem> = emptyList(),
    val downloads: List<DownloadItem> = emptyList(),
    val searchHistory: List<String> = emptyList()
)

class LibraryStore(private val file: File) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private val _state = MutableStateFlow(read())
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    private fun read(): LibraryState = runCatching {
        if (file.exists()) json.decodeFromString<LibraryState>(file.readText()) else LibraryState()
    }.getOrDefault(LibraryState())

    private fun write(state: LibraryState) {
        runCatching {
            file.parentFile?.mkdirs()
            file.writeText(json.encodeToString(state))
        }
    }

    private fun update(block: (LibraryState) -> LibraryState) {
        scope.launch {
            mutex.withLock {
                val next = block(_state.value)
                _state.value = next
                write(next)
            }
        }
    }

    fun toggleFavourite(item: SavedItem) = update { state ->
        if (state.favourites.any { it.id == item.id }) {
            state.copy(favourites = state.favourites.filterNot { it.id == item.id })
        } else {
            state.copy(favourites = (listOf(item) + state.favourites).take(300))
        }
    }

    fun toggleWatchLater(item: SavedItem) = update { state ->
        if (state.watchLater.any { it.id == item.id }) {
            state.copy(watchLater = state.watchLater.filterNot { it.id == item.id })
        } else {
            state.copy(watchLater = (listOf(item) + state.watchLater).take(300))
        }
    }

    fun saveProgress(contentId: Int, episodeId: Int, name: String, image: String?, position: Long, duration: Long) = update { state ->
        val key = "$contentId-$episodeId"
        val filtered = state.progress.filterNot { it.key == key }
        if (position < 5_000L || (duration > 0 && position >= duration - 10_000L)) {
            if (position < 5_000L) state.copy(progress = filtered) else state.copy(progress = filtered)
        } else {
            val entry = ProgressItem(contentId, episodeId, name, image, position, duration)
            state.copy(progress = (listOf(entry) + filtered).take(40))
        }
    }

    fun removeProgress(contentId: Int, episodeId: Int) = update { state ->
        state.copy(progress = state.progress.filterNot { it.id == contentId && it.episodeId == episodeId })
    }

    fun clearProgress() = update { it.copy(progress = emptyList()) }

    fun upsertDownload(item: DownloadItem) = update { state ->
        state.copy(downloads = (listOf(item) + state.downloads.filterNot { it.id == item.id && it.episodeId == item.episodeId }).take(100))
    }

    fun patchDownload(contentId: Int, episodeId: Int, block: (DownloadItem) -> DownloadItem) = update { state ->
        state.copy(downloads = state.downloads.map {
            if (it.id == contentId && it.episodeId == episodeId) block(it) else it
        })
    }

    fun removeDownload(contentId: Int, episodeId: Int) = update { state ->
        state.copy(downloads = state.downloads.filterNot { it.id == contentId && it.episodeId == episodeId })
    }

    fun addSearchTerm(term: String) = update { state ->
        if (term.isBlank()) state
        else state.copy(searchHistory = (listOf(term.trim()) + state.searchHistory.filterNot { it.equals(term.trim(), true) }).take(20))
    }

    fun clearSearchHistory() = update { it.copy(searchHistory = emptyList()) }
}
