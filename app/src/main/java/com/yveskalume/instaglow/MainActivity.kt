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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.yveskalume.instaglow.ui.navigation.Destination
import com.yveskalume.instaglow.ui.navigation.composable
import com.yveskalume.instaglow.ui.navigation.navigate
import com.yveskalume.instaglow.ui.screen.editor.EditorScreen
import com.yveskalume.instaglow.ui.screen.home.HomeScreen
import com.yveskalume.instaglow.ui.theme.InstaglowTheme
import com.yveskalume.instaglow.util.AssetsUtil.getAssetFileDescriptorOrCached
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


class MainActivity : ComponentActivity() {

    private val LR_IMAGE_HEIGHT = 50
    private val LR_IMAGE_WIDTH = 50
    private val UPSCALE_FACTOR = 4
    private val SR_IMAGE_HEIGHT = LR_IMAGE_HEIGHT * UPSCALE_FACTOR
    private val SR_IMAGE_WIDTH = LR_IMAGE_WIDTH * UPSCALE_FACTOR

    private var superResolutionNativeHandle: Long = 0
    private lateinit var selectedBitmap: Bitmap

    private var scaledImagePainterFlow: MutableStateFlow<BitmapPainter?> = MutableStateFlow(null)

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
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Destination.Home.route
                    ) {
                        composable(Destination.Home) {
                            HomeScreen(
                                onImageSelected = {
                                    selectedBitmap = it
                                    navController.navigate(Destination.Editor)
                                }
                            )
                        }
                        composable(Destination.Editor) {
                            val imagePainter by scaledImagePainterFlow.collectAsState()

                            val coroutineScope = rememberCoroutineScope()

                            LaunchedEffect(Unit) {
                                coroutineScope.launch(Dispatchers.IO) {
                                    process()
                                }
                            }

                            EditorScreen(
                                image = imagePainter,
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        deinit()
    }

    private suspend fun process() {
        if (superResolutionNativeHandle == 0L) {
            superResolutionNativeHandle = initTFLiteInterpreter() ?: 0
        }

        if (superResolutionNativeHandle == 0L) {
            Toast.makeText(
                this,
                "TFLite interpreter failed to create!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (!::selectedBitmap.isInitialized) {
            Toast.makeText(
                this,
                "Please choose one low resolution image",
                Toast.LENGTH_LONG
            ).show();
            return;
        }

        val lowResRGB = IntArray(LR_IMAGE_HEIGHT * LR_IMAGE_WIDTH)
        selectedBitmap.getPixels(
            lowResRGB,
            0,
            LR_IMAGE_WIDTH,
            0,
            0,
            LR_IMAGE_WIDTH,
            LR_IMAGE_HEIGHT
        )

        val superResRGB = doSuperResolution(lowResRGB)

        val srImgBitmap = Bitmap.createBitmap(
            superResRGB,
            SR_IMAGE_WIDTH,
            SR_IMAGE_HEIGHT,
            Bitmap.Config.ARGB_8888
        )

        scaledImagePainterFlow.emit(BitmapPainter(srImgBitmap.asImageBitmap()))

    }

    @WorkerThread
    @Synchronized
    fun doSuperResolution(lowResRGB: IntArray?): IntArray {
        return superResolutionFromJNI(superResolutionNativeHandle, lowResRGB!!)
    }

    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer {
        getAssetFileDescriptorOrCached(applicationContext, "ESRGAN.tflite")
            .use { fileDescriptor ->
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

    private fun initTFLiteInterpreter(): Long? {
        return try {
            val model = loadModelFile()
            initWithByteBufferFromJNI(model, true)
        } catch (e: IOException) {
            Log.e("MainActivity", "Fail to load model", e)
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