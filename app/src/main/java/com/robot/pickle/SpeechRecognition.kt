package com.robot.pickle

import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.util.Log
import com.robot.pickle.databinding.FragmentMainBinding
import io.socket.client.Socket

class SpeechRecognition(private val binding: FragmentMainBinding, private val socket: Socket): RecognitionListener {

    companion object {
        private const val TAG = "SpeechRecognition"
    }

    override fun onReadyForSpeech(p0: Bundle?) {
        Log.d(TAG, "onReadyForSpeech: $p0")
        binding.listenButton.isEnabled = false
    }

    override fun onBeginningOfSpeech() {
        Log.d(TAG, "onBeginningOfSpeech")
    }

    override fun onRmsChanged(p0: Float) {
    }

    override fun onBufferReceived(p0: ByteArray?) {
    }

    override fun onEndOfSpeech() {
        Log.d(TAG, "onEndOfSpeech")
        binding.listenButton.isEnabled = true
    }

    override fun onError(error: Int) {
        SpeechRecognizer.ERROR_AUDIO
        Log.d(TAG, "onError: $error")
        if (error == SpeechRecognizer.ERROR_NO_MATCH) {
            binding.resultTextView.alpha = 1f
            binding.resultTextView.text = " ? "
        }
    }

    override fun onPartialResults(bundle: Bundle?) {
        val results = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        Log.d(TAG, "onPartialResults: $results")
        binding.resultTextView.alpha = 0.5f
        binding.resultTextView.text = results?.first()
    }

    override fun onResults(bundle: Bundle?) {
        val results = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        Log.d(TAG, "onResults: $results")
        binding.resultTextView.alpha = 1f
        binding.resultTextView.text = results?.first()

        socket.emit("heard", results?.first())
    }

    override fun onEvent(evenType: Int, p1: Bundle?) {
        Log.d(TAG, "onEvent: $evenType")
    }

}