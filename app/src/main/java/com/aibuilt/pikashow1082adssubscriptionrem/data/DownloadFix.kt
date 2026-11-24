package com.aibuilt.pikashow1082adssubscriptionrem.data

import com.aibuilt.pikashow1082adssubscriptionrem.data.model.DownloadItem

/** Helpers used by the download screens so percentages are always safe to show. */
fun DownloadItem.percent(): Int {
    if (status == "completed") return 100
    if (total <= 0L) return 0
    return ((bytes * 100) / total).toInt().coerceIn(0, 100)
}

fun DownloadItem.isReady(): Boolean = status == "completed" && path.isNotBlank()
