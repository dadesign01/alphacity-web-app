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
import kotlinx.browser.window
import org.w3c.dom.HTMLVideoElement
import org.w3c.dom.mediacapture.MediaStream
import kotlin.js.toJsBoolean

private fun createRearCameraConstraints():
        org.w3c.dom.mediacapture.MediaStreamConstraints =
    js(
        """({
            video: {
                facingMode: {
                    exact: "environment"
                }
            },
            audio: false
        })"""
    )

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

        var cameraStream: MediaStream? = null

        val constraints = createRearCameraConstraints()

        window.navigator.mediaDevices
            ?.getUserMedia(constraints)
            ?.then { stream ->
                cameraStream = stream
                video.srcObject = stream
                null
            }
            ?.catch { error ->
                println("후면 카메라 실행 실패: $error")
                null
            }

        onDispose {
            video.srcObject = null
            cameraStream = null
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