package com.alphacity.stamptour.ui.screen

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import composewebtest.generated.resources.Res
import composewebtest.generated.resources.splash_img
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

@Composable
fun SplashScreen(
    onStartClick: () -> Unit = {},
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Fade In
        visible = true

        // 화면에 약 1.6초 정도 유지
        delay(1600)

        // Fade Out
        visible = false

        // Fade Out 애니메이션 시간
        delay(400)

        // Login 화면으로 이동
        onStartClick()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(
                animationSpec = tween(400)
            ),
            exit = fadeOut(
                animationSpec = tween(400)
            ),
        ) {
            Image(
                painter = painterResource(Res.drawable.splash_img),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}