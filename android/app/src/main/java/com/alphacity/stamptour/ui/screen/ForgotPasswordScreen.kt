package com.alphacity.stamptour.ui.screen

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.ui.theme.Primary
import com.alphacity.stamptour.viewmodel.ForgotPasswordViewModel

@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val isFormValid = email.isNotBlank() && newPassword.length >= 8 && newPassword == confirmPassword

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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 헤더
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_revert),
                    contentDescription = "뒤로가기",
                    tint = Color(0xFF121212),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(24.dp)
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

            // 안내 텍스트
            Text(
                text = "가입 시 사용한 이메일을 입력하고\n새로운 비밀번호를 설정하세요.",
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                ),
                color = Color(0xFF8F8F8F),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 이메일
            ForgotPasswordFieldLabel("이메일")
            ForgotPasswordTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "example@email.com",
                leadingIcon = android.R.drawable.ic_dialog_email,
                keyboardType = KeyboardType.Email,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 새 비밀번호
            ForgotPasswordFieldLabel("새 비밀번호")
            ForgotPasswordTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                placeholder = "8자 이상 입력해주세요.",
                leadingIcon = android.R.drawable.ic_lock_idle_lock,
                isPassword = true,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 새 비밀번호 확인
            ForgotPasswordFieldLabel("새 비밀번호 확인")
            ForgotPasswordTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "비밀번호를 다시 입력해주세요.",
                leadingIcon = android.R.drawable.ic_lock_idle_lock,
                isPassword = true,
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 비밀번호 변경 버튼
            Button(
                onClick = { viewModel.resetPassword(email, newPassword, confirmPassword) },
                enabled = isFormValid && !uiState.isLoading,
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
                    text = "비밀번호 변경",
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                    ),
                    color = Color(0xFFF8F8F8),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "© 2026 Alpha Stamp. All rights reserved.",
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                ),
                color = Color(0xFF8F8F8F),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 40.dp),
            )
        }

        // 로딩 오버레이
        if (uiState.isLoading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
            ) {
                CircularProgressIndicator(color = Primary)
            }
        }
    }
}

@Composable
private fun ForgotPasswordFieldLabel(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    ) {
        Text(
            text = text,
            style = androidx.compose.ui.text.TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
            ),
            color = Color(0xFF121212),
        )
    }
}

@Composable
private fun ForgotPasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: Int,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                ),
                color = Color(0xFFBFBFBF),
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(leadingIcon),
                contentDescription = null,
                modifier = Modifier.size(17.dp),
                tint = Color(0xFFC7C7C7),
            )
        },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Primary,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
    )
}
