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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alphacity.stamptour.network.ApiService
import com.alphacity.stamptour.network.dto.UserProfile
import com.alphacity.stamptour.web.WebTokenManager

private val Primary = Color(0xFF02CDF8)
private val Pretendard = FontFamily.SansSerif

@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit = {},
) {
    var userProfile by remember {
        mutableStateOf<UserProfile?>(null)
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null

        if (!WebTokenManager.isLoggedIn()) {
            errorMessage = "로그인이 필요합니다."
            isLoading = false
            return@LaunchedEffect
        }

        val response = ApiService.getUserProfile()

        if (response.success) {
            val profile = response.data

            if (profile != null) {
                userProfile = profile
            } else {
                errorMessage = "사용자 정보를 불러오지 못했습니다."
            }
        } else {
            errorMessage =
                response.error?.message
                    ?: "사용자 정보를 불러오지 못했습니다."
        }

        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(50.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "←",
                fontFamily = Pretendard,
                fontSize = 34.sp,
                color = Color(0xFF121212),
                modifier = Modifier
                    .size(width = 13.dp, height = 26.dp)
                    .clickable {
                        onBackClick()
                    },
            )

            Spacer(
                modifier = Modifier.width(24.dp)
            )

            Text(
                text = "개인정보",
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

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    color = Primary,
                )
            }
        } else if (userProfile == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = errorMessage
                        ?: "사용자 정보를 불러오지 못했습니다.",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color(0xFF8F8F8F),
                )
            }
        } else {
            val profile = userProfile!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(
                    modifier = Modifier.height(32.dp)
                )

                // Profile
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF5F5F5)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = profile.nickname
                                .firstOrNull()
                                ?.uppercase()
                                ?.toString()
                                ?: "P",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 32.sp,
                            color = Primary,
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(32.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ProfileInfoRow(
                        label = "닉네임",
                        value = profile.nickname,
                    )

                    ProfileInfoRow(
                        label = "이메일",
                        value = profile.email,
                    )

                    ProfileInfoRow(
                        label = "이름",
                        value = profile.name,
                    )

                    ProfileInfoRow(
                        label = "전화번호",
                        value = profile.phone,
                    )

                    ProfileInfoRow(
                        label = "주소",
                        value = profile.address,
                    )

                    ProfileInfoRow(
                        label = "상세주소",
                        value = profile.addressDetail,
                    )

                    ProfileInfoRow(
                        label = "생년월일",
                        value = profile.birthDate,
                    )

                    ProfileInfoRow(
                        label = "성별",
                        value = formatGender(profile.gender),
                    )

                    ProfileInfoRow(
                        label = "가입 방식",
                        value = formatProvider(profile.provider),
                    )
                }

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                // Privacy notice
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color(0xFFF8F8F8))
                        .padding(16.dp),
                ) {
                    Text(
                        text = "개인정보는 서비스 제공 목적으로만 사용하며,\n관련 법령에 따라 안전하게 관리됩니다.",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Color(0xFF8F8F8F),
                        lineHeight = 18.sp,
                    )
                }

                Spacer(
                    modifier = Modifier.height(32.dp)
                )

                // Footer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF9F9F9))
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "2026 OLLYMOA. All rights reserved.",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Normal,
                        fontSize = 10.sp,
                        color = Color(0xFF8F8F8F),
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String?,
) {
    Column {
        Text(
            text = label,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color(0xFF121212),
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    Color(0xFFF8F8F8),
                    RoundedCornerShape(8.dp),
                )
                .border(
                    width = 1.dp,
                    color = Color(0xFFE9E9E9),
                    shape = RoundedCornerShape(8.dp),
                )
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = value?.takeIf {
                    it.isNotBlank()
                } ?: "등록된 정보가 없습니다.",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = if (
                    value.isNullOrBlank()
                ) {
                    Color(0xFFBFBFBF)
                } else {
                    Color(0xFF121212)
                },
            )
        }
    }
}

private fun formatGender(
    gender: String?,
): String? {
    return when (gender?.lowercase()) {
        "male" -> "남성"
        "female" -> "여성"
        "other" -> "기타"
        else -> gender
    }
}

private fun formatProvider(
    provider: String?,
): String? {
    return when (provider?.lowercase()) {
        "email" -> "이메일"
        "kakao" -> "카카오"
        "naver" -> "네이버"
        "google" -> "구글"
        "apple" -> "애플"
        else -> provider
    }
}