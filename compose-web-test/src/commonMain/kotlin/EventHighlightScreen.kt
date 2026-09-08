package com.alphacity.stamptour.ui.screen
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Primary = Color(0xFF02CDF8)
private val Pretendard = FontFamily.SansSerif

private enum class EventTab(val title: String) {
    RAFFLE("추첨 이벤트"),
    FIRST_COME("선착순 사은품"),
}

private data class WebEventItem(
    val id: Int,
    val name: String,
    val type: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val startDate: String = "",
    val endDate: String = "",
    val reward: String? = null,
    val participantCount: Int = 0,
    val participantLimit: Int = 0,
    val status: String = "scheduled",
    val price: Int? = null,
    val duration: Int? = null,
    val capacity: Int? = null,
    val location: String? = null,
)

@Composable
fun EventHighlightScreen(
    onBackClick: () -> Unit = {},
) {
    var selectedTab by remember {
        mutableStateOf(EventTab.RAFFLE)
    }

    var selectedEventId by remember {
        mutableStateOf(-1)
    }

    var selectedEventType by remember {
        mutableStateOf("")
    }

    // 웹 화면 렌더링용 테스트 데이터
    // 추후에 기존 API 응답으로 교체
    val raffleEvents = remember {
        listOf(
            WebEventItem(
                id = 1,
                name = "추첨 이벤트",
                type = "raffle",
                description = "이벤트에 참여하고 다양한 경품을 받아보세요!",
                startDate = "2026-09-01",
                endDate = "2026-09-30",
                reward = "특별 경품",
                participantCount = 128,
                status = "in_progress",
            ),
            WebEventItem(
                id = 2,
                name = "스탬프 투어 추첨 이벤트",
                type = "raffle",
                description = "스탬프를 모으고 추첨 이벤트에 참여해보세요.",
                startDate = "2026-09-05",
                endDate = "2026-10-01",
                reward = "상품권",
                participantCount = 54,
                status = "in_progress",
            ),
        )
    }

    val firstComeEvents = remember {
        listOf(
            WebEventItem(
                id = 3,
                name = "선착순 특별 사은품",
                type = "first_come",
                description = "선착순으로 참여하고 특별한 사은품을 받아보세요!",
                startDate = "2026-09-01",
                endDate = "2026-09-20",
                reward = "기념품",
                participantCount = 42,
                participantLimit = 100,
                status = "in_progress",
            ),
            WebEventItem(
                id = 4,
                name = "방문객 한정 굿즈",
                type = "first_come",
                description = "행사장을 방문한 분들을 위한 특별 굿즈입니다.",
                startDate = "2026-09-10",
                endDate = "2026-09-30",
                reward = "한정 굿즈",
                participantCount = 100,
                participantLimit = 100,
                status = "in_progress",
            ),
        )
    }

    if (selectedEventId > 0) {
        // 실제 상세 화면은 다음 단계에서 기존 Android 파일 기준으로 연결
        PlaceholderEventDetail(
            eventId = selectedEventId,
            eventType = selectedEventType,
            onBackClick = {
                selectedEventId = -1
                selectedEventType = ""
            },
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        EventHighlightHeader(
            onBackClick = onBackClick,
        )

        HorizontalDivider(
            color = Color(0xFFE2E2E2),
            thickness = 1.dp,
        )

        EventTabBar(
            selectedTab = selectedTab,
            onTabSelected = {
                selectedTab = it
            },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                ),
        ) {
            when (selectedTab) {
                EventTab.RAFFLE -> {
                    RaffleTabContent(
                        events = raffleEvents,
                        onEventClick = { event ->
                            selectedEventId = event.id
                            selectedEventType = event.type
                        },
                    )
                }

                EventTab.FIRST_COME -> {
                    FirstComeTabContent(
                        events = firstComeEvents,
                        onEventClick = { event ->
                            selectedEventId = event.id
                            selectedEventType = event.type
                        },
                    )
                }
            }

            Footer()
        }
    }
}

