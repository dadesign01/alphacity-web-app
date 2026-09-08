package com.alphacity.stamptour

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import composewebtest.generated.resources.Res
import composewebtest.generated.resources.error_page
import kotlinx.browser.window
import org.jetbrains.compose.resources.painterResource

@Composable
fun ErrorScreen() {
    Image(
        painter = painterResource(Res.drawable.error_page),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                window.location.href = "/"
            },
    )
}