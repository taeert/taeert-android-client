package com.robot.pickle

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.util.Base64
import java.io.ByteArrayOutputStream


object ImageUtils {

    fun Image.toByteArray(): ByteArray {
        val buffer = planes[0].buffer
        buffer.rewind()
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        return bytes
    }

    fun Image.toBitmap(): Bitmap {
        val buffer = planes[0].buffer
        buffer.rewind()
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    fun Bitmap.resize(width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(this, width, height, false)
    }

    fun Bitmap.toByteArray(format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG, quality: Int = 100): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        compress(format, quality, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    fun ByteArray.toBase64(): String? {
        return Base64.encodeToString(this, Base64.DEFAULT)
    }

}