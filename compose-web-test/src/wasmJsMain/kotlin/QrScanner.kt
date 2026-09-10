@file:OptIn(ExperimentalComposeUiApi::class)

package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
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
        callback: (
            result: ZXingResult?,
            error: JsAny?
        ) -> Unit,
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
    val video = remember {
        document.createElement(
            "video"
        ) as HTMLVideoElement
    }

    val reader = remember {
        BrowserQRCodeReader()
    }

    DisposableEffect(Unit) {

        video.autoplay = true
        video.muted = true

        video.setAttribute(
            "playsinline",
            "true",
        )

        video.style.width = "100%"
        video.style.height = "100%"
        video.style.objectFit = "cover"

        var controls: ZXingControls? = null
        var detected = false

        controls =
            reader.decodeFromVideoDevice(
                null,
                video,
            ) { result, error ->

                if (
                    !detected &&
                    result != null &&
                    result.text.isNotBlank()
                ) {
                    detected = true

                    println(
                        "[QrScanner] QR detected: ${result.text}"
                    )

                    onQrDetected(
                        result.text
                    )

                    controls?.stop()
                }
            }

        onDispose {
            controls?.stop()

            video.srcObject = null

            println(
                "[QrScanner] camera stopped"
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.Black
            ),
    ) {
        WebElementView(
            factory = {
                video
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}