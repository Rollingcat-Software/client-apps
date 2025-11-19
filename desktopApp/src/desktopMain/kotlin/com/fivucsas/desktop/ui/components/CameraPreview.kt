package com.fivucsas.desktop.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import com.fivucsas.desktop.data.DesktopCameraService
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import org.jetbrains.skia.Image as SkiaImage

/**
 * Camera Preview Composable
 * Shows live webcam feed and allows capturing
 * Falls back to mock capture if camera fails
 */
@Composable
fun CameraPreview(
    cameraService: DesktopCameraService,
    onCapture: (ByteArray) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var previewImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var useMockMode by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Initialize camera and start preview
    LaunchedEffect(Unit) {
        if (useMockMode) return@LaunchedEffect

        val initResult = cameraService.initialize()
        if (initResult.isFailure) {
            error = initResult.exceptionOrNull()?.message
            // Don't enable mock mode automatically - let user decide
            return@LaunchedEffect
        }

        // Continuous preview loop
        while (isActive && !useMockMode) {
            val frameResult = cameraService.getPreviewFrame()
            if (frameResult.isSuccess) {
                frameResult.getOrNull()?.let { bufferedImage ->
                    previewImage = bufferedImage.toImageBitmap()
                }
            } else {
                error = frameResult.exceptionOrNull()?.message
                kotlinx.coroutines.delay(1000) // Wait before retry
            }
            kotlinx.coroutines.delay(33) // ~30 FPS
        }
    }

    // Clean up camera on dispose
    DisposableEffect(Unit) {
        onDispose {
            scope.launch {
                cameraService.release()
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            if (useMockMode) "Mock Capture Mode" else "Live Camera Preview",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Camera preview or error message
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    useMockMode -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = "Mock Mode",
                                modifier = Modifier.size(120.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Mock Capture Mode",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Click capture to generate test image",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    error != null -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "❌ Camera Error",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                error ?: "Unknown error",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { useMockMode = true }) {
                                Text("Use Mock Capture Instead")
                            }
                        }
                    }

                    previewImage != null -> {
                        Image(
                            bitmap = previewImage!!,
                            contentDescription = "Camera Preview",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Initializing camera...")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Capture controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onClose,
                enabled = !isCapturing
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    isCapturing = true
                    scope.launch {
                        if (useMockMode) {
                            // Generate mock JPEG image
                            kotlinx.coroutines.delay(500) // Simulate capture
                            val mockImage = generateMockImage()
                            onCapture(mockImage)
                        } else {
                            val captureResult = cameraService.captureFrame()
                            if (captureResult.isSuccess) {
                                captureResult.getOrNull()?.let { imageBytes ->
                                    onCapture(imageBytes)
                                }
                            } else {
                                // If capture fails, offer mock mode
                                error = captureResult.exceptionOrNull()?.message
                                useMockMode = true
                            }
                        }
                        isCapturing = false
                    }
                },
                enabled = !isCapturing && (error == null || useMockMode)
            ) {
                if (isCapturing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isCapturing) "Capturing..." else "📸 Capture Photo")
            }
        }
    }
}

/**
 * Generate a mock image for testing when camera is unavailable
 */
private fun generateMockImage(): ByteArray {
    // Create a simple test image
    val width = 640
    val height = 480
    val image =
        java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB)
    val graphics = image.createGraphics()

    // Draw gradient background
    val paint = java.awt.GradientPaint(
        0f, 0f, java.awt.Color(100, 150, 200),
        width.toFloat(), height.toFloat(), java.awt.Color(50, 100, 150)
    )
    graphics.paint = paint
    graphics.fillRect(0, 0, width, height)

    // Draw face circle
    graphics.color = java.awt.Color(255, 220, 180)
    graphics.fillOval(width / 2 - 100, height / 2 - 120, 200, 240)

    // Draw eyes
    graphics.color = java.awt.Color(50, 50, 50)
    graphics.fillOval(width / 2 - 60, height / 2 - 60, 40, 40)
    graphics.fillOval(width / 2 + 20, height / 2 - 60, 40, 40)

    // Draw smile
    graphics.drawArc(width / 2 - 50, height / 2 - 20, 100, 80, 0, -180)

    // Draw text
    graphics.color = java.awt.Color.WHITE
    graphics.font = java.awt.Font("Arial", java.awt.Font.BOLD, 24)
    graphics.drawString("MOCK TEST IMAGE", width / 2 - 100, height - 50)

    graphics.dispose()

    // Convert to JPEG bytes
    val outputStream = ByteArrayOutputStream()
    ImageIO.write(image, "jpg", outputStream)
    return outputStream.toByteArray()
}

/**
 * Extension function to convert BufferedImage to ImageBitmap
 */
private fun BufferedImage.toImageBitmap(): ImageBitmap {
    val outputStream = ByteArrayOutputStream()
    ImageIO.write(this, "png", outputStream)
    val bytes = outputStream.toByteArray()
    return SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
}
