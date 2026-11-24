package com.aibuilt.pikashow1082adssubscriptionrem

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import com.aibuilt.pikashow1082adssubscriptionrem.data.DownloadRepository
import com.aibuilt.pikashow1082adssubscriptionrem.data.LibraryStore
import com.aibuilt.pikashow1082adssubscriptionrem.data.LiveTvApi
import com.aibuilt.pikashow1082adssubscriptionrem.data.RemoteApi
import com.aibuilt.pikashow1082adssubscriptionrem.data.SettingsHolder
import com.aibuilt.pikashow1082adssubscriptionrem.data.SettingsStore
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {

    val okHttp: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    val api = RemoteApi(okHttp, json)
    val liveApi = LiveTvApi(okHttp)
    val library = LibraryStore(File(context.filesDir, "library.json"))
    val settings = SettingsStore(context)
    val settingsState = SettingsHolder(settings)
    val downloads = DownloadRepository(context, library, okHttp)
}

class PikaApp : Application(), SingletonImageLoader.Factory {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components { add(OkHttpNetworkFetcherFactory()) }
            .crossfade(true)
            .build()
}
