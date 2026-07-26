package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.alphacity.stamptour.BuildConfig
import com.alphacity.stamptour.R
import com.alphacity.stamptour.network.dto.MyCouponItem
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.ui.theme.Primary
import com.alphacity.stamptour.viewmodel.MyCouponsViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun MyCouponsScreen(
    onBackClick: () -> Unit,
    viewModel: MyCouponsViewModel = hiltViewModel(),
) {
    val myCoupons by viewModel.myCoupons.collectAsState()
    val useSuccess by viewModel.useSuccess.collectAsState()
    val useError by viewModel.useError.collectAsState()
    var selectedCoupon by remember { mutableStateOf<MyCouponItem?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchMyCoupons()
    }

    // 사용 성공 시 알림
    if (useSuccess) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { viewModel.clearUseSuccess() },
            title = { Text("알림", fontFamily = com.alphacity.stamptour.ui.theme.Pretendard, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold) },
            text = { Text("쿠폰이 사용되었습니다.", fontFamily = com.alphacity.stamptour.ui.theme.Pretendard) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { viewModel.clearUseSuccess() }) {
                    Text("확인", fontFamily = com.alphacity.stamptour.ui.theme.Pretendard, color = Primary)
                }
            },
        )
    }

    // 사용 실패 시 알림
    useError?.let { error ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { viewModel.clearUseError() },
            title = { Text("오류", fontFamily = com.alphacity.stamptour.ui.theme.Pretendard, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold) },
            text = { Text(error, fontFamily = com.alphacity.stamptour.ui.theme.Pretendard) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { viewModel.clearUseError() }) {
                    Text("확인", fontFamily = com.alphacity.stamptour.ui.theme.Pretendard, color = Primary)
                }
            },
        )
    }

    // issued 상태 + 유효기간 남은 쿠폰만 표시
    val availableCoupons = myCoupons.filter { coupon ->
        coupon.status == "issued" && try {
            val expiry = coupon.validUntil.substring(0, 10)
            expiry >= java.time.LocalDate.now().toString()
        } catch (_: Exception) { true }
    }

    Box(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
        ) {
            // === Header ===
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icon_back_arrow),
                    contentDescription = "뒤로",
                    modifier = Modifier
                        .size(width = 13.dp, height = 26.dp)
                        .clickable(onClick = onBackClick),
                    contentScale = ContentScale.Fit,
                )
                Spacer(modifier = Modifier.width(24.dp))
                Text(
                    text = "쿠폰 보관함",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color(0xFF121212),
                )
            }
            Divider(color = Color(0xFFE2E2E2), thickness = 1.dp)

            // 빈 상태에서는 스크롤을 끄고 안내 문구를 남은 공간 세로 중앙에 배치
            val couponScrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .then(
                        if (availableCoupons.isEmpty()) Modifier
                        else Modifier.verticalScroll(couponScrollState)
                    ),
            ) {
                // === Blue Info Card ===
                Box(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(top = 30.dp)
                        .fillMaxWidth()
                        .height(185.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFFEDF7FF)),
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxSize(),
                    ) {
                        Text(
                            text = "나의 보유 쿠폰",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = Color(0xFF121212),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "사용 시에는 해당 장소에서 고유 코드를 제시해주세요.",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color(0xFF3D608D),
                        )
                    }
                    Text(
                        text = "${availableCoupons.size}개",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Bold,
                        fontSize = 35.sp,
                        color = Primary,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(20.dp),
                    )
                }

                // === CTA Button ===
                Box(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF6092FF), Color(0xFF2563EB), Color(0xFF1551D3))
                            )
                        )
                        .clickable(onClick = onBackClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Row {
                        Text(
                            text = "추가 쿠폰 받으러 가기",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = Color.White,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = ">",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = Color.White,
                        )
                    }
                }

                // === Section Title ===
                Text(
                    text = "내 쿠폰",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color(0xFF121212),
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(top = 24.dp),
                )

                // === Coupon Cards ===
                if (availableCoupons.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "사용 가능한 쿠폰이 없습니다.",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Color(0xFF9CA3AF),
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        availableCoupons.forEach { coupon ->
                            MyCouponCardItem(coupon = coupon) { selectedCoupon = coupon }
                        }
                    }
                }
            }

            // === Footer (하단 고정) ===
            Text(
                text = "2026 OLLYMOA. All rights reserved.",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = Color(0xFF999999),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9F9F9))
                    .padding(vertical = 24.dp),
            )
        }

        // === Bottom Sheet Overlay ===
        selectedCoupon?.let { coupon ->
            MyCouponDetailBottomSheet(
                coupon = coupon,
                onDismiss = { selectedCoupon = null },
                onUseCoupon = {
                    viewModel.useCoupon(coupon.id)
                    selectedCoupon = null
                },
            )
        }
    }
}

