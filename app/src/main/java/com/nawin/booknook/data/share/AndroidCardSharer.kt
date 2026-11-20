package com.nawin.booknook.data.share

import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.nawin.booknook.domain.service.CardSharer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * Shares the card through a cache file exposed by a [FileProvider], and saves
 * it to Pictures/BookNook with MediaStore (Android 10+) or as a plain file on
 * older versions, which needs the storage permission the screen asks for.
 */
class AndroidCardSharer(private val context: Context) : CardSharer {

    override suspend fun share(png: ByteArray) {
        val file = withContext(Dispatchers.IO) {
            File(context.cacheDir, SHARE_DIR).apply { mkdirs() }
                .resolve(SHARE_FILE)
                .apply { writeBytes(png) }
        }
        val uri = FileProvider.getUriForFile(context, context.packageName + AUTHORITY_SUFFIX, file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = PNG_MIME_TYPE
            putExtra(Intent.EXTRA_STREAM, uri)
            // Lets the share sheet show a preview of the picture.
            clipData = ClipData.newRawUri(null, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        // Started from the application context, so the chooser needs its own task.
        context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    override suspend fun saveToGallery(png: ByteArray, fileName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) saveWithMediaStore(png, fileName) else saveAsFile(png, fileName)
            true
        } catch (e: IOException) {
            false
        } catch (e: SecurityException) {
            false
        }
    }

    private fun saveWithMediaStore(png: ByteArray, fileName: String) {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName + PNG_EXTENSION)
            put(MediaStore.Images.Media.MIME_TYPE, PNG_MIME_TYPE)
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + ALBUM)
            // Hidden from other apps until the bytes are written.
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IOException("Cannot create $fileName in the gallery")
        try {
            val stream = resolver.openOutputStream(uri) ?: throw IOException("Cannot open $uri")
            stream.use { it.write(png) }
            resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
        } catch (e: IOException) {
            resolver.delete(uri, null, null)
            throw e
        }
    }

    @Suppress("DEPRECATION") // The public Pictures folder is the only option before scoped storage.
    private fun saveAsFile(png: ByteArray, fileName: String) {
        val album = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), ALBUM)
        if (!album.exists() && !album.mkdirs()) throw IOException("Cannot create $album")
        val file = album.resolve(fileName + PNG_EXTENSION)
        file.writeBytes(png)
        // Without a scan the gallery would not see the picture until the next reboot.
        MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf(PNG_MIME_TYPE), null)
    }

    private companion object {
        /** Must match the provider authority and the cache path in the manifest's file paths. */
        const val AUTHORITY_SUFFIX = ".fileprovider"
        const val SHARE_DIR = "shared"
        const val SHARE_FILE = "booknook.png"
        const val ALBUM = "BookNook"
        const val PNG_MIME_TYPE = "image/png"
        const val PNG_EXTENSION = ".png"
    }
}
