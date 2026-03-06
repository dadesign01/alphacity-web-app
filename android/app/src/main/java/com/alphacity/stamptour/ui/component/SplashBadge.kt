package com.alphacity.stamptour.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alphacity.stamptour.ui.theme.AlphaCityTheme
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.ui.theme.Primary
import com.alphacity.stamptour.ui.theme.White

private val TriangleShape = GenericShape { size, _ ->
    moveTo(size.width / 2f, size.height)
    lineTo(0f, 0f)
    lineTo(size.width, 0f)
    close()
}

@Composable
fun SplashBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        // 라운드 사각형 배지
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(Primary)
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text = text,
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    letterSpacing = (-0.54).sp,
                ),
                color = White,
            )
        }

        // 삼각형 꼬리
        Box(
            modifier = Modifier
                .offset(y = (-1).dp)
                .size(width = 16.dp, height = 12.dp)
                .clip(TriangleShape)
                .background(Primary)
        )
    }
}

@Preview
@Composable
private fun SplashBadgePreview() {
    AlphaCityTheme {
        SplashBadge(text = "디지털 혁신거점")
    }
}