// Custom shape for coupon card right side decoration
private val CouponDecoShape = object : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.35f, 0f)
            lineTo(w, 0f)
            lineTo(w, h)
            lineTo(w * 0.35f, h)
            cubicTo(w * 0.0f, h * 0.85f, w * 0.0f, h * 0.65f, w * 0.15f, h * 0.5f)
            cubicTo(w * 0.3f, h * 0.35f, w * 0.15f, h * 0.15f, w * 0.35f, 0f)
            close()
        }
        return Outline.Generic(path)
    }
}

private fun formatCouponValidUntil(raw: String): String {
    return try {
        val date = LocalDateTime.parse(raw, DateTimeFormatter.ISO_DATE_TIME)
        date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")) + "까지 사용 가능"
    } catch (_: Exception) {
        try {
            val date = java.time.LocalDate.parse(raw.take(10))
            date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")) + "까지 사용 가능"
        } catch (_: Exception) {
            raw
        }
    }
}

@Composable
private fun MyCouponCardItem(coupon: MyCouponItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(134.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(Color.White)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left: Thumbnail
            Box(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(85.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF8F8F8)),
                contentAlignment = Alignment.Center,
            ) {
                if (!coupon.imageUrl.isNullOrBlank()) {
                    val fullUrl = if (coupon.imageUrl.startsWith("http")) {
                        coupon.imageUrl
                    } else {
                        BuildConfig.SERVER_URL + coupon.imageUrl
                    }
                    AsyncImage(
                        model = fullUrl,
                        contentDescription = coupon.name,
                        modifier = Modifier.fillMaxSize(0.7f),
                        contentScale = ContentScale.Fit,
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.icon_coupon),
                        contentDescription = null,
                        modifier = Modifier.size(52.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
            }

            // Middle: Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 18.dp, end = 8.dp),
            ) {
                Text(
                    text = coupon.name,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color(0xFF121212),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!coupon.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = coupon.description,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = Primary,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatCouponValidUntil(coupon.validUntil),
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color(0xFF8F8F8F),
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color(0xFFF8F8F8))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                )
            }

            // Right: Blue decoration with QR icon
            Box(
                modifier = Modifier
                    .width(82.dp)
                    .fillMaxHeight()
                    .clip(CouponDecoShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF4B8BF5), Primary)
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode2,
                    contentDescription = "QR",
                    tint = Color.White,
                    modifier = Modifier.size(35.dp),
                )
            }
        }
    }
}

@Composable
private fun MyCouponDetailBottomSheet(
    coupon: MyCouponItem,
    onDismiss: () -> Unit,
    onUseCoupon: () -> Unit,
) {
    // Dimmed overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.61f))
            .clickable(onClick = onDismiss),
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Handle
            Box(
                modifier = Modifier
                    .padding(top = 19.dp)
                    .width(65.dp)
                    .height(7.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0xFFD9D9D9)),
            )

            // Store name
            Text(
                text = coupon.storeName ?: coupon.name,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color(0xFF121212),
                modifier = Modifier.padding(top = 24.dp),
            )

            // Benefit
            Text(
                text = coupon.description ?: coupon.name,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Color(0xFF121212),
                modifier = Modifier.padding(top = 4.dp),
            )

            // Code box
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp)
                    .fillMaxWidth()
                    .height(86.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFEDF7FF)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = coupon.code,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Bold,
                    fontSize = 42.sp,
                    color = Primary,
                )
            }

            // Notice
            Text(
                text = "이 화면을 쿠폰 사용처 관계자에게 보여주세요.",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color(0xFF8F8F8F),
                modifier = Modifier.padding(top = 16.dp),
            )

            // Use button
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp)
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF6092FF), Color(0xFF2563EB), Color(0xFF1551D3))
                        )
                    )
                    .clickable(onClick = onUseCoupon),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "쿠폰 사용하기",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.White,
                )
            }

            // Caution section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .background(Color(0xFFF8F8F8))
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                Text(
                    text = "\u26A0 쿠폰 사용 시 주의해주세요.",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    color = Color(0xFF8F8F8F),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "- 본 쿠폰은 유효기간 내에만 사용 가능합니다.\n" +
                        "- 쿠폰은 1회 사용 원칙이며, 재사용 불가합니다.\n" +
                        "- 현금 교환 및 환불은 불가합니다.\n" +
                        "- 타 쿠폰과 중복 사용이 불가할 수 있습니다.\n" +
                        "- 알파시티 축제 고객센터 : 053-123-4567\n" +
                        "- 쿠폰 사용 조건은 사용처 사정에 따라 변경될 수 있습니다.",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    color = Color(0xFF8F8F8F),
                    lineHeight = 16.sp,
                )
            }
        }
    }
}
