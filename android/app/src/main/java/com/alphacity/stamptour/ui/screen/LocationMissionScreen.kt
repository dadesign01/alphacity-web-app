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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
fun LocationMissionScreen(
    mission: MissionItem,
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
    val showAlert by viewModel.showAlert.collectAsState()
    val alertMessage by viewModel.alertMessage.collectAsState()
    val earnedStamp by viewModel.earnedStamp.collectAsState()
    val currentDistance by viewModel.currentDistance.collectAsState()

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

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            val lat = mission.place?.latitude ?: return@rememberLauncherForActivityResult
            val lng = mission.place?.longitude ?: return@rememberLauncherForActivityResult
            viewModel.completeLocationMission(context, mission.id, lat, lng)
        }
    }

    if (showAlert) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissAlert() },
            title = { Text("알림", fontFamily = Pretendard, fontWeight = FontWeight.SemiBold) },
            text = { Text(alertMessage, fontFamily = Pretendard) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissAlert() }) {
                    Text("확인", fontFamily = Pretendard, color = Primary)
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
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onDismiss() },
                tint = Color(0xFF121212),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "위치 인증 미션",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF121212),
            )
        }
        Divider(color = Color(0xFFE2E2E2))

        if (isCompleted) {
            // 완료 화면
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(64.dp), tint = Color(0xFF16A34A))
                Spacer(modifier = Modifier.height(20.dp))
                Text("위치 인증 완료!", fontFamily = Pretendard, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF121212))
                Spacer(modifier = Modifier.height(8.dp))
                Text(mission.name, fontFamily = Pretendard, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color(0xFF595959))
                mission.place?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(it.name, fontFamily = Pretendard, fontSize = 14.sp, color = Color(0xFF828282))
                }
                currentDistance?.let { dist ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "현재 거리: ${dist}m",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color(0xFF16A34A),
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Primary)
                        .clickable { onCompleted(); onDismiss() }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("확인", fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White)
                }
            }
        } else {
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
                            .border(2.dp, Color(0xFFDBEAFE), CircleShape),
                    )
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFF6FF)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Primary,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 장소 정보 카드
                mission.place?.let { place ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                            .padding(20.dp),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Text(
                            text = place.name,
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = Color(0xFF121212),
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "현재 위치를 인증하면 스탬프를 획득할 수 있습니다",
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
                                tint = Primary,
                            )
                            Text(
                                text = "GPS 인증 사용",
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = Primary,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // 위치 인증하기 버튼
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
                            .background(Primary)
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
                                Text("위치 인증하기", fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // GPS 안내
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
        }
    }
}
