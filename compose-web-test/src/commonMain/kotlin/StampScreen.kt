
package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alphacity.stamptour.network.ApiService
import com.alphacity.stamptour.network.dto.FestivalItem
import com.alphacity.stamptour.network.dto.MissionItem
import com.alphacity.stamptour.network.dto.StampItem
import com.alphacity.stamptour.network.dto.UserStampItem
import com.alphacity.stamptour.repository.HomeRepository
import com.alphacity.stamptour.repository.StampRepository
import com.alphacity.stamptour.web.WebTokenManager

private val Primary = Color(0xFF02CDF8)
private val Pretendard = FontFamily.SansSerif

@Composable
fun StampScreen(
    onNavigateToMap: (lat: Double?, lng: Double?) -> Unit = { _, _ -> },
    onNavigateToExchange: () -> Unit = {},
    onNavigateToMyPage: () -> Unit = {},
    showBack: Boolean = false,
    onBack: () -> Unit = {},
) {
    val stampRepository = remember {
        StampRepository(ApiService)
    }

    val homeRepository = remember {
        HomeRepository()
    }

    var festivals by remember {
        mutableStateOf<List<FestivalItem>>(emptyList())
    }

    var selectedFestivalId by remember {
        mutableStateOf<Int?>(null)
    }

    var stamps by remember {
        mutableStateOf<List<StampItem>>(emptyList())
    }

    var missions by remember {
        mutableStateOf<List<MissionItem>>(emptyList())
    }

    var userStamps by remember {
        mutableStateOf<List<UserStampItem>>(emptyList())
    }

    /*
     * 축제 목록 조회
     */
    LaunchedEffect(Unit) {
        homeRepository.getFestivals()
            .onSuccess {
                festivals = it
            }
            .onFailure {
                println(
                    "[StampScreen] 축제 조회 실패: ${it.message}"
                )
            }
    }

    /*
     * 선택된 축제 기준으로
     * 스탬프 / 미션 / 내 스탬프 조회
     */
    LaunchedEffect(selectedFestivalId) {
        stamps = emptyList()
        missions = emptyList()
        userStamps = emptyList()

        /*
         * 전체 스탬프
         *
         * 중요:
         * 선택한 축제의 스탬프가 0개라고 해서
         * getStamps(null)을 다시 호출하지 않는다.
         *
         * 선택된 축제에 실제 등록된 데이터만 표시한다.
         */
        stampRepository.getStamps(selectedFestivalId)
            .onSuccess { list ->
                stamps = list

                println(
                    "[StampScreen] 스탬프 조회 성공: ${list.size}개"
                )
            }
            .onFailure {
                println(
                    "[StampScreen] 스탬프 조회 실패: ${it.message}"
                )
            }

        /*
         * 선택된 축제의 미션
         */
        stampRepository.getMissions(selectedFestivalId)
            .onSuccess {
                missions = it

                println(
                    "[StampScreen] 미션 조회 성공: ${it.size}개"
                )
            }
            .onFailure {
                println(
                    "[StampScreen] 미션 조회 실패: ${it.message}"
                )
            }

        /*
         * 로그인 사용자만 내 스탬프 조회
         */
        if (WebTokenManager.isLoggedIn()) {
            stampRepository.getUserStamps(selectedFestivalId)
                .onSuccess {
                    userStamps = it

                    println(
                        "[StampScreen] 내 스탬프 조회 성공: ${it.size}개"
                    )
                }
                .onFailure {
                    println(
                        "[StampScreen] 유저 스탬프 조회 실패: ${it.message}"
                    )
                }
        }
    }

    /*
     * 선택된 축제 이름
     */
    val selectedFestivalName =
        festivals
            .find { it.id == selectedFestivalId }
            ?.name
            ?: "전체 축제"

    /*
     * 내가 획득한 스탬프 ID
     *
     * 같은 stampId가 여러 번 내려와도
     * 하나의 스탬프로 처리한다.
     */
    val collectedStampIds =
        userStamps
            .map { it.stampId }
            .toSet()

    /*
     * 실제 획득한 스탬프 개수
     *
     * userStamps.size가 아니라
     * stampId 기준 중복 제거한 개수를 사용한다.
     */
    val userStampCount =
        collectedStampIds.size

    /*
     * 실제 등록된 전체 스탬프 개수
     *
     * 더 이상 maxOf(..., 1)을 사용하지 않는다.
     */
    val totalStampCount =
        stamps.size

    /*
     * 스탬프 진행률
     */
    val progress =
        if (totalStampCount > 0) {
            (
                    userStampCount.toFloat() /
                            totalStampCount.toFloat()
                    ).coerceIn(0f, 1f)
        } else {
            0f
        }

    val percentText =
        "${(progress * 100).toInt()}%"

    /*
     * 완료된 미션
     */
    val completedMissionCount =
        missions.count {
            it.isCompleted == true
        }

    /*
     * 남은 미션
     */
    val remainingMissionCount =
        missions.size - completedMissionCount

    /*
     * 스탬프 클릭
     *
     * 해당 스탬프에 연결된 미션의 장소 좌표로
     * 지도 이동
     */
    val onStampClick: (StampItem) -> Unit = { stamp ->

        val linkedMission =
            missions.find {
                it.stampId == stamp.id
            }

        onNavigateToMap(
            linkedMission?.place?.latitude,
            linkedMission?.place?.longitude,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        StampHeader(
            showBack = showBack,
            onBack = onBack,
            onProfileClick = onNavigateToMyPage,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                ),
        ) {
            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text(
                text = "스탬프 컬렉션",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color(0xFF121212),
                modifier = Modifier.padding(
                    horizontal = 20.dp
                ),
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "수성 수성페스티벌 스탬프를 모아 진행 상황을 확인하세요.",
                fontFamily = Pretendard,
                fontSize = 14.sp,
                color = Color(0xFF121212),
                modifier = Modifier.padding(
                    horizontal = 20.dp
                ),
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            FestivalDropdown(
                festivals = festivals,
                selectedName = selectedFestivalName,
                selectedId = selectedFestivalId,
                onSelect = {
                    selectedFestivalId = it
                },
                modifier = Modifier.padding(
                    horizontal = 20.dp
                ),
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            StampProgressCard(
                progress = progress,
                percentText = percentText,
                userStampCount = userStampCount,
                totalStampCount = totalStampCount,
                completedMissionCount = completedMissionCount,
                remainingMissionCount = remainingMissionCount,
                totalMissionCount = missions.size,
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            RewardButton(
                onClick = onNavigateToExchange
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEDF7FF))
                    .padding(
                        top = 24.dp,
                        bottom = 32.dp,
                    ),
            ) {
                Text(
                    text = "스탬프 현황",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color(0xFF121212),
                    modifier = Modifier.padding(
                        horizontal = 20.dp
                    ),
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                StampGrid(
                    stamps = stamps,
                    collectedStampIds = collectedStampIds,
                    onStampClick = onStampClick,
                )
            }

            MissionListSection(
                missions = missions
            )

            StampHistorySection(
                stamps = stamps,
                userStamps = userStamps,
                onStampClick = onStampClick,
            )
        }
    }
}

@Composable
private fun StampHeader(
    showBack: Boolean,
    onBack: () -> Unit,
    onProfileClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(
                start = 15.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = 12.dp,
            ),
        verticalAlignment =
            Alignment.CenterVertically,
    ) {
        if (showBack) {
            Text(
                text = "←",
                fontFamily = Pretendard,
                fontSize = 34.sp,
                color = Color(0xFF121212),
                modifier = Modifier.clickable {
                    onBack()
                },
            )

            Spacer(
                modifier = Modifier.width(14.dp)
            )

            Text(
                text = "스탬프 컬렉션",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF121212),
            )

            Spacer(
                modifier = Modifier.weight(1f)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(
                        RoundedCornerShape(10.dp)
                    )
                    .background(
                        Color(0xFFDCF6FF)
                    ),
                contentAlignment =
                    Alignment.Center,
            ) {
                Text(
                    text = "O",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    color = Primary,
                )
            }

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Text(
                text = "스탬프투어",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF121212),
            )

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(
                        1.dp,
                        Color(0xFFEBEBEB),
                        CircleShape,
                    )
                    .background(
                        Color(0xFFF8F8F8)
                    )
                    .clickable {
                        onProfileClick()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "👤",
                    fontSize = 18.sp,
                )
            }
        }
    }

    HorizontalDivider(
        color = Color(0xFFE2E2E2),
        thickness = 1.dp,
    )
}

