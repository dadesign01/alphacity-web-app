package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.alphacity.stamptour.BuildConfig
import com.alphacity.stamptour.R
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.ui.theme.Primary
import com.alphacity.stamptour.viewmodel.EventDetailViewModel
import kotlinx.coroutines.delay

@Composable
fun FirstComeEventDetailScreen(
    eventId: Int,
    onBackClick: () -> Unit = {},
    viewModel: EventDetailViewModel = hiltViewModel(),
) {
    val event by viewModel.event.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isParticipating by viewModel.isParticipating.collectAsState()
    val myRaffleNumber by viewModel.myRaffleNumber.collectAsState()
    val message by viewModel.message.collectAsState()

    LaunchedEffect(eventId) {
        viewModel.fetchEventDetail(eventId)
    }

    LaunchedEffect(message) {
        if (message != null) {
            delay(2000)
            viewModel.clearMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icon_back_arrow),
                    contentDescription = "뒤로가기",
                    modifier = Modifier
                        .size(width = 13.dp, height = 26.dp)
                        .clickable { onBackClick() },
                    colorFilter = ColorFilter.tint(Color(0xFF121212)),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "선착순 사은품",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color(0xFF121212),
                )
            }

            Divider(color = Color(0xFFE2E2E2), thickness = 1.dp)

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (event != null) {
                val ev = event!!
                val hasParticipated = ev.isParticipated == true || myRaffleNumber != null
                val remaining = maxOf(ev.participantLimit - ev.participantCount, 0)
                val isSoldOut = remaining <= 0
                val isOpen = ev.status != "ended"

                if (hasParticipated) {
                    // 참여 완료 화면
                    ParticipationCompleteContent(
                        event = ev,
                        raffleNumber = myRaffleNumber ?: ev.myRaffleNumber,
                        onBackClick = onBackClick,
                    )
                } else {
                    // 상세 정보 화면
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        // 이벤트 이미지
                        EventDetailImageBox(event = ev)

                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            Spacer(modifier = Modifier.height(16.dp))

                            // 유형 태그 + 상태
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color(0xFFEDF7FF))
                                        .padding(horizontal = 10.dp, vertical = 3.dp),
                                ) {
                                    Text(
                                        text = "선착순",
                                        fontFamily = Pretendard,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = Primary,
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (isOpen && !isSoldOut) Primary else Color(0xFF8F8F8F))
                                        .padding(horizontal = 10.dp, vertical = 3.dp),
                                ) {
                                    Text(
                                        text = if (isOpen && !isSoldOut) "진행중" else "마감",
                                        fontFamily = Pretendard,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = Color.White,
                                    )
                                }
                            }

                            // 제목
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = ev.name,
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp,
                                color = Color(0xFF121212),
                            )

                            // 설명
                            if (!ev.description.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = ev.description,
                                    fontFamily = Pretendard,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp,
                                    color = Color(0xFF595959),
                                )
                            }

                            // 잔여 수량
                            Spacer(modifier = Modifier.height(16.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFAFAFA))
                                    .padding(16.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = "잔여 수량",
                                        fontFamily = Pretendard,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 12.sp,
                                        color = Color(0xFF595959),
                                    )
                                    Text(
                                        text = "${remaining}/${ev.participantLimit}명",
                                        fontFamily = Pretendard,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isSoldOut) Color(0xFF8F8F8F) else Primary,
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                val progress = if (ev.participantLimit > 0)
                                    ev.participantCount.toFloat() / ev.participantLimit.toFloat()
                                else 0f

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF8F8F8)),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                                            .clip(CircleShape)
                                            .background(if (isSoldOut) Color(0xFF8E8E8E) else Primary),
                                    )
                                }
                            }

                            // 수령 방법
                            Spacer(modifier = Modifier.height(16.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF8F8F8))
                                    .padding(16.dp),
                            ) {
                                Text(
                                    text = "수령 방법",
                                    fontFamily = Pretendard,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF121212),
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                ReceiptStep(1, "이벤트 부스 스텝이 참여하기 버튼 클릭")
                                Spacer(modifier = Modifier.height(8.dp))
                                ReceiptStep(2, "참여완료 화면 확인")
                                Spacer(modifier = Modifier.height(8.dp))
                                ReceiptStep(3, "사은품 수령")
                                Spacer(modifier = Modifier.height(8.dp))
                                ReceiptStep(4, "재발급 불가")
                            }

                            // 참여 버튼
                            Spacer(modifier = Modifier.height(24.dp))
                            if (isOpen && !isSoldOut) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Primary)
                                        .clickable(enabled = !isParticipating) {
                                            viewModel.participate(ev.id)
                                        }
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (isParticipating) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp,
                                        )
                                    } else {
                                        Text(
                                            text = "참여하기",
                                            fontFamily = Pretendard,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 16.sp,
                                            color = Color.White,
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFCCCCCC))
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "마감됨",
                                        fontFamily = Pretendard,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        color = Color.White,
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                }
            }
        }

        // Toast
        if (message != null && event?.let { it.isParticipated != true && myRaffleNumber == null } != false) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(horizontal = 24.dp, vertical = 12.dp),
            ) {
                Text(
                    text = message!!,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun ParticipationCompleteContent(
    event: com.alphacity.stamptour.network.dto.EventItem,
    raffleNumber: Int?,
    onBackClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // 체크 아이콘 (텍스트로 대체)
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Primary),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "✓",
                fontSize = 32.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "참여 완료!",
            fontFamily = Pretendard,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = Color(0xFF121212),
        )

        if (raffleNumber != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF0F7FF))
                    .padding(horizontal = 24.dp, vertical = 12.dp),
            ) {
                Text(
                    text = "참여 번호: #$raffleNumber",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Primary,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = event.name,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = Color(0xFF595959),
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 재발급 불가 안내
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFF5F5))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "⚠ 재발급 불가",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color(0xFFE74C3C),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "사은품 수령 후 재발급이 불가합니다.\n부스에서 이 화면을 보여주세요.",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = Color(0xFF8F8F8F),
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 돌아가기 버튼
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Primary)
                .clickable { onBackClick() }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "돌아가기",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.White,
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun EventDetailImageBox(event: com.alphacity.stamptour.network.dto.EventItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
    ) {
        val imageUrl = event.imageUrl
        if (!imageUrl.isNullOrBlank()) {
            val fullUrl = if (imageUrl.startsWith("http")) imageUrl else BuildConfig.SERVER_URL + imageUrl
            AsyncImage(
                model = fullUrl,
                contentDescription = event.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE8E8E8)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = event.name.take(1),
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Bold,
                    fontSize = 40.sp,
                    color = Color(0xFFB5B5B5),
                )
            }
        }
    }
}

@Composable
private fun ReceiptStep(number: Int, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Primary),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "$number",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.White,
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            color = Color(0xFF595959),
        )
    }
}
