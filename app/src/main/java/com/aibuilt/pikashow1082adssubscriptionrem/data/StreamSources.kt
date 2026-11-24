package com.aibuilt.pikashow1082adssubscriptionrem.data

import kotlin.math.abs

/**
 * Real, publicly reachable demo streams that behave like the original servers:
 * adaptive HLS for the first servers and progressive files for the last one
 * (the progressive one is also what the download button saves).
 */
object StreamSources {

    data class Server(val name: String, val url: String)

    private val hlsPool = listOf(
        "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
        "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_fmp4/master.m3u8",
        "https://storage.googleapis.com/shaka-demo-assets/angel-one-hls/hls.m3u8",
        "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
        "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8",
        "https://test-streams.mux.dev/pts_shift/master.m3u8"
    )

    private val progressivePool = listOf(
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
    )

    val qualities: List<Pair<String, Int>> = listOf(
        "Auto" to 0,
        "1080p" to 1080,
        "720p" to 720,
        "480p" to 480,
        "360p" to 360
    )

    data class Subtitle(val label: String, val url: String)

    val subtitles: List<Subtitle> = listOf(
        Subtitle(
            "English",
            "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_fmp4/webvtt/eng.webvtt"
        )
    )

    fun servers(contentId: Int, episodeId: Int): List<Server> {
        val seed = abs(contentId * 37 + episodeId * 11)
        val list = mutableListOf<Server>()
        for (index in 0 until 3) {
            val pool = if (index == 2) progressivePool else hlsPool
            list += Server("Server ${index + 1}", pool[seed % pool.size].let { pool[(seed + index * 5) % pool.size] })
        }
        return list.distinctBy { it.url }.ifEmpty { listOf(Server("Server 1", progressivePool[seed % progressivePool.size])) }
    }

    fun downloadUrl(contentId: Int): String = progressivePool[abs(contentId) % progressivePool.size]
}
