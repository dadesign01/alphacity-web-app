package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.alphacity.stamptour.BuildConfig
import com.alphacity.stamptour.network.dto.StoreData
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.ui.theme.Primary

private val CATEGORY_MAP = mapOf(
    "cafe" to "카페",
    "restaurant" to "음식점",
    "shopping" to "쇼핑",
    "hotel" to "호텔",
    "convenience" to "편의시설",
)

private val MISSION_TYPE_MAP = mapOf(
    "quiz" to "퀴즈",
    "location_auth" to "위치인증",
    "stay_time" to "체류시간",
)

private val GradientGreen = Brush.linearGradient(
    colors = listOf(Color(0xFF22C55E), Color(0xFF16A34A), Color(0xFF15803D)),
    start = Offset(0f, 0f),
    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
)

@Composable
fun StoreDetailScreen(
    store: StoreData,
    onBackClick: () -> Unit = {},
    onNavigateToMap: (lat: Double, lng: Double) -> Unit = { _, _ -> },
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "뒤로",
                    tint = Color(0xFF121212),
                )
            }
            Text(
                text = "상점 상세",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF121212),
            )
        }

        Divider(color = Color(0xFFE2E2E2), thickness = 1.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Store image
            if (store.imageUrl != null) {
                val fullImageUrl = if (store.imageUrl!!.startsWith("http")) store.imageUrl else BuildConfig.SERVER_URL + store.imageUrl
                AsyncImage(
                    model = fullImageUrl,
                    contentDescription = store.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop,
                )
            }

            // Store name + category badge
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = store.name,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color(0xFF121212),
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF16A34A).copy(alpha = 0.1f),
                    ) {
                        Text(
                            text = CATEGORY_MAP[store.category] ?: store.category,
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color(0xFF16A34A),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }

                if (store.program != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Primary.copy(alpha = 0.1f),
                    ) {
                        Text(
                            text = store.program.name,
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = Primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            // Info section
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (store.address != null) {
                        StoreInfoRow(
                            icon = Icons.Default.LocationOn,
                            label = "주소",
                            value = "${store.address}${store.addressDetail?.let { " $it" } ?: ""}",
                        )
                    }

                    if (store.operatingDays != null || store.openTime != null) {
                        val operatingText = buildString {
                            if (store.operatingDays != null) append(store.operatingDays)
                            if (store.openTime != null && store.closeTime != null) {
                                if (isNotEmpty()) append(" ")
                                append("${store.openTime} ~ ${store.closeTime}")
                            }
                        }
                        StoreInfoRow(
                            icon = Icons.Outlined.Schedule,
                            label = "운영 시간",
                            value = operatingText,
                        )
                    }

                    StoreInfoRow(
                        icon = Icons.Default.Phone,
                        label = "연락처",
                        value = store.phone,
                    )
                }
            }

            // Description
            if (store.description != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                    ) {
                        Text(
                            text = "상점 소개",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color(0xFF121212),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = store.description,
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            lineHeight = 22.sp,
                        )
                    }
                }
            }

            // Connected mission
            if (store.mission != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFEDF7FF),
                    shadowElevation = 2.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "연결 미션",
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = Color(0xFF121212),
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Primary.copy(alpha = 0.15f),
                            ) {
                                Text(
                                    text = MISSION_TYPE_MAP[store.mission.type] ?: store.mission.type,
                                    fontFamily = Pretendard,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = Primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                )
                            }
                        }
                        Text(
                            text = store.mission.name,
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Color(0xFF3D608D),
                        )
                    }
                }
            }

            // Connected coupons
            if (!store.storeCoupons.isNullOrEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "사용 가능 쿠폰",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color(0xFF121212),
                        )
                        store.storeCoupons.forEach { sc ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFFF7ED),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                ) {
                                    Text(
                                        text = sc.coupon.name,
                                        fontFamily = Pretendard,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = Color(0xFFEA580C),
                                    )
                                    if (sc.coupon.description != null) {
                                        Text(
                                            text = sc.coupon.description,
                                            fontFamily = Pretendard,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 12.sp,
                                            color = Color(0xFF8F8F8F),
                                            modifier = Modifier.padding(top = 4.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Navigate to map button
            if (store.latitude != null && store.longitude != null) {
                Button(
                    onClick = { onNavigateToMap(store.latitude, store.longitude) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "지도에서 보기",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.White,
                    )
                }
            }

            // Footer
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun StoreInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF9CA3AF),
            modifier = Modifier.size(20.dp),
        )
        Column {
            Text(
                text = label,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF),
            )
            Text(
                text = value,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color(0xFF121212),
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
