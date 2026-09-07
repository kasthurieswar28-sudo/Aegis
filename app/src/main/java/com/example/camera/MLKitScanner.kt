package com.example.camera

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class DetectionResult(
    val qrCode: String? = null,
    val textContent: String = "",
    val hasQr: Boolean = false,
    val hasText: Boolean = false
)

class MLKitScanner(private val context: Context) {

    init {
        try {
            com.google.mlkit.common.sdkinternal.MlKitContext.initializeIfNeeded(context.applicationContext)
        } catch (_: Throwable) {
        }
    }

    private val barcodeScanner by lazy {
        try {
            com.google.mlkit.common.sdkinternal.MlKitContext.initializeIfNeeded(context.applicationContext)
            BarcodeScanning.getClient()
        } catch (_: Throwable) {
            null
        }
    }

    private val textRecognizer by lazy {
        try {
            com.google.mlkit.common.sdkinternal.MlKitContext.initializeIfNeeded(context.applicationContext)
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        } catch (_: Throwable) {
            null
        }
    }

    @OptIn(ExperimentalGetImage::class)
    fun createAnalyzer(
        onResult: (DetectionResult) -> Unit
    ): ImageAnalysis.Analyzer {
        var lastAnalyzedTimestamp = 0L

        return ImageAnalysis.Analyzer { imageProxy ->
            val currentTimestamp = System.currentTimeMillis()
            // Throttle to ~4 FPS to save power and keep device cool
            if (currentTimestamp - lastAnalyzedTimestamp < 250) {
                imageProxy.close()
                return@Analyzer
            }
            lastAnalyzedTimestamp = currentTimestamp

            val mediaImage = imageProxy.image
            val scanner = barcodeScanner
            val recognizer = textRecognizer
            if (mediaImage != null && scanner != null && recognizer != null) {
                val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                
                // First attempt barcode scanning
                scanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        val firstQr = barcodes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }?.rawValue
                        
                        // Then analyze text
                        recognizer.process(inputImage)
                            .addOnSuccessListener { visionText ->
                                val text = visionText.text.trim()
                                val hasQr = !firstQr.isNullOrBlank()
                                val hasText = text.length > 8
                                
                                if (hasQr || hasText) {
                                    onResult(
                                        DetectionResult(
                                            qrCode = firstQr,
                                            textContent = text,
                                            hasQr = hasQr,
                                            hasText = hasText
                                        )
                                    )
                                }
                                imageProxy.close()
                            }
                            .addOnFailureListener {
                                imageProxy.close()
                            }
                    }
                    .addOnFailureListener {
                        imageProxy.close()
                    }
            } else {
                imageProxy.close()
            }
        }
    }

    suspend fun analyzeBitmap(bitmap: Bitmap): DetectionResult {
        val inputImage = InputImage.fromBitmap(bitmap, 0)

        val scanner = barcodeScanner
        val barcodes = if (scanner != null) {
            try {
                scanner.process(inputImage).await()
            } catch (_: Exception) {
                emptyList<Barcode>()
            }
        } else {
            emptyList()
        }

        val firstQr = barcodes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }?.rawValue

        val recognizer = textRecognizer
        val text = if (recognizer != null) {
            try {
                val visionText = recognizer.process(inputImage).await()
                visionText.text.trim()
            } catch (_: Exception) {
                ""
            }
        } else {
            ""
        }

        return DetectionResult(
            qrCode = firstQr,
            textContent = text,
            hasQr = !firstQr.isNullOrBlank(),
            hasText = text.isNotBlank()
        )
    }

    suspend fun analyzeRawText(text: String): DetectionResult {
        return DetectionResult(
            qrCode = null,
            textContent = text.trim(),
            hasQr = false,
            hasText = text.isNotBlank()
        )
    }
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { result ->
        if (cont.isActive) cont.resume(result)
    }
    addOnFailureListener { exception ->
        if (cont.isActive) cont.resumeWithException(exception)
    }
    addOnCanceledListener {
        if (cont.isActive) cont.cancel()
    }
}
