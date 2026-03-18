package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alphacity.stamptour.R
import com.alphacity.stamptour.ui.component.SplashBadge
import com.alphacity.stamptour.ui.theme.AlphaCityTheme
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.ui.theme.Primary
import com.alphacity.stamptour.ui.theme.SplashGradientEnd
import com.alphacity.stamptour.ui.theme.SplashGradientStart
import com.alphacity.stamptour.ui.theme.TextDark
import com.alphacity.stamptour.ui.theme.TextGray
import com.alphacity.stamptour.ui.theme.White

@Composable
fun SplashScreen(
    onStartClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val gifImageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components { add(GifDecoder.Factory()) }
            .build()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
    ) {
        // 상단 그라데이션 배경 (하늘색 → 투명)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            SplashGradientStart,
                            SplashGradientEnd,
                        ),
                    )
                )
        )

        // 하단 배경 이미지 (도시 풍경)
        Image(
            painter = painterResource(R.drawable.splash_bg_bottom),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        )

        // 왼쪽 구름
        Image(
            painter = painterResource(R.drawable.splash_cloud_right),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(width = 180.dp, height = 234.dp)
                .offset(x = (-60).dp, y = 40.dp)
                .align(Alignment.TopStart)
        )

        // 오른쪽 구름
        Image(
            painter = painterResource(R.drawable.splash_cloud_right),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(width = 180.dp, height = 234.dp)
                .offset(x = 60.dp, y = 300.dp)
                .align(Alignment.TopEnd)
        )

        // 메인 콘텐츠 (중앙 정렬)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(150.dp))

            // "디지털 혁신거점" 배지
            SplashBadge(text = "디지털 혁신거점")

            Spacer(modifier = Modifier.height(20.dp))

            // 메인 타이틀
            Text(
                text = "수성알파시티\n스탬프 투어",
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Black,
                    fontSize = 45.sp,
                    letterSpacing = (-0.45).sp,
                    lineHeight = 56.sp,
                ),
                color = TextDark,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 서브 타이틀
            Text(
                text = "뚜비와 함께 알파시티 스탬프 나들이를 떠나요!",
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    letterSpacing = (-0.42).sp,
                ),
                color = TextGray,
                textAlign = TextAlign.Center,
            )

        }

        // 캐릭터 (뚜비) + 하단 "투어 시작하기" 버튼
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        ) {
            AsyncImage(
                model = R.raw.alphacity_splash,
                imageLoader = gifImageLoader,
                contentDescription = "뚜비 캐릭터",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(320.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onStartClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = White,
                ),
                shape = RoundedCornerShape(19.dp),
                modifier = Modifier
                    .width(309.dp)
                    .height(65.dp)
            ) {
                Text(
                    text = "투어 시작하기",
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        letterSpacing = (-0.6).sp,
                    ),
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SplashScreenPreview() {
    AlphaCityTheme {
        SplashScreen()
    }
}
