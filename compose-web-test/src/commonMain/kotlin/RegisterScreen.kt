package com.alphacity.stamptour.ui.screen
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Primary = Color(0xFF02CDF8)
private val Pretendard = FontFamily.SansSerif

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var agreedToTerms by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var isCodeSent by remember { mutableStateOf(false) }
    var isPhoneVerified by remember { mutableStateOf(false) }

    val isFormValid =
        name.isNotBlank() &&
                email.isNotBlank() &&
                (phone.isBlank() || isPhoneVerified) &&
                password.length >= 8 &&
                password == confirmPassword &&
                agreedToTerms

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
        ) {
            // 헤더
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 16.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "‹",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 34.sp,
                    color = Color(0xFF121212),
                    modifier = Modifier
                        .size(
                            width = 13.dp,
                            height = 26.dp,
                        )
                        .clickable {
                            onBackClick()
                        },
                )

                Spacer(
                    modifier = Modifier.width(24.dp)
                )

                Text(
                    text = "회원가입",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color(0xFF121212),
                )
            }

            HorizontalDivider(
                color = Color(0xFFE2E2E2),
                thickness = 1.dp,
            )

            // 폼
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
            ) {
                Spacer(
                    modifier = Modifier.height(32.dp)
                )

                // 이름
                FieldLabel(
                    text = "이름",
                    required = true,
                )

                RegisterTextField(
                    value = name,
                    onValueChange = {
                        name = it
                    },
                    placeholder = "홍길동",
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                // 이메일
                FieldLabel(
                    text = "이메일",
                    required = true,
                )

                RegisterTextField(
                    value = email,
                    onValueChange = {
                        email = it
                    },
                    placeholder = "example@email.com",
                    keyboardType = KeyboardType.Email,
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                // 전화번호
                FieldLabel(
                    text = "전화번호 (선택)",
                    required = false,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    RegisterTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            isPhoneVerified = false
                            isCodeSent = false
                        },
                        placeholder = "010-1234-5678",
                        keyboardType = KeyboardType.Phone,
                        enabled = !isPhoneVerified,
                        modifier = Modifier.weight(1f),
                    )

                    Button(
                        onClick = {
                            isCodeSent = true
                        },
                        enabled = !isPhoneVerified && phone.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEDF7FF),
                            disabledContainerColor = Color(0xFFF5F5F5),
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .width(81.dp)
                            .height(48.dp),
                    ) {
                        Text(
                            text = if (isCodeSent) "재전송" else "본인인증",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = if (isPhoneVerified) {
                                Color(0xFF8F8F8F)
                            } else {
                                Primary
                            },
                        )
                    }
                }

                // 인증번호
                if (isCodeSent && !isPhoneVerified) {
                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        RegisterTextField(
                            value = verificationCode,
                            onValueChange = {
                                verificationCode = it
                            },
                            placeholder = "인증번호 6자리",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
                        )

                        Button(
                            onClick = {
                                if (verificationCode.length == 6) {
                                    isPhoneVerified = true
                                    isCodeSent = false
                                }
                            },
                            enabled = verificationCode.length == 6,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary,
                                disabledContainerColor = Primary.copy(alpha = 0.5f),
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .width(81.dp)
                                .height(48.dp),
                        ) {
                            Text(
                                text = "확인",
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = Color.White,
                            )
                        }
                    }
                }

                // 인증 완료
                if (isPhoneVerified) {
                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "✓ 인증 완료",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Color(0xFF22C55E),
                    )
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                // 비밀번호
                FieldLabel(
                    text = "비밀번호",
                    required = true,
                )

                RegisterTextField(
                    value = password,
                    onValueChange = {
                        password = it
                    },
                    placeholder = "8자 이상 입력해주세요.",
                    isPassword = true,
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                // 비밀번호 확인
                FieldLabel(
                    text = "비밀번호 확인",
                    required = true,
                )

                RegisterTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                    },
                    placeholder = "비밀번호를 다시 입력해주세요.",
                    isPassword = true,
                )

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                // 약관
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(18.dp)
                            .border(
                                width = 1.dp,
                                color = Color(0xFFE9E9E9),
                                shape = RoundedCornerShape(3.dp),
                            )
                            .background(
                                color = if (agreedToTerms) {
                                    Color(0xFFEAFBFF)
                                } else {
                                    Color.White
                                },
                                shape = RoundedCornerShape(3.dp),
                            )
                            .clickable {
                                agreedToTerms = !agreedToTerms
                            },
                    ) {
                        if (agreedToTerms) {
                            Text(
                                text = "✓",
                                fontFamily = Pretendard,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Primary,
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text(
                        text = "이용약관 및 개인정보처리방침에 동의합니다.",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color(0xFF121212),
                    )

                    Spacer(
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "자세히 보기 >",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = Color(0xFF8D8D8D),
                        modifier = Modifier.clickable {
                            // TODO: 약관 보기
                        },
                    )
                }

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                // 가입하기
                Button(
                    onClick = {
                        if (isFormValid) {
                            onRegisterSuccess()
                        }
                    },
                    enabled = isFormValid && !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        disabledContainerColor = Primary.copy(alpha = 0.5f),
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                ) {
                    Text(
                        text = "가입하기",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color(0xFFF8F8F8),
                    )
                }

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                // 로그인
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "이미 계정이 있으신가요? ",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color(0xFF8F8F8F),
                    )

                    Text(
                        text = "로그인",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Primary,
                        modifier = Modifier.clickable {
                            onBackClick()
                        },
                    )
                }

                Spacer(
                    modifier = Modifier.height(40.dp)
                )

                Text(
                    text = "2026 OLLYMOA. All rights reserved.",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                    color = Color(0xFF8F8F8F),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 40.dp),
                )
            }
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
                    color = Primary
                )
            }
        }
    }
}

@Composable
private fun FieldLabel(
    text: String,
    required: Boolean = false,
) {
    Row(
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Text(
            text = text,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color(0xFF121212),
        )

        if (required) {
            Text(
                text = " *",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = Primary,
            )
        }
    }
}

@Composable
private fun RegisterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        textStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color(0xFF121212),
        ),
        placeholder = {
            Text(
                text = placeholder,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color(0xFFBFBFBF),
            )
        },
        visualTransformation =
            if (isPassword) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = if (enabled) {
                Color.White
            } else {
                Color(0xFFF5F5F5)
            },
            unfocusedContainerColor = if (enabled) {
                Color.White
            } else {
                Color(0xFFF5F5F5)
            },
            disabledContainerColor = Color(0xFFF5F5F5),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = Primary,
        ),
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .border(
                width = 1.dp,
                color = Color(0xFFE9E9E9),
                shape = RoundedCornerShape(8.dp),
            ),
    )
}
