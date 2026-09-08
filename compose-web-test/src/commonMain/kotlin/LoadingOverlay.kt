package com.alphacity.stamptour.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoadingOverlay(
    visible: Boolean,
) {
    if (!visible) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.White.copy(alpha = 0.9f)
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator()

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text = "잠시만 기다려주세요",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF555555),
            )
        }
    }
}