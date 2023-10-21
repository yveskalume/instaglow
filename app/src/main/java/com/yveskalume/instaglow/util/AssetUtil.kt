package com.yveskalume.instaglow.util

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager.ACCESS_BUFFER
import android.os.ParcelFileDescriptor
import android.os.ParcelFileDescriptor.MODE_READ_ONLY
import com.google.common.io.ByteStreams
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


object AssetsUtil {
    /**
     * Gets AssetFileDescriptor directly for given a path, or returns its copy by caching for the
     * compressed one.
     */
    @Throws(IOException::class)
    fun getAssetFileDescriptorOrCached(
        context: Context, assetPath: String
    ): AssetFileDescriptor {
        return try {
            context.assets.openFd(assetPath)
        } catch (e: FileNotFoundException) {
            // If it cannot read from asset file (probably compressed), try copying to cache folder and
            // reloading.
            val cacheFile = File(context.cacheDir, assetPath)
            cacheFile.parentFile?.mkdirs()
            copyToCacheFile(context, assetPath, cacheFile)
            val cachedFd = ParcelFileDescriptor.open(cacheFile, MODE_READ_ONLY)
            AssetFileDescriptor(cachedFd, 0, cacheFile.length())
        }
    }

    @Throws(IOException::class)
    private fun copyToCacheFile(context: Context, assetPath: String, cacheFile: File) {
        context.assets.open(assetPath, ACCESS_BUFFER).use { inputStream ->
            FileOutputStream(cacheFile, false).use { fileOutputStream ->
                ByteStreams.copy(
                    inputStream,
                    fileOutputStream
                )
            }
        }
    }
}