package com.alphacity.stamptour.ui.screen

import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import com.alphacity.stamptour.viewmodel.RegisterViewModel

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = hiltViewModel(),
    onRegisterSuccess: () -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var agreedToTerms by remember { mutableStateOf(false) }

    val isFormValid = name.isNotBlank() && email.isNotBlank() && uiState.isPhoneVerified &&
        password.length >= 8 && password == confirmPassword && agreedToTerms

    LaunchedEffect(uiState.isRegistered) {
        if (uiState.isRegistered) onRegisterSuccess()
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
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
                    text = "회원가입",
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                    ),
                    color = Color(0xFF121212),
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 이름
            FieldLabel("이름", required = true)
            RegisterTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "홍길동",
                leadingIcon = android.R.drawable.ic_menu_myplaces,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 이메일
            FieldLabel("이메일", required = true)
            RegisterTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "example@email.com",
                leadingIcon = android.R.drawable.ic_dialog_email,
                keyboardType = KeyboardType.Email,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 휴대폰
            FieldLabel("휴대폰", required = true)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RegisterTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = "01012345678",
                    leadingIcon = android.R.drawable.ic_menu_call,
                    keyboardType = KeyboardType.Phone,
                    enabled = !uiState.isPhoneVerified,
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = { viewModel.sendCode(phone) },
                    enabled = !uiState.isPhoneVerified && phone.isNotBlank(),
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
                        text = if (uiState.isCodeSent) "재전송" else "본인인증",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                        ),
                        color = if (uiState.isPhoneVerified) Color(0xFF8F8F8F) else Primary,
                    )
                }
            }

            // 인증번호 입력 (코드 발송 후 표시)
            if (uiState.isCodeSent && !uiState.isPhoneVerified) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    RegisterTextField(
                        value = verificationCode,
                        onValueChange = { verificationCode = it },
                        placeholder = "인증번호 6자리",
                        leadingIcon = android.R.drawable.ic_dialog_info,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                    )
                    Button(
                        onClick = { viewModel.verifyCode(phone, verificationCode) },
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
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                            ),
                            color = Color.White,
                        )
                    }
                }
            }

            // 인증 완료 표시
            if (uiState.isPhoneVerified) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "✓ 인증 완료",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                        ),
                        color = Color(0xFF22C55E),
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 비밀번호
            FieldLabel("비밀번호", required = true)
            RegisterTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "8자 이상 입력해주세요.",
                leadingIcon = android.R.drawable.ic_lock_idle_lock,
                isPassword = true,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 비밀번호 확인
            FieldLabel("비밀번호 확인", required = true)
            RegisterTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "비밀번호를 다시 입력해주세요.",
                leadingIcon = android.R.drawable.ic_lock_idle_lock,
                isPassword = true,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 이용약관 동의
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Checkbox(
                    checked = agreedToTerms,
                    onCheckedChange = { agreedToTerms = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Primary,
                        uncheckedColor = Color(0xFFE9E9E9),
                    ),
                    modifier = Modifier.size(18.dp),
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "이용약관 및 개인정보처리방침에 동의합니다.",
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                    ),
                    color = Color(0xFF121212),
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "자세히 보기 >",
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                    ),
                    color = Color(0xFF8D8D8D),
                    modifier = Modifier.clickable { /* 약관 보기 */ },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 가입하기 버튼
            Button(
                onClick = { viewModel.register(name, email, phone, password, confirmPassword) },
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
                    text = "가입하기",
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                    ),
                    color = Color(0xFFF8F8F8),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 로그인 링크
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "이미 계정이 있으신가요? ",
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                    ),
                    color = Color(0xFF8F8F8F),
                )
                Text(
                    text = "로그인",
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    ),
                    color = Primary,
                    modifier = Modifier.clickable { onBackClick() },
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "© 2026 Alpha Stamp. All rights reserved.",
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                ),
                color = Color(0xFF8F8F8F),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
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
private fun FieldLabel(text: String, required: Boolean = false) {
    Row(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = text,
            style = androidx.compose.ui.text.TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
            ),
            color = Color(0xFF121212),
        )
        if (required) {
            Text(
                text = " *",
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                ),
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
    leadingIcon: Int,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
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
            focusedContainerColor = if (enabled) Color.White else Color(0xFFF5F5F5),
            unfocusedContainerColor = if (enabled) Color.White else Color(0xFFF5F5F5),
            disabledContainerColor = Color(0xFFF5F5F5),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = Primary,
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
    )
}