@Composable
private fun EventHighlightHeader(
    onBackClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(
                horizontal = 20.dp,
                vertical = 16.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "‹",
            fontFamily = Pretendard,
            fontWeight = FontWeight.Normal,
            fontSize = 34.sp,
            color = Color(0xFF121212),
            modifier = Modifier
                .size(
                    width = 13.dp,
                    height = 26.dp,
                )
                .clickable {
                    onBackClick()
                },
        )

        Spacer(
            modifier = Modifier.width(12.dp)
        )

        Text(
            text = "이벤트 하이라이트",
            fontFamily = Pretendard,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = Color(0xFF121212),
        )
    }
}

@Composable
private fun EventTabBar(
    selectedTab: EventTab,
    onTabSelected: (EventTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(
                horizontal = 20.dp,
                vertical = 12.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        EventTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(11.dp))
                    .background(
                        if (isSelected) {
                            Color(0xFF121212)
                        } else {
                            Color(0xFFF8F8F8)
                        }
                    )
                    .clickable {
                        onTabSelected(tab)
                    }
                    .padding(
                        horizontal = 14.dp,
                        vertical = 8.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = tab.title,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = if (isSelected) {
                        Color.White
                    } else {
                        Color(0xFF121212)
                    },
                )
            }
        }
    }
}

@Composable
private fun RaffleTabContent(
    events: List<WebEventItem>,
    onEventClick: (WebEventItem) -> Unit,
) {
    if (events.isEmpty()) {
        EmptyContent()
    } else {
        events.forEachIndexed { index, event ->
            Box(
                modifier = Modifier.clickable {
                    onEventClick(event)
                }
            ) {
                RaffleEventCard(
                    event = event,
                )
            }

            if (index < events.lastIndex) {
                HorizontalDivider(
                    color = Color(0xFFB5B5B5),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(
                        horizontal = 20.dp
                    ),
                )
            }
        }
    }
}

@Composable
private fun RaffleEventCard(
    event: WebEventItem,
) {
    val isOpen = event.status != "ended"

    Column(
        modifier = Modifier.padding(
            horizontal = 20.dp,
            vertical = 12.dp,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(165.dp)
                .clip(RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center,
        ) {
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

            if (!isOpen) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color.Black.copy(alpha = 0.68f)
                        ),
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

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (isOpen) {
                            Primary
                        } else {
                            Color(0xFF8F8F8F)
                        }
                    )
                    .padding(
                        horizontal = 10.dp,
                        vertical = 3.dp,
                    ),
            ) {
                Text(
                    text = if (isOpen) {
                        "참여 가능"
                    } else {
                        "마감"
                    },
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = Color.White,
                )
            }

            Spacer(
                modifier = Modifier.width(10.dp)
            )

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

        if (!event.description.isNullOrBlank()) {
            Spacer(
                modifier = Modifier.height(9.dp)
            )

            Text(
                text = event.description,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = Color(0xFF595959),
            )
        }

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Text(
            text = "이벤트 기간 : ${event.startDate} ~ ${event.endDate}",
            fontFamily = Pretendard,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = Color(0xFF828282),
        )

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (!event.reward.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color(0xFFEDF7FF))
                        .padding(
                            horizontal = 8.dp,
                            vertical = 5.dp,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "🎟",
                        fontSize = 14.sp,
                    )

                    Spacer(
                        modifier = Modifier.width(3.dp)
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

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color(0xFFF8F8F8))
                    .padding(
                        horizontal = 8.dp,
                        vertical = 5.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "👤",
                    fontSize = 14.sp,
                )

                Spacer(
                    modifier = Modifier.width(3.dp)
                )

                Text(
                    text = "${event.participantCount}명 참여",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color(0xFF8F8F8F),
                )
            }
        }
    }
}

