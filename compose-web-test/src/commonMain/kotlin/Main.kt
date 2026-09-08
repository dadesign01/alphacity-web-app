package com.alphacity.stamptour

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.alphacity.stamptour.ui.navigation.AppNavigation
import composewebtest.theme.AlphaCityTheme
import kotlinx.browser.window
import web.QrTargetParser

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport("compose-root") {
        AlphaCityTheme {
            App()
        }
    }
}

@Composable
private fun App() {
    val currentPath = window.location.pathname

    val isValidPath =
        currentPath == "/" ||
                currentPath.startsWith("/stamp/")

    if (!isValidPath) {
        ErrorScreen()
        return
    }

    val qrTarget = QrTargetParser.parse()

    AppNavigation(
        qrTarget = qrTarget,
    )
}