@Composable
private fun StampProgressCard(
    progress: Float,
    percentText: String,
    userStampCount: Int,
    totalStampCount: Int,
    totalMissionCount: Int,
    completedMissionCount: Int,
    remainingMissionCount: Int,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(
                RoundedCornerShape(22.dp)
            )
            .background(
                Color(0xFFEDF7FF)
            ),
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 20.dp,
                vertical = 24.dp,
            ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "나의 스탬프 진행률",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Color(0xFF121212),
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        text = "차곡차곡 모아 다양한 리워드를 만나보세요.",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color(0xFF3D608D),
                    )
                }

                Text(
                    text = percentText,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Bold,
                    fontSize = 35.sp,
                    color = Primary,
                )
            }

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(47.dp),
            ) {
                val filledWidth =
                    maxWidth * progress

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .align(
                            Alignment.BottomStart
                        )
                        .clip(
                            RoundedCornerShape(100.dp)
                        )
                        .background(
                            Color.White
                        ),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .clip(
                                RoundedCornerShape(
                                    100.dp
                                )
                            )
                            .background(
                                Color(0xFF4C27D0)
                            ),
                    )
                }

                Text(
                    text = "📍",
                    fontSize = 28.sp,
                    modifier = Modifier.offset(
                        x = filledWidth - 15.dp,
                        y = 0.dp,
                    ),
                )
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceEvenly,
            ) {
                StatItem(
                    label = "획득 스탬프",
                    value = "$userStampCount",
                    total = "/ $totalStampCount",
                    color = Color(0xFF2563EB),
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(
                            Color(0xFFD0D5DD)
                        ),
                )

                StatItem(
                    label = "완료 미션",
                    value = "$completedMissionCount",
                    total = "/ $totalMissionCount",
                    color = Color(0xFF16A34A),
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(
                            Color(0xFFD0D5DD)
                        ),
                )

                StatItem(
                    label = "남은 미션",
                    value = "$remainingMissionCount",
                    total = "",
                    color = Color(0xFFEA580C),
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    total: String,
    color: Color,
) {
    Column(
        horizontalAlignment =
            Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = Color(0xFF6B7280),
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Row(
            verticalAlignment =
                Alignment.Bottom,
        ) {
            Text(
                text = value,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = color,
            )

            if (total.isNotEmpty()) {
                Text(
                    text = total,
                    fontFamily = Pretendard,
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF),
                )
            }
        }
    }
}

