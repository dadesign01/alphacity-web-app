package com.alphacity.stamptour.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alphacity.stamptour.R
import com.alphacity.stamptour.network.dto.UserProfile
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.ui.theme.Primary
import com.alphacity.stamptour.viewmodel.MyPageViewModel

@Composable
fun MyPageScreen(
    onLogout: () -> Unit,
    viewModel: MyPageViewModel = hiltViewModel(),
) {
    var showActivityHistory by remember { mutableStateOf(false) }
    var showProfileInfo by remember { mutableStateOf(false) }
    var showEditProfile by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showStoreRegister by remember { mutableStateOf(false) }
    var showMyCoupons by remember { mutableStateOf(false) }
    var showStampExchange by remember { mutableStateOf(false) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }
    var showServiceTerms by remember { mutableStateOf(false) }

    val userProfile by viewModel.userProfile.collectAsState()
    val profileImageUri by viewModel.profileImageUri.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchProfile()
    }

    // 서브화면 뒤로가기 처리 (MainScreen BackHandler가 가로채지 못하게)
    val hasSubScreen = showActivityHistory || showProfileInfo || showEditProfile || showSettings || showStoreRegister || showMyCoupons || showStampExchange || showPrivacyPolicy || showServiceTerms
    BackHandler(enabled = hasSubScreen) {
        when {
            showEditProfile -> showEditProfile = false
            showProfileInfo -> showProfileInfo = false
            showActivityHistory -> showActivityHistory = false
            showSettings -> showSettings = false
            showStoreRegister -> showStoreRegister = false
            showMyCoupons -> showMyCoupons = false
            showStampExchange -> showStampExchange = false
            showPrivacyPolicy -> showPrivacyPolicy = false
            showServiceTerms -> showServiceTerms = false
        }
    }

    if (showPrivacyPolicy) {
        PolicyDetailScreen(
            policyType = PolicyType.PRIVACY,
            onBackClick = { showPrivacyPolicy = false },
        )
        return
    }

    if (showServiceTerms) {
        PolicyDetailScreen(
            policyType = PolicyType.TERMS,
            onBackClick = { showServiceTerms = false },
        )
        return
    }

    if (showActivityHistory) {
        ActivityHistoryScreen(onBackClick = { showActivityHistory = false })
        return
    }

    if (showEditProfile) {
        EditProfileScreen(
            onBackClick = {
                showEditProfile = false
                // 수정 후 프로필 정보 새로고침
                viewModel.fetchProfile()
            },
            onLogout = onLogout,
            initialNickname = userProfile?.nickname ?: "",
            initialEmail = userProfile?.email ?: "",
            initialName = userProfile?.name,
            initialPhone = userProfile?.phone,
            initialAddress = userProfile?.address,
            initialAddressDetail = userProfile?.addressDetail,
            initialProvider = userProfile?.provider,
            initialBirthDate = userProfile?.birthDate,
            initialGender = userProfile?.gender,
            viewModel = viewModel,
        )
        return
    }

    if (showProfileInfo) {
        ProfileInfoScreen(
            onBackClick = { showProfileInfo = false },
            onEditClick = { showEditProfile = true },
            userProfile = userProfile,
        )
        return
    }

    if (showSettings) {
        SettingsScreen(
            onBackClick = { showSettings = false },
            onLogout = onLogout,
            viewModel = viewModel,
        )
        return
    }

    if (showStoreRegister) {
        StoreRegisterScreen(onBackClick = { showStoreRegister = false })
        return
    }

    if (showMyCoupons) {
        MyCouponsScreen(onBackClick = { showMyCoupons = false })
        return
    }

    if (showStampExchange) {
        StampExchangeScreen(onBackClick = { showStampExchange = false })
        return
    }

    val nickname = userProfile?.nickname ?: "게스트"
    val email = userProfile?.email ?: ""
    val stampCount = userProfile?.stampCount ?: 0
    val couponCount = userProfile?.couponCount ?: 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEDF7FF))
            .verticalScroll(rememberScrollState()),
    ) {
        // === Profile Section ===
        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Profile Avatar
            if (profileImageUri != null) {
                AsyncImage(
                    model = profileImageUri,
                    contentDescription = "프로필",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.icon_profile),
                    contentDescription = "프로필",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Name & Email
            Column {
                Text(
                    text = "${nickname}님",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    color = Color(0xFF121212),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = email,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = Color(0xFF6D919E),
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // === Stats Cards ===
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 획득 스탬프 (Gradient Blue)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(90.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF6092FF),
                                Color(0xFF2563EB),
                                Color(0xFF1551D3),
                            )
                        )
                    )
                    .clickable { showStampExchange = true }
                    .padding(16.dp),
            ) {
                Text(
                    text = "획득 스탬프",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.align(Alignment.TopStart),
                )
                Text(
                    text = "$stampCount",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 33.sp,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.BottomEnd),
                )
            }

            // 보유 쿠폰 (White)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(90.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(Color.White)
                    .clickable { showMyCoupons = true }
                    .padding(16.dp),
            ) {
                Text(
                    text = "보유 쿠폰",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color(0xFF121212).copy(alpha = 0.8f),
                    modifier = Modifier.align(Alignment.TopStart),
                )
                Text(
                    text = "$couponCount",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 33.sp,
                    color = Primary,
                    modifier = Modifier.align(Alignment.BottomEnd),
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // === White Card with Menu Items ===
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 33.dp))
                .background(Color.White),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Menu Items (white background)
            MenuItemRow(
                iconRes = R.drawable.icon_activity,
                title = "활동이력",
                onClick = { showActivityHistory = true },
            )
            MenuDivider()

            MenuItemRow(
                iconRes = R.drawable.icon_settings,
                title = "설정",
                onClick = { showSettings = true },
            )
            MenuDivider()

            MenuItemRow(
                iconRes = R.drawable.icon_edit_profile,
                title = "개인정보",
                onClick = { showProfileInfo = true },
            )
            MenuDivider()

            MenuItemRow(
                iconRes = R.drawable.icon_store_register,
                title = "상점 등록",
                onClick = { showStoreRegister = true },
            )
            MenuDivider()

            MenuItemRow(
                iconRes = R.drawable.icon_settings,
                title = "개인정보처리방침",
                onClick = { showPrivacyPolicy = true },
            )
            MenuDivider()

            MenuItemRow(
                iconRes = R.drawable.icon_settings,
                title = "이용약관",
                onClick = { showServiceTerms = true },
            )

            // === Gray Section (로그아웃 ~ 앱 버전) ===
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8F8F8)),
            ) {
                Divider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = Color(0xFFEDEDED),
                    thickness = 1.dp,
                )

                // Logout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.logout()
                            onLogout()
                        }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier.width(40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.icon_logout),
                            contentDescription = "로그아웃",
                            modifier = Modifier.height(21.dp),
                            contentScale = ContentScale.Fit,
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "로그아웃",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Color(0xFF8F8F8F),
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // App Version
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "올리모아 앱 v1.0.0",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Color(0xFF8F8F8F),
                    )
                }
            }

            // === Company Info Footer ===
            CompanyInfoFooter(
                onTermsClick = { showServiceTerms = true },
                onPrivacyClick = { showPrivacyPolicy = true },
            )
        }
    }
}

