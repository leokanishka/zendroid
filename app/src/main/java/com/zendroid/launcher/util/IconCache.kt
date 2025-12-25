package com.zendroid.launcher.util

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.collection.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import com.zendroid.launcher.config.Constants
import javax.inject.Singleton

@Singleton
class IconCache @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Cache size calculated based on available memory (HIGH Issue Fix 1)
    // Assumes ~150KB per icon (192x192 ARGB_8888)
    // Use 1/32 of available memory for icon cache
    private val cache = LruCache<String, ImageBitmap>(calculateCacheSize())

    private fun calculateCacheSize(): Int {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val memoryClass = am.memoryClass // MB available per app
        val cacheMemoryMB = memoryClass / 32 // Use ~3% of total memory
        val iconSizeKB = 150
        val maxIcons = (cacheMemoryMB * 1024) / iconSizeKB
        return maxIcons.coerceIn(Constants.ICON_CACHE_MIN_ICONS, Constants.ICON_CACHE_MAX_ICONS) // Min/Max from Constants
    }

    fun getIcon(packageName: String): ImageBitmap? {
        val cached = cache.get(packageName)
        if (cached != null) return cached

        return try {
            val pm = context.packageManager
            val drawable = pm.getApplicationIcon(packageName)
            val bitmap = drawable.toBitmap().asImageBitmap()
            cache.put(packageName, bitmap)
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    fun clear() {
        cache.evictAll()
    }
}
