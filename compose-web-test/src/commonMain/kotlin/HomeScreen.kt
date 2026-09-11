package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.alphacity.stamptour.network.ApiService
import com.alphacity.stamptour.network.dto.BannerItem
import com.alphacity.stamptour.network.dto.EventItem
import com.alphacity.stamptour.network.dto.FestivalItem
import com.alphacity.stamptour.network.dto.ProgramItem
import com.alphacity.stamptour.network.dto.StampItem
import com.alphacity.stamptour.repository.HomeRepository
import composewebtest.generated.resources.Res
import composewebtest.generated.resources.header_left_arrow
import composewebtest.generated.resources.header_logo
import composewebtest.generated.resources.icon_coupon
import composewebtest.generated.resources.icon_mapview
import composewebtest.generated.resources.icon_profile
import composewebtest.generated.resources.join_event_popup
import composewebtest.generated.resources.olly_walking_graphic
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.jetbrains.compose.resources.painterResource

private val Primary = Color(0xFF02CDF8)
private val Pretendard = FontFamily.SansSerif

private const val API_BASE_URL = "https://ollymoa-server.vercel.app"

// ============================================================
// Main Button Gradient
// ============================================================

private val MainGradient = Brush.horizontalGradient(
    colorStops = arrayOf(
        0.02f to Color(0xFF6092FF),
        0.36f to Color(0xFF2563EB),
        1.0f to Color(0xFF1551D3),
    ),
)

// ============================================================
// Image URL
// ============================================================

private fun imageUrl(path: String?): String? {
    if (path.isNullOrBlank()) {
        return null
    }

    return if (
        path.startsWith("http://") ||
        path.startsWith("https://")
    ) {
        path
    } else {
        "$API_BASE_URL${if (path.startsWith("/")) path else "/$path"}"
    }
}

// ============================================================
// VH
// ============================================================

private fun vh(
    viewportHeight: Dp,
    value: Float,
): Dp {
    return viewportHeight * (value / 800f)
}

// ============================================================
// Home Screen
// ============================================================

