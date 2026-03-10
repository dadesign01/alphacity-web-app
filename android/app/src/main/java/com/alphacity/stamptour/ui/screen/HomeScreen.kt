package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.alphacity.stamptour.BuildConfig
import com.alphacity.stamptour.R
import com.alphacity.stamptour.network.dto.BannerItem
import com.alphacity.stamptour.network.dto.EventItem
import com.alphacity.stamptour.network.dto.ProgramItem
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.ui.theme.Primary
import com.alphacity.stamptour.viewmodel.HomeViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HomeScreen(
    onNavigateToMyPage: () -> Unit = {},
    onNavigateToProgramList: () -> Unit = {},
    onNavigateToEventHighlight: () -> Unit = {},
    onNavigateToMap: () -> Unit = {},
    onNavigateToStamp: () -> Unit = {},
    onNavigateToCoupons: () -> Unit = {},
    onProgramClick: (ProgramItem) -> Unit = {},
    onGuestRestricted: () -> Unit = {},
    isGuest: Boolean = false,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val banners by viewModel.banners.collectAsState()
    val programs by viewModel.programs.collectAsState()
    val events by viewModel.events.collectAsState()
    val userStampCount by viewModel.userStampCount.collectAsState()
    val totalStampCount by viewModel.totalStampCount.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchHomeData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        // Header
        HomeHeader(onProfileTap = onNavigateToMyPage)

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // Banner Carousel
            BannerCarousel(banners = banners)

            // 오늘의 프로그램
            ProgramSection(
                programs = programs,
                onSeeAllClick = onNavigateToProgramList,
                onProgramClick = onProgramClick,
            )

            // 알파시티 이벤트
            EventSection(
                events = events,
                onSeeAllTapped = onNavigateToEventHighlight,
                onEventClick = onNavigateToEventHighlight,
            )

            // 나의 스탬프 진행률
            StampProgressSection(
                stampCount = userStampCount,
                totalStamps = totalStampCount,
                onStampClick = {
                    if (isGuest) onGuestRestricted() else onNavigateToStamp()
                },
            )

            // 지도보기 + 쿠폰함 + Footer (gray background area)
            QuickMenuSection(
                onMapClick = {
                    if (isGuest) onGuestRestricted() else onNavigateToMap()
                },
                onCouponClick = {
                    if (isGuest) onGuestRestricted() else onNavigateToCoupons()
                },
            )

            Text(
                text = "© 2026 Alpha Stamp. All rights reserved.",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = Color(0xFF999999),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9F9F9))
                    .padding(vertical = 24.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}

// MARK: - Header

