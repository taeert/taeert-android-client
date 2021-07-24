package com.robot.pickle.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.robot.pickle.App
import com.robot.pickle.ImageUtils.resize
import com.robot.pickle.ImageUtils.toByteArray
import com.robot.pickle.ImageUtils.toBase64
import com.robot.pickle.ImageUtils.toBitmap
import com.robot.pickle.R
import com.robot.pickle.SpeechRecognition
import com.robot.pickle.databinding.FragmentMainBinding
import io.socket.client.Socket
import java.util.*


class MainFragment: Fragment(R.layout.fragment_main) {

    companion object {
        private const val TAG = "MainFragment"
    }

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var socket: Socket
    private var imageCapture: ImageCapture? = null

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        for ((permission, result) in permissions) {
            when (permission) {
                Manifest.permission.RECORD_AUDIO -> {
                    if (result)
                        binding.listenButton.isEnabled = true
                    else
                        toast("Please enable microphone permission")
                }
                Manifest.permission.CAMERA -> {
                    if (result)
                        startCamera()
                    else
                        toast("Please enable camera permission")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get global socket
        socket = (requireActivity().application as App).socket
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMainBinding.bind(view)

        // Create text to speech
        textToSpeech = TextToSpeech(activity) {
            binding.sayButton.isEnabled = true
        }

        // Set button click listener
        binding.sayButton.setOnClickListener {
            textToSpeech.speak(binding.sayText.text, TextToSpeech.QUEUE_ADD, null, null)
        }

        // Create speech recognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity).apply {
            setRecognitionListener(SpeechRecognition(binding))
        }

        // Set listen button click listener
        binding.listenButton.setOnClickListener {
            speechRecognizer.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString())
            })
            binding.resultTextView.text = "..."
        }

        // Set send picture button click listener
        binding.sendButton.setOnClickListener {
            // Take a picture
            imageCapture?.takePicture(ContextCompat.getMainExecutor(activity), object : ImageCapture.OnImageCapturedCallback() {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                @SuppressLint("UnsafeOptInUsageError")
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    Log.i(TAG, "Photo capture success: ${imageProxy.width}x${imageProxy.height}")

                    val imageString = imageProxy.image
                        ?.toBitmap()
                        ?.resize(640, 480)
                        ?.toByteArray(quality = 70)
                        ?.toBase64()

                    Log.i(TAG, "Sending: \"$imageString\"")
                    socket.send(imageString)
                }
            })
        }
    }

    override fun onStart() {
        super.onStart()

        // Check permission and ask if needed
        activityResultLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA))
    }

    override fun onStop() {
        super.onStop()

        speechRecognizer.stopListening()
        textToSpeech.stop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /** Bind the camera to the activity lifecycle */
    private fun startCamera() {
        Log.d(TAG, "Starting camera")
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            imageCapture = ImageCapture.Builder()
                .setTargetResolution(Size(480, 640))
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture)
            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(activity))
    }

    private fun runOnUiThread(action: () -> Unit) {
        if (isAdded) activity?.runOnUiThread(action)
    }

    private fun toast(text: String, duration: Int = Toast.LENGTH_LONG) {
        if (isAdded) Toast.makeText(activity, text, duration).show()
    }

}