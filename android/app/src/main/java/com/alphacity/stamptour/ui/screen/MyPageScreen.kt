package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alphacity.stamptour.R
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.ui.theme.Primary
import com.alphacity.stamptour.viewmodel.MyPageViewModel

@Composable
fun MyPageScreen(
    onLogout: () -> Unit,
    viewModel: MyPageViewModel = hiltViewModel(),
) {
    var showActivityHistory by remember { mutableStateOf(false) }
    var showEditProfile by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showStoreRegister by remember { mutableStateOf(false) }
    var showMyCoupons by remember { mutableStateOf(false) }
    var showStampExchange by remember { mutableStateOf(false) }

    if (showActivityHistory) {
        ActivityHistoryScreen(onBackClick = { showActivityHistory = false })
        return
    }

    if (showEditProfile) {
        EditProfileScreen(
            onBackClick = { showEditProfile = false },
            onLogout = onLogout,
        )
        return
    }

    if (showSettings) {
        SettingsScreen(onBackClick = { showSettings = false })
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

    val userProfile by viewModel.userProfile.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchProfile()
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
            // Profile Avatar - 이미지가 원형에 꽉 차게
            Image(
                painter = painterResource(id = R.drawable.icon_profile),
                contentDescription = "프로필",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )

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
                .weight(1f)
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
                title = "개인정보 수정",
                onClick = { showEditProfile = true },
            )
            MenuDivider()

            MenuItemRow(
                iconRes = R.drawable.icon_store_register,
                title = "상점 등록",
                onClick = { showStoreRegister = true },
            )

            // === Gray Section (로그아웃 ~ 앱 버전) ===
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
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

                Spacer(modifier = Modifier.weight(1f))

                // App Version
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "디지털 페스티벌 앱 v1.0.0",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Color(0xFF8F8F8F),
                    )
                }
            }
        }
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
