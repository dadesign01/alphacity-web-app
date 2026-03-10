package com.alphacity.stamptour.ui.screen

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alphacity.stamptour.network.dto.MissionItem
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.ui.theme.Primary
import com.alphacity.stamptour.viewmodel.MissionViewModel

@Composable
fun QuizMissionScreen(
    mission: MissionItem,
    onDismiss: () -> Unit = {},
    onCompleted: () -> Unit = {},
    viewModel: MissionViewModel = hiltViewModel(),
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val isCompleted by viewModel.isCompleted.collectAsState()
    val showAlert by viewModel.showAlert.collectAsState()
    val alertMessage by viewModel.alertMessage.collectAsState()
    var selectedOption by remember { mutableStateOf<String?>(null) }

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
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
            ) {
                Spacer(modifier = Modifier.height(24.dp))

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

                // 제출 버튼
                Spacer(modifier = Modifier.height(28.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedOption != null) Primary else Color(0xFFD1D5DB))
                        .clickable(enabled = selectedOption != null && !isLoading) {
                            selectedOption?.let { answer ->
                                viewModel.completeQuizMission(mission.id, answer)
                            }
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
