package com.fivucsas.shared.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import java.io.File

/**
 * Desktop File Saver Implementation (Adapter)
 *
 * Uses Swing JFileChooser to present a native save-file dialog.
 * Writes content as UTF-8 text to the selected file.
 *
 * Hexagonal Architecture Role: ADAPTER (implements IFileSaver port)
 */
class DesktopFileSaver : IFileSaver {

    override suspend fun saveTextFile(
        content: String,
        suggestedFileName: String,
        mimeType: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val chooser = JFileChooser().apply {
                dialogTitle = "Export CSV"
                selectedFile = File(suggestedFileName)
                fileFilter = FileNameExtensionFilter("CSV Files (*.csv)", "csv")
                isAcceptAllFileFilterUsed = false
            }

            val result = chooser.showSaveDialog(null)
            if (result != JFileChooser.APPROVE_OPTION) {
                return@withContext Result.failure(
                    IllegalStateException("File save cancelled by user")
                )
            }

            var file = chooser.selectedFile
            // Ensure .csv extension
            if (!file.name.endsWith(".csv", ignoreCase = true)) {
                file = File(file.absolutePath + ".csv")
            }

            file.writeText(content, Charsets.UTF_8)
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
