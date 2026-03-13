package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
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
import com.alphacity.stamptour.network.dto.MissionItem
import com.alphacity.stamptour.network.dto.StampItem
import com.alphacity.stamptour.network.dto.UserStampItem
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.ui.theme.Primary
import com.alphacity.stamptour.viewmodel.StampViewModel

@Composable
fun StampScreen(
    viewModel: StampViewModel = hiltViewModel(),
    onNavigateToMap: (lat: Double?, lng: Double?) -> Unit = { _, _ -> },
    onNavigateToExchange: () -> Unit = {},
) {
    val stamps by viewModel.stamps.collectAsState()
    val userStampCount by viewModel.userStampCount.collectAsState()
    val totalStampCount by viewModel.totalStampCount.collectAsState()
    val collectedStampIds by viewModel.collectedStampIds.collectAsState()
    val missions by viewModel.missions.collectAsState()
    val userStamps by viewModel.userStamps.collectAsState()

    // 탭 전환 시마다 최신 데이터 로드
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.fetchStampData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val progress = if (totalStampCount > 0) {
        (userStampCount.toFloat() / totalStampCount).coerceIn(0f, 1f)
    } else 0f
    val percentText = "${(progress * 100).toInt()}%"

    val completedMissionCount = missions.count { it.isCompleted == true }
    val remainingMissionCount = missions.size - completedMissionCount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        // Header
        StampHeader()

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // Title Section
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "스탬프 컬렉션",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color(0xFF121212),
                letterSpacing = (-0.48).sp,
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "수성 알파시티 스탬프 투어의 진행 현황을 확인하세요.",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = Color(0xFF121212),
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Progress Card
            StampProgressCard(
                progress = progress,
                percentText = percentText,
                userStampCount = userStampCount,
                totalStampCount = totalStampCount,
                completedMissionCount = completedMissionCount,
                remainingMissionCount = remainingMissionCount,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Reward Button
            RewardButton(onClick = onNavigateToExchange)

            Spacer(modifier = Modifier.height(20.dp))

            // Stamp Grid Section (light blue background)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEDF7FF))
                    .padding(top = 24.dp, bottom = 32.dp),
            ) {
                Text(
                    text = "스탬프 현황",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color(0xFF121212),
                    letterSpacing = (-0.36).sp,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stamp Grid
                StampGrid(
                    stamps = stamps,
                    collectedStampIds = collectedStampIds,
                    onUncollectedClick = { stamp ->
                        onNavigateToMap(null, null)
                    },
                )
            }

            // Mission List Section
            if (missions.isNotEmpty()) {
                MissionListSection(missions = missions)
            }

            // Stamp History Section
            if (stamps.isNotEmpty()) {
                StampHistorySection(
                    stamps = stamps,
                    collectedStampIds = collectedStampIds,
                    userStamps = userStamps,
                    onUncollectedClick = { stamp ->
                        onNavigateToMap(null, null)
                    },
                )
            }

            // Footer
            Text(
                text = "\u00A9 2026 Alpha Stamp. All rights reserved.",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                color = Color(0xFFAFBFCC),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEDF7FF))
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}

// MARK: - Header

@Composable
private fun StampHeader() {
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
                .border(1.dp, Color(0xFFEBEBEB), CircleShape),
        ) {
            Image(
                painter = painterResource(id = R.drawable.icon_profile),
                contentDescription = "프로필",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }

    Divider(color = Color(0xFFE2E2E2), thickness = 1.dp)
}

// MARK: - Progress Card (반원 데코레이션 포함)

