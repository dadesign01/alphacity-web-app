package com.alphacity.stamptour.ui.screen

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alphacity.stamptour.network.ApiClient
import com.alphacity.stamptour.network.dto.ApiResponse
import com.alphacity.stamptour.network.dto.PhoneLoginData
import com.alphacity.stamptour.network.dto.PhoneLoginRequest
import com.alphacity.stamptour.network.dto.SendCodeData
import com.alphacity.stamptour.network.dto.SendCodeRequest
import com.alphacity.stamptour.web.WebTokenManager
import composewebtest.generated.resources.Res
import composewebtest.generated.resources.header_left_arrow
import composewebtest.theme.MainGradient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.launch
import kotlinx.browser.window
import org.jetbrains.compose.resources.painterResource
import web.QrTarget

@Composable
fun PhoneLogin(
    qrTarget: QrTarget? = null,
    onAuthSuccess: () -> Unit = {},
    onGuestClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    var phone by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }

    var isCodeSent by remember { mutableStateOf(false) }
    var isPhoneVerified by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val phoneFocusRequester = remember {
        FocusRequester()
    }

    val verificationFocusRequester = remember {
        FocusRequester()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {

            // ========================================
            // 헤더
            // ========================================

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = {
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFF121212),
                    ),
                    contentPadding = androidx.compose.foundation.layout
                        .PaddingValues(0.dp),
                    modifier = Modifier.size(28.dp),
                ) {
                    Image(
                        painter = painterResource(
                            Res.drawable.header_left_arrow
                        ),
                        contentDescription = "뒤로가기",
                        modifier = Modifier
                            .size(
                                width = 8.dp,
                                height = 15.dp,
                            )
                            .clickable {
                                onBackClick()
                            },
                    )
                }

                Spacer(
                    modifier = Modifier.width(12.dp)
                )

                Text(
                    text = "휴대폰 번호로 입장하기",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF121212),
                )
            }

            // ========================================
            // 헤더 하단 border
            // ========================================

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFB5B5B5)),
            )

            // ========================================
            // 본문
            // ========================================

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {

                Spacer(
                    modifier = Modifier.height(36.dp)
                )

                Text(
                    text = "원활한 스탬프 적립 및 사용을 위한\n휴대폰 인증을 해주세요.",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF1B1B1B),
                    lineHeight = 24.sp,
                )

                Spacer(
                    modifier = Modifier.height(40.dp)
                )

                // ========================================
                // 전화번호
                // ========================================

                Text(
                    text = "전화번호",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                AuthTextField(
                    value = phone,
                    onValueChange = {
                        phone = it

                        isCodeSent = false
                        isPhoneVerified = false
                        verificationCode = ""
                    },
                    placeholder = "하이픈(-)을 제외하고 숫자만 입력해 주세요.",
                    keyboardType = KeyboardType.Phone,
                    focusRequester = phoneFocusRequester,
                    enabled = !isPhoneVerified,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                )

                // ========================================
                // 인증번호 발송
                // ========================================

                if (!isCodeSent) {

                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )

                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true

                                try {
                                    val response: HttpResponse =
                                        ApiClient.client.post(
                                            "auth/send-code"
                                        ) {
                                            contentType(
                                                ContentType.Application.Json
                                            )

                                            setBody(
                                                SendCodeRequest(
                                                    phone = phone,
                                                )
                                            )
                                        }

                                    val result:
                                            ApiResponse<SendCodeData> =
                                        response.body()

                                    if (result.success) {
                                        isCodeSent = true
                                        verificationCode = ""

                                        println(
                                            "================================"
                                        )
                                        println(
                                            "[PhoneLogin] 인증번호 발송 성공"
                                        )
                                        println(
                                            "================================"
                                        )
                                    } else {
                                        println(
                                            result.error?.message
                                                ?: "인증번호 발송에 실패했습니다."
                                        )
                                    }
                                } catch (e: Exception) {
                                    println(
                                        "인증번호 발송 오류: ${e.message}"
                                    )
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled =
                            phone.isNotBlank() &&
                                    !isPhoneVerified &&
                                    !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor =
                                Color(0xFFB5B5B5),
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding =
                            androidx.compose.foundation.layout
                                .PaddingValues(0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = MainGradient,
                                    shape = RoundedCornerShape(8.dp),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "인증번호 발송",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.White,
                            )
                        }
                    }
                }

                // ========================================
                // 인증번호 영역
                // ========================================

                if (isCodeSent) {

                    Spacer(
                        modifier = Modifier.height(32.dp)
                    )

                    Text(
                        text = "인증번호",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                    )

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    AuthTextField(
                        value = verificationCode,
                        onValueChange = { input ->

                            val code =
                                input.filter {
                                    it.isDigit()
                                }

                            if (code.length <= 6) {
                                verificationCode = code
                            }
                        },
                        placeholder = "인증번호를 입력해주세요",
                        keyboardType = KeyboardType.Number,
                        focusRequester = verificationFocusRequester,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                    )
                }
            }

            // ========================================
            // 인증하기
            // ========================================

            if (isCodeSent) {

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true

                            try {
                                println(
                                    "================================"
                                )
                                println(
                                    "[PhoneLogin] 인증 요청 시작"
                                )
                                println(
                                    "[PhoneLogin] phone = $phone"
                                )
                                println(
                                    "[PhoneLogin] code = $verificationCode"
                                )
                                println(
                                    "================================"
                                )

                                val response: HttpResponse =
                                    ApiClient.client.post(
                                        "auth/phone-login"
                                    ) {
                                        contentType(
                                            ContentType.Application.Json
                                        )

                                        setBody(
                                            PhoneLoginRequest(
                                                phone = phone,
                                                code = verificationCode,
                                            )
                                        )
                                    }

                                println(
                                    "[PhoneLogin] HTTP status = ${response.status}"
                                )

                                val result:
                                        ApiResponse<PhoneLoginData> =
                                    response.body()

                                println(
                                    "[PhoneLogin] API success = ${result.success}"
                                )
                                println(
                                    "[PhoneLogin] data exists = ${result.data != null}"
                                )

                                if (
                                    result.success &&
                                    result.data != null
                                ) {
                                    val data = result.data

                                    println(
                                        "================================"
                                    )
                                    println(
                                        "[PhoneLogin] 휴대폰 인증 성공"
                                    )
                                    println(
                                        "[PhoneLogin] token 존재 = ${data.token.isNotBlank()}"
                                    )
                                    println(
                                        "[PhoneLogin] refreshToken 존재 = ${data.refreshToken.isNotBlank()}"
                                    )

                                    WebTokenManager.saveTokens(
                                        accessToken = data.token,
                                        refreshToken = data.refreshToken,
                                    )

                                    println(
                                        "[PhoneLogin] 토큰 저장 완료"
                                    )
                                    println(
                                        "[PhoneLogin] isLoggedIn = " +
                                                WebTokenManager.isLoggedIn()
                                    )
                                    println(
                                        "[PhoneLogin] qrTarget = $qrTarget"
                                    )
                                    println(
                                        "[PhoneLogin] onAuthSuccess 호출 직전"
                                    )
                                    println(
                                        "================================"
                                    )

                                    isPhoneVerified = true
                                    isCodeSent = false
                                    verificationCode = ""

                                    onAuthSuccess()

                                    println(
                                        "[PhoneLogin] onAuthSuccess 호출 완료"
                                    )
                                } else {
                                    println(
                                        "================================"
                                    )
                                    println(
                                        "[PhoneLogin] 인증 실패"
                                    )
                                    println(
                                        "[PhoneLogin] error = " +
                                                (
                                                        result.error?.message
                                                            ?: "인증번호가 올바르지 않습니다."
                                                        )
                                    )
                                    println(
                                        "================================"
                                    )
                                }
                            } catch (e: Exception) {
                                println(
                                    "================================"
                                )
                                println(
                                    "[PhoneLogin] 휴대폰 인증 로그인 오류"
                                )
                                println(
                                    "[PhoneLogin] ${e.message}"
                                )
                                println(
                                    "================================"
                                )
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled =
                        verificationCode.length == 6 &&
                                !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor =
                            Color(0xFFB5B5B5),
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding =
                        androidx.compose.foundation.layout
                            .PaddingValues(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = MainGradient,
                                shape = RoundedCornerShape(8.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "인증하기",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White,
                        )
                    }
                }
            }
        }

        // ========================================
        // Loading
        // ========================================

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
                    color = Color(0xFF2563EB),
                )
            }
        }
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    focusRequester: FocusRequester,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Done,
        ),
        modifier = modifier
            .focusRequester(focusRequester),
        textStyle = TextStyle(
            fontSize = 16.sp,
            color = Color.Black,
        ),
        decorationBox = { innerTextField ->
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Bottom,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(47.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 15.sp,
                            color = Color(0xFF8F8F8F),
                        )
                    }

                    innerTextField()
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.Black),
                )
            }
        },
    )
}