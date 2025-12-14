package com.fivucsas.desktop.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.fivucsas.desktop.platform.DesktopCameraServiceImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.jetbrains.skia.Image as SkiaImage
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/**
 * Desktop Camera Preview Composable
 *
 * Platform-specific implementation of camera preview for Desktop.
 * Continuously captures frames from JavaCV and displays them in Compose.
 *
 * Design Patterns:
 * - Observer Pattern: Observes camera frames and updates UI
 * - Adapter Pattern: Adapts BufferedImage to Compose ImageBitmap
 *
 * @param cameraService The DesktopCameraServiceImpl instance
 * @param modifier Modifier for styling
 */
@Composable
fun DesktopCameraPreview(
    cameraService: DesktopCameraServiceImpl,
    modifier: Modifier = Modifier
) {
    var currentFrame by remember { mutableStateOf<ImageBitmap?>(null) }

    // Continuously update frames while camera is active
    LaunchedEffect(cameraService) {
        while (isActive) {
            val frame = cameraService.getCurrentFrame()
            frame?.let {
                currentFrame = it.toComposeImageBitmap()
            }
            delay(33) // ~30 FPS
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        currentFrame?.let { frame ->
            Image(
                bitmap = frame,
                contentDescription = "Camera preview",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

/**
 * Converts BufferedImage to Compose ImageBitmap
 */
private fun BufferedImage.toComposeImageBitmap(): ImageBitmap {
    val outputStream = ByteArrayOutputStream()
    ImageIO.write(this, "PNG", outputStream)
    val bytes = outputStream.toByteArray()
    return SkiaImage.makeFromEncoded(bytes).asComposeImageBitmap()
}
