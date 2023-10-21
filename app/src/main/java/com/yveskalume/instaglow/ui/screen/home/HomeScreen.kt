package com.yveskalume.instaglow.ui.screen.home

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraEnhance
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onImageSelected: (Bitmap) -> Unit) {
    val context = LocalContext.current
    val pickMediaLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                val imageBitmap = BitmapFactory
                    .decodeStream(context.contentResolver.openInputStream(uri))
                onImageSelected(imageBitmap)
            }
        }

    fun pickMedia() {
        pickMediaLauncher.launch(
            PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.SingleMimeType("image/*")
            )
        )
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(text = "New") },
                icon = {
                    Icon(
                        imageVector = Icons.Default.CameraEnhance,
                        contentDescription = null
                    )
                },
                onClick = ::pickMedia
            )
        }
    ) { contentPadding ->

    }
}