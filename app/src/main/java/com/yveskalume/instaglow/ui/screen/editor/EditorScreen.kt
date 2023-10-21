package com.yveskalume.instaglow.ui.screen.editor

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yveskalume.instaglow.R
import com.yveskalume.instaglow.ui.theme.InstaglowTheme

@Composable
fun EditorScreen(image: Painter, onChangeResolution: (Int) -> Unit) {
    var scaleFactor by remember {
        mutableIntStateOf(1)
    }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = image,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.Center)
                .size(width = this.maxWidth, height = this.maxHeight)
        )

        Slider(
            value = scaleFactor.toFloat(),
            valueRange = 1f..4f,
            steps = 1,
            onValueChange = {
                scaleFactor = it.toInt()
                onChangeResolution(it.toInt())
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
        )
    }
}

@Preview
@Composable
fun EditorScreenPreview() {
    InstaglowTheme {
        EditorScreen(
            image = painterResource(id = R.drawable.ic_launcher_background),
            onChangeResolution = {})
    }
}