package com.yveskalume.instaglow.ui.screen.editor

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.yveskalume.instaglow.R
import com.yveskalume.instaglow.ui.theme.InstaglowTheme

@Composable
fun EditorScreen(image: Painter?) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        if (image != null) {
            Image(
                painter = image,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(width = this.maxWidth, height = this.maxHeight)
            )
        } else {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Preview
@Composable
fun EditorScreenPreview() {
    InstaglowTheme {
        EditorScreen(
            image = painterResource(id = R.drawable.ic_launcher_background),
        )
    }
}