package com.aibuilt.pikashow1082adssubscriptionrem.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aibuilt.pikashow1082adssubscriptionrem.PikaApp
import com.aibuilt.pikashow1082adssubscriptionrem.data.MediaCache
import com.aibuilt.pikashow1082adssubscriptionrem.data.model.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LiveState(
    val country: String = "in",
    val loading: Boolean = false,
    val error: String? = null,
    val channels: List<Channel> = emptyList(),
    val query: String = ""
)

class LiveViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as PikaApp).container

    private val _state = MutableStateFlow(LiveState())
    val state: StateFlow<LiveState> = _state.asStateFlow()

    val countries: List<Pair<String, String>> = container.liveApi.countries

    init {
        load("in")
    }

    fun selectCountry(code: String) {
        if (_state.value.country == code && _state.value.channels.isNotEmpty()) return
        load(code)
    }

    fun onQueryChange(query: String) = _state.update { it.copy(query = query) }

    fun load(code: String = _state.value.country) {
        viewModelScope.launch {
            _state.update { it.copy(country = code, loading = true, error = null, channels = it.channels.takeIf { c -> c.isNotEmpty() && it.country == code }.orEmpty()) }
            runCatching { container.liveApi.channels(code) }
                .onSuccess { list ->
                    _state.update { it.copy(loading = false, channels = list, error = null) }
                }
                .onFailure { error ->
                    _state.update { it.copy(loading = false, error = error.message ?: "Could not load channels") }
                }
        }
    }

    fun visible(): List<Channel> {
        val current = _state.value
        val query = current.query.trim()
        if (query.isBlank()) return current.channels
        return current.channels.filter { it.name.contains(query, true) || it.group.contains(query, true) }
    }

    fun prepare(channel: Channel): String {
        val key = "live-${channel.id}"
        MediaCache.put(key, channel.name, channel.url)
        return key
    }
}
