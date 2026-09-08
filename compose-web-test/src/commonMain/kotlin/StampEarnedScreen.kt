package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Primary = Color(0xFF02CDF8)
private val Background = Color(0xFFEFF4FF)

@Composable
fun StampEarnedScreen(
    eventName: String = "00 행사",
    onEventClick: () -> Unit = {},
    onStampClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "✓",
                fontSize = 56.sp,
                color = Color(0xFF22C55E),
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "스탬프 적립 완료",
                fontSize = 24.sp,
                color = Color(0xFF121212),
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "$eventName 스탬프를 획득했습니다",
                fontSize = 16.sp,
                color = Color(0xFF4F5870),
            )

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = onEventClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                Text(
                    text = "행사 보러가기",
                    fontSize = 15.sp,
                    color = Color.White,
                )
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onStampClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                Text(
                    text = "스탬프 확인하기",
                    fontSize = 15.sp,
                    color = Color(0xFF4F5870),
                )
            }
        }
    }
}