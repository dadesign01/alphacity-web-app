package com.alphacity.stamptour.ui.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alphacity.stamptour.network.dto.MissionItem
import com.alphacity.stamptour.ui.component.StampEarnedDialog
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.ui.theme.Primary
import com.alphacity.stamptour.viewmodel.MissionViewModel

@Composable
fun StayTimeMissionScreen(
    mission: MissionItem,
    programLat: Double? = null,
    programLng: Double? = null,
    onDismiss: () -> Unit = {},
    onCompleted: () -> Unit = {},
    viewModel: MissionViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    // 이전 미션 상태 초기화 (같은 ViewModel 인스턴스가 재사용되므로 필수)
    LaunchedEffect(mission.id) {
        viewModel.reset()
    }

    val isLoading by viewModel.isLoading.collectAsState()
    val isCompleted by viewModel.isCompleted.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val showAlert by viewModel.showAlert.collectAsState()
    val alertMessage by viewModel.alertMessage.collectAsState()
    val earnedStamp by viewModel.earnedStamp.collectAsState()
    val locationVerified by viewModel.locationVerified.collectAsState()
    val currentDistance by viewModel.currentDistance.collectAsState()

    val targetLat = mission.place?.latitude ?: programLat
    val targetLng = mission.place?.longitude ?: programLng
    val hasLocation = targetLat != null && targetLng != null
    val needsLocationVerification = hasLocation && !locationVerified

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted && targetLat != null && targetLng != null) {
            viewModel.verifyLocation(context, targetLat, targetLng)
        }
    }

    if (isCompleted && earnedStamp != null) {
        StampEarnedDialog(
            stamp = earnedStamp,
            onDismiss = {
                onCompleted()
                onDismiss()
            },
        )
        return
    }

    if (showAlert) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissAlert() },
            title = { Text("알림", fontFamily = Pretendard, fontWeight = FontWeight.SemiBold) },
            text = { Text(alertMessage, fontFamily = Pretendard) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissAlert() }) {
                    Text("확인", fontFamily = Pretendard, color = Color(0xFFD97706))
                }
            },
        )
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "닫기",
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        viewModel.stopTimer()
                        onDismiss()
                    },
                tint = Color(0xFF121212),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text("체류시간 미션", fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Color(0xFF121212))
        }
        Divider(color = Color(0xFFE2E2E2))

        if (isCompleted) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(64.dp), tint = Color(0xFFD97706))
                Spacer(modifier = Modifier.height(20.dp))
                Text("체류시간 미션 완료!", fontFamily = Pretendard, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF121212))
                Spacer(modifier = Modifier.height(8.dp))
                Text(mission.name, fontFamily = Pretendard, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color(0xFF595959))
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFD97706))
                        .clickable { onCompleted(); onDismiss() }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("확인", fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White)
                }
            }
        } else if (needsLocationVerification) {
            // 위치 확인 단계
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.weight(1f))

                // 위치 아이콘
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color(0xFFFDE68A), CircleShape),
                    )
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFFBEB)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFFD97706),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 안내 카드
                val placeName = mission.place?.name ?: "지정 장소"
                run {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFFFBEB))
                            .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(14.dp))
                            .padding(20.dp),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Text(
                            text = "위치 확인 필요",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = Color(0xFF121212),
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "${placeName}에서 위치를 먼저 확인해야\n체류시간 미션에 참여할 수 있습니다.",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = Color(0xFF595959),
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFFD97706),
                            )
                            Text(
                                text = "100m 이내에서 인증 가능",
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = Color(0xFFD97706),
                            )
                        }
                    }
                }

                // 현재 거리 표시
                currentDistance?.let { dist ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "현재 거리: ${dist}m",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = if (dist <= 100) Color(0xFF16A34A) else Color(0xFFEA580C),
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // 위치 확인 버튼
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFD97706))
                            .clickable(enabled = !isLoading) {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                    )
                                )
                            }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(Icons.Default.MyLocation, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Text("위치 확인", fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "위치 서비스가 켜져있는지 확인해주세요",
                        fontFamily = Pretendard,
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.weight(1f))

                // 위치 확인 완료 배지 (위치가 있는 미션인 경우)
                if (hasLocation) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF0FFF4))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
                        Text(
                            text = "위치 확인 완료 (${currentDistance ?: 0}m)",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = Color(0xFF16A34A),
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 배지
                Text(
                    text = "체류시간",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = Color.White,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFFD97706))
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(mission.name, fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = Color(0xFF121212))

                // 장소명
                if (hasLocation) {
                    val locationName = mission.place?.name ?: "지정 장소"
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.LocationOn, null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                        Text(locationName, fontFamily = Pretendard, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color(0xFF595959))
                    }
                }

                // 타이머
                Spacer(modifier = Modifier.height(30.dp))
                Text(
                    text = viewModel.formattedTime,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 56.sp,
                    color = if (isTimerRunning) Color(0xFFD97706) else Color(0xFF121212),
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "필요 체류시간: ${mission.stayMinutes ?: 0}분",
                    fontFamily = Pretendard,
                    fontSize = 13.sp,
                    color = Color(0xFF828282),
                )

                if (isTimerRunning) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "타이머가 작동 중입니다.\n이 화면을 유지해주세요.",
                        fontFamily = Pretendard,
                        fontSize = 12.sp,
                        color = Color(0xFFD97706),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFFFBEB))
                            .padding(12.dp),
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // 버튼
                if (isTimerRunning) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFDC2626))
                            .clickable { viewModel.stopTimer() }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("타이머 중지", fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFD97706))
                            .clickable(enabled = !isLoading) {
                                viewModel.startStayTimeMission(mission.id, mission.stayMinutes ?: 1)
                            }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Outlined.Timer, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Text("체류 시작", fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
