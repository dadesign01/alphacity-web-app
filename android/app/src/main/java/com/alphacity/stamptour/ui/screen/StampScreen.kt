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
import com.alphacity.stamptour.network.dto.StampItem
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.ui.theme.Primary
import com.alphacity.stamptour.viewmodel.StampViewModel

@Composable
fun StampScreen(
    viewModel: StampViewModel = hiltViewModel(),
) {
    val stamps by viewModel.stamps.collectAsState()
    val userStampCount by viewModel.userStampCount.collectAsState()
    val totalStampCount by viewModel.totalStampCount.collectAsState()
    val collectedStampIds by viewModel.collectedStampIds.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchStampData()
    }

    val progress = if (totalStampCount > 0) {
        (userStampCount.toFloat() / totalStampCount).coerceIn(0f, 1f)
    } else 0f
    val percentText = "${(progress * 100).toInt()}%"

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
            // Title Section - 둘 다 left 정렬
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
                text = "축제 현장에서 미션에 참여해 스탬프를 획득해보세요.",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = Color(0xFF121212),
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Progress Card
            StampProgressCard(progress, percentText)

            Spacer(modifier = Modifier.height(12.dp))

            // Reward Button
            RewardButton()

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
                StampGrid(stamps = stamps, collectedStampIds = collectedStampIds)
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
private fun StampProgressCard(progress: Float, percentText: String) {
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
        }
    }
}

// MARK: - Reward Button (피그마 아이콘 사용)

@Composable
private fun RewardButton() {
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
            .clickable { },
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
private fun StampGrid(stamps: List<StampItem>, collectedStampIds: Set<Int>) {
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
) {
    // conditionType에 따라 다른 이미지
    val fallbackRes = when (stamp.conditionType) {
        "mission_complete", "place_visit" -> R.drawable.stamp_trophy
        "event_participate", "quiz_correct" -> R.drawable.stamp_medal
        else -> R.drawable.stamp_trophy
    }

    Column(
        modifier = modifier,
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
