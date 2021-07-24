package com.robot.pickle

import android.app.Application
import io.socket.client.IO
import io.socket.client.Socket

class App: Application() {

    val socket: Socket = IO.socket("http://192.168.2.13:5000")

}