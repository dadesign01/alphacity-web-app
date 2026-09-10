package com.alphacity.stamptour.ui.screen

import androidx.compose.runtime.Composable

@Composable
actual fun QrScannerHost(
    onQrDetected: (String) -> Unit,
    onClose: () -> Unit,
) {
    QrScanner(
        onQrDetected = onQrDetected,
        onClose = onClose,
    )
}