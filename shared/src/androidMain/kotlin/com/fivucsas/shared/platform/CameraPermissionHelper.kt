package com.fivucsas.shared.platform

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

/**
 * Camera Permission Helper
 *
 * Provides utilities for handling camera permissions on Android.
 * Uses Accompanist Permissions library for a Compose-friendly API.
 *
 * Design Principles:
 * - Single Responsibility Principle: Only handles camera permissions
 * - Separation of Concerns: Separates permission logic from camera logic
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
 * Composable for handling camera permission requests
 *
 * @param onPermissionGranted Callback when permission is granted
 * @param onPermissionDenied Callback when permission is denied
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

    LaunchedEffect(cameraPermissionState.status) {
        when {
            cameraPermissionState.status.isGranted -> {
                onPermissionGranted()
            }
            cameraPermissionState.status.shouldShowRationale -> {
                // User has denied permission, but can still be asked again
                onPermissionDenied()
            }
            else -> {
                // Permission is permanently denied or not requested yet
                if (!cameraPermissionState.status.isGranted) {
                    // Request permission if not already requested
                    cameraPermissionState.launchPermissionRequest()
                }
            }
        }
    }
}

/**
 * Check if camera permission is granted
 *
 * Note: This is a non-composable utility function.
 * For Composable contexts, use RequestCameraPermission instead.
 */
object CameraPermissionChecker {
    /**
     * Check if camera permission is granted
     * @param context Android Context
     * @return true if permission is granted
     */
    fun isGranted(context: android.content.Context): Boolean {
        return androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    /**
     * Get camera permission state
     * @param context Android Context
     * @return Current permission state
     */
    fun getState(context: android.content.Context): CameraPermissionState {
        return if (isGranted(context)) {
            CameraPermissionState.Granted
        } else {
            CameraPermissionState.RequestRequired
        }
    }
}