@Composable
fun ProfileInfoScreen(
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    userProfile: UserProfile?,
) {
    val genderText = when (userProfile?.gender) {
        "male" -> "남성"
        "female" -> "여성"
        "other" -> "기타"
        else -> ""
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
                text = "개인정보",
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
            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                ProfileInfoRow(label = "이름", value = userProfile?.name ?: "", placeholder = "이름을 입력해주세요.")
                ProfileInfoRow(label = "닉네임", value = userProfile?.nickname ?: "", placeholder = "닉네임을 입력해주세요.")
                ProfileInfoRow(label = "이메일", value = userProfile?.email ?: "", placeholder = "이메일을 입력해주세요.")
                ProfileInfoRow(label = "휴대폰", value = userProfile?.phone ?: "", placeholder = "휴대폰 번호를 입력해주세요.")
                ProfileInfoRow(
                    label = "주소",
                    value = buildString {
                        val addr = userProfile?.address ?: ""
                        val detail = userProfile?.addressDetail ?: ""
                        if (addr.isNotBlank()) append(addr)
                        if (detail.isNotBlank()) {
                            if (isNotBlank()) append(" ")
                            append(detail)
                        }
                    },
                    placeholder = "주소를 입력해주세요.",
                )
                ProfileInfoRow(label = "생년월일", value = userProfile?.birthDate ?: "", placeholder = "생년월일을 입력해주세요.")
                ProfileInfoRow(label = "성별", value = genderText, placeholder = "성별을 선택해주세요.")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // === 수정 Button ===
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
                    .clickable(onClick = onEditClick),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "수정",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.White,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String,
    placeholder: String = "-",
) {
    Column {
        Text(
            text = label,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color(0xFF8F8F8F),
        )
        Spacer(modifier = Modifier.height(6.dp))
        // 값이 없으면 입력 안내 멘트를 회색으로 표시
        Text(
            text = value.ifBlank { placeholder },
            fontFamily = Pretendard,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            color = if (value.isBlank()) Color(0xFFBFBFBF) else Color(0xFF121212),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Divider(color = Color(0xFFEDEDED), thickness = 1.dp)
    }
}

@Composable
private fun MenuItemRow(
    iconRes: Int,
    title: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 아이콘을 고정 너비 컨테이너에 가운데 정렬
        Box(
            modifier = Modifier.width(40.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.height(28.dp),
                contentScale = ContentScale.Fit,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color(0xFF121212),
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = ">",
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = Color(0xFF121212),
        )
    }
}

@Composable
private fun MenuDivider() {
    Divider(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = Color(0xFFEDEDED),
        thickness = 1.dp,
    )
}

@Composable
private fun CompanyInfoFooter(
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5)),
    ) {
        // Top separator
        Divider(
            color = Color(0xFFE2E2E2),
            thickness = 1.dp,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            // Company name
            Text(
                text = "(주)디플로",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Color(0xFF666666),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Address
            Text(
                text = "주소 : 대구광역시 수성구 알파시티 1로 42길 11, 1024호 태왕알파시티수성(대흥동)",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = Color(0xFF8F8F8F),
                lineHeight = 16.sp,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "이메일 : contact@di-flo.com",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = Color(0xFF8F8F8F),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "고객문의 : 070-4798-9299",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = Color(0xFF8F8F8F),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "대표자 : 하다인",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = Color(0xFF8F8F8F),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "사업자등록번호 : 892-86-03341",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = Color(0xFF8F8F8F),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Disclaimer
            Text(
                text = "본 서비스는 위치 기반 스탬프 투어 플랫폼으로, 참여 상점 및 기관이 제공하는 이벤트 및 정보에 대한 책임은 해당 제공자에게 있습니다.",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = Color(0xFF8F8F8F),
                lineHeight = 16.sp,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Links row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "이용약관",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.clickable(onClick = onTermsClick),
                )
                Text(
                    text = " | ",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    color = Color(0xFFA0A0A0),
                )
                Text(
                    text = "개인정보처리방침",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.clickable(onClick = onPrivacyClick),
                )
                Text(
                    text = " | ",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    color = Color(0xFFA0A0A0),
                )
                Text(
                    text = "문의하기",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:contact@di-flo.com")
                        }
                        context.startActivity(intent)
                    },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Copyright
            Text(
                text = "\u00A9 (주)디플로",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                color = Color(0xFFAFBFCC),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
