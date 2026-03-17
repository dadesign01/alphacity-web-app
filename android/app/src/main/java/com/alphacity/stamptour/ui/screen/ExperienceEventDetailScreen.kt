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
import com.alphacity.stamptour.viewmodel.EventDetailViewModel
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

private val ExperienceOrange = Color(0xFFE67E22)

@Composable
fun ExperienceEventDetailScreen(
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

    Box(modifier = Modifier.fillMaxSize().background(Color.White).statusBarsPadding().navigationBarsPadding()) {
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
                    text = "체험 이벤트",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color(0xFF121212),
                )
            }

            Divider(color = Color(0xFFE2E2E2), thickness = 1.dp)

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ExperienceOrange)
                }
            } else if (event != null) {
                val ev = event!!
                val hasParticipated = ev.isParticipated == true || myRaffleNumber != null
                val isOpen = ev.status != "ended"
                val isFull = ev.capacity != null && ev.participantCount >= ev.capacity!!

                if (hasParticipated) {
                    ExperienceCompleteContent(
                        event = ev,
                        onBackClick = onBackClick,
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        // 이벤트 이미지
                        ExperienceImageBox(event = ev)

                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            Spacer(modifier = Modifier.height(16.dp))

                            // 유형 태그 + 상태
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFF3E0))
                                        .padding(horizontal = 10.dp, vertical = 3.dp),
                                ) {
                                    Text(
                                        text = "체험",
                                        fontFamily = Pretendard,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = ExperienceOrange,
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (isOpen && !isFull) ExperienceOrange else Color(0xFF8F8F8F))
                                        .padding(horizontal = 10.dp, vertical = 3.dp),
                                ) {
                                    Text(
                                        text = if (isOpen && !isFull) "신청 가능" else "마감",
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

                            // 체험 정보 카드
                            Spacer(modifier = Modifier.height(16.dp))
                            ExperienceInfoCard(event = ev)

                            // 체험 기간
                            Spacer(modifier = Modifier.height(16.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF8F8F8))
                                    .padding(16.dp),
                            ) {
                                Text(
                                    text = "체험 기간",
                                    fontFamily = Pretendard,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF121212),
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${formatExperienceDate(ev.startDate)} ~ ${formatExperienceDate(ev.endDate)}",
                                    fontFamily = Pretendard,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = Color(0xFF595959),
                                )
                            }

                            // 참여 안내
                            Spacer(modifier = Modifier.height(16.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF8F8F8))
                                    .padding(16.dp),
                            ) {
                                Text(
                                    text = "참여 안내",
                                    fontFamily = Pretendard,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF121212),
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                ExperienceGuideStep(1, "아래 참여하기 버튼을 눌러 신청")
                                Spacer(modifier = Modifier.height(8.dp))
                                ExperienceGuideStep(2, "체험 장소에서 참여 완료 화면 제시")
                                Spacer(modifier = Modifier.height(8.dp))
                                ExperienceGuideStep(3, "현장에서 체험 진행")
                            }

                            // 참여 버튼
                            Spacer(modifier = Modifier.height(24.dp))
                            if (isOpen && !isFull) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ExperienceOrange)
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
private fun ExperienceInfoCard(event: com.alphacity.stamptour.network.dto.EventItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFF8F0))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (event.duration != null) {
            ExperienceInfoRow(icon = "⏱", label = "체험 시간", value = "${event.duration}분")
        }
        if (event.capacity != null) {
            ExperienceInfoRow(icon = "👥", label = "정원", value = "1회 ${event.capacity}명")
        }
        if (!event.location.isNullOrBlank()) {
            ExperienceInfoRow(icon = "📍", label = "장소", value = event.location)
        }
        if (event.price != null) {
            val priceText = if (event.price == 0) "무료"
            else "${NumberFormat.getNumberInstance(Locale.KOREA).format(event.price)}원"
            ExperienceInfoRow(icon = "💰", label = "참가비", value = priceText)
        }
    }
}

@Composable
private fun ExperienceInfoRow(icon: String, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = icon,
            fontSize = 16.sp,
            modifier = Modifier.width(24.dp),
        )
        Text(
            text = label,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            color = Color(0xFF595959),
            modifier = Modifier.width(60.dp),
        )
        Text(
            text = value,
            fontFamily = Pretendard,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = Color(0xFF121212),
        )
    }
}

@Composable
private fun ExperienceCompleteContent(
    event: com.alphacity.stamptour.network.dto.EventItem,
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

        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(ExperienceOrange),
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
            text = "신청 완료!",
            fontFamily = Pretendard,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = Color(0xFF121212),
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = event.name,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = Color(0xFF595959),
        )

        // 체험 정보 요약
        Spacer(modifier = Modifier.height(16.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (event.duration != null) {
                Text(
                    text = "체험 시간: ${event.duration}분",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = Color(0xFF595959),
                )
            }
            if (!event.location.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "장소: ${event.location}",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = Color(0xFF595959),
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 참여 확인 안내
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFF8F0))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "ℹ 참여 확인",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = ExperienceOrange,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "체험 장소에서 이 화면을 보여주세요.",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = Color(0xFF8F8F8F),
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ExperienceOrange)
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
private fun ExperienceImageBox(event: com.alphacity.stamptour.network.dto.EventItem) {
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
private fun ExperienceGuideStep(number: Int, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(ExperienceOrange),
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

private fun formatExperienceDate(dateStr: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val display = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        val date = parser.parse(dateStr.take(19))
        date?.let { display.format(it) } ?: dateStr.take(10)
    } catch (_: Exception) {
        dateStr.take(10)
    }
}
