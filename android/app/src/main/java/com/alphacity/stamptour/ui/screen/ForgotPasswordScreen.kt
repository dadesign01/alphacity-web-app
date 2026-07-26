package com.alphacity.stamptour.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alphacity.stamptour.R
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.ui.theme.Primary
import com.alphacity.stamptour.viewmodel.ForgotPasswordStep
import com.alphacity.stamptour.viewmodel.ForgotPasswordViewModel

@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            Toast.makeText(context, "비밀번호가 변경되었습니다", Toast.LENGTH_SHORT).show()
            onBackClick()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 헤더
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(id = R.drawable.icon_back_arrow),
                    contentDescription = "뒤로가기",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(13.dp, 26.dp)
                        .clickable { onBackClick() },
                )
                Text(
                    text = "비밀번호 찾기",
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                    ),
                    color = Color(0xFF121212),
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // 단계별 표시
            val stepText = when (uiState.step) {
                ForgotPasswordStep.INPUT_INFO -> "1/3 단계"
                ForgotPasswordStep.VERIFY_CODE -> "2/3 단계"
                ForgotPasswordStep.NEW_PASSWORD -> "3/3 단계"
            }
            Text(
                text = stepText,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = Primary,
            )

            Spacer(modifier = Modifier.height(8.dp))

            when (uiState.step) {
                ForgotPasswordStep.INPUT_INFO -> {
                    Text(
                        text = "가입 시 사용한 이메일과\n전화번호를 입력해주세요.",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = Color(0xFF8F8F8F),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    FieldLabel("이메일")
                    StyledTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "example@email.com",
                        keyboardType = KeyboardType.Email,
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    FieldLabel("전화번호")
                    StyledTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        placeholder = "010-0000-0000",
                        keyboardType = KeyboardType.Phone,
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    Button(
                        onClick = { viewModel.sendCode(email, phone) },
                        enabled = email.isNotBlank() && phone.isNotBlank() && !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            disabledContainerColor = Color(0xFFD0D5DD),
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("인증번호 발송", fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White)
                        }
                    }
                }

                ForgotPasswordStep.VERIFY_CODE -> {
                    Text(
                        text = "전화번호로 발송된\n6자리 인증코드를 입력해주세요.",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = Color(0xFF8F8F8F),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    FieldLabel("인증코드")
                    StyledTextField(
                        value = code,
                        onValueChange = { if (it.length <= 6) code = it },
                        placeholder = "6자리 숫자 입력",
                        keyboardType = KeyboardType.Number,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "인증코드를 받지 못하셨나요?",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = Primary,
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable { viewModel.sendCode(email, phone) },
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    Button(
                        onClick = { viewModel.verifyCode(code) },
                        enabled = code.length == 6 && !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            disabledContainerColor = Color(0xFFD0D5DD),
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                    ) {
                        Text("확인", fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White)
                    }
                }

                ForgotPasswordStep.NEW_PASSWORD -> {
                    Text(
                        text = "새로운 비밀번호를 설정해주세요.",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = Color(0xFF8F8F8F),
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    FieldLabel("새 비밀번호")
                    StyledTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        placeholder = "8자 이상 입력해주세요",
                        isPassword = true,
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    FieldLabel("새 비밀번호 확인")
                    StyledTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = "비밀번호를 다시 입력해주세요",
                        isPassword = true,
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    Button(
                        onClick = { viewModel.resetPassword(code, newPassword, confirmPassword) },
                        enabled = newPassword.length >= 8 && newPassword == confirmPassword && !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            disabledContainerColor = Color(0xFFD0D5DD),
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("비밀번호 변경", fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
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
            Text(placeholder, fontFamily = Pretendard, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color(0xFFBFBFBF))
        },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF5F5F5),
            unfocusedContainerColor = Color(0xFFF5F5F5),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Primary,
        ),
        modifier = Modifier.fillMaxWidth().height(54.dp),
    )
}
