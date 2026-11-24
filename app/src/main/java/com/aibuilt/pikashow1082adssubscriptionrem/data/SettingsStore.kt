package com.aibuilt.pikashow1082adssubscriptionrem.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "pika_settings")

data class Settings(
    val themeMode: Int = 0,
    val defaultQuality: String = "Auto",
    val preferredServer: Int = 0,
    val subtitlesOn: Boolean = false,
    val autoplay: Boolean = true,
    val proActive: Boolean = true,
    val accountName: String = "",
    val accountEmail: String = ""
)

class SettingsStore(private val context: Context) {

    private object Keys {
        val THEME = intPreferencesKey("theme_mode")
        val QUALITY = stringPreferencesKey("default_quality")
        val SERVER = intPreferencesKey("preferred_server")
        val SUBTITLES = booleanPreferencesKey("subtitles_on")
        val AUTOPLAY = booleanPreferencesKey("autoplay")
        val PRO = booleanPreferencesKey("pro_active")
        val NAME = stringPreferencesKey("account_name")
        val EMAIL = stringPreferencesKey("account_email")
    }

    val settings: Flow<Settings> = context.dataStore.data.map { prefs ->
        Settings(
            themeMode = prefs[Keys.THEME] ?: 0,
            defaultQuality = prefs[Keys.QUALITY] ?: "Auto",
            preferredServer = prefs[Keys.SERVER] ?: 0,
            subtitlesOn = prefs[Keys.SUBTITLES] ?: false,
            autoplay = prefs[Keys.AUTOPLAY] ?: true,
            // Subscription removed: always PRO, whatever is stored.
            proActive = true,
            accountName = prefs[Keys.NAME].orEmpty(),
            accountEmail = prefs[Keys.EMAIL].orEmpty()
        )
    }

    suspend fun setThemeMode(mode: Int) = context.dataStore.edit { it[Keys.THEME] = mode }

    suspend fun setDefaultQuality(quality: String) = context.dataStore.edit { it[Keys.QUALITY] = quality }

    suspend fun setPreferredServer(index: Int) = context.dataStore.edit { it[Keys.SERVER] = index }

    suspend fun setSubtitles(on: Boolean) = context.dataStore.edit { it[Keys.SUBTITLES] = on }

    suspend fun setAutoplay(on: Boolean) = context.dataStore.edit { it[Keys.AUTOPLAY] = on }

    suspend fun forcePro() = context.dataStore.edit { it[Keys.PRO] = true }

    suspend fun setAccount(name: String, email: String) = context.dataStore.edit {
        it[Keys.NAME] = name
        it[Keys.EMAIL] = email
    }

    suspend fun signOut() = context.dataStore.edit {
        it.remove(Keys.NAME)
        it.remove(Keys.EMAIL)
    }
}
