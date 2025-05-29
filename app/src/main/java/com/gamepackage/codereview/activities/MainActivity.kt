package com.gamepackage.codereview.activities

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
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
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) startCamera()
            else MySignal.getInstance().showToast("Camera permission denied.")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MySignal.init(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestPermissionLauncher.launch(Manifest.permission.CAMERA)

        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.captureButton.setOnClickListener {
            if (!isProcessing) captureImage()
        }

        binding.explainButton.setOnClickListener {
            if (!lastDetectedCode.isNullOrBlank()) {
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("CODE_TEXT", lastDetectedCode)
                startActivity(intent)
            } else {
                MySignal.getInstance().showToast("Please capture code first.")
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                e.printStackTrace()
                MySignal.getInstance().showToast("Camera initialization failed.")
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun captureImage() {
        val imageCapture = imageCapture ?: return
        isProcessing = true

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                @OptIn(ExperimentalGetImage::class)
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                        recognizer.process(image)
                            .addOnSuccessListener { visionText ->
                                val detectedText = visionText.text.trim()
                                if (detectedText.isNotEmpty()) {
                                    lastDetectedCode = detectedText
                                    binding.extractedText.text = detectedText
                                    MySignal.getInstance().showToast("Text captured.")
                                } else {
                                    binding.extractedText.text = ""
                                    MySignal.getInstance().showToast("No text recognized.")
                                }
                            }
                            .addOnFailureListener {
                                MySignal.getInstance().showToast("Text recognition failed.")
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                                isProcessing = false
                            }
                    } else {
                        imageProxy.close()
                        isProcessing = false
                        MySignal.getInstance().showToast("Image capture failed.")
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    MySignal.getInstance().showToast("Capture failed: ${exception.message}")
                    isProcessing = false
                }
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
