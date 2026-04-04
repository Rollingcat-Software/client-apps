package com.fivucsas.shared.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.writeToFile

/**
 * iOS File Saver Implementation (Adapter)
 *
 * Writes the CSV content to a temporary file. The calling UI layer
 * is expected to present a UIActivityViewController (share sheet)
 * to let the user choose where to save or share the file.
 *
 * Hexagonal Architecture Role: ADAPTER (implements IFileSaver port)
 */
class IosFileSaver : IFileSaver {

    override suspend fun saveTextFile(
        content: String,
        suggestedFileName: String,
        mimeType: String
    ): Result<String> = withContext(Dispatchers.Default) {
        try {
            val tempDir = NSTemporaryDirectory()
            val filePath = "$tempDir$suggestedFileName"

            val nsString = content as NSString
            val success = nsString.writeToFile(
                filePath,
                atomically = true,
                encoding = NSUTF8StringEncoding,
                error = null
            )

            if (success) {
                Result.success(filePath)
            } else {
                Result.failure(RuntimeException("Failed to write CSV file to $filePath"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
