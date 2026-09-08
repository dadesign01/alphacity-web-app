package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alphacity.stamptour.network.ApiService
import com.alphacity.stamptour.network.dto.ActivityItemDto
import com.alphacity.stamptour.web.WebTokenManager

private val Primary = Color(0xFF02CDF8)
private val Pretendard = FontFamily.SansSerif

enum class ActivityType(val label: String) {
    STAMP("스탬프"),
    MISSION("미션"),
    EVENT("이벤트"),
    COUPON("쿠폰"),
    COUPON_USED("쿠폰 사용"),
    COUPON_EXPIRED("쿠폰 만료"),
}

data class ActivityItem(
    val type: ActivityType,
    val title: String,
    val datetime: String,
    val validUntil: String? = null,
)

enum class ActivityFilter(
    val label: String,
    val apiType: String,
) {
    STAMP("스탬프", "stamp"),
    MISSION("미션", "mission"),
    EVENT("행사", "event"),
    COUPON("쿠폰 교환", "coupon"),
}

@Composable
fun ActivityHistoryScreen(
    onBackClick: () -> Unit = {},
) {
    var selectedFilter by remember {
        mutableStateOf<ActivityFilter?>(null)
    }

    var activities by remember {
        mutableStateOf<List<ActivityItem>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null

        if (!WebTokenManager.isLoggedIn()) {
            activities = emptyList()
            errorMessage = "로그인이 필요합니다."
            isLoading = false
            return@LaunchedEffect
        }

        val response = ApiService.getActivityHistory()

        if (response.success) {
            activities = response.data
                ?.mapNotNull { it.toActivityItem() }
                ?.sortedByDescending { it.datetime }
                ?: emptyList()
        } else {
            activities = emptyList()
            errorMessage = response.error?.message
                ?: "활동 이력을 불러오지 못했습니다."
        }

        isLoading = false
    }

    val filteredActivities = remember(
        selectedFilter,
        activities,
    ) {
        if (selectedFilter == null) {
            activities
        } else {
            activities.filter {
                when (selectedFilter) {
                    ActivityFilter.STAMP ->
                        it.type == ActivityType.STAMP

                    ActivityFilter.MISSION ->
                        it.type == ActivityType.MISSION

                    ActivityFilter.EVENT ->
                        it.type == ActivityType.EVENT

                    ActivityFilter.COUPON ->
                        it.type == ActivityType.COUPON ||
                                it.type == ActivityType.COUPON_USED ||
                                it.type == ActivityType.COUPON_EXPIRED

                    null -> true
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(50.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "←",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 34.sp,
                color = Color(0xFF121212),
                modifier = Modifier
                    .size(width = 13.dp, height = 26.dp)
                    .clickable {
                        onBackClick()
                    },
            )

            Spacer(
                modifier = Modifier.width(24.dp)
            )

            Text(
                text = "활동 이력",
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

        // Filter Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 25.dp,
                    vertical = 12.dp,
                ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ActivityFilter.entries.forEach { filter ->
                val isSelected = selectedFilter == filter

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
                            selectedFilter =
                                if (isSelected) {
                                    null
                                } else {
                                    filter
                                }
                        }
                        .padding(
                            horizontal = 14.dp,
                            vertical = 7.dp,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    ActivityFilterIcon(
                        filter = filter,
                        selected = isSelected,
                    )

                    Text(
                        text = filter.label,
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

        // Activity List
        if (isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    color = Primary,
                )
            }
        } else {
            val historyScrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .then(
                        if (filteredActivities.isEmpty()) {
                            Modifier
                        } else {
                            Modifier.verticalScroll(
                                historyScrollState
                            )
                        }
                    ),
            ) {
                if (filteredActivities.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = errorMessage
                                ?: "활동 이력이 없습니다.",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = Color(0xFF8F8F8F),
                        )
                    }
                } else {
                    filteredActivities.forEach { activity ->
                        ActivityItemRow(
                            activity = activity,
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(
                                horizontal = 20.dp
                            ),
                            color = Color(0xFFB5B5B5),
                            thickness = 0.5.dp,
                        )
                    }
                }
            }

            // Footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9F9F9))
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center,
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
    }
}

private fun ActivityItemDto.toActivityItem(): ActivityItem? {
    val activityType = when (type.lowercase()) {
        "stamp" -> ActivityType.STAMP
        "mission" -> ActivityType.MISSION
        "event" -> ActivityType.EVENT
        "coupon" -> {
            when (subType?.lowercase()) {
                "used" -> ActivityType.COUPON_USED
                "expired" -> ActivityType.COUPON_EXPIRED
                "redeemed" -> ActivityType.COUPON
                else -> ActivityType.COUPON
            }
        }

        else -> return null
    }

    return ActivityItem(
        type = activityType,
        title = title,
        datetime = formatActivityDate(date),
        validUntil = validUntil?.takeIf {
            it.isNotBlank()
        },
    )
}

private fun formatActivityDate(
    value: String,
): String {
    return try {
        val datePart = value
            .substringBefore("T")
            .replace("-", ".")

        val timePart = value
            .substringAfter("T", "")
            .substringBefore(".")
            .substringBefore("Z")

        if (timePart.length >= 5) {
            "$datePart ${timePart.substring(0, 5)}"
        } else {
            datePart
        }
    } catch (_: Exception) {
        value
    }
}

@Composable
private fun ActivityFilterIcon(
    filter: ActivityFilter,
    selected: Boolean,
) {
    val backgroundColor = if (selected) {
        Color.White.copy(alpha = 0.2f)
    } else {
        Color(0xFFEAEAEA)
    }

    val textColor = if (selected) {
        Color.White
    } else {
        Color(0xFF595959)
    }

    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = when (filter) {
                ActivityFilter.STAMP -> "S"
                ActivityFilter.MISSION -> "M"
                ActivityFilter.EVENT -> "E"
                ActivityFilter.COUPON -> "C"
            },
            fontFamily = Pretendard,
            fontWeight = FontWeight.Bold,
            fontSize = 8.sp,
            color = textColor,
        )
    }
}

