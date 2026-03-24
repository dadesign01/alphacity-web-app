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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.alphacity.stamptour.R
import com.alphacity.stamptour.network.dto.ActivityItemDto
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.ui.theme.Primary
import com.alphacity.stamptour.viewmodel.ActivityHistoryViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

enum class ActivityType(val label: String) {
    STAMP("스탬프"),
    MISSION("미 션"),
    EVENT("행 사"),
    COUPON("교  환"),
    COUPON_USED("사  용"),
    COUPON_EXPIRED("미사용"),
}

data class ActivityItem(
    val type: ActivityType,
    val title: String,
    val datetime: String,
    val validUntil: String? = null,
)

enum class ActivityFilter(val label: String, val apiType: String) {
    STAMP("스탬프", "stamp"),
    MISSION("미션", "mission"),
    EVENT("행사", "event"),
    COUPON("쿠폰 교환", "coupon"),
}

private fun ActivityItemDto.toActivityItem(): ActivityItem {
    val activityType = when (type) {
        "stamp" -> ActivityType.STAMP
        "mission" -> ActivityType.MISSION
        "event" -> ActivityType.EVENT
        "coupon" -> when (subType) {
            "used" -> ActivityType.COUPON_USED
            "expired" -> ActivityType.COUPON_EXPIRED
            else -> ActivityType.COUPON
        }
        else -> ActivityType.STAMP
    }
    val formattedDate = try {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        isoFormat.timeZone = TimeZone.getTimeZone("UTC")
        val parsed = isoFormat.parse(date)
        val displayFormat = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
        if (parsed != null) displayFormat.format(parsed) else date
    } catch (_: Exception) {
        date
    }
    return ActivityItem(
        type = activityType,
        title = title,
        datetime = formattedDate,
        validUntil = validUntil,
    )
}

@Composable
fun ActivityHistoryScreen(
    onBackClick: () -> Unit,
    viewModel: ActivityHistoryViewModel = hiltViewModel(),
) {
    var selectedFilter by remember { mutableStateOf<ActivityFilter?>(null) }
    val activitiesDto by viewModel.activities.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchActivities()
    }

    val allActivities = remember(activitiesDto) {
        activitiesDto.map { it.toActivityItem() }
    }
    val filteredActivities = remember(selectedFilter, allActivities) {
        if (selectedFilter == null) {
            allActivities
        } else {
            allActivities.filter {
                when (selectedFilter) {
                    ActivityFilter.STAMP -> it.type == ActivityType.STAMP
                    ActivityFilter.MISSION -> it.type == ActivityType.MISSION
                    ActivityFilter.EVENT -> it.type == ActivityType.EVENT
                    ActivityFilter.COUPON -> it.type == ActivityType.COUPON || it.type == ActivityType.COUPON_USED || it.type == ActivityType.COUPON_EXPIRED
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
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
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
}

@Composable
private fun ActivityItemRow(activity: ActivityItem) {
    val iconRes = when (activity.type) {
        ActivityType.STAMP -> R.drawable.activity_stamp
        ActivityType.MISSION -> R.drawable.activity_mission
        ActivityType.EVENT -> R.drawable.activity_event
        ActivityType.COUPON, ActivityType.COUPON_USED, ActivityType.COUPON_EXPIRED -> R.drawable.activity_coupon
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
            if (activity.type == ActivityType.COUPON || activity.type == ActivityType.COUPON_USED || activity.type == ActivityType.COUPON_EXPIRED) {
                // Filled badge
                val badgeColor = when (activity.type) {
                    ActivityType.COUPON_EXPIRED -> Color(0xFF9CA3AF)
                    ActivityType.COUPON_USED -> Color(0xFF16A34A)
                    else -> Primary
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(badgeColor)
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
