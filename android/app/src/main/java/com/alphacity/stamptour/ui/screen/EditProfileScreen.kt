package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alphacity.stamptour.R
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.ui.theme.Primary

@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit,
) {
    var nickname by remember { mutableStateOf("울퉁불퉁한만두") }
    var email by remember { mutableStateOf("mandu@example.com") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    val name = "홍길동"
    var phone by remember { mutableStateOf("010-1234-5678") }
    var address by remember { mutableStateOf("") }
    var addressDetail by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding(),
    ) {
        // === Header ===
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(50.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(id = R.drawable.icon_back_arrow),
                contentDescription = "뒤로",
                modifier = Modifier
                    .size(13.dp, 26.dp)
                    .clickable(onClick = onBackClick),
                contentScale = ContentScale.Fit,
            )
            Spacer(modifier = Modifier.width(24.dp))
            Text(
                text = "개인정보 수정",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF121212),
            )
        }
        Divider(color = Color(0xFFE2E2E2), thickness = 1.dp)

        // === Scrollable Content ===
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // === Profile Image ===
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Box(modifier = Modifier.size(104.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.icon_profile),
                        contentDescription = "프로필",
                        modifier = Modifier
                            .size(104.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                    // Camera icon overlay
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE9E9E9), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = "사진 변경",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF8F8F8F),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // === Form Fields ===
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                // 닉네임
                FormField(
                    label = "닉네임",
                    required = true,
                    value = nickname,
                    onValueChange = { nickname = it },
                    placeholder = "닉네임을 입력해주세요.",
                )

                // 이메일
                FormField(
                    label = "이메일",
                    required = true,
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "이메일을 입력해주세요.",
                    helperText = "이메일은 로그인 시 사용됩니다.",
                )

                // 비밀번호
                FormField(
                    label = "비밀번호",
                    required = true,
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "8자 이상 입력해주세요.",
                    isPassword = true,
                )

                // 비밀번호 확인
                FormField(
                    label = "비밀번호 확인",
                    required = true,
                    value = passwordConfirm,
                    onValueChange = { passwordConfirm = it },
                    placeholder = "비밀번호를 다시 입력해주세요.",
                    isPassword = true,
                )

                // 이름 (disabled)
                FormField(
                    label = "이름",
                    required = true,
                    value = name,
                    onValueChange = {},
                    placeholder = "",
                    enabled = false,
                    trailingIcon = {
                        Image(
                            painter = painterResource(id = R.drawable.icon_lock),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            contentScale = ContentScale.Fit,
                        )
                    },
                )

                // 휴대폰
                Column {
                    FormLabel(label = "휴대폰", required = true)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ProfileTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                placeholder = "010-0000-0000",
                            )
                        }
                        Box(
                            modifier = Modifier
                                .height(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEDF7FF))
                                .clickable { /* TODO: 본인인증 */ }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "본인인증",
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Primary,
                            )
                        }
                    }
                }

                // 주소
                Column {
                    FormLabel(label = "주소", required = false)
                    Spacer(modifier = Modifier.height(6.dp))
                    ProfileTextField(
                        value = address,
                        onValueChange = { address = it },
                        placeholder = "주소를 입력해주세요.",
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ProfileTextField(
                        value = addressDetail,
                        onValueChange = { addressDetail = it },
                        placeholder = "상세주소를 입력해주세요.",
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // === Privacy Notice ===
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(Color(0xFFF8F8F8))
                    .padding(16.dp),
            ) {
                Text(
                    text = "개인정보는 서비스 제공 목적으로만 사용되며,\n관련 법령에 따라 안전하게 관리됩니다.",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = Color(0xFF8F8F8F),
                    lineHeight = 18.sp,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // === Save Button ===
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF6092FF),
                                Color(0xFF2563EB),
                                Color(0xFF1551D3),
                            )
                        )
                    )
                    .clickable { /* TODO: save */ },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "저장하기",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.White,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // === Logout | Withdraw ===
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "로그아웃",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = Color(0xFF8F8F8F),
                    modifier = Modifier.clickable {
                        onLogout()
                    },
                )
                Text(
                    text = "  |  ",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = Color(0xFFD9D9D9),
                )
                Text(
                    text = "회원탈퇴",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = Color(0xFF8F8F8F),
                    modifier = Modifier.clickable { /* TODO */ },
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // === Footer ===
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9F9F9))
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "\u00A9 2026 Alpha Stamp. All rights reserved.",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                    color = Color(0xFF8F8F8F),
                )
            }
        }
    }
}

@Composable
private fun FormLabel(label: String, required: Boolean) {
    Row {
        Text(
            text = label,
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
                fontSize = 14.sp,
                color = Primary,
            )
        }
    }
}

@Composable
private fun FormField(
    label: String,
    required: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    helperText: String? = null,
    isPassword: Boolean = false,
    enabled: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    Column {
        FormLabel(label = label, required = required)
        Spacer(modifier = Modifier.height(6.dp))
        ProfileTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            isPassword = isPassword,
            enabled = enabled,
            trailingIcon = trailingIcon,
        )
        if (helperText != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = helperText,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = Color(0xFF8F8F8F),
            )
        }
    }
}

@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    enabled: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = Pretendard,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = if (enabled) Color(0xFF121212) else Color(0xFFBFBFBF),
        ),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(1.dp, Color(0xFFE9E9E9), RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = Color(0xFFBFBFBF),
                        )
                    }
                    innerTextField()
                }
                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    trailingIcon()
                }
            }
        },
    )
}
