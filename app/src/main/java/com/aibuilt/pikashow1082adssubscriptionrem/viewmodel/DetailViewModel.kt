package com.aibuilt.pikashow1082adssubscriptionrem.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aibuilt.pikashow1082adssubscriptionrem.PikaApp
import com.aibuilt.pikashow1082adssubscriptionrem.data.MediaCache
import com.aibuilt.pikashow1082adssubscriptionrem.data.StreamSources
import com.aibuilt.pikashow1082adssubscriptionrem.data.model.DownloadItem
import com.aibuilt.pikashow1082adssubscriptionrem.data.model.Episode
import com.aibuilt.pikashow1082adssubscriptionrem.data.model.SavedItem
import com.aibuilt.pikashow1082adssubscriptionrem.data.model.Show
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailState(
    val loading: Boolean = true,
    val error: String? = null,
    val show: Show? = null,
    val episodes: List<Episode> = emptyList(),
    val favourite: Boolean = false,
    val watchLater: Boolean = false,
    val selectedServer: Int = 0,
    val quality: String = "Auto",
    val subtitles: Boolean = false
)

class DetailViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as PikaApp).container
    private val _state = MutableStateFlow(DetailState())
    val state: StateFlow<DetailState> = _state.asStateFlow()

    val downloads: StateFlow<List<DownloadItem>> = container.library.state
        .map { it.downloads }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var loadedId = 0
    private var savedJob: Job? = null

    fun load(id: Int) {
        if (loadedId == id && _state.value.show != null) return
        loadedId = id
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val settings = runCatching { container.settings.settings.first() }.getOrNull()
            runCatching { container.api.show(id) }
                .onSuccess { show ->
                    MediaCache.put("show-${show.id}", show.name)
                    val episodes = runCatching { container.api.episodes(id) }.getOrDefault(emptyList())
                    episodes.forEach { MediaCache.put("episode-${it.id}", it.name) }
                    _state.update {
                        it.copy(
                            loading = false,
                            show = show,
                            episodes = episodes,
                            selectedServer = settings?.preferredServer ?: 0,
                            quality = settings?.defaultQuality ?: "Auto",
                            subtitles = settings?.subtitlesOn ?: false
                        )
                    }
                    refreshSaved()
                }
                .onFailure { error ->
                    _state.update { it.copy(loading = false, error = error.message ?: "Could not load this title") }
                }
        }
    }

    private fun refreshSaved() {
        val id = loadedId
        savedJob?.cancel()
        savedJob = viewModelScope.launch {
            container.library.state.collect { library ->
                val favourite = library.favourites.any { it.id == id }
                val later = library.watchLater.any { it.id == id }
                if (favourite != _state.value.favourite || later != _state.value.watchLater) {
                    _state.update { it.copy(favourite = favourite, watchLater = later) }
                }
            }
        }
    }

    fun toggleFavourite() {
        val show = _state.value.show ?: return
        container.library.toggleFavourite(SavedItem(id = show.id, name = show.name, image = show.image))
    }

    fun toggleWatchLater() {
        val show = _state.value.show ?: return
        container.library.toggleWatchLater(SavedItem(id = show.id, name = show.name, image = show.image))
    }

    fun selectServer(index: Int) {
        _state.update { it.copy(selectedServer = index) }
        viewModelScope.launch { container.settings.setPreferredServer(index) }
    }

    fun selectQuality(quality: String) {
        _state.update { it.copy(quality = quality) }
        viewModelScope.launch { container.settings.setDefaultQuality(quality) }
    }

    fun toggleSubtitles() {
        val next = !_state.value.subtitles
        _state.update { it.copy(subtitles = next) }
        viewModelScope.launch { container.settings.setSubtitles(next) }
    }

    fun startDownload(contentId: Int, episodeId: Int, name: String, image: String?) {
        container.downloads.start(contentId, episodeId, name, image, StreamSources.downloadUrl(contentId + episodeId))
    }

    fun cancelDownload(contentId: Int, episodeId: Int) = container.downloads.cancel(contentId, episodeId)

    fun localPath(contentId: Int, episodeId: Int): String = container.downloads.localPath(contentId, episodeId)
}
