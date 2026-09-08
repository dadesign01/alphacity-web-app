package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alphacity.stamptour.network.ApiService
import com.alphacity.stamptour.network.dto.UserProfile
import com.alphacity.stamptour.web.WebTokenManager

private val Primary = Color(0xFF02CDF8)
private val Pretendard = FontFamily.SansSerif

@Composable
fun MyPageScreen(
    onLogout: () -> Unit = {},
) {
    var userProfile by remember {
        mutableStateOf<UserProfile?>(null)
    }

    var stampCount by remember {
        mutableStateOf(0)
    }

    var couponCount by remember {
        mutableStateOf(0)
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var reloadKey by remember {
        mutableStateOf(0)
    }

    var showProfileInfo by remember {
        mutableStateOf(false)
    }

    var showActivityHistory by remember {
        mutableStateOf(false)
    }

    var showStoreRegister by remember {
        mutableStateOf(false)
    }

    var showPrivacyPolicy by remember {
        mutableStateOf(false)
    }

    var showTerms by remember {
        mutableStateOf(false)
    }

    /*
     * 실제 로그인 사용자 정보 조회
     */
    LaunchedEffect(reloadKey) {
        isLoading = true
        errorMessage = null

        if (!WebTokenManager.isLoggedIn()) {
            userProfile = null
            stampCount = 0
            couponCount = 0
            errorMessage = "로그인이 필요합니다."
            isLoading = false
            return@LaunchedEffect
        }

        try {
            val profileResponse = ApiService.getUserProfile()

            if (profileResponse.success && profileResponse.data != null) {
                userProfile = profileResponse.data

                stampCount = profileResponse.data.stampCount ?: 0
                couponCount = profileResponse.data.couponCount ?: 0
            } else {
                userProfile = null
                stampCount = 0
                couponCount = 0
                errorMessage =
                    profileResponse.error?.message
                        ?: "사용자 정보를 불러오지 못했습니다."
            }

            /*
             * 실제 보유 스탬프 조회
             */
            val stampResponse = ApiService.getUserStamps()

            if (stampResponse.success && stampResponse.data != null) {
                stampCount = stampResponse.data
                    .distinctBy { it.stampId }
                    .size
            }

            /*
             * 실제 보유 쿠폰 조회
             */
            val couponResponse = ApiService.getMyCoupons()

            if (couponResponse.success && couponResponse.data != null) {
                couponCount = couponResponse.data.size
            }
        } catch (e: Exception) {
            userProfile = null
            stampCount = 0
            couponCount = 0
            errorMessage =
                e.message ?: "사용자 정보를 불러오지 못했습니다."
        } finally {
            isLoading = false
        }
    }

    /*
     * 하위 화면
     */
    when {
        showProfileInfo -> {
            EditProfileScreen(
                onBackClick = {
                    showProfileInfo = false
                },
            )
            return
        }

        showActivityHistory -> {
            ActivityHistoryScreen(
                onBackClick = {
                    showActivityHistory = false
                },
            )
            return
        }

        showStoreRegister -> {
            SimpleSubScreen(
                title = "매장 등록",
                onBackClick = {
                    showStoreRegister = false
                },
            )
            return
        }

        showPrivacyPolicy -> {
            PolicyDetailScreen(
                policyType = PolicyType.PRIVACY,
                onBackClick = {
                    showPrivacyPolicy = false
                },
            )
            return
        }

        showTerms -> {
            PolicyDetailScreen(
                policyType = PolicyType.TERMS,
                onBackClick = {
                    showTerms = false
                },
            )
            return
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        /*
         * Header
         */
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
                text = "마이페이지",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color(0xFF121212),
            )
        }

        HorizontalDivider(
            color = Color(0xFFE5E5E5),
            thickness = 1.dp,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                ),
        ) {
            /*
             * Profile
             */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 24.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEDF7FF)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = userProfile
                            ?.nickname
                            ?.take(1)
                            ?.ifBlank { "?" }
                            ?: "?",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = Primary,
                    )
                }

                Spacer(
                    modifier = Modifier.width(16.dp)
                )

                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = userProfile
                            ?.nickname
                            ?: if (isLoading) {
                                "불러오는 중..."
                            } else {
                                "사용자 정보 없음"
                            },
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF121212),
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = userProfile?.email ?: "",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = Color(0xFF777777),
                    )
                }

                Text(
                    text = "›",
                    fontFamily = Pretendard,
                    fontSize = 28.sp,
                    color = Color(0xFF999999),
                    modifier = Modifier.clickable {
                        if (userProfile != null) {
                            showProfileInfo = true
                        }
                    },
                )
            }

            /*
             * 에러 메시지
             */
            if (errorMessage != null && userProfile == null) {
                Text(
                    text = errorMessage ?: "",
                    fontFamily = Pretendard,
                    fontSize = 13.sp,
                    color = Color(0xFFE85151),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 20.dp,
                            vertical = 8.dp,
                        ),
                )
            }

            /*
             * Stats
             */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 20.dp,
                    ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MyPageStatCard(
                    title = "나의 스탬프",
                    value = "${stampCount}개",
                    modifier = Modifier.weight(1f),
                )

                MyPageStatCard(
                    title = "보유 쿠폰",
                    value = "${couponCount}개",
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            /*
             * 메뉴
             */
            MyPageMenuItem(
                icon = "◷",
                title = "활동이력",
                onClick = {
                    showActivityHistory = true
                },
            )

            MyPageMenuItem(
                icon = "◎",
                title = "개인정보",
                onClick = {
                    if (userProfile != null) {
                        showProfileInfo = true
                    }
                },
            )

            MyPageMenuItem(
                icon = "▣",
                title = "상점등록",
                onClick = {
                    showStoreRegister = true
                },
            )

            MyPageMenuItem(
                icon = "▤",
                title = "개인정보처리방침",
                onClick = {
                    showPrivacyPolicy = true
                },
            )

            MyPageMenuItem(
                icon = "▤",
                title = "이용약관",
                onClick = {
                    showTerms = true
                },
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            /*
             * 로그아웃
             */
            Text(
                text = "로그아웃",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = Color(0xFFE85151),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        WebTokenManager.clear()
                        onLogout()
                    }
                    .padding(
                        vertical = 18.dp,
                    ),
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text(
                text = "2026 OLLYMOA. All rights reserved.",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                color = Color(0xFF999999),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        bottom = 40.dp,
                    ),
            )
        }
    }

    /*
     * Loading
     */
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(alpha = 0.15f)
                ),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                color = Primary,
            )
        }
    }
}

