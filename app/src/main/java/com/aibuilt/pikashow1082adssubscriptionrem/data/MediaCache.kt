package com.aibuilt.pikashow1082adssubscriptionrem.data

/** Holds the title (and, for Live TV, the direct stream address) of what is about to play. */
object MediaCache {

    private val titles = LinkedHashMap<String, String>()
    private val urls = LinkedHashMap<String, String>()

    fun put(key: String, title: String, directUrl: String? = null) {
        titles[key] = title
        if (directUrl != null) urls[key] = directUrl
    }

    fun title(key: String, fallback: String): String = titles[key] ?: fallback

    fun url(key: String): String? = urls[key]
}
