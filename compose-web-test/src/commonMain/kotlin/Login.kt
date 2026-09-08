package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import composewebtest.generated.resources.Res
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
            .background(Color.White),
    ) {
        // 상단 영역: 화면의 절반
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.web_app_main),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(116.dp)
                    .height(116.dp),
            )
        }

        // 하단 영역: 화면의 절반
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(
                    top = 24.dp,
                    bottom = 24.dp,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            LoginButton(
                onClick = onPhoneLoginClick,
                background = MainGradient,
                text = "휴대폰번호로 입장하기",
                textColor = Color.White,
            )

            Spacer(modifier = Modifier.height(5.dp))

            LoginButton(
                onClick = onKakaoLoginClick,
                backgroundColor = Color(0xFFFDE249),
                text = "카카오톡으로 입장하기",
                textColor = Color(0xFF461F22),
                icon = "K",
            )

            Spacer(modifier = Modifier.height(5.dp))

            LoginButton(
                onClick = onNaverLoginClick,
                backgroundColor = Color(0xFF03C75A),
                text = "네이버로 입장하기",
                textColor = Color.White,
                icon = "N",
            )

            Button(
                onClick = onGuestClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF8E8E8E),
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .width(350.dp)
                    .height(52.dp),
            ) {
                Text(
                    text = "비회원으로 입장하기",
                    fontSize = 15.sp,
                    color = Color(0xFF8E8E8E),
                )
            }
        }
    }
}

@Composable
private fun LoginButton(
    onClick: () -> Unit,
    backgroundColor: Color? = null,
    background: androidx.compose.ui.graphics.Brush? = null,
    text: String,
    textColor: Color,
    icon: String? = null,
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor ?: Color.Transparent,
            contentColor = textColor,
        ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
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
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                if (icon != null) {
                    Text(
                        text = icon,
                        fontSize = 20.sp,
                        color = textColor,
                    )

                    Spacer(modifier = Modifier.width(10.dp))
                }

                Text(
                    text = text,
                    fontSize = 15.sp,
                    color = textColor,
                )
            }
        }
    }
}