@Composable
fun HomeScreen(
    onNavigateToMyPage: () -> Unit = {},
    onNavigateToProgramList: () -> Unit = {},
    onNavigateToProgramListWithCategory: (String) -> Unit = {},
    onNavigateToEventHighlight: () -> Unit = {},
    onNavigateToMap: () -> Unit = {},
    onNavigateToStamp: () -> Unit = {},
    onNavigateToCoupons: () -> Unit = {},
    onProgramClick: (Int) -> Unit = {},
    onFestivalClick: (Int) -> Unit = {},
    onBannerClick: (Int) -> Unit = {},
    onGuestRestricted: () -> Unit = {},
    isGuest: Boolean = false,
) {
    val repository = remember {
        HomeRepository()
    }

    var banners by remember {
        mutableStateOf<List<BannerItem>>(emptyList())
    }

    var festivals by remember {
        mutableStateOf<List<FestivalItem>>(emptyList())
    }

    var events by remember {
        mutableStateOf<List<EventItem>>(emptyList())
    }

    var stampCount by remember {
        mutableStateOf(0)
    }

    var totalStamps by remember {
        mutableStateOf(0)
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var showFirstComeCouponPopup by remember {
        mutableStateOf(false)
    }

    // ========================================================
    // 모든 API를 동시에 요청
    // ========================================================

    LaunchedEffect(Unit) {
        isLoading = true

        coroutineScope {

            // 세미나 x2 실질 스탬프 계산에 사용할 데이터
            var loadedPrograms = emptyList<ProgramItem>()
            var loadedStamps = emptyList<StampItem>()

            val bannersDeferred = async {
                try {
                    ApiService.getBanners()
                } catch (_: Exception) {
                    null
                }
            }

            val festivalsDeferred = async {
                repository.getFestivals()
            }

            val programsDeferred = async {
                repository.getPrograms()
            }

            val stampsDeferred = async {
                repository.getStamps()
            }

            val userStampsDeferred = async {
                if (!isGuest) {
                    repository.getUserStamps()
                } else {
                    null
                }
            }

            val popupDeferred = async {
                if (!isGuest) {
                    try {
                        ApiService.checkFirstComeCouponPopup()
                    } catch (_: Exception) {
                        null
                    }
                } else {
                    null
                }
            }

            // ------------------------------------------------
            // Banners
            // ------------------------------------------------

            val bannersResponse = bannersDeferred.await()

            if (
                bannersResponse != null &&
                bannersResponse.success
            ) {
                banners = bannersResponse.data.orEmpty()
            } else {
                banners = emptyList()
            }

            // ------------------------------------------------
            // Festivals
            // ------------------------------------------------

            festivalsDeferred
                .await()
                .onSuccess { result ->
                    festivals = result
                }
                .onFailure {
                    festivals = emptyList()
                }

            // ------------------------------------------------
            // Programs / Events
            // ------------------------------------------------

            programsDeferred
                .await()
                .onSuccess { programs ->

                    // 세미나 여부 판별용으로 보관
                    loadedPrograms = programs

                    events = programs
                        .flatMap { program ->
                            program.events.orEmpty()
                        }
                        .distinctBy { event ->
                            event.id
                        }
                        .sortedBy { event ->
                            event.startDate
                        }
                }
                .onFailure {
                    loadedPrograms = emptyList()
                    events = emptyList()
                }

            // ------------------------------------------------
            // All Stamps
            // ------------------------------------------------

            stampsDeferred
                .await()
                .onSuccess { stamps ->

                    // 세미나 스탬프 판별용으로 보관
                    loadedStamps = stamps

                    totalStamps = stamps.size
                }
                .onFailure {
                    loadedStamps = emptyList()
                    totalStamps = 0
                }

            // ------------------------------------------------
            // User Stamps
            // ------------------------------------------------

            if (!isGuest) {
                userStampsDeferred
                    .await()
                    ?.onSuccess { userStamps ->

                        // 세미나 스탬프는 실질적으로 2개로 계산
                        stampCount = getEffectiveTotalStampCount(
                            userStamps = userStamps,
                            stamps = loadedStamps,
                            programs = loadedPrograms,
                        )
                    }
                    ?.onFailure {
                        stampCount = 0
                    }
            } else {
                stampCount = 0
            }

            // ------------------------------------------------
            // First Come Coupon Popup
            // ------------------------------------------------

            if (!isGuest) {
                val popupResponse = popupDeferred.await()

                if (
                    popupResponse != null &&
                    popupResponse.success &&
                    popupResponse.data == true
                ) {
                    showFirstComeCouponPopup = true
                }
            }
        }

        isLoading = false
    }

    // ========================================================
    // UI
    // ========================================================

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
    ) {
        val viewportHeight = maxHeight

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
            ) {

                // ============================================
                // Header
                // ============================================

                HomeHeader(
                    onProfileTap = onNavigateToMyPage,
                )

                // ============================================
                // Home Content
                // ============================================

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(
                            rememberScrollState()
                        ),
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                    ) {

                        BannerCarousel(
                            banners = banners,
                            isLoading = isLoading,
                            viewportHeight = viewportHeight,
                            onBannerClick = {
                                onBannerClick(it)
                            },
                        )

                        FestivalSection(
                            festivals = festivals,
                            isLoading = isLoading,
                            viewportHeight = viewportHeight,
                            onSeeAllClick = onNavigateToProgramList,
                            onFestivalClick = {
                                onFestivalClick(it.id)
                            },
                        )

                        EventSection(
                            events = events,
                            isLoading = isLoading,
                            viewportHeight = viewportHeight,
                            onSeeAllTapped = onNavigateToEventHighlight,
                            onEventClick = onNavigateToEventHighlight,
                        )

                        StampProgressSection(
                            stampCount = stampCount,
                            totalStamps = totalStamps,
                            viewportHeight = viewportHeight,
                            onStampClick = {
                                if (isGuest) {
                                    onGuestRestricted()
                                } else {
                                    onNavigateToStamp()
                                }
                            },
                        )
                    }

                    QuickMenuSection(
                        viewportHeight = viewportHeight,
                        onMapClick = {
                            if (isGuest) {
                                onGuestRestricted()
                            } else {
                                onNavigateToMap()
                            }
                        },
                        onCouponClick = {
                            if (isGuest) {
                                onGuestRestricted()
                            } else {
                                onNavigateToCoupons()
                            }
                        },
                    )
                }
            }

            // ============================================
            // 선착순 5,000원 쿠폰 팝업
            // ============================================

            if (showFirstComeCouponPopup) {
                FirstComeCouponPopup(
                    viewportHeight = viewportHeight,
                    onMapClick = {
                        showFirstComeCouponPopup = false
                        onNavigateToMap()
                    },
                    onCloseClick = {
                        showFirstComeCouponPopup = false
                    },
                )
            }
        }
    }
}

