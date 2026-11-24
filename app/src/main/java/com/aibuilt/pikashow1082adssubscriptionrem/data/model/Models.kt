package com.aibuilt.pikashow1082adssubscriptionrem.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Show(
    val id: Int,
    val name: String,
    val summary: String = "",
    val image: String? = null,
    val genres: List<String> = emptyList(),
    val rating: Double? = null,
    val premiered: String? = null,
    val status: String? = null,
    val runtime: Int? = null,
    val network: String? = null,
    val officialSite: String? = null
)

@Serializable
data class Episode(
    val id: Int,
    val showId: Int,
    val name: String,
    val season: Int,
    val number: Int?,
    val summary: String = "",
    val image: String? = null,
    val runtime: Int? = null
)

data class Channel(
    val id: String,
    val name: String,
    val logo: String?,
    val url: String,
    val group: String
)

@Serializable
data class SavedItem(
    val id: Int,
    val name: String,
    val image: String? = null,
    val kind: String = "show",
    val addedAt: Long = System.currentTimeMillis()
)

@Serializable
data class ProgressItem(
    val id: Int,
    val episodeId: Int = 0,
    val name: String,
    val image: String? = null,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val key: String get() = "$id-$episodeId"
    val fraction: Float get() = if (durationMs <= 0) 0f else (positionMs.toFloat() / durationMs).coerceIn(0f, 1f)
}

@Serializable
data class DownloadItem(
    val id: Int,
    val episodeId: Int = 0,
    val name: String,
    val image: String? = null,
    val url: String = "",
    val path: String = "",
    val bytes: Long = 0,
    val total: Long = 0,
    val status: String = "queued",
    val updatedAt: Long = System.currentTimeMillis()
)

fun String.stripHtml(): String =
    this.replace(Regex("<[^>]*>"), " ")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace(Regex("\\s+"), " ")
        .trim()