@Composable
private fun MyPageStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF8FAFC))
            .clickable {
                onClick()
            }
            .padding(18.dp),
    ) {
        Text(
            text = title,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = Color(0xFF777777),
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = value,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = Primary,
        )
    }
}

@Composable
private fun MyPageMenuItem(
    icon: String,
    title: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(
                horizontal = 20.dp,
                vertical = 16.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = icon,
            fontFamily = Pretendard,
            fontSize = 18.sp,
            color = Color(0xFF555555),
            modifier = Modifier.width(30.dp),
        )

        Text(
            text = title,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            color = Color(0xFF222222),
            modifier = Modifier.weight(1f),
        )

        Text(
            text = "›",
            fontFamily = Pretendard,
            fontSize = 24.sp,
            color = Color(0xFFAAAAAA),
        )
    }

    HorizontalDivider(
        color = Color(0xFFF0F0F0),
        thickness = 1.dp,
        modifier = Modifier.padding(
            horizontal = 20.dp,
        ),
    )
}

@Composable
private fun SubScreenHeader(
    title: String,
    onBackClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
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
                fontSize = 34.sp,
                color = Color(0xFF121212),
                modifier = Modifier.clickable {
                    onBackClick()
                },
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Text(
                text = title,
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
    }
}

@Composable
private fun SimpleSubScreen(
    title: String,
    onBackClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        SubScreenHeader(
            title = title,
            onBackClick = onBackClick,
        )
    }
}