package com.aibuilt.pikashow1082adssubscriptionrem.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aibuilt.pikashow1082adssubscriptionrem.PikaApp
import com.aibuilt.pikashow1082adssubscriptionrem.data.Settings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as PikaApp).container

    val settings: StateFlow<Settings> = container.settings.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Settings())

    fun setTheme(mode: Int) = viewModelScope.launch { container.settings.setThemeMode(mode) }

    fun setQuality(quality: String) = viewModelScope.launch { container.settings.setDefaultQuality(quality) }

    fun setServer(index: Int) = viewModelScope.launch { container.settings.setPreferredServer(index) }

    fun setSubtitles(on: Boolean) = viewModelScope.launch { container.settings.setSubtitles(on) }

    fun setAutoplay(on: Boolean) = viewModelScope.launch { container.settings.setAutoplay(on) }

    fun signIn(name: String, email: String) = viewModelScope.launch {
        container.settings.setAccount(name.trim(), email.trim())
        container.settings.forcePro()
    }

    fun signOut() = viewModelScope.launch {
        container.settings.signOut()
        container.library.clearProgress()
        container.library.clearSearchHistory()
    }
}
