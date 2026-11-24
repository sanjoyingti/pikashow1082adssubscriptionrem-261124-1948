package com.aibuilt.pikashow1082adssubscriptionrem.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aibuilt.pikashow1082adssubscriptionrem.PikaApp
import com.aibuilt.pikashow1082adssubscriptionrem.data.MediaCache
import com.aibuilt.pikashow1082adssubscriptionrem.data.model.Show
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchState(
    val query: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val results: List<Show> = emptyList(),
    val searched: Boolean = false
)

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as PikaApp).container
    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    val history = container.library.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), container.library.state.value)
        .let { flow -> flow }

    private var job: Job? = null

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
        job?.cancel()
        if (query.isBlank()) {
            _state.update { it.copy(results = emptyList(), loading = false, error = null, searched = false) }
            return
        }
        job = viewModelScope.launch {
            delay(400)
            runSearch(query)
        }
    }

    fun submit() {
        val query = _state.value.query
        if (query.isBlank()) return
        job?.cancel()
        viewModelScope.launch { runSearch(query) }
    }

    fun openHistory(term: String) {
        _state.update { it.copy(query = term) }
        job?.cancel()
        viewModelScope.launch { runSearch(term) }
    }

    private suspend fun runSearch(query: String) {
        _state.update { it.copy(loading = true, error = null, searched = true) }
        runCatching { container.api.search(query) }
            .onSuccess { list ->
                val usable = list.filter { !it.image.isNullOrBlank() }
                usable.forEach { MediaCache.put("show-${it.id}", it.name) }
                container.library.addSearchTerm(query)
                _state.update { it.copy(loading = false, results = usable, error = null) }
            }
            .onFailure { error ->
                _state.update { it.copy(loading = false, error = error.message ?: "Search failed") }
            }
    }
}
