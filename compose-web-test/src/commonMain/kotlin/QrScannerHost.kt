package com.alphacity.stamptour.ui.screen

import androidx.compose.runtime.Composable

@Composable
expect fun QrScannerHost(
    onQrDetected: (String) -> Unit,
    onClose: () -> Unit,
)