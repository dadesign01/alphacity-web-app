package com.alphacity.stamptour.ui.screen

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.border
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alphacity.stamptour.R
import com.alphacity.stamptour.ui.theme.AlphaCityTheme
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.ui.theme.Primary
import com.alphacity.stamptour.viewmodel.LoginViewModel
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit = {},
    onGuestClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // 로그인 성공 시 화면 전환
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) onLoginSuccess()
    }

    // 에러 토스트
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    // 카카오 로그인 콜백
    val kakaoCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            android.util.Log.e("KakaoLogin", "카카오 로그인 실패: ${error.message}", error)
            Toast.makeText(context, "카카오 로그인 실패: ${error.message}", Toast.LENGTH_LONG).show()
        } else if (token != null) {
            android.util.Log.d("KakaoLogin", "카카오 토큰 획득 성공")
            viewModel.socialLogin("kakao", token.accessToken)
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
            Spacer(modifier = Modifier.height(100.dp))

            Text(
                text = "로그인",
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                ),
                color = Color(0xFF121212),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "축제와 함께 스탬프 여행을 시작하세요!",
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                ),
                color = Color(0xFF121212),
            )

            Spacer(modifier = Modifier.height(50.dp))

            LoginTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "example@email.com",
                leadingIcon = R.drawable.icon_email,
                keyboardType = KeyboardType.Email,
            )

            Spacer(modifier = Modifier.height(12.dp))

            LoginTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "********",
                leadingIcon = R.drawable.icon_lock,
                isPassword = true,
            )

            Text(
                text = "비밀번호를 잊어버리셨나요?",
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                ),
                color = Color(0xFF8F8F8F),
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 10.dp)
                    .clickable { onForgotPasswordClick() },
            )

            Spacer(modifier = Modifier.height(48.dp))

            // 로그인 버튼
            Button(
                onClick = { viewModel.login(email, password) },
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                Text(
                    text = "로그인",
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                    ),
                    color = Color(0xFFF8F8F8),
                )
            }

            // 3회 이상 실패 시 비밀번호 찾기 안내
            if (uiState.showForgotPasswordHint) {
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFF8E1), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "로그인에 3회 이상 실패했습니다.",
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                            ),
                            color = Color(0xFF795548),
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "비밀번호 찾기",
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                            ),
                            color = Primary,
                            modifier = Modifier.clickable { onForgotPasswordClick() },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onGuestClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEDF7FF)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                Text(
                    text = "비회원으로 계속하기",
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                    ),
                    color = Primary,
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // "또는" 구분선
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = Color(0xFFDBDBDB))
                Text(
                    text = "또는",
                    style = androidx.compose.ui.text.TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Medium, fontSize = 14.sp),
                    color = Color(0xFF8F8F8F),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = Color(0xFFDBDBDB))
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 소셜 로그인
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
                            if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
                                UserApiClient.instance.loginWithKakaoTalk(context, callback = kakaoCallback)
                            } else {
                                UserApiClient.instance.loginWithKakaoAccount(context, callback = kakaoCallback)
                            }
                        },
                ) {
                    Image(
                        painter = painterResource(R.drawable.icon_kakao),
                        contentDescription = "카카오 로그인",
                        modifier = Modifier.size(28.dp),
                        contentScale = ContentScale.Fit,
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 네이버
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF03C75A))
                        .clickable {
                            NaverIdLoginSDK.authenticate(
                                context as Activity,
                                object : OAuthLoginCallback {
                                    override fun onSuccess() {
                                        val accessToken = NaverIdLoginSDK.getAccessToken() ?: return
                                        viewModel.socialLogin("naver", accessToken)
                                    }
                                    override fun onFailure(httpStatus: Int, message: String) {
                                        Toast.makeText(context, "네이버 로그인 실패", Toast.LENGTH_SHORT).show()
                                    }
                                    override fun onError(errorCode: Int, message: String) {
                                        Toast.makeText(context, "네이버 로그인 오류", Toast.LENGTH_SHORT).show()
                                    }
                                },
                            )
                        },
                ) {
                    Image(
                        painter = painterResource(R.drawable.icon_naver),
                        contentDescription = "네이버 로그인",
                        modifier = Modifier.size(23.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "아직 계정이 없으신가요? ",
                    style = androidx.compose.ui.text.TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Medium, fontSize = 14.sp),
                    color = Color(0xFF8F8F8F),
                )
                Text(
                    text = "회원가입",
                    style = androidx.compose.ui.text.TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                    color = Primary,
                    modifier = Modifier.clickable { onRegisterClick() },
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "© 2026 Alpha Stamp. All rights reserved.",
                style = androidx.compose.ui.text.TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Normal, fontSize = 10.sp),
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
private fun LoginTextField(
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
                style = androidx.compose.ui.text.TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Medium, fontSize = 14.sp),
                color = Color(0xFF8F8F8F),
            )
        },
        leadingIcon = {
            Image(
                painter = painterResource(leadingIcon),
                contentDescription = null,
                modifier = Modifier.size(17.dp),
                contentScale = ContentScale.Fit,
            )
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
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    AlphaCityTheme {
        // Preview without ViewModel
    }
}
