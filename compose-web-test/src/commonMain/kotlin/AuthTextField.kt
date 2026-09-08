package com.alphacity.stamptour.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    inputMode: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
)