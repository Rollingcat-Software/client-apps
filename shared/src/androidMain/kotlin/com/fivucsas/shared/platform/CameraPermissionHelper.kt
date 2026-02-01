package com.fivucsas.shared.platform

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

/**
 * Camera Permission Helper
 *
 * Provides utilities for handling camera permissions on Android.
 * Uses Accompanist Permissions library for a Compose-friendly API.
 */

/**
 * Camera Permission State
 */
sealed class CameraPermissionState {
    data object Granted : CameraPermissionState()
    data object Denied : CameraPermissionState()
    data object PermanentlyDenied : CameraPermissionState()
    data object RequestRequired : CameraPermissionState()
}

/**
 * Composable for handling camera permission requests.
 *
 * Tracks whether a request has already been made so it can distinguish
 * "not yet asked" from "permanently denied" (where shouldShowRationale
 * is false AND isGranted is false after the first request).
 *
 * @param onPermissionGranted Callback when permission is granted
 * @param onPermissionDenied Callback when permission is denied but can be re-asked
 * @param onPermissionPermanentlyDenied Callback when permission is permanently denied
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestCameraPermission(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
    onPermissionPermanentlyDenied: () -> Unit
) {
    val cameraPermissionState = rememberPermissionState(
        Manifest.permission.CAMERA
    )

    // Track whether we have already launched the permission request at least once
    var hasRequestedOnce by remember { mutableStateOf(false) }

    LaunchedEffect(cameraPermissionState.status) {
        when {
            cameraPermissionState.status.isGranted -> {
                onPermissionGranted()
            }
            cameraPermissionState.status.shouldShowRationale -> {
                // User denied but can still be asked again
                onPermissionDenied()
            }
            hasRequestedOnce -> {
                // Not granted, no rationale, and we already asked once =>
                // the user selected "Don't ask again" or the system won't show the dialog
                onPermissionPermanentlyDenied()
            }
            else -> {
                // First time: launch the system permission dialog
                hasRequestedOnce = true
                cameraPermissionState.launchPermissionRequest()
            }
        }
    }
}

/**
 * A button that opens the app's system settings page so the user
 * can manually grant camera permission after permanently denying it.
 */
@Composable
fun OpenAppSettingsButton(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Button(
        onClick = {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        },
        modifier = modifier
    ) {
        Text("Open Settings")
    }
}

/**
 * A small card shown when camera permission has been permanently denied.
 * Contains an explanation and an "Open Settings" button.
 */
@Composable
fun PermanentlyDeniedPermissionCard(
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.padding(16.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Camera permission is required for face enrollment.",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Please enable camera access in your device settings.",
                style = MaterialTheme.typography.bodySmall
            )
            OpenAppSettingsButton()
        }
    }
}

/**
 * Check if camera permission is granted (non-composable utility).
 */
object CameraPermissionChecker {
    fun isGranted(context: android.content.Context): Boolean {
        return androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    fun getState(context: android.content.Context): CameraPermissionState {
        return if (isGranted(context)) {
            CameraPermissionState.Granted
        } else {
            CameraPermissionState.RequestRequired
        }
    }
}
