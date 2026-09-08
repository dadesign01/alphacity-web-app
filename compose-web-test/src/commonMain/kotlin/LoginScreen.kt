package com.alphacity.stamptour.ui.screen
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Primary = Color(0xFF02CDF8)
private val Pretendard = FontFamily.SansSerif

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit = { _, _ -> },
    onGuestClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var showForgotPasswordHint by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(
                modifier = Modifier.height(100.dp)
            )

            Text(
                text = "로그인",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Color(0xFF121212),
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = "축제와 함께 스탬프 여행을 시작하세요!",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = Color(0xFF121212),
            )

            Spacer(
                modifier = Modifier.height(50.dp)
            )

            LoginTextField(
                value = email,
                onValueChange = {
                    email = it
                },
                placeholder = "example@email.com",
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            LoginTextField(
                value = password,
                onValueChange = {
                    password = it
                },
                placeholder = "********",
                isPassword = true,
            )

            Text(
                text = "비밀번호를 잊어버리셨나요?",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color(0xFF8F8F8F),
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 10.dp)
                    .clickable {
                        onForgotPasswordClick()
                    },
            )

            Spacer(
                modifier = Modifier.height(48.dp)
            )

            Button(
                onClick = {
                    onLoginClick(email, password)
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                Text(
                    text = "로그인",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color(0xFFF8F8F8),
                )
            }

            if (showForgotPasswordHint) {
                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = Color(0xFFFFB74D),
                            shape = RoundedCornerShape(8.dp),
                        )
                        .background(
                            color = Color(0xFFFFF8E1),
                            shape = RoundedCornerShape(8.dp),
                        )
                        .padding(
                            horizontal = 16.dp,
                            vertical = 12.dp,
                        ),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "로그인에 3회 이상 실패했습니다.",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = Color(0xFF795548),
                            modifier = Modifier.weight(1f),
                        )

                        Text(
                            text = "비밀번호 찾기",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Primary,
                            modifier = Modifier.clickable {
                                onForgotPasswordClick()
                            },
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Button(
                onClick = onGuestClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEDF7FF),
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                Text(
                    text = "비회원으로 계속하기",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Primary,
                )
            }

            Spacer(
                modifier = Modifier.height(48.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = Color(0xFFDBDBDB),
                )

                Text(
                    text = "또는",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color(0xFF8F8F8F),
                    modifier = Modifier.padding(
                        horizontal = 16.dp
                    ),
                )

                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = Color(0xFFDBDBDB),
                )
            }

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // 카카오
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFE200))
                        .clickable {
                            // TODO: 카카오 로그인
                        },
                ) {
                    Text(
                        text = "K",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF121212),
                    )
                }

                Spacer(
                    modifier = Modifier.width(12.dp)
                )

                // 네이버
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF03C75A))
                        .clickable {
                            // TODO: 네이버 로그인
                        },
                ) {
                    Text(
                        text = "N",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White,
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "아직 계정이 없으신가요? ",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color(0xFF8F8F8F),
                )

                Text(
                    text = "회원가입",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Primary,
                    modifier = Modifier.clickable {
                        onRegisterClick()
                    },
                )
            }

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "2026 OLLYMOA. All rights reserved.",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                color = Color(0xFF8F8F8F),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(
                    bottom = 40.dp
                ),
            )
        }

        if (isLoading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = 0.3f)
                    ),
            ) {
                CircularProgressIndicator(
                    color = Primary,
                )
            }
        }
    }
}

@Composable
private fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color(0xFF8F8F8F),
            )
        },
        visualTransformation =
            if (isPassword) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF5F5F5),
            unfocusedContainerColor = Color(0xFFF5F5F5),
            disabledContainerColor = Color(0xFFF5F5F5),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Primary,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
    )
}