package com.alphacity.stamptour.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alphacity.stamptour.R
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.ui.theme.Primary
import com.alphacity.stamptour.viewmodel.MyPageViewModel
import kotlinx.coroutines.flow.MutableStateFlow

enum class PolicyType(val title: String) {
    TERMS("서비스 이용약관"),
    PRIVACY("개인정보 처리방침"),
    LOCATION("위치 정보 이용약관"),
}

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit = {},
    viewModel: MyPageViewModel? = null,
) {
    var showPolicy by remember { mutableStateOf<PolicyType?>(null) }

    showPolicy?.let { policy ->
        PolicyDetailScreen(
            policyType = policy,
            onBackClick = { showPolicy = null },
        )
        return
    }

    var notifyNewEvent by remember { mutableStateOf(true) }
    var notifyEventUpdate by remember { mutableStateOf(true) }
    var notifyMission by remember { mutableStateOf(true) }
    var notifyMarketing by remember { mutableStateOf(false) }
    var locationConsent by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val userProfile by (viewModel?.userProfile ?: MutableStateFlow(null)).collectAsState()
    val deleteSuccess by (viewModel?.deleteSuccess ?: MutableStateFlow(false)).collectAsState()
    val deleteError by (viewModel?.deleteError ?: MutableStateFlow(null)).collectAsState()

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
                text = "설정",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF121212),
            )
        }
        Divider(color = Color(0xFFE2E2E2), thickness = 1.dp)

        // === Content ===
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // Notice banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(Color(0xFFF8F8F8))
                    .padding(horizontal = 15.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icon_bell),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    contentScale = ContentScale.Fit,
                )
                Spacer(modifier = Modifier.width(9.dp))
                Text(
                    text = "알림 허용 시 행사 정보를 놓치지 않을 수 있습니다.",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color(0xFF121212),
                )
            }

            // === Notification Toggles ===
            SettingsToggleItem(
                title = "새로운 행사 알림",
                subtitle = "수성알파시티 내 새로운 행사 진행 시 알림 받기",
                isOn = notifyNewEvent,
                onToggle = { notifyNewEvent = it },
            )
            SettingsDivider()
            SettingsToggleItem(
                title = "이벤트 업데이트 알림",
                subtitle = "참여 중인 이벤트 변경 시 알림 받기",
                isOn = notifyEventUpdate,
                onToggle = { notifyEventUpdate = it },
            )
            SettingsDivider()
            SettingsToggleItem(
                title = "미션 알림",
                subtitle = "새로운 미션 등록 시 알림 받기",
                isOn = notifyMission,
                onToggle = { notifyMission = it },
            )
            SettingsDivider()
            SettingsToggleItem(
                title = "마케팅 알림",
                subtitle = "프로모션 및 혜택 정보 알림 받기",
                isOn = notifyMarketing,
                onToggle = { notifyMarketing = it },
            )

            // === 권한 설정 Section ===
            SettingsDivider()
            SectionHeader(title = "권한 설정")
            SettingsDivider()
            SettingsToggleItem(
                title = "위치 정보 이용 동의",
                subtitle = "위치 기반 미션 참여에 사용됩니다.",
                isOn = locationConsent,
                onToggle = { locationConsent = it },
            )

            // === 약관 및 정책 Section ===
            SettingsDivider()
            SectionHeader(title = "약관 및 정책")
            SettingsDivider()
            SettingsNavItem(title = "서비스 이용약관") { showPolicy = PolicyType.TERMS }
            SettingsDivider()
            SettingsNavItem(title = "개인정보 처리방침") { showPolicy = PolicyType.PRIVACY }
            SettingsDivider()
            SettingsNavItem(title = "위치 정보 이용약관") { showPolicy = PolicyType.LOCATION }

            // === 계정 Section ===
            if (userProfile != null) {
                SettingsDivider()
                SectionHeader(title = "계정")
                SettingsDivider()
                SettingsNavItem(title = "회원탈퇴") { showDeleteDialog = true }
            }

            // === 앱 정보 Section ===
            SettingsDivider()
            SectionHeader(title = "앱 정보")

            // 앱 버전
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "앱 버전",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = Color(0xFF121212),
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "v1.0.0",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Primary,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "현재 사용 중인 버전입니다. ",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Color(0xFF595959),
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color(0xFF121212))
                            .clickable { /* TODO: update */ }
                            .padding(horizontal = 9.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = "업데이트 하기",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Normal,
                            fontSize = 10.sp,
                            color = Color.White,
                        )
                    }
                }
            }

            SettingsDivider()

            // Security notice
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(Color(0xFFF8F8F8))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "디지털 페스티벌 앱은 안전한 사용자 경험을 위해\n최신 보안 기술을 적용하고 있습니다.",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                    lineHeight = 16.sp,
                    color = Color(0xFF8F8F8F),
                )
            }

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
private fun SettingsToggleItem(
    title: String,
    subtitle: String,
    isOn: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color(0xFF121212),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = Color(0xFF595959),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        CustomToggle(isOn = isOn, onToggle = { onToggle(!isOn) })
    }
}

@Composable
private fun CustomToggle(
    isOn: Boolean,
    onToggle: () -> Unit,
) {
    val trackColor = if (isOn) {
        Brush.linearGradient(
            colors = listOf(Color(0xFF6092FF), Color(0xFF2563EB), Color(0xFF1551D3))
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0xFFEDEDED), Color(0xFFEDEDED))
        )
    }

    Box(
        modifier = Modifier
            .width(42.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(600.dp))
            .background(trackColor)
            .clickable(onClick = onToggle),
    ) {
        Box(
            modifier = Modifier
                .padding(2.dp)
                .size(22.dp)
                .align(if (isOn) Alignment.CenterEnd else Alignment.CenterStart)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}

@Composable
private fun SettingsNavItem(
    title: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = Color(0xFF121212),
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = ">",
            fontFamily = Pretendard,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            color = Color(0xFF121212),
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F8F8))
            .padding(horizontal = 20.dp, vertical = 9.dp),
    ) {
        Text(
            text = title,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            lineHeight = 16.sp,
            color = Color(0xFF8F8F8F),
        )
    }
}

@Composable
private fun SettingsDivider() {
    Divider(
        color = Color(0xFFB5B5B5),
        thickness = 0.5.dp,
    )
}
