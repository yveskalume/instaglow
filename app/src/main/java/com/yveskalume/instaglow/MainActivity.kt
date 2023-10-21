package com.yveskalume.instaglow

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.WorkerThread
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import com.yveskalume.instaglow.ui.theme.InstaglowTheme
import com.yveskalume.instaglow.util.AssetsUtil.getAssetFileDescriptorOrCached
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


class MainActivity : ComponentActivity() {

    private val TAG = "SuperResolution"
    private val MODEL_NAME = "ESRGAN.tflite"
    private val LR_IMAGE_HEIGHT = 50
    private val LR_IMAGE_WIDTH = 50
    private val UPSCALE_FACTOR = 4
    private val SR_IMAGE_HEIGHT = LR_IMAGE_HEIGHT * UPSCALE_FACTOR
    private val SR_IMAGE_WIDTH = LR_IMAGE_WIDTH * UPSCALE_FACTOR
    private val LR_IMG_1 = "lr-1.jpg"
    private val LR_IMG_2 = "lr-2.jpg"
    private val LR_IMG_3 = "lr-3.jpg"

    //    private var model: MappedByteBuffer? = null
    private var superResolutionNativeHandle: Long = 0
    private val selectedLRBitmap: Bitmap? = null
    private var useGPU = false

    var imageBitmap: Bitmap? = null

    companion object {
        init {
            System.loadLibrary("SuperResolution")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        setContent {
            InstaglowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                }
            }
        }
    }

    private fun process(imageBitmap: Bitmap, scale: Int): BitmapPainter? {
        if (superResolutionNativeHandle == 0L) {
            superResolutionNativeHandle = initTFLiteInterpreter(true) ?: 0
        } else if (useGPU !== true) {
            // We need to reinitialize interpreter when execution hardware is changed
            deinit()
            superResolutionNativeHandle = initTFLiteInterpreter(true) ?: 0
        }
//        useGPU = gpuSwitch.isChecked()
        if (superResolutionNativeHandle == 0L) {
            Toast.makeText(
                this,
                "TFLite interpreter failed to create!",
                Toast.LENGTH_SHORT
            ).show()
            return null
        }

        val lowResRGB = IntArray(imageBitmap.height * imageBitmap.width)
        imageBitmap.getPixels(
            lowResRGB,
            0,
            imageBitmap.width,
            0,
            0,
            imageBitmap.width,
            imageBitmap.height
        )

        val superResRGB = doSuperResolution(lowResRGB)

        val srImgBitmap = Bitmap.createBitmap(
            superResRGB,
            imageBitmap.width * scale,
            imageBitmap.height * scale,
            Bitmap.Config.ARGB_8888
        )
        return BitmapPainter(srImgBitmap.asImageBitmap())
    }

    @WorkerThread
    @Synchronized
    fun doSuperResolution(lowResRGB: IntArray?): IntArray {
        return superResolutionFromJNI(superResolutionNativeHandle, lowResRGB!!)
    }

    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer {
        getAssetFileDescriptorOrCached(applicationContext, MODEL_NAME).use { fileDescriptor ->
            FileInputStream(fileDescriptor.fileDescriptor).use { inputStream ->
                val fileChannel = inputStream.channel
                val startOffset = fileDescriptor.startOffset
                val declaredLength = fileDescriptor.declaredLength
                return fileChannel.map(
                    FileChannel.MapMode.READ_ONLY,
                    startOffset,
                    declaredLength
                )
            }
        }
    }

    private fun initTFLiteInterpreter(useGPU: Boolean): Long? {
        return try {
            val model = loadModelFile()
            initWithByteBufferFromJNI(model, useGPU)
        } catch (e: IOException) {
            Log.e(TAG, "Fail to load model", e)
            null
        }
    }

    private fun deinit() {
        deinitFromJNI(superResolutionNativeHandle)
    }

    external fun superResolutionFromJNI(
        superResolutionNativeHandle: Long,
        lowResRGB: IntArray
    ): IntArray

    external fun initWithByteBufferFromJNI(
        modelBuffer: MappedByteBuffer,
        useGPU: Boolean
    ): Long

    external fun deinitFromJNI(superResolutionNativeHandle: Long)
}