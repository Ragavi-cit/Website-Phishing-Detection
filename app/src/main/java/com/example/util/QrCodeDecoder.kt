package com.example.util

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer
import java.util.EnumMap

object QrCodeDecoder {

    private val reader = MultiFormatReader().apply {
        val hints = EnumMap<DecodeHintType, Any>(DecodeHintType::class.java).apply {
            put(DecodeHintType.POSSIBLE_FORMATS, listOf(BarcodeFormat.QR_CODE))
            put(DecodeHintType.TRY_HARDER, java.lang.Boolean.TRUE)
        }
        setHints(hints)
    }

    /**
     * Attempts to decode a QR code from a Bitmap.
     * Uses HybridBinarizer first, then falls back to GlobalHistogramBinarizer.
     */
    fun decodeQrCode(bitmap: Bitmap): String? {
        try {
            // Resize if bitmap is too large for fast decoding
            val targetBitmap = if (bitmap.width > 1200 || bitmap.height > 1200) {
                val scale = 1200f / maxOf(bitmap.width, bitmap.height)
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scale).toInt(),
                    (bitmap.height * scale).toInt(),
                    true
                )
            } else {
                bitmap
            }

            val width = targetBitmap.width
            val height = targetBitmap.height
            val intArray = IntArray(width * height)
            targetBitmap.getPixels(intArray, 0, width, 0, 0, width, height)

            val source = RGBLuminanceSource(width, height, intArray)

            // Attempt 1: HybridBinarizer
            try {
                val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
                val result = reader.decodeWithState(binaryBitmap)
                reader.reset()
                if (!result.text.isNullOrBlank()) {
                    return result.text
                }
            } catch (e: Exception) {
                reader.reset()
            }

            // Attempt 2: GlobalHistogramBinarizer (better for low-contrast images)
            try {
                val binaryBitmap = BinaryBitmap(GlobalHistogramBinarizer(source))
                val result = reader.decodeWithState(binaryBitmap)
                reader.reset()
                if (!result.text.isNullOrBlank()) {
                    return result.text
                }
            } catch (e: Exception) {
                reader.reset()
            }

            // Attempt 3: Inverted luminance (for light-on-dark QR codes)
            try {
                val invertedSource = source.invert()
                val binaryBitmap = BinaryBitmap(HybridBinarizer(invertedSource))
                val result = reader.decodeWithState(binaryBitmap)
                reader.reset()
                if (!result.text.isNullOrBlank()) {
                    return result.text
                }
            } catch (e: Exception) {
                reader.reset()
            }

        } catch (e: Exception) {
            // Decoding failed
        }
        return null
    }
}
