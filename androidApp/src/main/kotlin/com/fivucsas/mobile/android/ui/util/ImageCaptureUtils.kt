package com.fivucsas.mobile.android.ui.util

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

private const val DEFAULT_JPEG_QUALITY = 90
private const val MAX_UPLOAD_BYTES = 950 * 1024 // keep under backend 1MB limit
private const val MIN_JPEG_QUALITY = 45
private const val SCALE_FACTOR = 0.85f
private const val MAX_SCALE_ATTEMPTS = 4

/**
 * Converts an [ImageProxy] to a compressed JPEG byte array.
 *
 * Uses the CameraX 1.3+ built-in [ImageProxy.toBitmap] for reliable format conversion
 * across all device-specific image formats (YUV_420_888, JPEG, etc.).
 * Applies rotation correction based on [ImageProxy.getImageInfo] and recycles
 * all intermediate bitmaps to prevent memory leaks.
 *
 * @param quality JPEG compression quality (0-100). Default is 90.
 * @return Compressed JPEG byte array ready for network transmission.
 */
fun ImageProxy.toCompressedJpegBytes(quality: Int = DEFAULT_JPEG_QUALITY): ByteArray {
    val originalBitmap = toBitmap()
    val rotationDegrees = imageInfo.rotationDegrees

    val finalBitmap = applyRotationIfNeeded(originalBitmap, rotationDegrees)

    return try {
        compressBitmapUnderLimit(finalBitmap, quality)
    } finally {
        finalBitmap.recycle()
    }
}

private fun applyRotationIfNeeded(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
    if (rotationDegrees == 0) return bitmap

    val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
    val rotated = Bitmap.createBitmap(
        bitmap, 0, 0,
        bitmap.width, bitmap.height,
        matrix, true
    )

    if (rotated !== bitmap) {
        bitmap.recycle()
    }

    return rotated
}

private fun compressBitmapToJpeg(bitmap: Bitmap, quality: Int): ByteArray {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
    return outputStream.toByteArray()
}

private fun compressBitmapUnderLimit(bitmap: Bitmap, initialQuality: Int): ByteArray {
    var workingBitmap = bitmap
    var attempt = 0
    var bestBytes = compressBitmapToJpeg(workingBitmap, initialQuality)

    while (attempt <= MAX_SCALE_ATTEMPTS) {
        var quality = initialQuality.coerceAtMost(95)
        while (quality >= MIN_JPEG_QUALITY) {
            val bytes = compressBitmapToJpeg(workingBitmap, quality)
            bestBytes = bytes
            if (bytes.size <= MAX_UPLOAD_BYTES) {
                if (workingBitmap !== bitmap) workingBitmap.recycle()
                return bytes
            }
            quality -= 10
        }

        if (attempt == MAX_SCALE_ATTEMPTS) break

        val newWidth = (workingBitmap.width * SCALE_FACTOR).toInt().coerceAtLeast(480)
        val newHeight = (workingBitmap.height * SCALE_FACTOR).toInt().coerceAtLeast(640)
        val scaled = Bitmap.createScaledBitmap(workingBitmap, newWidth, newHeight, true)
        if (workingBitmap !== bitmap) workingBitmap.recycle()
        workingBitmap = scaled
        attempt += 1
    }

    if (workingBitmap !== bitmap) workingBitmap.recycle()
    return bestBytes
}
