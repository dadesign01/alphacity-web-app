package com.alphacity.stamptour

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
                currentPath == "/login" ||
                currentPath == "/phone-login" ||
                currentPath == "/stamp-earned" ||
                currentPath == "/stamp-not-earned" ||
                currentPath == "/terms" ||
                currentPath == "/stamp" ||
                currentPath == "/qr-scanner" ||
                currentPath.startsWith("/stamp/")

    if (!isValidPath) {
        ErrorScreen()
        return
    }

    /*
     * QR URL은 최초 1회만 파싱한다.
     *
     * Compose가 다시 recomposition 되더라도
     * 같은 QR URL을 다시 QrStampScreen으로
     * 진입시키지 않는다.
     */
    val qrTarget = remember {
        QrTargetParser.parse()
    }

    AppNavigation(
        qrTarget = qrTarget,
    )
}
