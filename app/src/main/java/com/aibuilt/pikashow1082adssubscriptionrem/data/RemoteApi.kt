package com.aibuilt.pikashow1082adssubscriptionrem.data

import com.aibuilt.pikashow1082adssubscriptionrem.data.model.Channel
import com.aibuilt.pikashow1082adssubscriptionrem.data.model.Episode
import com.aibuilt.pikashow1082adssubscriptionrem.data.model.Show
import com.aibuilt.pikashow1082adssubscriptionrem.data.model.stripHtml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

@Serializable
private data class ImageDto(val medium: String? = null, val original: String? = null)

@Serializable
private data class RatingDto(val average: Double? = null)

@Serializable
private data class NetworkDto(val name: String? = null)

@Serializable
private data class ShowDto(
    val id: Int = 0,
    val name: String = "",
    val summary: String? = null,
    val image: ImageDto? = null,
    val genres: List<String> = emptyList(),
    val rating: RatingDto? = null,
    val premiered: String? = null,
    val status: String? = null,
    val runtime: Int? = null,
    val network: NetworkDto? = null,
    val officialSite: String? = null
) {
    fun toModel(): Show = Show(
        id = id,
        name = name,
        summary = summary.orEmpty().stripHtml(),
        image = image?.original ?: image?.medium,
        genres = genres,
        rating = rating?.average,
        premiered = premiered,
        status = status,
        runtime = runtime,
        network = network?.name,
        officialSite = officialSite
    )
}

@Serializable
private data class SearchDto(val show: ShowDto? = null)

@Serializable
private data class EpisodeDto(
    val id: Int = 0,
    val name: String = "",
    val season: Int = 1,
    val number: Int? = null,
    val summary: String? = null,
    val image: ImageDto? = null,
    val runtime: Int? = null
)

class RemoteApi(private val client: OkHttpClient, private val json: Json) {

    private suspend fun get(url: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "PikaShow/10.8.2 (Android)")
            .header("Accept", "application/json, text/plain, */*")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Server returned ${response.code}")
            response.body?.string() ?: error("Empty response")
        }
    }

    suspend fun shows(page: Int): List<Show> {
        val body = get("https://api.tvmaze.com/shows?page=$page")
        return json.decodeFromString<List<ShowDto>>(body).map { it.toModel() }
    }

    suspend fun show(id: Int): Show {
        val body = get("https://api.tvmaze.com/shows/$id")
        return json.decodeFromString<ShowDto>(body).toModel()
    }

    suspend fun search(query: String): List<Show> {
        if (query.isBlank()) return emptyList()
        val body = get("https://api.tvmaze.com/search/shows?q=" + java.net.URLEncoder.encode(query, "UTF-8"))
        return json.decodeFromString<List<SearchDto>>(body).mapNotNull { it.show?.toModel() }
    }

    suspend fun episodes(showId: Int): List<Episode> {
        val body = runCatching { get("https://api.tvmaze.com/shows/$showId/episodes") }.getOrNull() ?: return emptyList()
        return runCatching {
            json.decodeFromString<List<EpisodeDto>>(body).map {
                Episode(
                    id = it.id,
                    showId = showId,
                    name = it.name,
                    season = it.season,
                    number = it.number,
                    summary = it.summary.orEmpty().stripHtml(),
                    image = it.image?.medium,
                    runtime = it.runtime
                )
            }
        }.getOrDefault(emptyList())
    }
}

/**
 * Live TV is read from free public playlists that already list real, working streams.
 */
class LiveTvApi(private val client: OkHttpClient) {

    private val extInf = Regex("""#EXTINF:-1(.*),(.*)""")
    private val logoAttr = Regex("""tvg-logo="([^"]*)"""")
    private val groupAttr = Regex("""group-title="([^"]*)"""")

    val countries: List<Pair<String, String>> = listOf(
        "in" to "India",
        "us" to "USA",
        "gb" to "UK",
        "ae" to "UAE",
        "ca" to "Canada",
        "au" to "Australia"
    )

    suspend fun channels(country: String): List<Channel> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://iptv-org.github.io/iptv/countries/$country.m3u")
            .header("User-Agent", "PikaShow/10.8.2 (Android)")
            .build()
        val text = runCatching {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("Server returned ${response.code}")
                response.body?.string().orEmpty()
            }
        }.getOrElse { throw it }

        val result = mutableListOf<Channel>()
        var pendingName: String? = null
        var pendingLogo: String? = null
        var pendingGroup: String = "Live"
        for (raw in text.lineSequence()) {
            val line = raw.trim()
            if (line.isEmpty()) continue
            if (line.startsWith("#EXTINF")) {
                val match = extInf.find(line)
                val attrs = match?.groupValues?.getOrNull(1).orEmpty()
                pendingName = match?.groupValues?.getOrNull(2)?.trim()
                pendingLogo = logoAttr.find(attrs)?.groupValues?.getOrNull(1)?.takeIf { it.isNotBlank() }
                pendingGroup = groupAttr.find(attrs)?.groupValues?.getOrNull(1)?.takeIf { it.isNotBlank() } ?: "Live"
            } else if (!line.startsWith("#") && pendingName != null) {
                val name = pendingName!!
                if (line.startsWith("http")) {
                    result += Channel(
                        id = line.hashCode().toString(),
                        name = name,
                        logo = pendingLogo,
                        url = line,
                        group = pendingGroup
                    )
                }
                pendingName = null
                pendingLogo = null
            }
        }
        result.distinctBy { it.url }
    }
}