@Composable
private fun ActivityItemRow(
    activity: ActivityItem,
) {
    val iconText = when (activity.type) {
        ActivityType.STAMP -> "S"
        ActivityType.MISSION -> "M"
        ActivityType.EVENT -> "E"
        ActivityType.COUPON,
        ActivityType.COUPON_USED,
        ActivityType.COUPON_EXPIRED -> "C"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 20.dp,
                vertical = 16.dp,
            ),
        verticalAlignment = Alignment.Top,
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(85.dp)
                .clip(CircleShape)
                .background(Color(0xFFF8F8F8)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        when (activity.type) {
                            ActivityType.STAMP ->
                                Primary

                            ActivityType.MISSION ->
                                Color(0xFF8B5CF6)

                            ActivityType.EVENT ->
                                Color(0xFFFF781C)

                            ActivityType.COUPON ->
                                Primary

                            ActivityType.COUPON_USED ->
                                Color(0xFF16A34A)

                            ActivityType.COUPON_EXPIRED ->
                                Color(0xFF9CA3AF)
                        }
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = iconText,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White,
                )
            }
        }

        Spacer(
            modifier = Modifier.width(20.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
        ) {
            if (
                activity.type == ActivityType.COUPON ||
                activity.type == ActivityType.COUPON_USED ||
                activity.type == ActivityType.COUPON_EXPIRED
            ) {
                val badgeColor = when (activity.type) {
                    ActivityType.COUPON_EXPIRED ->
                        Color(0xFF9CA3AF)

                    ActivityType.COUPON_USED ->
                        Color(0xFF16A34A)

                    else ->
                        Primary
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(badgeColor)
                        .padding(
                            horizontal = 10.dp,
                            vertical = 3.dp,
                        ),
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
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .border(
                            width = 1.dp,
                            color = Primary,
                            shape = RoundedCornerShape(100.dp),
                        )
                        .padding(
                            horizontal = 10.dp,
                            vertical = 3.dp,
                        ),
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

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = activity.title,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color(0xFF121212),
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            Text(
                text = "활동 일시 : ${activity.datetime}",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = Color(0xFF595959),
            )

            if (activity.validUntil != null) {
                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color(0xFFF8F8F8))
                        .padding(
                            horizontal = 10.dp,
                            vertical = 5.dp,
                        ),
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