package com.fivucsas.mobile.android.ui.component

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fivucsas.mobile.android.data.export.DataExportFileWriter
import com.fivucsas.mobile.android.ui.viewmodel.DataExportViewModel
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.ui.theme.AppColors

/**
 * Profile-screen row that launches the GDPR / KVKK data-export flow.
 *
 * Observes [DataExportViewModel] state and renders:
 * - Idle: clickable "Download" row.
 * - Exporting: spinner + "Preparing your export…" copy.
 * - Success: snackbar-style line with file path + auto-launch the Android
 *   share-sheet so the user can forward the JSON to Drive, email, etc.
 * - RateLimited: orange copy with Retry-After seconds.
 * - Error: red copy with the failure reason.
 *
 * Visibility contract: the caller (ProfileScreen) decides whether to render
 * this row at all (e.g. only for self-profile), so no role gating lives here.
 */
@Composable
fun ExportDataRow(
    userId: String,
    viewModel: DataExportViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    // TODO(i18n): DATA_EXPORT_* keys in /tmp/i18n_agent_20B.txt
    val titleText = "Export my data"
    val descriptionText =
        "Download a JSON copy of all personal data we store about you (GDPR Art. 20 / KVKK)."
    val inProgressText = "Preparing your export…"
    val shareSheetTitle = "Share your data export"

    // Auto-launch share sheet once the file is written.
    LaunchedEffect(state) {
        val current = state
        if (current is DataExportViewModel.State.Success) {
            runCatching {
                val sendIntent: Intent = if (current.filePath.startsWith("content://")) {
                    // API 29+ MediaStore.Downloads path — use the content URI directly.
                    Intent(Intent.ACTION_SEND).apply {
                        type = "application/json"
                        putExtra(Intent.EXTRA_STREAM, android.net.Uri.parse(current.filePath))
                        putExtra(Intent.EXTRA_SUBJECT, current.fileName)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                } else {
                    // Legacy absolute path — delegate to FileProvider via the
                    // existing DataExportFileWriter helper.
                    DataExportFileWriter.buildShareIntent(context, current.filePath)
                        ?: Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(Intent.EXTRA_SUBJECT, current.fileName)
                        }
                }
                val chooser = Intent.createChooser(sendIntent, shareSheetTitle).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            }
            // Reset so re-clicking works and the share-sheet doesn't re-open on recomposition.
            viewModel.reset()
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = state !is DataExportViewModel.State.Exporting) {
                viewModel.exportData(userId)
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(UIDimens.SpacingMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium)
        ) {
            if (state is DataExportViewModel.State.Exporting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 2.dp,
                    color = AppColors.Primary,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(28.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.OnSurface,
                )
                Text(
                    text = descriptionText,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.OnSurfaceVariant,
                )
                when (val s = state) {
                    is DataExportViewModel.State.Exporting -> {
                        Text(
                            text = inProgressText,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.Primary,
                        )
                    }
                    is DataExportViewModel.State.RateLimited -> {
                        // TODO(i18n): DATA_EXPORT_* keys in /tmp/i18n_agent_20B.txt
                        val retry = s.retryAfterSeconds?.let { "$it s" } ?: "a moment"
                        Text(
                            text = "Rate-limited. Please try again in $retry.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.Error,
                        )
                    }
                    is DataExportViewModel.State.Error -> {
                        // TODO(i18n): DATA_EXPORT_* keys in /tmp/i18n_agent_20B.txt
                        Text(
                            text = "Export failed: ${s.message}",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.Error,
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}