@Composable
private fun RewardButton(
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(56.dp)
            .clip(
                RoundedCornerShape(8.dp)
            )
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF6092FF),
                        Color(0xFF2563EB),
                        Color(0xFF1551D3),
                    )
                )
            )
            .clickable {
                onClick()
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment =
                Alignment.CenterVertically,
        ) {
            Text(
                text = "🎁",
                fontSize = 18.sp,
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Text(
                text = "스탬프로 리워드 교환하러 가기",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color.White,
            )

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Text(
                text = ">",
                fontSize = 16.sp,
                color = Color.White,
            )
        }
    }
}

/*
 * 실제 등록된 스탬프 개수만큼 동적으로 표시한다.
 *
 * 기존:
 *   4행 × 3열 = 무조건 12칸
 *
 * 변경:
 *   DB에서 내려온 stamps 개수만큼 표시
 *
 * 예:
 *   5개 → 3개 + 2개
 *   12개 → 3개씩 4행
 *   15개 → 3개씩 5행
 *
 * 등록된 스탬프인데 아직 획득하지 않은 경우에는
 * StampSlot에서 🔒로 표시한다.
 *
 * DB에 존재하지 않는 가짜 placeholder는 만들지 않는다.
 */
@Composable
private fun StampGrid(
    stamps: List<StampItem>,
    collectedStampIds: Set<Int>,
    onStampClick: (StampItem) -> Unit,
) {
    if (stamps.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp
                )
                .clip(
                    RoundedCornerShape(16.dp)
                )
                .background(Color.White)
                .padding(
                    vertical = 32.dp
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "등록된 스탬프가 없습니다.",
                fontFamily = Pretendard,
                fontSize = 14.sp,
                color = Color(0xFF9CA3AF),
            )
        }

        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        stamps
            .chunked(3)
            .forEach { rowStamps ->

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(19.dp),
                ) {
                    rowStamps.forEach { stamp ->

                        StampSlot(
                            stamp = stamp,
                            collected =
                                stamp.id in collectedStampIds,
                            modifier =
                                Modifier.weight(1f),
                            onClick = {
                                onStampClick(stamp)
                            },
                        )
                    }

                    /*
                     * 마지막 줄에 3개보다 적은 경우
                     * 남은 공간만큼 빈 영역을 유지해서
                     * 기존 3열 레이아웃을 유지한다.
                     */
                    repeat(3 - rowStamps.size) {
                        Spacer(
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
    }
}

@Composable
private fun StampSlot(
    stamp: StampItem,
    collected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clickable {
                onClick()
            },
        horizontalAlignment =
            Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(
                    RoundedCornerShape(35.dp)
                )
                .background(Color.White),
            contentAlignment =
                Alignment.Center,
        ) {
            Text(
                text =
                    if (collected) {
                        "🏆"
                    } else {
                        "🔒"
                    },
                fontSize = 42.sp,
            )

            if (!collected) {
                Text(
                    text = "MISSION CLEAR",
                    fontFamily = Pretendard,
                    fontSize = 8.sp,
                    color = Color(0xFFB5B5B5),
                    modifier = Modifier.padding(
                        top = 55.dp
                    ),
                )
            }
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = stamp.name,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color(0xFF121212),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun MissionListSection(
    missions: List<MissionItem>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(
                top = 24.dp,
                bottom = 16.dp,
            ),
    ) {
        Text(
            text = "스탬프 미션",
            fontFamily = Pretendard,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = Color(0xFF121212),
            modifier = Modifier.padding(
                horizontal = 20.dp
            ),
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = "미션을 완료하고 스탬프를 획득하세요.",
            fontFamily = Pretendard,
            fontSize = 13.sp,
            color = Color(0xFF9CA3AF),
            modifier = Modifier.padding(
                horizontal = 20.dp
            ),
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        if (missions.isEmpty()) {
            Text(
                text = "등록된 미션이 없습니다.",
                fontFamily = Pretendard,
                fontSize = 14.sp,
                color = Color(0xFF9CA3AF),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 16.dp,
                    ),
                textAlign = TextAlign.Center,
            )

            return
        }

        missions.forEach { mission ->

            val completed =
                mission.isCompleted == true

            val missionType =
                when (mission.type) {
                    "quiz" -> "퀴즈"
                    "location_auth" -> "위치인증"
                    "stay_time" -> "체류시간"
                    else -> mission.type
                }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 4.dp,
                    )
                    .clip(
                        RoundedCornerShape(12.dp)
                    )
                    .background(
                        if (completed) {
                            Color(0xFFF0FDF4)
                        } else {
                            Color(0xFFF8FAFC)
                        }
                    )
                    .padding(16.dp),
                verticalAlignment =
                    Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (completed) {
                                Color(0xFF16A34A)
                            } else {
                                Color(0xFF2563EB)
                            }
                        ),
                    contentAlignment =
                        Alignment.Center,
                ) {
                    Text(
                        text =
                            if (completed) {
                                "✓"
                            } else {
                                "!"
                            },
                        color = Color.White,
                        fontWeight =
                            FontWeight.Bold,
                    )
                }

                Spacer(
                    modifier = Modifier.width(12.dp)
                )

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {
                    Text(
                        text = mission.name,
                        fontFamily = Pretendard,
                        fontWeight =
                            FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color(0xFF121212),
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Row {
                        Text(
                            text = missionType,
                            fontFamily = Pretendard,
                            fontSize = 11.sp,
                            color = Color(0xFF6B7280),
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(4.dp)
                                )
                                .background(
                                    Color(0xFFE5E7EB)
                                )
                                .padding(
                                    horizontal = 6.dp,
                                    vertical = 2.dp,
                                ),
                        )

                        mission.place?.name?.let {
                            Spacer(
                                modifier = Modifier.width(6.dp)
                            )

                            Text(
                                text = it,
                                fontFamily = Pretendard,
                                fontSize = 12.sp,
                                color = Color(0xFF9CA3AF),
                            )
                        }
                    }
                }

                Text(
                    text =
                        if (completed) {
                            "완료"
                        } else {
                            "진행 중"
                        },
                    fontFamily = Pretendard,
                    fontWeight =
                        FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color =
                        if (completed) {
                            Color(0xFF16A34A)
                        } else {
                            Color(0xFF2563EB)
                        },
                )
            }
        }
    }
}

