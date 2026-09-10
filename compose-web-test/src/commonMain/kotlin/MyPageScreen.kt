package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alphacity.stamptour.network.ApiService
import com.alphacity.stamptour.network.dto.UserProfile
import com.alphacity.stamptour.web.WebTokenManager
import composewebtest.generated.resources.Res
import composewebtest.generated.resources.icon_activity_log
import composewebtest.generated.resources.icon_edit_profile
import composewebtest.generated.resources.icon_profile
import composewebtest.generated.resources.icon_store_register
import composewebtest.generated.resources.logout
import composewebtest.generated.resources.mypage_background
import composewebtest.theme.MainGradient
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

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
        errorMessage = null

        if (!WebTokenManager.isLoggedIn()) {
            userProfile = null
            stampCount = 0
            couponCount = 0
            errorMessage = "로그인이 필요합니다."
            return@LaunchedEffect
        }

        try {
            /*
             * 실제 사용자 정보 조회
             */
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

    /*
     * 전체 페이지
     *
     * mypage_background.png가 전체 영역을 감싸는 구조
     *
     * 상단:
     * - 프로필
     * - 이름
     * - 이메일
     * - 스탬프 / 쿠폰
     *
     * 하단:
     * - 메뉴
     * - 로그아웃
     *
     * 메뉴와 로그아웃은 좌우 패딩 없이 화면 벽에 붙음
     */
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        /*
         * 전체 배경 이미지
         */
        Image(
            painter = painterResource(
                Res.drawable.mypage_background
            ),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        /*
         * 실제 페이지 콘텐츠
         */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                ),
        ) {
            /*
             * 상단 배경 영역
             *
             * 이 영역에만 좌우 10dp 패딩 적용
             */
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                /*
                 * 프로필 이미지
                 *
                 * 97 x 97
                 * 상단 30dp
                 */
                Spacer(
                    modifier = Modifier.height(30.dp)
                )

                Image(
                    painter = painterResource(
                        Res.drawable.icon_profile
                    ),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(97.dp),
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                /*
                 * 이름
                 *
                 * DB 이름 + 님
                 */
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = userProfile?.nickname ?: "사용자",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                    )

                    Text(
                        text = "님",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(
                            start = 3.dp,
                            bottom = 3.dp,
                        ),
                    )
                }

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                /*
                 * 이메일
                 */
                Text(
                    text = userProfile?.email ?: "",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = Color(0xFF6D919E),
                    textDecoration = TextDecoration.Underline,
                    textAlign = TextAlign.Center,
                )

                /*
                 * 사용자 정보 조회 실패
                 */
                if (errorMessage != null && userProfile == null) {
                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = errorMessage ?: "",
                        fontFamily = Pretendard,
                        fontSize = 13.sp,
                        color = Color(0xFFE85151),
                        textAlign = TextAlign.Center,
                    )
                }

                /*
                 * 스탬프 / 쿠폰 통계
                 *
                 * 350 x 105
                 * 비율 유지
                 * 위 30dp / 아래 20dp
                 */
                Spacer(
                    modifier = Modifier.height(30.dp)
                )

                MyPageStatCard(
                    stampCount = stampCount,
                    couponCount = couponCount,
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )
            }

            /*
             * 메뉴 영역
             *
             * 좌우 패딩 없음
             * 화면 벽에 딱 붙음
             *
             * 상단 좌/우 25dp Radius
             * 메뉴 영역에만 Shadow
             */
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 17.dp,
                        shape = RoundedCornerShape(
                            topStart = 25.dp,
                            topEnd = 25.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 0.dp,
                        ),
                        ambientColor = Color(0x5CAEC2DD),
                        spotColor = Color(0x5CAEC2DD),
                    )
                    .clip(
                        RoundedCornerShape(
                            topStart = 25.dp,
                            topEnd = 25.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 0.dp,
                        )
                    )
                    .background(Color.White),
            ) {
                /*
                 * 활동이력
                 */
                MyPageMenuItem(
                    icon = Res.drawable.icon_activity_log,
                    title = "활동이력",
                    onClick = {
                        showActivityHistory = true
                    },
                )

                HorizontalDivider(
                    color = Color(0xFFE5E5E5),
                    thickness = 1.dp,
                    modifier = Modifier.padding(
                        horizontal = 20.dp,
                    ),
                )

                /*
                 * 상점등록
                 */
                MyPageMenuItem(
                    icon = Res.drawable.icon_store_register,
                    title = "상점등록",
                    onClick = {
                        showStoreRegister = true
                    },
                )

                HorizontalDivider(
                    color = Color(0xFFE5E5E5),
                    thickness = 1.dp,
                    modifier = Modifier.padding(
                        horizontal = 20.dp,
                    ),
                )

                /*
                 * 개인정보처리방침
                 */
                MyPageMenuItem(
                    icon = Res.drawable.icon_edit_profile,
                    title = "개인정보처리방침",
                    onClick = {
                        showPrivacyPolicy = true
                    },
                )
            }

            /*
             * 로그아웃 영역
             *
             * 여기부터 Gray 배경 시작
             * 위쪽 구분선 없음
             * 좌우 패딩 없음
             */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF3F4F6))
                    .padding(
                        horizontal = 40.dp,
                        vertical = 24.dp,
                    )
                    .clickable {
                        WebTokenManager.clear()
                        onLogout()
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(
                        Res.drawable.logout
                    ),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(20.dp),
                )

                Spacer(
                    modifier = Modifier.width(35.dp)
                )

                Text(
                    text = "로그아웃",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color(0xFF8F8F8F),
                )
            }
        }
    }
}

@Composable
private fun MyPageStatCard(
    stampCount: Int,
    couponCount: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(
                max = 350.dp,
            )
            .aspectRatio(
                350f / 105f
            )
            .clip(
                RoundedCornerShape(30.dp)
            )
            .background(MainGradient)
            .padding(
                horizontal = 20.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        /*
         * 획득 스탬프
         */
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "${stampCount}",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            Text(
                text = "획득 스탬프",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
        }

        /*
         * 가운데 구분선
         */
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(44.dp)
                .background(
                    Color(0x70EDF7FF)
                ),
        )

        /*
         * 보유 쿠폰
         */
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "${couponCount}",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            Text(
                text = "보유 쿠폰",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun MyPageMenuItem(
    icon: DrawableResource,
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
                horizontal = 30.dp,
                vertical = 18.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(40.dp),
        )

        Spacer(
            modifier = Modifier.width(20.dp)
        )

        Text(
            text = title,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            color = Color(0xFF222222),
        )
    }
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