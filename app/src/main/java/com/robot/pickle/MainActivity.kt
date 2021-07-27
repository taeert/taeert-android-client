package com.robot.pickle

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.robot.pickle.fragments.MainFragment
import io.socket.client.IO
import io.socket.client.Socket


class MainActivity : AppCompatActivity(R.layout.activity_main) {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var socket: Socket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        socket = (application as App).socket
        val mainFragment = supportFragmentManager.findFragmentById(R.id.mainFragment) as MainFragment

        socket.on("connect") { Log.d(TAG, "Connected") }
        socket.on("disconnect") { Log.d(TAG, "Disconnected") }
        socket.on("message") { data ->
            Log.d(TAG, "Received message: ${data.getOrNull(1)}")
        }
        socket.on("say") { data ->
            val sayText = data.getOrNull(0) as String?
            Log.d(TAG, "Received say: ${data.getOrNull(0)}")

            // do tts
            if (sayText != null)
                mainFragment.say(sayText)
        }

        Log.d(TAG, "Connecting socket...")
        socket.connect()
    }

    override fun onDestroy() {
        super.onDestroy()

        with(socket) {
            Log.d(TAG, "Disconnecting socket...")
            disconnect()
            off()
        }
    }

}