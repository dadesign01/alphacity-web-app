package com.alphacity.stamptour.ui.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.alphacity.stamptour.R
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.ui.theme.Primary
import com.alphacity.stamptour.viewmodel.MyPageViewModel

@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit,
    initialNickname: String = "",
    initialEmail: String = "",
    initialProvider: String? = null,
    viewModel: MyPageViewModel? = null,
) {
    var nickname by remember { mutableStateOf(initialNickname) }
    var email by remember { mutableStateOf(initialEmail) }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    val name = ""
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var addressDetail by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val isSocialLogin = initialProvider != null
    val context = LocalContext.current
    val isLoading by (viewModel?.isLoading ?: MutableStateFlow(false)).collectAsState()
    val saveSuccess by (viewModel?.saveSuccess ?: MutableStateFlow(false)).collectAsState()
    val saveError by (viewModel?.saveError ?: MutableStateFlow(null)).collectAsState()
    val deleteSuccess by (viewModel?.deleteSuccess ?: MutableStateFlow(false)).collectAsState()
    val deleteError by (viewModel?.deleteError ?: MutableStateFlow(null)).collectAsState()
    val isCodeSent by (viewModel?.isCodeSent ?: MutableStateFlow(false)).collectAsState()
    val isPhoneVerified by (viewModel?.isPhoneVerified ?: MutableStateFlow(false)).collectAsState()
    val verificationError by (viewModel?.verificationError ?: MutableStateFlow(null)).collectAsState()

    // ViewModel에서 현재 프로필 이미지 URI 읽기 (화면 재진입 시 유지됨)
    val persistedImageUri by (viewModel?.profileImageUri ?: MutableStateFlow(null)).collectAsState()
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    // 화면 열릴 때 ViewModel에 저장된 URI로 초기화
    LaunchedEffect(Unit) {
        if (selectedImageUri == null) selectedImageUri = persistedImageUri
    }

    // 저장 성공/실패 처리
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            Toast.makeText(context, "저장되었습니다.", Toast.LENGTH_SHORT).show()
            viewModel?.clearSaveState()
            onBackClick()
        }
    }
    LaunchedEffect(saveError) {
        saveError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel?.clearSaveState()
        }
    }
    LaunchedEffect(deleteSuccess) {
        if (deleteSuccess) {
            Toast.makeText(context, "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
            viewModel?.clearDeleteState()
            onLogout()
        }
    }
    LaunchedEffect(deleteError) {
        deleteError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel?.clearDeleteState()
        }
    }
    LaunchedEffect(verificationError) {
        verificationError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    // 회원탈퇴 확인 다이얼로그
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("회원탈퇴", fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
            text = { Text("정말 탈퇴하시겠습니까?\n모든 데이터가 삭제됩니다.", fontFamily = Pretendard, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel?.deleteAccount()
                }) {
                    Text("탈퇴", fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, color = Color(0xFFEA580C))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("취소", fontFamily = Pretendard, fontWeight = FontWeight.Medium, color = Color(0xFF8F8F8F))
                }
            },
        )
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            viewModel?.setProfileImageUri(it)  // ViewModel에 저장 → MyPageScreen에서도 반영
        }
    }

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
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clickable {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "프로필",
                            modifier = Modifier
                                .size(104.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.icon_profile),
                            contentDescription = "프로필",
                            modifier = Modifier
                                .size(104.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                    }
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

                // 비밀번호 (소셜 로그인 사용자에게는 숨김)
                if (!isSocialLogin) {
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
                }

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
                                onValueChange = {
                                    phone = it
                                    // 전화번호 변경 시 인증 상태 초기화
                                    if (isPhoneVerified || isCodeSent) {
                                        viewModel?.resetVerificationState()
                                        verificationCode = ""
                                    }
                                },
                                placeholder = "010-0000-0000",
                                enabled = !isPhoneVerified,
                                keyboardType = KeyboardType.Phone,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .height(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isPhoneVerified) Color(0xFFF5F5F5)
                                    else Color(0xFFEDF7FF)
                                )
                                .clickable(enabled = !isPhoneVerified && phone.isNotBlank()) {
                                    viewModel?.sendCode(phone)
                                }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (isCodeSent) "재전송" else "본인인증",
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = if (isPhoneVerified) Color(0xFF8F8F8F) else Primary,
                            )
                        }
                    }

                    // 인증번호 입력 (코드 발송 후, 인증 완료 전)
                    if (isCodeSent && !isPhoneVerified) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                ProfileTextField(
                                    value = verificationCode,
                                    onValueChange = { verificationCode = it },
                                    placeholder = "인증번호 6자리",
                                    keyboardType = KeyboardType.Number,
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (verificationCode.length == 6) Primary
                                        else Primary.copy(alpha = 0.5f)
                                    )
                                    .clickable(enabled = verificationCode.length == 6) {
                                        viewModel?.verifyCode(phone, verificationCode)
                                    }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "확인",
                                    fontFamily = Pretendard,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = Color.White,
                                )
                            }
                        }
                    }

                    // 인증 완료 표시
                    if (isPhoneVerified) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "인증 완료",
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFF22C55E),
                            )
                            Text(
                                text = "인증완료",
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = Color(0xFF22C55E),
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
                    .clickable(enabled = !isLoading) {
                        if (nickname.isBlank()) {
                            Toast.makeText(context, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
                            return@clickable
                        }
                        if (password.isNotBlank() && password != passwordConfirm) {
                            Toast.makeText(context, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                            return@clickable
                        }
                        viewModel?.updateProfile(
                            nickname,
                            password.ifBlank { null },
                            if (isPhoneVerified) phone else null,
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(
                        text = "저장하기",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.White,
                    )
                }
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
                    modifier = Modifier.clickable { showDeleteDialog = true },
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
    keyboardType: KeyboardType = KeyboardType.Text,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
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
