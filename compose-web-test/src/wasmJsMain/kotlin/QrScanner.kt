@file:OptIn(
    ExperimentalComposeUiApi::class,
    kotlin.js.ExperimentalWasmJsInterop::class,
)

package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.WebElementView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.browser.document
import org.w3c.dom.HTMLVideoElement

@JsModule("./zxing-wrapper.mjs")
external fun startQrScanner(
    video: HTMLVideoElement,
    callback: (String) -> Unit,
): JsAny

private val Primary = Color(0xFF02CDF8)

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
        /*
         * 카메라 화면
         */
        WebElementView(
            factory = {
                video
            },
            modifier = Modifier.fillMaxSize(),
        )

        /*
         * QR 인식 영역
         *
         * 250 x 250 영역의 네 모서리만
         * 올리모아 Primary 색상으로 표시
         */
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(250.dp),
        ) {

            /*
             * 왼쪽 위
             */
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(
                        width = 42.dp,
                        height = 3.dp,
                    )
                    .background(Primary),
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(
                        width = 3.dp,
                        height = 42.dp,
                    )
                    .background(Primary),
            )

            /*
             * 오른쪽 위
             */
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(
                        width = 42.dp,
                        height = 3.dp,
                    )
                    .background(Primary),
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(
                        width = 3.dp,
                        height = 42.dp,
                    )
                    .background(Primary),
            )

            /*
             * 왼쪽 아래
             */
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(
                        width = 42.dp,
                        height = 3.dp,
                    )
                    .background(Primary),
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(
                        width = 3.dp,
                        height = 42.dp,
                    )
                    .background(Primary),
            )

            /*
             * 오른쪽 아래
             */
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(
                        width = 42.dp,
                        height = 3.dp,
                    )
                    .background(Primary),
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(
                        width = 3.dp,
                        height = 42.dp,
                    )
                    .background(Primary),
            )
        }

        /*
         * 안내 문구
         */
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .background(
                    Color.Black.copy(alpha = 0.45f),
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "QR코드를 사각형 안에 맞춰주세요.",
                color = Color.White,
                fontSize = 15.sp,
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "자동으로 인식됩니다.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp,
            )
        }
    }
}
