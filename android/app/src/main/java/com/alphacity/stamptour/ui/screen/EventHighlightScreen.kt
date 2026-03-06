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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.alphacity.stamptour.BuildConfig
import com.alphacity.stamptour.R
import com.alphacity.stamptour.network.dto.EventItem
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.ui.theme.Primary
import com.alphacity.stamptour.viewmodel.EventHighlightViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

// ── Tab Enum ──

private enum class EventTab(val title: String) {
    RAFFLE("추첨 이벤트"),
    FIRST_COME("선착순 사은품"),
    EXPERIENCE("체험"),
}

// ── Mock Data for Experience tab (no DB type for this) ──

private data class ExperienceEvent(
    val imageRes: Int,
    val title: String,
    val description: String,
    val timeInfo: String,
    val location: String,
    val price: String,
)

private val mockExperienceEvents = listOf(
    ExperienceEvent(
        imageRes = R.drawable.event_img_1,
        title = "도자기 만들기",
        description = "전통 도예 기법으로 나만의 도자기를\n만들어 보세요.",
        timeInfo = "체험 시간 : 90분 | 1회 10명 정원",
        location = "알파시티 2로 33 공예 체험관",
        price = "15,000원",
    ),
    ExperienceEvent(
        imageRes = R.drawable.event_img_2,
        title = "천연비누 원데이 클래스",
        description = "천연 재료로 만드는 나만의 향기가득\n비누 만들기 원데이 클래스",
        timeInfo = "체험 시간 : 60분 | 1회 8명 정원",
        location = "알파시티 2로 33 DIY 공방",
        price = "12,000원",
    ),
    ExperienceEvent(
        imageRes = R.drawable.event_img_3,
        title = "3D 프린팅 액티비티",
        description = "3D 프린터로 나의 상상을 현실화하는\n나만의 작품을 뽐내보세요.",
        timeInfo = "체험 시간 : 120분 | 1회 6명 정원",
        location = "알파시티 2로 33 3D 프린팅 스튜디오",
        price = "무료",
    ),
    ExperienceEvent(
        imageRes = R.drawable.event_img_4,
        title = "수제 브레드 원데이 클래스",
        description = "유명 베이커리 카페 제빵사가 알려주는\n맛있는 빵 레시피! 제빵 체험해보세요.",
        timeInfo = "체험 시간 : 100분 | 1회 12명 정원",
        location = "알파시티 2로 33 ABC 베이커리",
        price = "18,000원",
    ),
)

// ── Main Screen ──

@Composable
fun EventHighlightScreen(
    onBackClick: () -> Unit = {},
    viewModel: EventHighlightViewModel = hiltViewModel(),
) {
    var selectedTab by remember { mutableStateOf(EventTab.RAFFLE) }
    val raffleEvents by viewModel.raffleEvents.collectAsState()
    val firstComeEvents by viewModel.firstComeEvents.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchEvents()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        // Header
        EventHighlightHeader(onBackClick = onBackClick)

        Divider(color = Color(0xFFE2E2E2), thickness = 1.dp)

        // Tab Bar (outside scroll to ensure clickability)
        EventTabBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
        )

        // Content (scrollable)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            when (selectedTab) {
                EventTab.RAFFLE -> RaffleTabContent(events = raffleEvents)
                EventTab.FIRST_COME -> FirstComeTabContent(events = firstComeEvents)
                EventTab.EXPERIENCE -> ExperienceTabContent()
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9F9F9))
                    .padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "© 2026 Alpha Stamp. All rights reserved.",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                    color = Color(0xFF8F8F8F),
                )
            }
        }
    }
}

// ── Header ──

@Composable
private fun EventHighlightHeader(onBackClick: () -> Unit) {
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
            text = "이벤트 하이라이트",
            fontFamily = Pretendard,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = Color(0xFF121212),
        )
    }
}

// ── Tab Bar ──

@Composable
private fun EventTabBar(
    selectedTab: EventTab,
    onTabSelected: (EventTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        EventTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (isSelected) Color(0xFF121212) else Color(0xFFF8F8F8))
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = tab.title,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = if (isSelected) Color.White else Color(0xFF121212),
                )
            }
        }
    }
}

// ── 추첨 이벤트 탭 ──

