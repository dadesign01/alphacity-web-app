package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alphacity.stamptour.R
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.ui.theme.Primary

enum class ActivityType(val label: String) {
    STAMP("스탬프"),
    MISSION("미 션"),
    EVENT("행 사"),
    COUPON("교  환"),
}

data class ActivityItem(
    val id: Int,
    val type: ActivityType,
    val title: String,
    val datetime: String,
    val validUntil: String? = null,
)

enum class ActivityFilter(val label: String) {
    STAMP("스탬프"),
    MISSION("미션"),
    EVENT("행사"),
    COUPON("쿠폰 교환"),
}

private fun getMockActivities(): List<ActivityItem> = listOf(
    ActivityItem(1, ActivityType.COUPON, "스탬프 3개 쿠폰 교환", "2026.01.28 16:55"),
    ActivityItem(2, ActivityType.STAMP, "디지털 갤러리 스탬프 획득", "2026.01.28 15:30", "2026년 03월 31일까지 사용 가능"),
    ActivityItem(3, ActivityType.MISSION, "디지털 갤러리 퀴즈 미션 완료", "2026.01.28 15:30"),
    ActivityItem(4, ActivityType.EVENT, "디지털 아트 전시회 참여", "2026.01.28 12:23"),
    ActivityItem(5, ActivityType.STAMP, "VR 스튜디오 스탬프 획득", "2026.01.28 12:00", "2026년 03월 31일까지 사용 가능"),
    ActivityItem(6, ActivityType.STAMP, "AI 스터디 스탬프 획득", "2026.01.28 11:48", "2026년 03월 31일까지 사용 가능"),
)

@Composable
fun ActivityHistoryScreen(
    onBackClick: () -> Unit,
) {
    var selectedFilter by remember { mutableStateOf<ActivityFilter?>(null) }
    val allActivities = remember { getMockActivities() }
    val filteredActivities = remember(selectedFilter) {
        if (selectedFilter == null) {
            allActivities
        } else {
            allActivities.filter {
                when (selectedFilter) {
                    ActivityFilter.STAMP -> it.type == ActivityType.STAMP
                    ActivityFilter.MISSION -> it.type == ActivityType.MISSION
                    ActivityFilter.EVENT -> it.type == ActivityType.EVENT
                    ActivityFilter.COUPON -> it.type == ActivityType.COUPON
                    null -> true
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding(),
    ) {
        // === Header ===
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(50.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(id = R.drawable.icon_back_arrow),
                contentDescription = "뒤로",
                modifier = Modifier
                    .size(13.dp, 26.dp)
                    .clickable(onClick = onBackClick),
                contentScale = ContentScale.Fit,
            )
            Spacer(modifier = Modifier.width(24.dp))
            Text(
                text = "활동 이력",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF121212),
            )
        }
        Divider(color = Color(0xFFE2E2E2), thickness = 1.dp)

        // === Filter Tabs ===
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 25.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ActivityFilter.entries.forEach { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(11.dp))
                        .background(if (isSelected) Color(0xFF121212) else Color(0xFFF8F8F8))
                        .clickable {
                            selectedFilter = if (isSelected) null else filter
                        }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = filter.label,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = if (isSelected) Color.White else Color(0xFF121212),
                    )
                }
            }
        }

        // === Activity List ===
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            items(filteredActivities) { activity ->
                ActivityItemRow(activity = activity)
                Divider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = Color(0xFFB5B5B5),
                    thickness = 0.5.dp,
                )
            }

            // Footer
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF9F9F9))
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center,
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
}

@Composable
private fun ActivityItemRow(activity: ActivityItem) {
    val iconRes = when (activity.type) {
        ActivityType.STAMP -> R.drawable.activity_stamp
        ActivityType.MISSION -> R.drawable.activity_mission
        ActivityType.EVENT -> R.drawable.activity_event
        ActivityType.COUPON -> R.drawable.activity_coupon
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // Thumbnail circle
        Box(
            modifier = Modifier
                .size(85.dp)
                .clip(CircleShape)
                .background(Color(0xFFF8F8F8)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(54.dp),
                contentScale = ContentScale.Fit,
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        // Content
        Column(modifier = Modifier.weight(1f)) {
            // Badge
            if (activity.type == ActivityType.COUPON) {
                // Filled badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(Primary)
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = activity.type.label,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = Color.White,
                    )
                }
            } else {
                // Outlined badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .border(1.dp, Primary, RoundedCornerShape(100.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = activity.type.label,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = Primary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Title
            Text(
                text = activity.title,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color(0xFF121212),
            )

            Spacer(modifier = Modifier.height(2.dp))

            // DateTime
            Text(
                text = "활동 일시 : ${activity.datetime}",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = Color(0xFF595959),
            )

            // Validity info
            if (activity.validUntil != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color(0xFFF8F8F8))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                ) {
                    Text(
                        text = activity.validUntil,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Color(0xFF8F8F8F),
                    )
                }
            }
        }
    }
}