// ============================================================
// First Come Coupon Popup
// ============================================================

@Composable
private fun FirstComeCouponPopup(
    viewportHeight: Dp,
    onMapClick: () -> Unit,
    onCloseClick: () -> Unit,
) {
    val popupWidth = 308.dp
    val popupHeight = vh(viewportHeight, 350f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(alpha = 0.55f)
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(popupWidth)
                .height(popupHeight),
        ) {

            Image(
                painter = painterResource(Res.drawable.join_event_popup),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .width(popupWidth)
                    .height(popupHeight),
            )

            Box(
                modifier = Modifier
                    .width(popupWidth)
                    .height(vh(viewportHeight, 70f))
                    .align(Alignment.BottomCenter)
                    .clickable {
                        onMapClick()
                    },
            )

            Box(
                modifier = Modifier
                    .size(15.dp)
                    .align(Alignment.TopEnd)
                    .offset(
                        x = (-45).dp,
                        y = vh(viewportHeight, 18f),
                    )
                    .clickable {
                        onCloseClick()
                    },
            )
        }
    }
}

// ============================================================
// Header
// ============================================================

@Composable
private fun HomeHeader(
    onProfileTap: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = 12.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Image(
            painter = painterResource(Res.drawable.header_logo),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(42.dp),
        )

        Spacer(
            modifier = Modifier.width(8.dp)
        )

        Text(
            text = "올리모아",
            color = Color(0xFF121212),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .clickable {
                    onProfileTap()
                },
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.icon_profile),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

// ============================================================
// Banner Carousel
// 350 : 247
// ============================================================

@Composable
private fun BannerCarousel(
    banners: List<BannerItem>,
    isLoading: Boolean,
    viewportHeight: Dp,
    onBannerClick: (Int) -> Unit = {},
) {
    val pageCount = banners.size

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = {
            pageCount
        },
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .aspectRatio(350f / 247f),
        contentAlignment = Alignment.Center,
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(350f / 247f)
                        .clip(
                            RoundedCornerShape(20.dp)
                        )
                        .background(
                            Color(0xFFEDF7FF)
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "배너 불러오는 중...",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Color(0xFF8DBDCC),
                    )
                }
            }

            banners.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(350f / 247f)
                        .clip(
                            RoundedCornerShape(20.dp)
                        )
                        .background(
                            Color(0xFFEDF7FF)
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "배너 준비중",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Color(0xFF8DBDCC),
                    )
                }
            }

            else -> {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(350f / 247f),
                    pageSpacing = 12.dp,
                ) { page ->

                    val banner = banners[page]

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(350f / 247f)
                            .clip(
                                RoundedCornerShape(20.dp)
                            )
                            .background(
                                Color(0xFFEDF7FF)
                            )
                            .clickable {
                                onBannerClick(banner.id)
                            },
                    ) {

                        WebImage(
                            path = banner.imageUrl,
                            contentDescription = banner.title,
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(20.dp),
                        )

                        Box(
                            modifier = Modifier
                                .padding(
                                    start = 12.dp,
                                    bottom = 12.dp,
                                )
                                .width(50.dp)
                                .height(
                                    vh(
                                        viewportHeight,
                                        28f
                                    )
                                )
                                .clip(
                                    RoundedCornerShape(15.dp)
                                )
                                .background(
                                    Color.Black.copy(alpha = 0.6f)
                                )
                                .align(Alignment.BottomStart),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "${page + 1} / $pageCount",
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                                color = Color.White,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// Festival Section
// ============================================================

@Composable
private fun FestivalSection(
    festivals: List<FestivalItem>,
    isLoading: Boolean,
    viewportHeight: Dp,
    onSeeAllClick: () -> Unit = {},
    onFestivalClick: (FestivalItem) -> Unit = {},
) {
    Column(
        modifier = Modifier.padding(
            top = vh(viewportHeight, 66f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "진행중인 스탬프 투어",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color(0xFF121212),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .size(
                        width = 8.dp,
                        height = 15.dp,
                    )
                    .clickable {
                        onSeeAllClick()
                    },
                contentAlignment = Alignment.Center,
            ) {
                HeaderRightArrow()
            }
        }

        Spacer(
            modifier = Modifier.height(
                vh(viewportHeight, 23f)
            )
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            vh(viewportHeight, 250f)
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "축제 불러오는 중...",
                        fontFamily = Pretendard,
                        fontSize = 13.sp,
                        color = Color(0xFF999999),
                    )
                }
            }

            festivals.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            vh(viewportHeight, 250f)
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "진행중인 축제가 없습니다.",
                        fontFamily = Pretendard,
                        fontSize = 13.sp,
                        color = Color(0xFF999999),
                    )
                }
            }

            else -> {
                Row(
                    modifier = Modifier.horizontalScroll(
                        rememberScrollState()
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    festivals.forEach { festival ->
                        FestivalCard(
                            festival = festival,
                            onClick = {
                                onFestivalClick(festival)
                            },
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// Header Right Arrow
// ============================================================

@Composable
private fun HeaderRightArrow() {
    Image(
        painter = painterResource(Res.drawable.header_left_arrow),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .size(
                width = 8.dp,
                height = 15.dp,
            )
            .graphicsLayer {
                scaleX = -1f
            },
    )
}

// ============================================================
// Festival Card
// 286 : 170
// ============================================================

@Composable
private fun FestivalCard(
    festival: FestivalItem,
    onClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .width(286.dp)
            .clickable {
                onClick()
            },
    ) {
        Box(
            modifier = Modifier
                .width(286.dp)
                .aspectRatio(286f / 170f)
                .clip(
                    RoundedCornerShape(15.dp)
                )
                .background(
                    Color(0xFFE8E8E8)
                ),
            contentAlignment = Alignment.Center,
        ) {
            WebImage(
                path = festival.imageUrl,
                contentDescription = festival.name,
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(15.dp),
            )
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        Text(
            text = festival.name,
            fontFamily = Pretendard,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = Color(0xFF212121),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(
            modifier = Modifier.height(3.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = festival.address ?: "",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color(0xFF7D7D7D),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            FestivalStatusChip(
                text = when (festival.status) {
                    "in_progress" -> "진행중"
                    "scheduled" -> "예정"
                    else -> "종료"
                }
            )
        }
    }
}

// ============================================================
// Festival Status Chip
// ============================================================

@Composable
private fun FestivalStatusChip(
    text: String,
) {
    Box(
        modifier = Modifier
            .clip(
                RoundedCornerShape(15.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFF2563EB),
                shape = RoundedCornerShape(15.dp),
            )
            .padding(
                horizontal = 12.dp,
                vertical = 5.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = Color(0xFF2563EB),
            maxLines = 1,
        )
    }
}

// ============================================================
// Event Section
// ============================================================

@Composable
private fun EventSection(
    events: List<EventItem>,
    isLoading: Boolean,
    viewportHeight: Dp,
    onSeeAllTapped: () -> Unit = {},
    onEventClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier.padding(
            top = vh(viewportHeight, 66f)
        )
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "이벤트",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color(0xFF121212),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .size(
                        width = 8.dp,
                        height = 15.dp,
                    )
                    .clickable {
                        onSeeAllTapped()
                    },
                contentAlignment = Alignment.Center,
            ) {
                HeaderRightArrow()
            }
        }

        Spacer(
            modifier = Modifier.height(
                vh(viewportHeight, 23f)
            )
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            vh(viewportHeight, 250f)
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "이벤트 불러오는 중...",
                        fontFamily = Pretendard,
                        fontSize = 13.sp,
                        color = Color(0xFF999999),
                    )
                }
            }

            events.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            vh(viewportHeight, 250f)
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "진행중인 이벤트가 없습니다.",
                        fontFamily = Pretendard,
                        fontSize = 13.sp,
                        color = Color(0xFF999999),
                    )
                }
            }

            else -> {
                Row(
                    modifier = Modifier.horizontalScroll(
                        rememberScrollState()
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    events.forEach { event ->
                        EventCard(
                            event = event,
                            onClick = onEventClick,
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// Event Card
// 170 : 170
// ============================================================

@Composable
private fun EventCard(
    event: EventItem,
    onClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .width(170.dp)
            .clickable {
                onClick()
            },
    ) {
        Box(
            modifier = Modifier
                .width(170.dp)
                .aspectRatio(170f / 170f)
                .clip(
                    RoundedCornerShape(15.dp)
                )
                .background(
                    Color(0xFFE8E8E8)
                ),
            contentAlignment = Alignment.Center,
        ) {
            WebImage(
                path = event.imageUrl,
                contentDescription = event.name,
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(15.dp),
            )
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        Text(
            text = event.name,
            fontFamily = Pretendard,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = Color(0xFF212121),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(
            modifier = Modifier.height(3.dp)
        )

        Text(
            text = "${formatDate(event.startDate)} ~ ${formatDate(event.endDate)}",
            fontFamily = Pretendard,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = Color(0xFF7D7D7D),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(
            modifier = Modifier.height(7.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            EventStatusChip(
                text = getEventStatus(event),
                borderColor = Color(0xFF2563EB),
                textColor = Color(0xFF2563EB),
            )

            if (!event.reward.isNullOrBlank()) {
                EventStatusChip(
                    text = "쿠폰",
                    borderColor = Color(0xFFEF9E1C),
                    textColor = Color(0xFFE08A00),
                )
            }
        }
    }
}

// ============================================================
// Event Status
// ============================================================

private fun getEventStatus(
    event: EventItem,
): String {
    return "진행중"
}

// ============================================================
// Event Status Chip
// ============================================================

@Composable
private fun EventStatusChip(
    text: String,
    borderColor: Color,
    textColor: Color,
) {
    Box(
        modifier = Modifier
            .clip(
                RoundedCornerShape(15.dp)
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(15.dp),
            )
            .padding(
                horizontal = 12.dp,
                vertical = 5.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = textColor,
            maxLines = 1,
        )
    }
}

// ============================================================
// Date
// ============================================================

private fun formatDate(
    date: String,
): String {
    if (date.length >= 10) {
        return date.substring(2, 10)
            .replace("-", ".")
    }

    return date
}

// ============================================================
// Web Image
//
// 서버 실제 데이터 이미지 전용
// WebElementView 제거
// ============================================================

@Composable
private fun WebImage(
    path: String?,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(0.dp),
) {
    val url = imageUrl(path)

    if (url.isNullOrBlank()) {
        return
    }

    AsyncImage(
        model = url,
        contentDescription = contentDescription,
        modifier = modifier.clip(shape),
        contentScale = ContentScale.Crop,
    )
}

// ============================================================
// Stamp Progress
// 350 : 192
// ============================================================

@Composable
private fun StampProgressSection(
    stampCount: Int,
    totalStamps: Int,
    viewportHeight: Dp,
    onStampClick: () -> Unit = {},
) {
    val progress =
        if (totalStamps > 0) {
            (stampCount.toFloat() / totalStamps)
                .coerceIn(0f, 1f)
        } else {
            0f
        }

    val percentText =
        "${(progress * 100).toInt()}%"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = vh(viewportHeight, 66f)
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(
            text = "나의 스탬프 진행률",
            fontFamily = Pretendard,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color(0xFF121212),
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(
            modifier = Modifier.height(
                vh(viewportHeight, 21f)
            )
        )

        Box(
            modifier = Modifier
                .width(350.dp)
                .aspectRatio(350f / 192f)
                .clip(
                    RoundedCornerShape(22.dp)
                )
                .background(
                    Color(0xFFEDF7FF)
                ),
        ) {

            Text(
                text = percentText,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Bold,
                fontSize = 50.sp,
                color = Color(0xFF2563EB),
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = vh(viewportHeight, 20f),
                        end = 24.dp,
                    ),
            )

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 31.dp,
                        end = 31.dp,
                        bottom = vh(viewportHeight, 32f),
                    )
                    .height(
                        vh(viewportHeight, 47f)
                    )
                    .align(Alignment.BottomCenter),
            ) {
                val filledWidth =
                    maxWidth * progress

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            vh(viewportHeight, 10f)
                        )
                        .align(Alignment.BottomStart)
                        .clip(
                            RoundedCornerShape(100.dp)
                        )
                        .background(Color.White),
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .clip(
                                RoundedCornerShape(100.dp)
                            )
                            .background(
                                Color(0xFF26AE3F)
                            ),
                    )
                }

                Box(
                    modifier = Modifier
                        .size(
                            vh(viewportHeight, 36f)
                        )
                        .offset(
                            x = filledWidth -
                                    vh(viewportHeight, 18f),
                            y = 0.dp,
                        )
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(
                            Res.drawable.olly_walking_graphic
                        ),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(
                vh(viewportHeight, 40f)
            )
        )

        Box(
            modifier = Modifier
                .width(350.dp)
                .aspectRatio(350f / 55f)
                .clip(
                    RoundedCornerShape(15.dp)
                )
                .background(
                    MainGradient
                )
                .clickable {
                    onStampClick()
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "나의 스탬프 보러가기 >",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.White,
            )
        }

        Spacer(
            modifier = Modifier.height(
                vh(viewportHeight, 66f)
            )
        )
    }
}

// ============================================================
// Quick Menu Section
// 402 : 313
// ============================================================

@Composable
private fun QuickMenuSection(
    viewportHeight: Dp,
    onMapClick: () -> Unit = {},
    onCouponClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(402f / 313f)
            .background(
                Color(0xFFF9F9F9)
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = vh(viewportHeight, 28f),
                    start = 20.dp,
                    end = 20.dp,
                    bottom = vh(viewportHeight, 32f),
                ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                QuickMenuCard(
                    title = "지도보기",
                    painter = painterResource(Res.drawable.icon_mapview),
                    modifier = Modifier.weight(1f),
                    viewportHeight = viewportHeight,
                    onClick = onMapClick,
                )

                QuickMenuCard(
                    title = "쿠폰함",
                    painter = painterResource(Res.drawable.icon_coupon),
                    modifier = Modifier.weight(1f),
                    viewportHeight = viewportHeight,
                    onClick = onCouponClick,
                )
            }
        }
    }
}

// ============================================================
// Quick Menu Card
// ============================================================

@Composable
private fun QuickMenuCard(
    title: String,
    painter: androidx.compose.ui.graphics.painter.Painter,
    modifier: Modifier = Modifier,
    viewportHeight: Dp,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .height(
                vh(viewportHeight, 168f)
            )
            .clip(
                RoundedCornerShape(20.dp)
            )
            .background(Color.White)
            .clickable {
                onClick()
            },
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = title,
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF121212),
                modifier = Modifier.padding(
                    top = vh(viewportHeight, 20f),
                    start = 18.dp,
                ),
            )

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .width(105.dp)
                    .height(
                        vh(viewportHeight, 105f)
                    )
                    .align(Alignment.End)
                    .padding(
                        end = 8.dp,
                        bottom = vh(viewportHeight, 12f),
                    ),
            )
        }
    }
}