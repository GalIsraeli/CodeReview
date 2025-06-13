// MainActivity.kt
package com.gamepackage.codereview.activities

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.gamepackage.codereview.databinding.ActivityMainBinding
import com.gamepackage.codereview.utils.MySignal
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private var lastDetectedCode: String? = null
    private var isProcessing = false

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startCamera()
            else MySignal.getInstance().showToast("Camera permission denied.")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showPreviewMode()
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.captureButton.setOnClickListener {
            if (!isProcessing) captureImage()
        }

        binding.retakeButton.setOnClickListener {
            lastDetectedCode = null
            binding.extractedText.text = ""
            showPreviewMode()
        }

        binding.explainButton.setOnClickListener {
            lastDetectedCode?.let { code ->
                startActivity(Intent(this, ChatActivity::class.java).apply {
                    putExtra("CODE_TEXT", code)
                })
            } ?: MySignal.getInstance().showToast("Please capture code first.")
        }
    }

    private fun showPreviewMode() {
        binding.previewContainer.visibility = View.VISIBLE
        binding.resultContainer.visibility = View.GONE
    }

    private fun showResultMode() {
        binding.previewContainer.visibility = View.GONE
        binding.resultContainer.visibility = View.VISIBLE
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()
            val preview = androidx.camera.core.Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(binding.previewView.surfaceProvider) }

            imageCapture = ImageCapture.Builder().build()
            try {
                provider.unbindAll()
                provider.bindToLifecycle(
                    this, androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA,
                    preview, imageCapture
                )
            } catch (e: Exception) {
                MySignal.getInstance().showToast("Camera init failed.")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun captureImage() {
        val capture = imageCapture ?: return
        isProcessing = true
        capture.takePicture(ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(proxy: androidx.camera.core.ImageProxy) {
                    processImage(proxy)
                }
                override fun onError(exc: ImageCaptureException) {
                    MySignal.getInstance().showToast("Capture failed: ${exc.message}")
                    isProcessing = false
                }
            })
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    private fun processImage(proxy: androidx.camera.core.ImageProxy) {
        proxy.image?.let { mediaImage ->
            val image = InputImage.fromMediaImage(mediaImage, proxy.imageInfo.rotationDegrees)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    lastDetectedCode = visionText.text.trim()
                    binding.extractedText.text = lastDetectedCode
                    MySignal.getInstance().showToast(
                        if (lastDetectedCode!!.isNotEmpty()) "Text captured."
                        else "No text recognized."
                    )
                }
                .addOnFailureListener {
                    MySignal.getInstance().showToast("Recognition failed.")
                }
                .addOnCompleteListener {
                    proxy.close()
                    isProcessing = false
                    showResultMode()
                }
        } ?: run {
            proxy.close()
            isProcessing = false
            MySignal.getInstance().showToast("Image capture failed.")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