@Composable
private fun StampProgressCard(
    progress: Float,
    percentText: String,
    userStampCount: Int = 0,
    totalStampCount: Int = 0,
    completedMissionCount: Int = 0,
    remainingMissionCount: Int = 0,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFFEDF7FF)),
    ) {
        // 반원 데코레이션 (Figma: 두 개의 큰 원, opacity 0.27, gradient)
        Canvas(
            modifier = Modifier.matchParentSize(),
        ) {
            val w = size.width
            val h = size.height
            // Figma 기준: 카드 362x185, 원 619x740.91
            val scaleX = w / 362f
            val scaleY = h / 185f

            // 왼쪽 하단 원 (Figma: x=-204, y=87.49, 619x740.91)
            val circle1Size = Size(619f * scaleX, 740.91f * scaleY)
            drawOval(
                brush = Brush.linearGradient(
                    colorStops = arrayOf(
                        0.15f to Color(0x002563EB),
                        0.88f to Color(0xFF2563EB),
                    ),
                    start = Offset(circle1Size.width * 0.3f, circle1Size.height),
                    end = Offset(circle1Size.width * 0.7f, 0f),
                ),
                topLeft = Offset(-204f * scaleX, 87.49f * scaleY),
                size = circle1Size,
                alpha = 0.27f,
            )

            // 오른쪽 상단 원 (Figma: x=42, y=-98.42, 619x740.91)
            val circle2Size = Size(619f * scaleX, 740.91f * scaleY)
            drawOval(
                brush = Brush.linearGradient(
                    colorStops = arrayOf(
                        0.59f to Color(0x002563EB),
                        0.75f to Color(0xFF2563EB),
                    ),
                    start = Offset(0f, circle2Size.height * 0.5f),
                    end = Offset(circle2Size.width, circle2Size.height * 0.2f),
                ),
                topLeft = Offset(42f * scaleX, -98.42f * scaleY),
                size = circle2Size,
                alpha = 0.27f,
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "나의 스탬프 진행률",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Color(0xFF121212),
                        letterSpacing = (-0.36).sp,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "차곡차곡 모아 다양한 리워드를 만나보세요!",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color(0xFF3D608D),
                        letterSpacing = (-0.26).sp,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = percentText,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Bold,
                    fontSize = 35.sp,
                    color = Primary,
                    letterSpacing = (-0.7).sp,
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Progress bar with character
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth().height(47.dp),
            ) {
                val barWidth = maxWidth
                val filledWidth = barWidth * progress

                // Bar track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .align(Alignment.BottomStart)
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color.White),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color(0xFF4C27D0)),
                    )
                }

                // Character on bar
                Image(
                    painter = painterResource(id = R.drawable.walk),
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

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
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
                        .background(Color(0xFFD0D5DD)),
                )
                StatItem(
                    label = "완료 미션",
                    value = "$completedMissionCount",
                    total = "/ ${completedMissionCount + remainingMissionCount}",
                    color = Color(0xFF16A34A),
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(Color(0xFFD0D5DD)),
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
private fun StatItem(label: String, value: String, total: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = Color(0xFF6B7280),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
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
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.padding(start = 2.dp, bottom = 2.dp),
                )
            }
        }
    }
}

// MARK: - Reward Button (피그마 아이콘 사용)

@Composable
private fun RewardButton(onClick: () -> Unit = {}) {
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF6092FF),
            Color(0xFF2563EB),
            Color(0xFF1551D3),
        ),
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(gradientBrush)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(id = R.drawable.icon_reward_exchange),
                contentDescription = "리워드",
                modifier = Modifier.size(18.dp),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "스탬프 리워드 교환하러 가기",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color.White,
                letterSpacing = (-0.32).sp,
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = ">",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color.White,
            )
        }
    }
}

// MARK: - Stamp Grid (트로피/메달 구분, 자물쇠)

