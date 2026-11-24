package com.aibuilt.pikashow1082adssubscriptionrem.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.aibuilt.pikashow1082adssubscriptionrem.PikaApp
import com.aibuilt.pikashow1082adssubscriptionrem.data.model.DownloadItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class DownloadRepository(
    private val context: Context,
    private val library: LibraryStore,
    private val client: OkHttpClient
) {

    fun directory(): File = File(context.filesDir, "downloads").apply { mkdirs() }

    fun start(contentId: Int, episodeId: Int, name: String, image: String?, url: String) {
        library.upsertDownload(
            DownloadItem(
                id = contentId,
                episodeId = episodeId,
                name = name,
                image = image,
                url = url,
                status = "queued"
            )
        )
        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(workDataOf("id" to contentId, "episodeId" to episodeId, "url" to url))
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .addTag(tag(contentId, episodeId))
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(tag(contentId, episodeId), ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(contentId: Int, episodeId: Int) {
        WorkManager.getInstance(context).cancelUniqueWork(tag(contentId, episodeId))
        File(directory(), file(contentId, episodeId)).delete()
        library.removeDownload(contentId, episodeId)
    }

    fun delete(contentId: Int, episodeId: Int) {
        File(directory(), file(contentId, episodeId)).delete()
        library.removeDownload(contentId, episodeId)
    }

    fun tag(contentId: Int, episodeId: Int) = "download-$contentId-$episodeId"

    fun file(contentId: Int, episodeId: Int) = "pika-$contentId-$episodeId.mp4"

    fun localPath(contentId: Int, episodeId: Int): String = File(directory(), file(contentId, episodeId)).absolutePath
}

class DownloadWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val container get() = (applicationContext as PikaApp).container

    override suspend fun doWork(): Result {
        val contentId = inputData.getInt("id", 0)
        val episodeId = inputData.getInt("episodeId", 0)
        val url = inputData.getString("url") ?: return Result.failure()
        val store = container.library

        val target = File(container.downloads.directory(), container.downloads.file(contentId, episodeId))
        if (target.exists() && target.length() > 0) {
            store.patchDownload(contentId, episodeId) {
                it.copy(path = target.absolutePath, status = "completed", bytes = target.length(), total = target.length())
            }
            return Result.success()
        }

        runCatching { setForeground(notification(contentId)) }

        return withContext(Dispatchers.IO) {
            val request = Request.Builder().url(url).header("User-Agent", "PikaShow/10.8.2 (Android)").build()
            runCatching {
                container.okHttp.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) error("HTTP ${response.code}")
                    val body = response.body ?: error("Empty body")
                    val total = body.contentLength()
                    store.patchDownload(contentId, episodeId) { it.copy(status = "downloading", total = total) }
                    body.byteStream().use { input ->
                        target.outputStream().use { output ->
                            val buffer = ByteArray(64 * 1024)
                            var written = 0L
                            var lastUpdate = 0L
                            while (true) {
                                if (isStopped) throw IllegalStateException("cancelled")
                                val read = input.read(buffer)
                                if (read <= 0) break
                                output.write(buffer, 0, read)
                                written += read
                                val now = System.currentTimeMillis()
                                if (now - lastUpdate > 400) {
                                    lastUpdate = now
                                    val progress = written
                                    store.patchDownload(contentId, episodeId) { it.copy(bytes = progress) }
                                }
                            }
                        }
                    }
                }
            }.fold(
                onSuccess = {
                    store.patchDownload(contentId, episodeId) {
                        it.copy(path = target.absolutePath, status = "completed", bytes = target.length(), total = target.length())
                    }
                    Result.success()
                },
                onFailure = { error ->
                    if (isStopped) {
                        Result.failure()
                    } else if (runAttemptCount < 3) {
                        store.patchDownload(contentId, episodeId) { it.copy(status = "queued") }
                        Result.retry()
                    } else {
                        target.delete()
                        store.patchDownload(contentId, episodeId) { it.copy(status = "failed") }
                        Result.failure()
                    }.also { if (error !is IllegalStateException) Unit }
                }
            )
        }
    }

    private fun notification(contentId: Int): ForegroundInfo {
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL) == null) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL, "Downloads", NotificationManager.IMPORTANCE_LOW)
            )
        }
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Downloading")
            .setContentText("Saving video for offline playback")
            .setOngoing(true)
            .setProgress(0, 0, true)
            .build()
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            ForegroundInfo(contentId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(contentId, notification)
        }
    }

    companion object {
        private const val CHANNEL = "pika_downloads"
    }
}
