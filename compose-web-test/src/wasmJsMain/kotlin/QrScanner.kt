@file:OptIn(
    ExperimentalComposeUiApi::class,
    kotlin.js.ExperimentalWasmJsInterop::class,
)

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

@JsModule("./zxing-wrapper.mjs")
external fun startQrScanner(
    video: HTMLVideoElement,
    callback: (String) -> Unit,
): JsAny

@Composable
fun QrScanner(
    onQrDetected: (String) -> Unit,
    onClose: () -> Unit,
) {
    val video = remember {
        document.createElement("video") as HTMLVideoElement
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

        println("QR Scanner 시작")

        startQrScanner(
            video = video,
            callback = { text ->
                println("QR 인식 결과: $text")
                onQrDetected(text)
            },
        )

        onDispose {
            video.srcObject = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        WebElementView(
            factory = {
                video
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}