package com.alphacity.stamptour.ui.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
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
fun QuizMissionScreen(
    mission: MissionItem,
    programLat: Double? = null,
    programLng: Double? = null,
    onDismiss: () -> Unit = {},
    onCompleted: () -> Unit = {},
    viewModel: MissionViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    // 최초 진입 시 1회만 상태 초기화
    LaunchedEffect(Unit) {
        viewModel.reset()
    }

    val isLoading by viewModel.isLoading.collectAsState()
    val isCompleted by viewModel.isCompleted.collectAsState()
    val showAlert by viewModel.showAlert.collectAsState()
    val alertMessage by viewModel.alertMessage.collectAsState()
    val earnedStamp by viewModel.earnedStamp.collectAsState()
    val locationVerified by viewModel.locationVerified.collectAsState()
    val currentDistance by viewModel.currentDistance.collectAsState()
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var textAnswer by remember { mutableStateOf(TextFieldValue("")) }
    val isShortAnswer = mission.options.isNullOrEmpty()

    // 미션 장소 좌표 우선, 없으면 프로그램 좌표 사용
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

    // 스탬프 적립 팝업
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
                    Text("확인", fontFamily = Pretendard, color = Primary)
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
                    .clickable { onDismiss() },
                tint = Color(0xFF121212),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "퀴즈 미션",
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
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFF16A34A),
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "퀴즈 미션 완료!",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color(0xFF121212),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = mission.name,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color(0xFF595959),
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Primary)
                        .clickable {
                            onCompleted()
                            onDismiss()
                        }
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

                // 안내 카드
                val placeName = mission.place?.name ?: "지정 장소"
                run {
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
                            text = "위치 확인 필요",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = Color(0xFF121212),
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "${placeName}에서 위치를 먼저 확인해야\n퀴즈 미션에 참여할 수 있습니다.",
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
                                text = "100m 이내에서 인증 가능",
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = Primary,
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
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
            ) {
                Spacer(modifier = Modifier.height(24.dp))

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

                // 미션 유형 배지
                Text(
                    text = "퀴즈",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = Color.White,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Primary)
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 미션명
                Text(
                    text = mission.name,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = Color(0xFF121212),
                )

                // 질문 카드
                if (!mission.question.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF0F7FF))
                            .padding(20.dp),
                    ) {
                        Text(
                            text = mission.question,
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            lineHeight = 26.sp,
                            color = Color(0xFF121212),
                        )
                    }
                }

                // 객관식 보기
                if (!mission.options.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        mission.options.forEach { option ->
                            OptionRow(
                                option = option,
                                isSelected = selectedOption == option,
                                onClick = { selectedOption = option },
                            )
                        }
                    }
                }

                // 서술형 입력
                if (isShortAnswer) {
                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedTextField(
                        value = textAnswer,
                        onValueChange = { textAnswer = it },
                        placeholder = {
                            Text("정답을 입력하세요", fontFamily = Pretendard, fontSize = 15.sp, color = Color(0xFF9CA3AF))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = Color(0xFF121212),
                        ),
                        singleLine = true,
                    )
                }

                // 제출 버튼
                val canSubmit = if (isShortAnswer) textAnswer.text.isNotBlank() else selectedOption != null
                Spacer(modifier = Modifier.height(28.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (canSubmit) Primary else Color(0xFFD1D5DB))
                        .clickable(enabled = canSubmit && !isLoading) {
                            val answer = if (isShortAnswer) textAnswer.text.trim() else selectedOption ?: return@clickable
                            viewModel.completeQuizMission(mission.id, answer)
                        }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("제출하기", fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun OptionRow(
    option: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFFEFF6FF) else Color.White)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) Primary else Color(0xFFE5E7EB),
                shape = RoundedCornerShape(12.dp),
            )
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // 라디오 버튼
        Box(
            modifier = Modifier
                .size(22.dp)
                .border(
                    width = 2.dp,
                    color = if (isSelected) Primary else Color(0xFFD1D5DB),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Primary),
                )
            }
        }

        Text(
            text = option,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            color = Color(0xFF121212),
        )
    }
}
