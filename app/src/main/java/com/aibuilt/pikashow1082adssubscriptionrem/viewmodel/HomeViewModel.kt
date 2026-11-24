package com.aibuilt.pikashow1082adssubscriptionrem.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aibuilt.pikashow1082adssubscriptionrem.PikaApp
import com.aibuilt.pikashow1082adssubscriptionrem.data.MediaCache
import com.aibuilt.pikashow1082adssubscriptionrem.data.model.ProgressItem
import com.aibuilt.pikashow1082adssubscriptionrem.data.model.Show
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeState(
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val error: String? = null,
    val shows: List<Show> = emptyList(),
    val genre: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as PikaApp).container

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    val genres = MutableStateFlow<List<String>>(emptyList())

    val continueWatching: StateFlow<List<ProgressItem>> = container.library.state
        .map { it.progress.sortedByDescending { entry -> entry.updatedAt }.take(15) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favouritesCount = container.library.state
        .map { it.favourites.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init {
        load()
    }

    fun load(force: Boolean = false) {
        if (_state.value.loading) return
        if (!force && _state.value.shows.isNotEmpty()) return
        viewModelScope.launch {
            _state.update { it.copy(loading = it.shows.isEmpty(), refreshing = it.shows.isNotEmpty(), error = null) }
            runCatching { container.api.shows(0) + container.api.shows(1) }
                .onSuccess { list ->
                    val usable = list.filter { !it.image.isNullOrBlank() }
                    usable.forEach { MediaCache.put("show-${it.id}", it.name) }
                    _state.update { it.copy(loading = false, refreshing = false, shows = usable, error = null) }
                    genres.value = usable.flatMap { it.genres }
                        .groupingBy { it }
                        .eachCount()
                        .entries
                        .sortedByDescending { it.value }
                        .take(14)
                        .map { it.key }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(loading = false, refreshing = false, error = error.message ?: "Could not reach the catalogue")
                    }
                }
        }
    }

    fun selectGenre(genre: String?) = _state.update { it.copy(genre = genre) }

    fun visibleShows(): List<Show> {
        val current = _state.value
        val genre = current.genre ?: return current.shows
        return current.shows.filter { it.genres.contains(genre) }
    }
}
