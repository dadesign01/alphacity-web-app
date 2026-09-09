package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import composewebtest.generated.resources.Res
import composewebtest.generated.resources.kakao_login
import composewebtest.generated.resources.naver_login
import composewebtest.generated.resources.phone_login_number
import composewebtest.generated.resources.web_app_main
import composewebtest.theme.MainGradient
import org.jetbrains.compose.resources.painterResource

@Composable
fun LoginScreen(
    onPhoneLoginClick: () -> Unit = {},
    onKakaoLoginClick: () -> Unit = {},
    onNaverLoginClick: () -> Unit = {},
    onAppleLoginClick: () -> Unit = {},
    onGuestClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(
                top = 24.dp,
                bottom = 24.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        // 상단 문구
        Text(
            text = "돌아다닐수록 쌓이는 즐거움!",
            fontSize = 20.sp,
            fontWeight = FontWeight.Light,
            color = Color.Black,
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        // 메인 타이틀
        Text(
            text = "모바일 스탬프투어",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2563EB),
        )

        Spacer(
            modifier = Modifier.height(30.dp)
        )

        // 메인 이미지
        Image(
            painter = painterResource(
                Res.drawable.web_app_main
            ),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(116.dp)
                .height(116.dp),
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        // 로고명
        Text(
            text = "OLLYMOA",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2563EB),
        )

        // 로그인 버튼 시작 전 간격
        Spacer(
            modifier = Modifier.height(30.dp)
        )

        // 휴대폰번호 로그인
        LoginButton(
            onClick = onPhoneLoginClick,
            background = MainGradient,
            text = "휴대폰번호로 입장하기",
            textColor = Color.White,
            icon = {
                Image(
                    painter = painterResource(
                        Res.drawable.phone_login_number
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    contentScale = ContentScale.Fit,
                )
            },
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        // 카카오 로그인
        LoginButton(
            onClick = onKakaoLoginClick,
            backgroundColor = Color(0xFFFDE249),
            text = "카카오톡으로 입장하기",
            textColor = Color(0xFF461F22),
            icon = {
                Image(
                    painter = painterResource(
                        Res.drawable.kakao_login
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    contentScale = ContentScale.Fit,
                )
            },
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        // 네이버 로그인
        LoginButton(
            onClick = onNaverLoginClick,
            backgroundColor = Color(0xFF03C75A),
            text = "네이버로 입장하기",
            textColor = Color.White,
            icon = {
                Image(
                    painter = painterResource(
                        Res.drawable.naver_login
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    contentScale = ContentScale.Fit,
                )
            },
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        // 비회원
        Button(
            onClick = onGuestClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF8E8E8E),
            ),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .width(350.dp)
                .height(52.dp),
        ) {
            Text(
                text = "비회원으로 둘러보기",
                fontSize = 15.sp,
                color = Color(0xFF8E8E8E),
            )
        }
    }
}

@Composable
private fun LoginButton(
    onClick: () -> Unit,
    backgroundColor: Color? = null,
    background: Brush? = null,
    text: String,
    textColor: Color,
    icon: (@Composable () -> Unit)? = null,
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor ?: Color.Transparent,
            contentColor = textColor,
        ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier
            .width(350.dp)
            .height(52.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (background != null) {
                        Modifier.background(
                            brush = background,
                            shape = RoundedCornerShape(16.dp),
                        )
                    } else {
                        Modifier
                    }
                ),
        ) {

            // 아이콘: 버튼 왼쪽 고정
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 24.dp),
                ) {
                    icon()
                }
            }

            // 텍스트: 버튼 전체 기준 정중앙
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = text,
                    fontSize = 15.sp,
                    color = textColor,
                )
            }
        }
    }
}
