package com.fivucsas.mobile.android.data.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File

/**
 * Android helper for the GDPR / KVKK data-export flow.
 *
 * The shared [com.fivucsas.shared.platform.IFileSaver] already writes the
 * JSON bundle to the public `Downloads/` directory (see
 * [com.fivucsas.shared.platform.AndroidFileSaver]). This helper adds the
 * Android-specific follow-up that the shared module can't do without a
 * `Context`: fire a `ACTION_SEND` chooser so the user can relocate /
 * back up the file to Drive, email, etc.
 *
 * We expose a [FileProvider] URI (not `file://`) because
 * `StrictMode.FileUriExposedException` will crash the app on API 24+
 * when sharing `file://` URIs.
 *
 * The provider authority is `${applicationId}.fileprovider`; it must match
 * the `android:authorities` declared in `AndroidManifest.xml`. On prod this
 * resolves to `com.fivucsas.mobile.android.fileprovider`.
 *
 * Note: [com.fivucsas.shared.platform.AndroidFileSaver] currently uses the
 * legacy `Environment.DIRECTORY_DOWNLOADS` path which works out-of-the-box
 * for the app's private external-files dir on API 29+. When we eventually
 * migrate it to `MediaStore.Downloads` (Scoped Storage), this helper stays
 * unchanged — both paths are addressable via a `content://` URI derived
 * from the saver's returned absolute path.
 */
object DataExportFileWriter {

    private const val FILE_PROVIDER_SUFFIX = ".fileprovider"
    private const val JSON_MIME = "application/json"

    /**
     * Build a share-intent chooser for [filePath] (absolute path returned by
     * [com.fivucsas.shared.platform.AndroidFileSaver]).
     *
     * The caller (Compose screen) should wrap the returned [Intent] in
     * [Intent.createChooser] and start it from the current activity.
     *
     * @return a shareable [Intent], or null if the file no longer exists
     *         (e.g. user deleted it from Files app between export + tap).
     */
    fun buildShareIntent(context: Context, filePath: String): Intent? {
        val file = File(filePath)
        if (!file.exists() || !file.canRead()) return null

        val uri: Uri = FileProvider.getUriForFile(
            context,
            context.packageName + FILE_PROVIDER_SUFFIX,
            file,
        )

        return Intent(Intent.ACTION_SEND).apply {
            type = JSON_MIME
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, file.name)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            // On API 24+ we must grant read permission to every package the
            // chooser will show — FLAG_GRANT_READ_URI_PERMISSION alone is
            // sufficient when the receiver is picked via chooser.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }
}
