import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Primary = Color(0xFF02CDF8)
private val Pretendard = FontFamily.SansSerif

private val categoryMap = mapOf(
    "cafe" to "카페",
    "restaurant" to "음식점",
    "shopping" to "쇼핑",
    "hotel" to "호텔",
    "convenience" to "편의시설",
)

private val missionTypeMap = mapOf(
    "quiz" to "퀴즈",
    "location_auth" to "위치인증",
    "stay_time" to "체류시간",
)

private data class DemoCoupon(
    val name: String,
    val description: String?,
)

private data class StoreDemoMission(
    val name: String,
    val type: String,
)

private data class DemoStore(
    val name: String,
    val category: String,
    val address: String?,
    val addressDetail: String?,
    val phone: String,
    val operatingDays: String?,
    val openTime: String?,
    val closeTime: String?,
    val description: String?,
    val mission: StoreDemoMission?,
    val coupons: List<DemoCoupon>,
)

@Composable
fun StoreDetailScreen(
    store: Any,
    onBackClick: () -> Unit = {},
    onNavigateToMap: (lat: Double, lng: Double) -> Unit = { _, _ -> },
) {
    val demoStore = DemoStore(
        name = "대구광역시 대표 매장",
        category = "cafe",
        address = "대구광역시 수성구 대구광역시",
        addressDetail = "대구광역시 2로 33",
        phone = "053-123-4567",
        operatingDays = "월~금",
        openTime = "09:00",
        closeTime = "20:00",
        description = "수성구 대구광역시에서 다양한 음료와 간단한 메뉴를 즐길 수 있는 매장입니다.",
        mission = StoreDemoMission(
            name = "매장 방문 인증 미션",
            type = "location_auth",
        ),
        coupons = listOf(
            DemoCoupon(
                name = "아메리카노 1잔 할인",
                description = "아메리카노 1,000원 할인",
            ),
            DemoCoupon(
                name = "음료 사이즈업",
                description = "음료 무료 사이즈업",
            ),
        ),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "←",
                fontFamily = Pretendard,
                fontSize = 34.sp,
                color = Color(0xFF121212),
                modifier = Modifier.clickable {
                    onBackClick()
                },
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Text(
                text = "상점 상세",
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(20.dp),
            verticalArrangement =
                Arrangement.spacedBy(16.dp),
        ) {
            // Store image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(
                        RoundedCornerShape(16.dp)
                    )
                    .background(Color(0xFFE8EEF2)),
                contentAlignment =
                    Alignment.Center,
            ) {
                Text(
                    text = "🏪",
                    fontSize = 58.sp,
                )
            }

            // Store name + category
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    verticalAlignment =
                        Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = demoStore.name,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color(0xFF121212),
                    )

                    Box(
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(6.dp)
                            )
                            .background(
                                Color(0xFF16A34A).copy(
                                    alpha = 0.1f
                                )
                            )
                            .padding(
                                horizontal = 10.dp,
                                vertical = 4.dp,
                            ),
                    ) {
                        Text(
                            text =
                                categoryMap[
                                    demoStore.category
                                ] ?: demoStore.category,
                            fontFamily = Pretendard,
                            fontWeight =
                                FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color =
                                Color(0xFF16A34A),
                        )
                    }
                }

                demoStore.mission?.let { mission ->
                    Box(
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(6.dp)
                            )
                            .background(
                                Primary.copy(
                                    alpha = 0.1f
                                )
                            )
                            .padding(
                                horizontal = 10.dp,
                                vertical = 4.dp,
                            ),
                    ) {
                        Text(
                            text = mission.name,
                            fontFamily = Pretendard,
                            fontWeight =
                                FontWeight.Medium,
                            fontSize = 12.sp,
                            color = Primary,
                        )
                    }
                }
            }

            // Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(16.dp)
                    )
                    .background(Color.White)
                    .padding(20.dp),
                verticalArrangement =
                    Arrangement.spacedBy(12.dp),
            ) {
                demoStore.address?.let {
                    StoreInfoRow(
                        icon = "📍",
                        label = "주소",
                        value =
                            "${it}${demoStore.addressDetail?.let { detail ->
                                " $detail"
                            } ?: ""}",
                    )
                }

                if (
                    demoStore.operatingDays != null ||
                    demoStore.openTime != null
                ) {
                    val operatingText =
                        buildString {
                            demoStore.operatingDays?.let {
                                append(it)
                            }

                            if (
                                demoStore.openTime != null &&
                                demoStore.closeTime != null
                            ) {
                                if (isNotEmpty()) {
                                    append(" ")
                                }

                                append(
                                    "${demoStore.openTime} ~ ${demoStore.closeTime}"
                                )
                            }
                        }

                    StoreInfoRow(
                        icon = "🕐",
                        label = "영업 시간",
                        value = operatingText,
                    )
                }

                StoreInfoRow(
                    icon = "☎️",
                    label = "연락처",
                    value = demoStore.phone,
                )
            }

            // Description
            demoStore.description?.let { description ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(
                            RoundedCornerShape(16.dp)
                        )
                        .background(Color.White)
                        .padding(20.dp),
                ) {
                    Text(
                        text = "상점 소개",
                        fontFamily = Pretendard,
                        fontWeight =
                            FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color(0xFF121212),
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = description,
                        fontFamily = Pretendard,
                        fontWeight =
                            FontWeight.Normal,
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        lineHeight = 22.sp,
                    )
                }
            }

            // Connected mission
            demoStore.mission?.let { mission ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(
                            RoundedCornerShape(16.dp)
                        )
                        .background(
                            Color(0xFFEDF7FF)
                        )
                        .padding(20.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "연결 미션",
                            fontFamily = Pretendard,
                            fontWeight =
                                FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color(0xFF121212),
                        )

                        Box(
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(6.dp)
                                )
                                .background(
                                    Primary.copy(
                                        alpha = 0.15f
                                    )
                                )
                                .padding(
                                    horizontal = 8.dp,
                                    vertical = 3.dp,
                                ),
                        ) {
                            Text(
                                text =
                                    missionTypeMap[
                                        mission.type
                                    ] ?: mission.type,
                                fontFamily =
                                    Pretendard,
                                fontWeight =
                                    FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = Primary,
                            )
                        }
                    }

                    Text(
                        text = mission.name,
                        fontFamily = Pretendard,
                        fontWeight =
                            FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color(0xFF3D608D),
                    )
                }
            }

            // Coupons
            if (demoStore.coupons.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(
                            RoundedCornerShape(16.dp)
                        )
                        .background(Color.White)
                        .padding(20.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "사용 가능 쿠폰",
                        fontFamily = Pretendard,
                        fontWeight =
                            FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color(0xFF121212),
                    )

                    demoStore.coupons.forEach { coupon ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(
                                    RoundedCornerShape(12.dp)
                                )
                                .background(
                                    Color(0xFFFFF7ED)
                                )
                                .padding(16.dp),
                        ) {
                            Text(
                                text = coupon.name,
                                fontFamily = Pretendard,
                                fontWeight =
                                    FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color(0xFFEA580C),
                            )

                            coupon.description?.let {
                                Spacer(
                                    modifier =
                                        Modifier.height(4.dp)
                                )

                                Text(
                                    text = it,
                                    fontFamily =
                                        Pretendard,
                                    fontSize = 12.sp,
                                    color =
                                        Color(0xFF8F8F8F),
                                )
                            }
                        }
                    }
                }
            }

            // Map button
            Button(
                onClick = {
                    onNavigateToMap(
                        35.842,
                        128.690,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape =
                    RoundedCornerShape(12.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color(0xFF16A34A)
                    ),
            ) {
                Text(
                    text = "📍  지도에서 보기",
                    fontFamily = Pretendard,
                    fontWeight =
                        FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.White,
                )
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )
        }
    }
}

@Composable
private fun StoreInfoRow(
    icon: String,
    label: String,
    value: String,
) {
    Row(
        verticalAlignment =
            Alignment.Top,
        horizontalArrangement =
            Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = icon,
            fontSize = 18.sp,
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
                modifier = Modifier.padding(
                    top = 2.dp
                ),
            )
        }
    }
}