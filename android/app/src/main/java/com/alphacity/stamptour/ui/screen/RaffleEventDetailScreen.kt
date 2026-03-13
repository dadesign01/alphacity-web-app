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
import com.alphacity.stamptour.network.dto.EventItem
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.ui.theme.Primary
import com.alphacity.stamptour.viewmodel.EventDetailViewModel
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun RaffleEventDetailScreen(
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

    Box(modifier = Modifier.fillMaxSize().background(Color.White).statusBarsPadding()) {
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
                    text = "추첨 이벤트",
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                ) {
                    // 이벤트 이미지
                    EventImageBox(event = ev, height = 200)

                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // 상태 뱃지 + 제목
                        val isOpen = ev.status != "ended"
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isOpen) Primary else Color(0xFF8F8F8F))
                                    .padding(horizontal = 10.dp, vertical = 3.dp),
                            ) {
                                Text(
                                    text = if (isOpen) "참여 가능" else "마  감",
                                    fontFamily = Pretendard,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = ev.name,
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp,
                                color = Color(0xFF121212),
                            )
                        }

                        // 경품 정보
                        if (!ev.reward.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF8F8F8))
                                    .padding(14.dp),
                            ) {
                                Text(
                                    text = "경품 정보",
                                    fontFamily = Pretendard,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF121212),
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(id = R.drawable.icon_coupon),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "상품: ${ev.reward}",
                                        fontFamily = Pretendard,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = Primary,
                                    )
                                }
                                val winnerCount = ev.winnerCount ?: 0
                                if (winnerCount > 0) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "당첨자 수: ${winnerCount}명",
                                        fontFamily = Pretendard,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = Color(0xFF595959),
                                    )
                                }
                            }
                        }

                        // 설명
                        if (!ev.description.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = ev.description,
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = Color(0xFF595959),
                            )
                        }

                        // 이벤트 기간
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "이벤트 기간: ${formatDate(ev.startDate)} ~ ${formatDate(ev.endDate)}",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = Color(0xFF828282),
                        )

                        // 참여 인원
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${NumberFormat.getNumberInstance(Locale.KOREA).format(ev.participantCount)}명 참여",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = Color(0xFF828282),
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        Divider(color = Color(0xFFE2E2E2))
                        Spacer(modifier = Modifier.height(20.dp))

                        // 번호 발급 영역
                        val hasParticipated = ev.isParticipated == true || myRaffleNumber != null
                        val raffleNum = myRaffleNumber ?: ev.myRaffleNumber

                        if (hasParticipated && raffleNum != null) {
                            // 번호 발급 완료 상태
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFF0F7FF))
                                    .padding(vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = "나의 추첨 번호",
                                    fontFamily = Pretendard,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = Color(0xFF595959),
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "#$raffleNum",
                                    fontFamily = Pretendard,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 48.sp,
                                    color = Primary,
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "추첨은 현장에서 진행됩니다",
                                    fontFamily = Pretendard,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp,
                                    color = Color(0xFF8F8F8F),
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 비활성 버튼
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFCCCCCC))
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "번호 발급 완료",
                                    fontFamily = Pretendard,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = Color.White,
                                )
                            }
                        } else if (isOpen) {
                            // 발급 가능
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
                                        text = "번호 발급받기",
                                        fontFamily = Pretendard,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        color = Color.White,
                                    )
                                }
                            }
                        } else {
                            // 마감
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFCCCCCC))
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "이벤트가 종료되었습니다",
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

        // Toast message
        if (message != null) {
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
private fun EventImageBox(event: EventItem, height: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp),
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

private fun formatDate(dateStr: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val display = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        val date = parser.parse(dateStr.take(19))
        date?.let { display.format(it) } ?: dateStr.take(10)
    } catch (_: Exception) {
        dateStr.take(10)
    }
}