@Composable
private fun StampGrid(
    stamps: List<StampItem>,
    collectedStampIds: Set<Int>,
    onUncollectedClick: (StampItem) -> Unit = {},
) {
    val chunkedStamps = stamps.chunked(3)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        chunkedStamps.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(19.dp),
            ) {
                row.forEach { stamp ->
                    val isCollected = collectedStampIds.contains(stamp.id)

                    StampSlot(
                        stamp = stamp,
                        isCollected = isCollected,
                        modifier = Modifier.weight(1f),
                        onClick = if (!isCollected) {
                            { onUncollectedClick(stamp) }
                        } else null,
                    )
                }

                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StampSlot(
    stamp: StampItem,
    isCollected: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    // conditionType에 따라 다른 이미지
    val fallbackRes = when (stamp.conditionType) {
        "mission_complete", "place_visit" -> R.drawable.stamp_trophy
        "event_participate", "quiz_correct" -> R.drawable.stamp_medal
        else -> R.drawable.stamp_trophy
    }

    Column(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(35.dp),
                    ambientColor = Color(0x0D000000),
                    spotColor = Color(0x0D000000),
                )
                .clip(RoundedCornerShape(35.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            if (isCollected) {
                // 수집된 스탬프: admin imageUrl 우선, 없으면 conditionType별 fallback
                if (!stamp.imageUrl.isNullOrBlank()) {
                    val fullUrl = if (stamp.imageUrl.startsWith("http")) {
                        stamp.imageUrl
                    } else {
                        BuildConfig.SERVER_URL + stamp.imageUrl
                    }
                    AsyncImage(
                        model = fullUrl,
                        contentDescription = stamp.name,
                        modifier = Modifier.fillMaxSize(0.65f),
                        contentScale = ContentScale.Fit,
                    )
                } else {
                    Image(
                        painter = painterResource(id = fallbackRes),
                        contentDescription = stamp.name,
                        modifier = Modifier.fillMaxSize(0.65f),
                        contentScale = ContentScale.Fit,
                    )
                }
            } else {
                // 미수집 스탬프: 자물쇠 + MISSION CLEAR (Figma: opacity 0.28)
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.stamp_locked),
                        contentDescription = "잠김",
                        modifier = Modifier.size(29.dp, 40.dp),
                        contentScale = ContentScale.Fit,
                        alpha = 0.28f,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "MISSION CLEAR",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 8.sp,
                        color = Color(0xFF121212).copy(alpha = 0.6f * 0.28f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stamp.name,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color(0xFF121212),
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

// MARK: - Mission List Section

@Composable
private fun MissionListSection(missions: List<MissionItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(top = 24.dp, bottom = 16.dp),
    ) {
        Text(
            text = "스탬프 미션",
            fontFamily = Pretendard,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = Color(0xFF121212),
            letterSpacing = (-0.36).sp,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "미션을 완료하고 스탬프를 획득하세요",
            fontFamily = Pretendard,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            color = Color(0xFF9CA3AF),
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        missions.forEach { mission ->
            MissionCard(mission = mission)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MissionCard(mission: MissionItem) {
    val isCompleted = mission.isCompleted == true
    val typeLabel = when (mission.type) {
        "quiz" -> "퀴즈"
        "location_auth" -> "위치인증"
        "stay_time" -> "체류시간"
        else -> mission.type
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isCompleted) Color(0xFFF0FDF4) else Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Status icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isCompleted) Color(0xFF16A34A) else Color(0xFF2563EB)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (isCompleted) "✓" else "!",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mission.name,
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Color(0xFF121212),
                maxLines = 1,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Type badge
                Text(
                    text = typeLabel,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = Color(0xFF6B7280),
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE5E7EB))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )

                if (mission.place != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = mission.place.name,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF),
                        maxLines = 1,
                    )
                }
            }
        }

        // Status badge
        Text(
            text = if (isCompleted) "완료" else "진행중",
            fontFamily = Pretendard,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = if (isCompleted) Color(0xFF16A34A) else Color(0xFF2563EB),
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isCompleted) Color(0xFFDCFCE7) else Color(0xFFDBEAFE))
                .padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

// MARK: - Stamp History Section

@Composable
private fun StampHistorySection(
    stamps: List<StampItem>,
    collectedStampIds: Set<Int>,
    userStamps: List<UserStampItem>,
    onUncollectedClick: (StampItem) -> Unit = {},
) {
    val userStampMap = userStamps.associateBy { it.stampId }

    // 수집된 스탬프 먼저, 미수집 스탬프 나중에
    val sortedStamps = stamps.sortedByDescending { collectedStampIds.contains(it.id) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEDF7FF))
            .padding(top = 24.dp, bottom = 32.dp),
    ) {
        Text(
            text = "스탬프 획득 이력",
            fontFamily = Pretendard,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = Color(0xFF121212),
            letterSpacing = (-0.36).sp,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        sortedStamps.forEach { stamp ->
            val isCollected = collectedStampIds.contains(stamp.id)
            val userStamp = userStampMap[stamp.id]

            StampHistoryRow(
                stamp = stamp,
                isCollected = isCollected,
                collectedAt = userStamp?.collectedAt,
                onClick = if (!isCollected) {
                    { onUncollectedClick(stamp) }
                } else null,
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun StampHistoryRow(
    stamp: StampItem,
    isCollected: Boolean,
    collectedAt: String?,
    onClick: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Stamp icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isCollected) Color(0xFFDBEAFE) else Color(0xFFF3F4F6)),
            contentAlignment = Alignment.Center,
        ) {
            if (isCollected && !stamp.imageUrl.isNullOrBlank()) {
                val fullUrl = if (stamp.imageUrl.startsWith("http")) {
                    stamp.imageUrl
                } else {
                    BuildConfig.SERVER_URL + stamp.imageUrl
                }
                AsyncImage(
                    model = fullUrl,
                    contentDescription = stamp.name,
                    modifier = Modifier.fillMaxSize(0.7f),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Image(
                    painter = painterResource(
                        id = if (isCollected) R.drawable.stamp_trophy else R.drawable.stamp_locked,
                    ),
                    contentDescription = stamp.name,
                    modifier = Modifier.size(24.dp),
                    contentScale = ContentScale.Fit,
                    alpha = if (isCollected) 1f else 0.4f,
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stamp.name,
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = if (isCollected) Color(0xFF121212) else Color(0xFF9CA3AF),
            )

            Spacer(modifier = Modifier.height(2.dp))

            if (isCollected && collectedAt != null) {
                Text(
                    text = collectedAt.take(10),
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF),
                )
            } else if (!isCollected) {
                Text(
                    text = "미획득 · 탭하여 지도에서 확인",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = Color(0xFF2563EB),
                )
            }
        }

        if (isCollected) {
            Text(
                text = "획득",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = Color(0xFF16A34A),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFDCFCE7))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        } else {
            Text(
                text = "미획득",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = Color(0xFFEA580C),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFFFF7ED))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
    }
}
