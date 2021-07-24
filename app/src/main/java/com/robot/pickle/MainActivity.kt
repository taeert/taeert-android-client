package com.robot.pickle

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
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

        with(socket) {
            on("connect") { Log.d(TAG, "Connected") }
            on("disconnect") { Log.d(TAG, "Disconnected") }
            on("message") {
                Log.d(TAG, "Received message: ${it.getOrNull(1)}")
            }

            Log.d(TAG, "Connecting socket...")
            connect()
        }
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