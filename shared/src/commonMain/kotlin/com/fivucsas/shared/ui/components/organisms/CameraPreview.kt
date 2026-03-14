package com.fivucsas.shared.ui.components.organisms

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.platform.CameraState
import com.fivucsas.shared.ui.theme.AppColors

/**
 * Camera Preview Container Composable
 *
 * Provides a common UI structure for camera preview across platforms.
 * Actual camera rendering is delegated to platform-specific implementations.
 *
 * Design Patterns:
 * - Template Method Pattern: Defines the structure, platform provides implementation
 * - Composition Pattern: Combines multiple UI elements into cohesive camera UI
 *
 * @param cameraState Current state of the camera
 * @param onCaptureClick Callback when capture button is clicked
 * @param onCloseClick Callback when close button is clicked
 * @param onFlipCamera Callback when flip camera button is clicked (optional)
 * @param showFlipButton Whether to show the flip camera button
 * @param overlayContent Optional content to overlay on camera preview (e.g., face detection guide)
 * @param cameraPreviewContent Platform-specific camera preview content
 */
@Composable
fun CameraPreviewContainer(
    cameraState: CameraState,
    onCaptureClick: () -> Unit,
    onCloseClick: () -> Unit,
    onFlipCamera: (() -> Unit)? = null,
    showFlipButton: Boolean = false,
    overlayContent: @Composable BoxScope.() -> Unit = {},
    cameraPreviewContent: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Camera Preview Content (Platform-specific)
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (cameraState) {
                is CameraState.Idle -> {
                    CameraStateMessage("Camera not initialized")
                }
                is CameraState.Initializing -> {
                    CameraStateMessage("Initializing camera...")
                }
                is CameraState.Ready -> {
                    CameraStateMessage("Camera ready")
                }
                is CameraState.Previewing -> {
                    cameraPreviewContent()
                }
                is CameraState.Capturing -> {
                    CameraStateMessage("Capturing...")
                }
                is CameraState.Error -> {
                    CameraErrorMessage(cameraState.error.message ?: "Camera error")
                }
                is CameraState.Released -> {
                    CameraStateMessage("Camera released")
                }
            }
        }

        // Overlay Content (e.g., face detection guide)
        overlayContent()

        // Top Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Close Button
            IconButton(
                onClick = onCloseClick,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close camera",
                    tint = Color.White
                )
            }

            // Flip Camera Button
            if (showFlipButton && onFlipCamera != null) {
                IconButton(
                    onClick = onFlipCamera,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                    enabled = cameraState == CameraState.Previewing
                ) {
                    Icon(
                        imageVector = Icons.Default.FlipCameraAndroid,
                        contentDescription = "Flip camera",
                        tint = Color.White
                    )
                }
            }
        }

        // Bottom Controls
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            // Capture Button
            FilledIconButton(
                onClick = onCaptureClick,
                modifier = Modifier
                    .size(72.dp),
                enabled = cameraState == CameraState.Previewing,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = AppColors.Primary,
                    disabledContainerColor = Color.Gray
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Capture photo",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

/**
 * Camera State Message
 * Shows informational messages about camera state
 */
@Composable
private fun CameraStateMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = AppColors.Primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
    }
}

/**
 * Camera Error Message
 * Shows error messages with appropriate styling
 */
@Composable
private fun CameraErrorMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Error",
                tint = AppColors.Error,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Camera Error",
                style = MaterialTheme.typography.headlineSmall,
                color = AppColors.Error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }
    }
}

/**
 * Face Detection Overlay
 *
 * Provides visual guidance for face positioning during capture.
 * When a detected face bounding box is provided, draws a green rectangle.
 * Otherwise falls back to the static oval guide.
 *
 * @param showGuide Whether to show the face positioning guide
 * @param guidanceText Optional text to guide the user
 * @param faceRect Normalized face bounding box (values 0..1 relative to preview).
 *                 Null means no face detected.
 */
@Composable
fun FaceDetectionOverlay(
    showGuide: Boolean = true,
    guidanceText: String? = null,
    faceRect: FaceBounds? = null
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (faceRect != null) {
            // Draw green bounding box for detected face
            Canvas(modifier = Modifier.fillMaxSize()) {
                val left = faceRect.left * size.width
                val top = faceRect.top * size.height
                val right = faceRect.right * size.width
                val bottom = faceRect.bottom * size.height

                drawRect(
                    color = Color.Green,
                    topLeft = Offset(left, top),
                    size = Size(right - left, bottom - top),
                    style = Stroke(width = 4f)
                )
            }
        } else if (showGuide) {
            // Fallback: static oval face guide
            Box(
                modifier = Modifier
                    .size(280.dp, 360.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
                    .background(Color.Transparent)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
                        .background(Color.Transparent)
                        .padding(2.dp)
                ) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.3f))
                    )
                }
            }
        }

        // Guidance text at the top
        val displayText = guidanceText ?: faceRect?.let { deriveFaceGuidance(it) }
        if (displayText != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 100.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.7f)
                    )
                ) {
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Normalized face bounding box with values in 0..1 range
 * relative to the camera preview dimensions.
 */
data class FaceBounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

/**
 * Derive guidance text from detected face position.
 */
private fun deriveFaceGuidance(face: FaceBounds): String? {
    val centerX = (face.left + face.right) / 2f
    val centerY = (face.top + face.bottom) / 2f
    val faceWidth = face.right - face.left
    val faceHeight = face.bottom - face.top

    return when {
        faceWidth > 0.7f || faceHeight > 0.7f -> "Move farther away"
        faceWidth < 0.15f || faceHeight < 0.15f -> "Move closer"
        centerX < 0.3f -> "Move right"
        centerX > 0.7f -> "Move left"
        centerY < 0.3f -> "Move down"
        centerY > 0.7f -> "Move up"
        else -> null // Face is well-centered
    }
}
