package com.alphacity.stamptour.ui.screen
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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

private enum class WebForgotPasswordStep {
    INPUT_INFO,
    VERIFY_CODE,
    NEW_PASSWORD,
    SUCCESS,
}

@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit = {},
) {
    var step by remember {
        mutableStateOf(WebForgotPasswordStep.INPUT_INFO)
    }

    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isLoading by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // Header
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "뒤로",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 34.sp,
                color = Color(0xFF121212),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(
                        width = 13.dp,
                        height = 26.dp,
                    )
                    .clickable {
                        onBackClick()
                    },
            )

            Text(
                text = "비밀번호 찾기",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF121212),
                modifier = Modifier.align(Alignment.Center),
            )
        }

        Spacer(
            modifier = Modifier.height(48.dp)
        )

        when (step) {
            WebForgotPasswordStep.INPUT_INFO -> {
                StepIndicator(
                    text = "1/3 단계"
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "가입 시 사용한 이메일과\n전화번호를 입력해주세요.",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color(0xFF8F8F8F),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                )

                Spacer(
                    modifier = Modifier.height(40.dp)
                )

                FieldLabel("이메일")

                StyledTextField(
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

                FieldLabel("전화번호")

                StyledTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                    },
                    placeholder = "010-0000-0000",
                    keyboardType = KeyboardType.Phone,
                )

                Spacer(
                    modifier = Modifier.height(40.dp)
                )

                Button(
                    onClick = {
                        isLoading = true
                        step = WebForgotPasswordStep.VERIFY_CODE
                        isLoading = false
                    },
                    enabled =
                        email.isNotBlank() &&
                                phone.isNotBlank() &&
                                !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        disabledContainerColor = Color(0xFFD0D5DD),
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = "인증번호 발송",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.White,
                        )
                    }
                }
            }

            WebForgotPasswordStep.VERIFY_CODE -> {
                StepIndicator(
                    text = "2/3 단계"
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "전화번호로 발송된\n6자리 인증코드를 입력해주세요.",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color(0xFF8F8F8F),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                )

                Spacer(
                    modifier = Modifier.height(40.dp)
                )

                FieldLabel("인증코드")

                StyledTextField(
                    value = code,
                    onValueChange = {
                        if (it.length <= 6) {
                            code = it
                        }
                    },
                    placeholder = "6자리 숫자 입력",
                    keyboardType = KeyboardType.Number,
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(
                    text = "인증코드를 받지 못하셨나요?",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = Primary,
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable {
                            code = ""
                        },
                )

                Spacer(
                    modifier = Modifier.height(40.dp)
                )

                Button(
                    onClick = {
                        step = WebForgotPasswordStep.NEW_PASSWORD
                    },
                    enabled = code.length == 6 && !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        disabledContainerColor = Color(0xFFD0D5DD),
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                ) {
                    Text(
                        text = "확인",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.White,
                    )
                }
            }

            WebForgotPasswordStep.NEW_PASSWORD -> {
                StepIndicator(
                    text = "3/3 단계"
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "새로운 비밀번호를 설정해주세요.",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color(0xFF8F8F8F),
                    textAlign = TextAlign.Center,
                )

                Spacer(
                    modifier = Modifier.height(40.dp)
                )

                FieldLabel("새 비밀번호")

                StyledTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                    },
                    placeholder = "8자 이상 입력해주세요",
                    isPassword = true,
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                FieldLabel("새 비밀번호 확인")

                StyledTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                    },
                    placeholder = "비밀번호를 다시 입력해주세요",
                    isPassword = true,
                )

                Spacer(
                    modifier = Modifier.height(40.dp)
                )

                Button(
                    onClick = {
                        step = WebForgotPasswordStep.SUCCESS
                    },
                    enabled =
                        newPassword.length >= 8 &&
                                newPassword == confirmPassword &&
                                !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        disabledContainerColor = Color(0xFFD0D5DD),
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = "비밀번호 변경",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.White,
                        )
                    }
                }
            }

            WebForgotPasswordStep.SUCCESS -> {
                Text(
                    text = "변경 완료",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color(0xFF121212),
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text = "비밀번호가 변경되었습니다.",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color(0xFF8F8F8F),
                    textAlign = TextAlign.Center,
                )

                Spacer(
                    modifier = Modifier.height(40.dp)
                )

                Button(
                    onClick = onBackClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                ) {
                    Text(
                        text = "로그인으로 돌아가기",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(
    text: String,
) {
    Text(
        text = text,
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        color = Primary,
    )
}

@Composable
private fun FieldLabel(
    text: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    ) {
        Text(
            text = text,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color(0xFF121212),
        )
    }
}

@Composable
private fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
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
            keyboardType = keyboardType,
        ),
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF5F5F5),
            unfocusedContainerColor = Color(0xFFF5F5F5),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Primary,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
    )
}