@Composable
private fun StampHistorySection(
    stamps: List<StampItem>,
    userStamps: List<UserStampItem>,
    onStampClick: (StampItem) -> Unit,
) {
    /*
     * stampId 기준으로 하나의 획득 정보만 사용한다.
     */
    val userStampMap =
        userStamps
            .distinctBy { it.stampId }
            .associateBy {
                it.stampId
            }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFFEDF7FF)
            )
            .padding(
                top = 24.dp,
                bottom = 32.dp,
            ),
    ) {
        Text(
            text = "스탬프 획득 이력",
            fontFamily = Pretendard,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = Color(0xFF121212),
            modifier = Modifier.padding(
                horizontal = 20.dp
            ),
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        if (stamps.isEmpty()) {
            Text(
                text = "등록된 스탬프가 없습니다.",
                fontFamily = Pretendard,
                fontSize = 14.sp,
                color = Color(0xFF9CA3AF),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 16.dp,
                    ),
                textAlign = TextAlign.Center,
            )

            return
        }

        /*
         * 획득한 스탬프를 위쪽으로 정렬하고
         * 미획득 스탬프를 아래쪽으로 정렬한다.
         */
        stamps
            .sortedByDescending {
                it.id in userStampMap
            }
            .forEach { stamp ->

                val userStamp =
                    userStampMap[stamp.id]

                val collected =
                    userStamp != null

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 20.dp,
                            vertical = 4.dp,
                        )
                        .clip(
                            RoundedCornerShape(12.dp)
                        )
                        .background(Color.White)
                        .clickable {
                            onStampClick(stamp)
                        }
                        .padding(14.dp),
                    verticalAlignment =
                        Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(
                                RoundedCornerShape(12.dp)
                            )
                            .background(
                                if (collected) {
                                    Color(0xFFDBEAFE)
                                } else {
                                    Color(0xFFF3F4F6)
                                }
                            ),
                        contentAlignment =
                            Alignment.Center,
                    ) {
                        Text(
                            text =
                                if (collected) {
                                    "🏆"
                                } else {
                                    "🔒"
                                },
                            fontSize = 22.sp,
                        )
                    }

                    Spacer(
                        modifier = Modifier.width(12.dp)
                    )

                    Column(
                        modifier =
                            Modifier.weight(1f)
                    ) {
                        Text(
                            text = stamp.name,
                            fontFamily = Pretendard,
                            fontWeight =
                                FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color =
                                if (collected) {
                                    Color(0xFF121212)
                                } else {
                                    Color(0xFF9CA3AF)
                                },
                        )

                        Spacer(
                            modifier = Modifier.height(2.dp)
                        )

                        Text(
                            text =
                                if (collected) {
                                    formatCollectedAt(
                                        userStamp?.collectedAt
                                    )
                                } else {
                                    "미션 완료 후 획득 미션 확인"
                                },
                            fontFamily = Pretendard,
                            fontSize = 12.sp,
                            color =
                                if (collected) {
                                    Color(0xFF9CA3AF)
                                } else {
                                    Color(0xFF2563EB)
                                },
                        )
                    }

                    Text(
                        text =
                            if (collected) {
                                "획득"
                            } else {
                                "미획득"
                            },
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color =
                            if (collected) {
                                Color(0xFF16A34A)
                            } else {
                                Color(0xFFEA580C)
                            },
                    )
                }
            }
    }
}

