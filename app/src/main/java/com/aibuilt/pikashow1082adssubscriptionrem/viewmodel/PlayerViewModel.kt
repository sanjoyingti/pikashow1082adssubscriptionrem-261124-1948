package com.aibuilt.pikashow1082adssubscriptionrem.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.aibuilt.pikashow1082adssubscriptionrem.PikaApp
import com.aibuilt.pikashow1082adssubscriptionrem.data.MediaCache
import com.aibuilt.pikashow1082adssubscriptionrem.data.StreamSources
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class PlaybackState(
    val ready: Boolean = false,
    val title: String = "",
    val error: String? = null,
    val qualities: List<String> = listOf("Auto"),
    val selectedQuality: String = "Auto",
    val subtitlesOn: Boolean = false,
    val speed: Float = 1f,
    val servers: List<String> = emptyList(),
    val selectedServer: Int = 0
)

@UnstableApi
class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as PikaApp).container

    var player: ExoPlayer? = null
        private set

    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private var saveJob: Job? = null
    private var contentId = 0
    private var episodeId = 0
    private var episodeName = ""
    private var image: String? = null
    private var sourceUrl = ""
    private var isLive = false
    private var bound = false

    fun bind(key: String, contentId: Int, episodeId: Int, server: Int, liveUrl: String?, imageUrl: String?) {
        if (bound) return
        bound = true
        this.contentId = contentId
        this.episodeId = episodeId
        this.image = imageUrl
        isLive = !liveUrl.isNullOrBlank()

        val title = MediaCache.title(key, "Now playing")
        episodeName = title

        val localPath = MediaCache.url(key)
        val direct = liveUrl ?: MediaCache.url(key)

        val servers = if (isLive) emptyList() else StreamSources.servers(contentId, episodeId).map { it.name }
        val chosenServer = server.coerceIn(0, (servers.size - 1).coerceAtLeast(0))
        sourceUrl = when {
            isLive -> direct.orEmpty()
            !localPath.isNullOrBlank() && localPath.startsWith("/") -> localPath
            else -> StreamSources.servers(contentId, episodeId).getOrNull(chosenServer)?.url.orEmpty()
        }

        _state.update {
            it.copy(
                title = title,
                servers = servers,
                selectedServer = chosenServer,
                subtitlesOn = container.settingsState.value.value.subtitlesOn && !isLive
            )
        }
        createPlayer()
        viewModelScope.launch { prepare(resume = !isLive) }
    }

    private fun createPlayer() {
        if (player != null) return
        player = ExoPlayer.Builder(getApplication()).build().apply {
            playWhenReady = true
            addListener(object : Player.Listener {
                override fun onTracksChanged(tracks: androidx.media3.common.Tracks) {
                    val heights = tracks.groups
                        .filter { it.type == C.TRACK_TYPE_VIDEO }
                        .flatMap { group -> (0 until group.length).mapNotNull { group.getTrackFormat(it).height } }
                        .filter { it > 0 }
                        .distinct()
                        .sortedDescending()
                        .map { "${it}p" }
                    _state.update { it.copy(qualities = listOf("Auto") + heights) }
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    _state.update { it.copy(error = error.errorCodeName + ": " + (error.message ?: "playback error")) }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) _state.update { it.copy(ready = true) }
                }
            })
        }
    }

    private suspend fun prepare(resume: Boolean) {
        val exo = player ?: return
        val uri = when {
            sourceUrl.startsWith("http") -> Uri.parse(sourceUrl)
            sourceUrl.startsWith("/") -> Uri.fromFile(File(sourceUrl))
            else -> Uri.parse(sourceUrl)
        }
        if (sourceUrl.isBlank()) {
            _state.update { it.copy(error = "No stream address") }
            return
        }
        val builder = MediaItem.Builder().setUri(uri)
        if (_state.value.subtitlesOn) {
            builder.setSubtitleConfigurations(
                StreamSources.subtitles.map { subtitle ->
                    MediaItem.SubtitleConfiguration.Builder(Uri.parse(subtitle.url))
                        .setMimeType(MimeTypes.TEXT_VTT)
                        .setLanguage("en")
                        .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                        .setLabel(subtitle.label)
                        .build()
                }
            )
        }
        exo.setMediaItem(builder.build())
        if (resume) {
            val saved = container.library.state.value.progress.firstOrNull {
                it.id == contentId && it.episodeId == episodeId
            }
            if (saved != null && saved.positionMs > 5_000) exo.seekTo(saved.positionMs)
        }
        exo.prepare()
        exo.playWhenReady = true
        startProgressLoop()
    }

    private fun startProgressLoop() {
        if (isLive) return
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            while (true) {
                delay(3_000)
                val exo = player ?: continue
                val duration = exo.duration
                if (duration > 0) {
                    container.library.saveProgress(
                        contentId,
                        episodeId,
                        episodeName,
                        image,
                        exo.currentPosition,
                        duration
                    )
                }
            }
        }
    }

    fun selectServer(index: Int) {
        if (index == _state.value.selectedServer || isLive) return
        _state.update { it.copy(selectedServer = index, error = null, ready = false) }
        sourceUrl = StreamSources.servers(contentId, episodeId).getOrNull(index)?.url.orEmpty()
        viewModelScope.launch { prepare(resume = false) }
    }

    fun selectQuality(quality: String) {
        _state.update { it.copy(selectedQuality = quality) }
        val exo = player ?: return
        val height = StreamSources.qualities.firstOrNull { it.first == quality }?.second ?: 0
        val builder = exo.trackSelectionParameters.buildUpon()
        if (height == 0) {
            builder.setMaxVideoSize(Int.MAX_VALUE, Int.MAX_VALUE)
        } else {
            builder.setMaxVideoSize(Int.MAX_VALUE, height)
        }
        exo.trackSelectionParameters = builder.build()
    }

    fun toggleSubtitles() {
        val enabled = !_state.value.subtitlesOn
        _state.update { it.copy(subtitlesOn = enabled) }
        viewModelScope.launch { container.settings.setSubtitles(enabled) }
        val exo = player ?: return
        val position = exo.currentPosition
        viewModelScope.launch {
            prepare(resume = false)
            exo.seekTo(position)
        }
    }

    fun setSpeed(speed: Float) {
        _state.update { it.copy(speed = speed) }
        player?.setPlaybackSpeed(speed)
    }

    fun replay() {
        _state.update { it.copy(error = null, ready = false) }
        viewModelScope.launch { prepare(resume = false) }
    }

    fun openExternally(): Boolean {
        val context = getApplication<Application>()
        val uri = when {
            sourceUrl.startsWith("/") -> FileProvider.getUriForFile(
                context,
                context.packageName + ".fileprovider",
                File(sourceUrl)
            )
            else -> Uri.parse(sourceUrl)
        }
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "video/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching { context.startActivity(Intent.createChooser(intent, "Play with")) }.isSuccess
    }

    fun currentPosition(): Long = player?.currentPosition ?: 0L

    override fun onCleared() {
        super.onCleared()
        val exo = player
        if (exo != null && !isLive) {
            val duration = exo.duration
            if (duration > 0) {
                container.library.saveProgress(contentId, episodeId, episodeName, image, exo.currentPosition, duration)
            }
        }
        saveJob?.cancel()
        player?.release()
        player = null
    }
}
