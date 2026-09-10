@file:OptIn(ExperimentalComposeUiApi::class)

package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.WebElementView
import kotlinx.browser.document
import org.w3c.dom.HTMLVideoElement

@JsModule("@zxing/browser")
external class BrowserQRCodeReader {
    fun decodeFromVideoDevice(
        deviceId: String?,
        video: HTMLVideoElement,
        callback: (result: ZXingResult?, error: JsAny?) -> Unit,
    ): ZXingControls
}

external interface ZXingResult {
    val text: String
}

external interface ZXingControls {
    fun stop()
}

@Composable
fun QrScanner(
    onQrDetected: (String) -> Unit,
    onClose: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        WebElementView(
            factory = {
                (document.createElement("video") as HTMLVideoElement).apply {
                    autoplay = true
                    muted = true

                    setAttribute("playsinline", "true")

                    style.width = "100%"
                    style.height = "100%"
                    style.objectFit = "cover"
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}