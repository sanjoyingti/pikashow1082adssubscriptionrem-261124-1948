package com.aibuilt.pikashow1082adssubscriptionrem.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Keeps the latest settings in memory so screens can read them instantly. */
class SettingsHolder(private val store: SettingsStore) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _value = MutableStateFlow(Settings())
    val value: StateFlow<Settings> = _value

    /** Synchronous snapshot of the newest settings. */
    val current: Settings get() = _value.value

    init {
        scope.launch { store.settings.collect { _value.value = it } }
    }
}