@Composable
private fun HomeHeader(onProfileTap: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(start = 15.dp, end = 20.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = R.drawable.header_logo),
            contentDescription = "Logo",
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Crop,
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "알파스탬프",
            fontFamily = Pretendard,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = Color(0xFF121212),
            letterSpacing = (-0.36).sp,
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.dp, Color(0xFFEBEBEB), CircleShape)
                .clickable { onProfileTap() },
        ) {
            Image(
                painter = painterResource(id = R.drawable.icon_profile),
                contentDescription = "마이페이지",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

// MARK: - Banner Carousel

@Composable
private fun BannerCarousel(banners: List<BannerItem> = emptyList()) {
    if (banners.isEmpty()) {
        // placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp)
                .height(150.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(Color(0xFFEDF7FF)),
        )
        return
    }

    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(banners.size) {
        while (true) {
            delay(4000)
            currentIndex = (currentIndex + 1) % banners.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp)
            .height(150.dp)
            .clip(RoundedCornerShape(15.dp)),
    ) {
        val banner = banners[currentIndex.coerceIn(0, banners.lastIndex)]
        val fullUrl = if (banner.imageUrl.startsWith("http")) {
            banner.imageUrl
        } else {
            BuildConfig.SERVER_URL + banner.imageUrl
        }

        AsyncImage(
            model = fullUrl,
            contentDescription = banner.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    }
}

// MARK: - 오늘의 프로그램

@Composable
private fun ProgramSection(programs: List<ProgramItem> = emptyList(), onSeeAllClick: () -> Unit = {}, onProgramClick: (ProgramItem) -> Unit = {}) {
    val fallbackPrograms = remember {
        listOf(
            Triple(R.drawable.program_img_1, "알파시티 카페", "#카페"),
            Triple(R.drawable.program_img_2, "스마트 비스트로", "#레스토랑"),
            Triple(R.drawable.event_img_1, "미래 모빌리티 전시", "#전시체험"),
        )
    }

    Column(modifier = Modifier.padding(top = 20.dp)) {
        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "오늘의 프로그램",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF121212),
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "보러가기  >",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = Color(0xFF121212),
                modifier = Modifier.clickable { onSeeAllClick() },
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal scroll
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (programs.isEmpty()) {
                fallbackPrograms.forEach { (imageRes, name, tag) ->
                    ProgramCard(imageRes = imageRes, name = name, tag = tag)
                }
            } else {
                programs.forEach { program ->
                    val tag = if (program.status == "in_progress") "#진행중" else "#예정"
                    ProgramCard(
                        imageRes = R.drawable.program_img_1,
                        imageUrl = program.imageUrl,
                        name = program.name,
                        tag = tag,
                        onClick = { onProgramClick(program) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgramCard(imageRes: Int, imageUrl: String? = null, name: String, tag: String, onClick: () -> Unit = {}) {
    Column(modifier = Modifier.width(168.dp).clickable { onClick() }) {
        if (!imageUrl.isNullOrEmpty()) {
            val fullUrl = if (imageUrl.startsWith("http")) imageUrl else BuildConfig.SERVER_URL + imageUrl
            AsyncImage(
                model = fullUrl,
                contentDescription = name,
                modifier = Modifier
                    .size(168.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
            )
        } else {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = name,
                modifier = Modifier
                    .size(168.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = name,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color(0xFF121212),
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TagChip(text = tag, textColor = Color(0xFFE85151), borderColor = Color(0xFFE85151))
            TagChip(text = "#쿠폰가능", textColor = Color(0xFF4C27D0), borderColor = Color(0xFF4C27D0))
        }
    }
}

// MARK: - 알파시티 이벤트

@Composable
private fun EventSection(events: List<EventItem> = emptyList(), onSeeAllTapped: () -> Unit = {}, onEventClick: () -> Unit = {}) {
    data class FallbackEvent(
        val imageRes: Int,
        val name: String,
        val date: String,
        val tags: List<Triple<String, Color, Color>>,
    )

    val fallbackEvents = remember {
        listOf(
            FallbackEvent(
                R.drawable.event_img_2, "갤럭시탭 추첨 이벤트", "26.04.02 ~ 26.06.05",
                listOf(
                    Triple("#추첨이벤트", Color(0xFFE85151), Color(0xFFE85151)),
                    Triple("#스탬프 10개", Color(0xFF5182FF), Color(0xFF5182FF)),
                ),
            ),
            FallbackEvent(
                R.drawable.event_img_3, "VR 헤드셋 이벤트", "26.04.02 ~ 26.06.05",
                listOf(
                    Triple("#추첨이벤트", Color(0xFFE85151), Color(0xFFE85151)),
                    Triple("#스탬프 15개", Color(0xFF5182FF), Color(0xFF5182FF)),
                ),
            ),
            FallbackEvent(
                R.drawable.event_img_4, "에코백 증정 이벤트", "26.04.02 ~ 26.06.05",
                listOf(
                    Triple("#선착순", Color(0xFFE85151), Color(0xFFE85151)),
                    Triple("#스탬프 2개", Color(0xFF5182FF), Color(0xFF5182FF)),
                ),
            ),
        )
    }

    fun formatDate(iso: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val display = SimpleDateFormat("yy.MM.dd", Locale.getDefault())
            val date = parser.parse(iso.take(19))
            date?.let { display.format(it) } ?: iso
        } catch (_: Exception) {
            iso.take(10)
        }
    }

    Column(modifier = Modifier.padding(top = 28.dp)) {
        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "알파시티 이벤트",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF121212),
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "보러가기  >",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = Color(0xFF121212),
                modifier = Modifier.clickable { onSeeAllTapped() },
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal scroll
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (events.isEmpty()) {
                fallbackEvents.forEach { event ->
                    EventCard(
                        imageRes = event.imageRes,
                        name = event.name,
                        date = event.date,
                        tags = event.tags,
                    )
                }
            } else {
                events.forEach { event ->
                    val typeTag = if (event.type == "raffle") "#추첨이벤트" else "#선착순"
                    val dateStr = "${formatDate(event.startDate)} ~ ${formatDate(event.endDate)}"
                    EventCard(
                        imageRes = R.drawable.event_img_2,
                        imageUrl = event.imageUrl,
                        name = event.name,
                        date = dateStr,
                        tags = listOf(
                            Triple(typeTag, Color(0xFFE85151), Color(0xFFE85151)),
                            Triple("#${event.reward ?: "보상"}", Color(0xFF5182FF), Color(0xFF5182FF)),
                        ),
                        onClick = onEventClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun EventCard(
    imageRes: Int,
    imageUrl: String? = null,
    name: String,
    date: String,
    tags: List<Triple<String, Color, Color>>,
    onClick: () -> Unit = {},
) {
    Column(modifier = Modifier.width(168.dp).clickable { onClick() }) {
        if (!imageUrl.isNullOrEmpty()) {
            val fullUrl = if (imageUrl.startsWith("http")) imageUrl else BuildConfig.SERVER_URL + imageUrl
            AsyncImage(
                model = fullUrl,
                contentDescription = name,
                modifier = Modifier
                    .size(168.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
            )
        } else {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = name,
                modifier = Modifier
                    .size(168.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = name,
            fontFamily = Pretendard,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = Color(0xFF121212),
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = date,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = Color(0xFF121212),
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            tags.forEach { (text, textColor, borderColor) ->
                TagChip(text = text, textColor = textColor, borderColor = borderColor)
            }
        }
    }
}

// MARK: - Tag Chip

@Composable
private fun TagChip(text: String, textColor: Color, borderColor: Color) {
    Text(
        text = text,
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 9.sp,
        color = textColor,
        modifier = Modifier
            .border(1.dp, borderColor, RoundedCornerShape(100.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp),
    )
}

// MARK: - 나의 스탬프 진행률

@Composable
private fun StampProgressSection(stampCount: Int = 0, totalStamps: Int = 10, onStampClick: () -> Unit = {}) {
    val progress = if (totalStamps > 0) (stampCount.toFloat() / totalStamps).coerceIn(0f, 1f) else 0f
    val percentText = "${(progress * 100).toInt()}%"

    // Card with everything inside
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 28.dp)
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFFEDF7FF))
            .padding(horizontal = 31.dp, vertical = 26.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "나의 스탬프 진행률",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color(0xFF121212),
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = percentText,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    color = Primary,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Progress bar + character
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth().height(47.dp),
            ) {
                val barWidth = maxWidth
                val filledWidth = barWidth * progress

                // Bar track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .align(Alignment.BottomStart)
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color.White),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color(0xFF26AE3F)),
                    )
                }

                // Character on bar
                Image(
                    painter = painterResource(id = R.drawable.stamp_character),
                    contentDescription = "스탬프 캐릭터",
                    modifier = Modifier
                        .size(36.dp)
                        .offset(
                            x = filledWidth - 18.dp,
                            y = 0.dp,
                        ),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Button inside card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(Primary)
                    .clickable { onStampClick() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "나의 스탬프 보러가기  >",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.White,
                )
            }
        }
    }
}

// MARK: - Quick Menu

@Composable
private fun QuickMenuSection(onMapClick: () -> Unit = {}, onCouponClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9F9F9))
            .padding(top = 20.dp, bottom = 20.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            QuickMenuCard(
                title = "지도보기",
                imageRes = R.drawable.icon_mapview,
                modifier = Modifier.weight(1f),
                onClick = onMapClick,
            )
            QuickMenuCard(
                title = "쿠폰함",
                imageRes = R.drawable.icon_coupon,
                modifier = Modifier.weight(1f),
                onClick = onCouponClick,
            )
        }
    }
}

@Composable
private fun QuickMenuCard(title: String, imageRes: Int, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Box(
        modifier = modifier
            .height(168.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .clickable { onClick() },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = title,
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF121212),
                modifier = Modifier.padding(top = 20.dp, start = 18.dp),
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = title,
                    modifier = Modifier
                        .size(width = 57.dp, height = 80.dp)
                        .padding(end = 10.dp, bottom = 10.dp),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}