private fun formatCollectedAt(
    collectedAt: String?,
): String {
    if (collectedAt.isNullOrBlank()) {
        return "획득 완료"
    }

    return collectedAt
        .take(10)
        .replace("-", ".")
}

@Composable
private fun FestivalDropdown(
    festivals: List<FestivalItem>,
    selectedName: String,
    selectedId: Int?,
    onSelect: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    Color(0xFFD0D5DD),
                    RoundedCornerShape(10.dp),
                )
                .clickable {
                    expanded = !expanded
                }
                .padding(
                    horizontal = 14.dp,
                    vertical = 12.dp,
                ),
            verticalAlignment =
                Alignment.CenterVertically,
        ) {
            Text(
                text = "축제 선택",
                fontFamily = Pretendard,
                fontSize = 13.sp,
                color = Color(0xFF8F8F8F),
            )

            Spacer(
                modifier = Modifier.width(10.dp)
            )

            Text(
                text = selectedName,
                fontFamily = Pretendard,
                fontWeight =
                    FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color(0xFF121212),
                modifier =
                    Modifier.weight(1f),
            )

            Text(
                text =
                    if (expanded) {
                        "▲"
                    } else {
                        "▼"
                    },
                fontSize = 12.sp,
                color = Color(0xFF8F8F8F),
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
        ) {
            DropdownMenuItem(
                text = {
                    Text("전체 축제")
                },
                onClick = {
                    onSelect(null)
                    expanded = false
                },
            )

            festivals.forEach { festival ->
                DropdownMenuItem(
                    text = {
                        Text(festival.name)
                    },
                    onClick = {
                        onSelect(festival.id)
                        expanded = false
                    },
                )
            }
        }
    }
}
