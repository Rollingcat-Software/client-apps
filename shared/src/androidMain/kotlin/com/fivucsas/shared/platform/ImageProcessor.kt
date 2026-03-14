package com.fivucsas.shared.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.fivucsas.shared.config.BiometricConfig
import java.io.ByteArrayOutputStream

/**
 * Image Processor for biometric capture pipeline.
 *
 * Consolidates image resizing, compression, and encoding
 * that is needed before sending captured images to the backend.
 */
object ImageProcessor {

    /**
     * Main entry point: decode, resize, and compress JPEG bytes
     * to meet biometric image requirements.
     */
    fun processForBiometric(imageBytes: ByteArray): ByteArray {
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            ?: return imageBytes

        val resized = resizeBitmap(
            bitmap,
            BiometricConfig.PREFERRED_IMAGE_WIDTH,
            BiometricConfig.PREFERRED_IMAGE_HEIGHT
        )

        val compressed = compressToTargetSize(resized, BiometricConfig.MAX_IMAGE_SIZE_KB)

        if (resized != bitmap) bitmap.recycle()
        resized.recycle()

        return compressed
    }

    /**
     * Encode raw image bytes to a Base64 string.
     */
    fun toBase64(imageBytes: ByteArray): String {
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP)
    }

    /**
     * Resize a Bitmap so that it fits within [maxWidth] x [maxHeight]
     * while preserving the aspect ratio.
     * Returns a new Bitmap (caller should recycle original if different).
     */
    fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) return bitmap

        val ratioW = maxWidth.toFloat() / width
        val ratioH = maxHeight.toFloat() / height
        val scale = minOf(ratioW, ratioH)

        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Compress a Bitmap to JPEG, iteratively lowering quality
     * until the output is under [maxSizeKB] kilobytes.
     */
    fun compressToTargetSize(bitmap: Bitmap, maxSizeKB: Int): ByteArray {
        var quality = 95
        val maxBytes = maxSizeKB * 1024

        while (quality >= 10) {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            val bytes = stream.toByteArray()
            if (bytes.size <= maxBytes) return bytes
            quality -= 10
        }

        // Fallback: return at minimum quality
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 10, stream)
        return stream.toByteArray()
    }
}