@Composable
private fun FirstComeTabContent(
    events: List<WebEventItem>,
    onEventClick: (WebEventItem) -> Unit,
) {
    if (events.isEmpty()) {
        EmptyContent()
    } else {
        events.forEachIndexed { index, event ->
            Box(
                modifier = Modifier.clickable {
                    onEventClick(event)
                }
            ) {
                FirstComeEventItem(
                    event = event,
                )
            }

            if (index < events.lastIndex) {
                HorizontalDivider(
                    color = Color(0xFFB5B5B5),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(
                        horizontal = 20.dp
                    ),
                )
            }
        }
    }
}

@Composable
private fun FirstComeEventItem(
    event: WebEventItem,
) {
    val remaining = maxOf(
        event.participantLimit - event.participantCount,
        0,
    )

    val isSoldOut = remaining <= 0

    val isOpen =
        event.status != "ended" &&
                !isSoldOut

    val progress =
        if (event.participantLimit > 0) {
            event.participantCount.toFloat() /
                    event.participantLimit.toFloat()
        } else {
            0f
        }

    Column(
        modifier = Modifier
            .padding(
                horizontal = 20.dp,
                vertical = 16.dp,
            )
            .alpha(
                if (isSoldOut) {
                    0.5f
                } else {
                    1f
                }
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFFEDF7FF))
                    .padding(
                        horizontal = 10.dp,
                        vertical = 3.dp,
                    ),
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
                    .background(
                        if (isOpen) {
                            Primary
                        } else {
                            Color(0xFF8F8F8F)
                        }
                    )
                    .padding(
                        horizontal = 10.dp,
                        vertical = 3.dp,
                    ),
            ) {
                Text(
                    text = if (isOpen) {
                        "진행중"
                    } else {
                        "마감"
                    },
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = Color.White,
                )
            }

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

        if (!event.description.isNullOrBlank()) {
            Spacer(
                modifier = Modifier.height(9.dp)
            )

            Text(
                text = event.description,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = Color(0xFF595959),
            )
        }

        if (!event.reward.isNullOrBlank()) {
            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color(0xFFF8F8F8))
                    .padding(
                        horizontal = 8.dp,
                        vertical = 5.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "🎟",
                    fontSize = 14.sp,
                )

                Spacer(
                    modifier = Modifier.width(3.dp)
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

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "잔여수량",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                lineHeight = 16.sp,
                color = Color(0xFF595959),
            )

            Text(
                text = "${remaining}/${event.participantLimit}명",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color =
                    if (isSoldOut) {
                        Color(0xFF8F8F8F)
                    } else {
                        Primary
                    },
            )
        }

        Spacer(
            modifier = Modifier.height(4.dp)
        )

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
                    .fillMaxWidth(
                        progress.coerceIn(0f, 1f)
                    )
                    .clip(CircleShape)
                    .background(
                        if (isOpen) {
                            Primary
                        } else {
                            Color(0xFF8E8E8E)
                        }
                    ),
            )
        }
    }
}

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

@Composable
private fun Footer() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9F9F9))
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "2026 OLLYMOA. All rights reserved.",
            fontFamily = Pretendard,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            color = Color(0xFF8F8F8F),
        )
    }
}

@Composable
private fun PlaceholderEventDetail(
    eventId: Int,
    eventType: String,
    onBackClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "‹",
                fontFamily = Pretendard,
                fontSize = 34.sp,
                color = Color(0xFF121212),
                modifier = Modifier
                    .clickable {
                        onBackClick()
                    }
            )

            Spacer(
                modifier = Modifier.width(20.dp)
            )

            Text(
                text = when (eventType) {
                    "raffle" -> "추첨 이벤트"
                    "experience" -> "체험 이벤트"
                    else -> "선착순 이벤트"
                },
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF121212),
            )
        }

        HorizontalDivider(
            color = Color(0xFFE2E2E2),
            thickness = 1.dp,
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "이벤트 상세 #$eventId",
                fontFamily = Pretendard,
                fontSize = 16.sp,
                color = Color(0xFF8F8F8F),
                textAlign = TextAlign.Center,
            )
        }
    }
}