@Composable
private fun RaffleTabContent(events: List<EventItem>) {
    if (events.isEmpty()) {
        EmptyContent()
    } else {
        events.forEachIndexed { index, event ->
            RaffleEventCard(event = event)
            if (index < events.lastIndex) {
                Divider(
                    color = Color(0xFFB5B5B5),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
        }
    }
}

@Composable
private fun RaffleEventCard(event: EventItem) {
    val isOpen = event.status != "ended"

    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        // 이미지 영역
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(22.dp)),
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
                        fontSize = 32.sp,
                        color = Color(0xFFB5B5B5),
                    )
                }
            }
            if (!isOpen) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.68f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "이벤트 종료",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 상태 배지 + 제목
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
                text = event.name,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color(0xFF121212),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // 설명
        if (!event.description.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(9.dp))
            Text(
                text = event.description,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = Color(0xFF595959),
            )
        }

        // 이벤트 기간
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = "이벤트 기간 : ${formatEventDate(event.startDate)} ~ ${formatEventDate(event.endDate)}",
            fontFamily = Pretendard,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = Color(0xFF828282),
        )

        Spacer(modifier = Modifier.height(5.dp))

        // 보상 + 참여자수
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (!event.reward.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color(0xFFEDF7FF))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.icon_coupon),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = event.reward,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Primary,
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color(0xFFF8F8F8))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icon_profile),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = "${NumberFormat.getNumberInstance(Locale.KOREA).format(event.participantCount)}명 참여",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color(0xFF8F8F8F),
                )
            }
        }
    }
}

// ── 선착순 사은품 탭 ──

@Composable
private fun FirstComeTabContent(events: List<EventItem>) {
    if (events.isEmpty()) {
        EmptyContent()
    } else {
        events.forEachIndexed { index, event ->
            FirstComeEventItem(event = event)
            if (index < events.lastIndex) {
                Divider(
                    color = Color(0xFFB5B5B5),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
        }
    }
}

@Composable
private fun FirstComeEventItem(event: EventItem) {
    val isOpen = event.status != "ended"
    val progress = if (event.participantLimit > 0) {
        event.participantCount.toFloat() / event.participantLimit.toFloat()
    } else 0f

    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        // 상태 배지 + 제목
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
                text = event.name,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color(0xFF121212),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // 설명
        if (!event.description.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(9.dp))
            Text(
                text = event.description,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = Color(0xFF595959),
            )
        }

        // 보상 태그
        if (!event.reward.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color(0xFFF8F8F8))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icon_coupon),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = event.reward,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color(0xFF8F8F8F),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 참여수량 + 수치
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "참여수량",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                lineHeight = 16.sp,
                color = Color(0xFF595959),
            )
            Text(
                text = "${event.participantCount}/${event.participantLimit}",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = Primary,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 프로그레스 바
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
                    .background(if (isOpen) Primary else Color(0xFF8E8E8E)),
            )
        }
    }
}

// ── 체험 탭 (mock data - no DB type) ──

@Composable
private fun ExperienceTabContent() {
    mockExperienceEvents.forEachIndexed { index, event ->
        ExperienceEventItem(event = event)
        if (index < mockExperienceEvents.lastIndex) {
            Divider(
                color = Color(0xFFB5B5B5),
                thickness = 0.5.dp,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }
    }
}

@Composable
private fun ExperienceEventItem(event: ExperienceEvent) {
    Row(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(17.dp),
    ) {
        Image(
            painter = painterResource(id = event.imageRes),
            contentDescription = event.title,
            modifier = Modifier
                .width(130.dp)
                .height(168.dp)
                .clip(RoundedCornerShape(22.dp)),
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .height(168.dp),
        ) {
            Text(
                text = event.title,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color(0xFF121212),
            )

            Spacer(modifier = Modifier.height(9.dp))

            Text(
                text = event.description,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = Color(0xFF595959),
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = event.timeInfo,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = Color(0xFF828282),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color(0xFFF8F8F8))
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icon_location_pin),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = event.location,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color(0xFF8F8F8F),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = event.price,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
            )
        }
    }
}

// ── Empty Content ──

@Composable
private fun EmptyContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 300.dp)
            .padding(top = 80.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Text(
            text = "등록된 이벤트가 없습니다",
            fontFamily = Pretendard,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = Color(0xFF8F8F8F),
        )
    }
}

// ── Helpers ──

private fun formatEventDate(dateStr: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val display = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        val date = parser.parse(dateStr.take(19))
        date?.let { display.format(it) } ?: dateStr.take(10)
    } catch (_: Exception) {
        dateStr.take(10)
    }
}
