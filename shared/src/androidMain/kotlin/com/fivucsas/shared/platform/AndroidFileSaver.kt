package com.fivucsas.shared.platform

import android.content.Context
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Android File Saver Implementation (Adapter)
 *
 * Writes CSV files to the public Downloads directory so the user
 * can find them easily via the Files app.
 *
 * Hexagonal Architecture Role: ADAPTER (implements IFileSaver port)
 */
class AndroidFileSaver(private val context: Context) : IFileSaver {

    override suspend fun saveTextFile(
        content: String,
        suggestedFileName: String,
        mimeType: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            // Avoid overwriting: append suffix if file exists
            var file = File(downloadsDir, suggestedFileName)
            var counter = 1
            while (file.exists()) {
                val baseName = suggestedFileName.substringBeforeLast(".")
                val extension = suggestedFileName.substringAfterLast(".", "csv")
                file = File(downloadsDir, "${baseName}_$counter.$extension")
                counter++
            }

            file.writeText(content, Charsets.UTF_8)
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
