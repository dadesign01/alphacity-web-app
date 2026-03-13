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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.alphacity.stamptour.BuildConfig
import com.alphacity.stamptour.R
import com.alphacity.stamptour.network.dto.CouponItem
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.ui.theme.Primary
import com.alphacity.stamptour.viewmodel.RedeemSuccess
import com.alphacity.stamptour.viewmodel.StampViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun StampExchangeScreen(
    onBackClick: () -> Unit,
    onNavigateToCoupons: () -> Unit = {},
    viewModel: StampViewModel = hiltViewModel(),
) {
    val coupons by viewModel.coupons.collectAsState()
    val userStampCount by viewModel.userStampCount.collectAsState()
    val isRedeeming by viewModel.isRedeeming.collectAsState()
    val redeemSuccess by viewModel.redeemSuccess.collectAsState()
    val redeemError by viewModel.redeemError.collectAsState()

    var showConfirmDialog by remember { mutableStateOf<CouponItem?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchStampData()
    }

    // 교환 확인 팝업
    showConfirmDialog?.let { coupon ->
        CouponConfirmDialog(
            coupon = coupon,
            isRedeeming = isRedeeming,
            onDismiss = { showConfirmDialog = null },
            onConfirm = {
                viewModel.redeemCoupon(coupon.id)
                showConfirmDialog = null
            },
        )
    }

    // 교환 완료 팝업
    redeemSuccess?.let { success ->
        CouponSuccessDialog(
            success = success,
            onDismiss = { viewModel.clearRedeemSuccess() },
            onNavigateToCoupons = {
                viewModel.clearRedeemSuccess()
                onNavigateToCoupons()
            },
        )
    }

    // 교환 실패 팝업
    redeemError?.let { error ->
        CouponErrorDialog(
            message = error,
            onDismiss = { viewModel.clearRedeemError() },
        )
    }

    val exchangeableCouponCount = coupons.count { userStampCount >= it.requiredStamps }

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
                text = "쿠폰 교환",
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
                .verticalScroll(rememberScrollState()),
        ) {
            // === 보유 스탬프 정보 카드 (HomeScreen 스타일) ===
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFFEDF7FF)),
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height
                    val scaleX = w / 362f
                    val scaleY = h / 140f

                    drawOval(
                        brush = Brush.linearGradient(
                            colorStops = arrayOf(
                                0.15f to Color(0x002563EB),
                                0.88f to Color(0xFF2563EB),
                            ),
                            start = Offset(0f, h),
                            end = Offset(w * 0.5f, 0f),
                        ),
                        topLeft = Offset(-204f * scaleX, 60f * scaleY),
                        size = Size(619f * scaleX, 740.91f * scaleY),
                        alpha = 0.15f,
                    )

                    drawOval(
                        brush = Brush.linearGradient(
                            colorStops = arrayOf(
                                0.59f to Color(0x002563EB),
                                0.75f to Color(0xFF2563EB),
                            ),
                            start = Offset(0f, h * 0.5f),
                            end = Offset(w, 0f),
                        ),
                        topLeft = Offset(42f * scaleX, -80f * scaleY),
                        size = Size(619f * scaleX, 740.91f * scaleY),
                        alpha = 0.15f,
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.stamp_trophy),
                            contentDescription = null,
                            modifier = Modifier.size(30.dp),
                            contentScale = ContentScale.Fit,
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "보유 스탬프",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = Color(0xFF3D608D),
                        )
                        Text(
                            text = "${userStampCount}개",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = Primary,
                            letterSpacing = (-0.56).sp,
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "교환 가능",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = Color(0xFF3D608D),
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${exchangeableCouponCount}개",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color(0xFF16A34A),
                        )
                    }
                }
            }

            // === 안내 메시지 ===
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF0F7FF))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(text = "💡", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "스탬프를 사용하여 다양한 혜택 쿠폰으로 교환하세요.\n교환 후 스탬프는 차감됩니다.",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = Color(0xFF3D608D),
                        lineHeight = 18.sp,
                    )
                }
            }

            // === Section Title ===
            Text(
                text = "교환 가능한 쿠폰",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF121212),
                letterSpacing = (-0.36).sp,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // === Coupon Exchange List ===
            if (coupons.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "현재 교환 가능한 쿠폰이 없습니다.",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color(0xFF9CA3AF),
                    )
                }
            } else {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    coupons.forEachIndexed { index, coupon ->
                        ExchangeCouponCard(
                            coupon = coupon,
                            userStamps = userStampCount,
                            onExchangeClick = { showConfirmDialog = coupon },
                        )
                        if (index < coupons.size - 1) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 23.dp)
                                    .height(1.dp),
                            ) {
                                drawLine(
                                    color = Color(0xFFEDEDED),
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, 0f),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 8f), 0f),
                                    strokeWidth = 2f,
                                )
                            }
                        }
                    }
                }
            }

            // Footer
            Text(
                text = "\u00A9 2026 Alpha Stamp. All rights reserved.",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                color = Color(0xFFAFBFCC),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
            )
        }
    }
}

// === 교환 확인 팝업 ===
@Composable
private fun CouponConfirmDialog(
    coupon: CouponItem,
    isRedeeming: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val validUntilText = formatValidUntil(coupon.validUntil)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(22.dp))
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 아이콘
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF3EAFF)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "🎁", fontSize = 26.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "쿠폰 교환 확인",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = Color(0xFF121212),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "스탬프를 사용하여 쿠폰을 발급하시겠습니까?",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 쿠폰 정보 카드
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFEDF7FF)),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = coupon.name,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF121212),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (!coupon.description.isNullOrBlank()) {
                        InfoRow(label = "할인 혜택", value = coupon.description, valueColor = Primary)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    InfoRow(label = "유효기간", value = validUntilText)
                    Spacer(modifier = Modifier.height(4.dp))
                    InfoRow(
                        label = "차감 스탬프",
                        value = "${coupon.requiredStamps}개",
                        valueColor = Primary,
                        valueBold = true,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF3F4F6))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "취소",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color(0xFF666666),
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF6092FF),
                                    Color(0xFF2563EB),
                                    Color(0xFF1551D3),
                                ),
                            ),
                        )
                        .clickable(enabled = !isRedeeming, onClick = onConfirm),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "교환하기",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

// === 교환 완료 팝업 ===
@Composable
private fun CouponSuccessDialog(
    success: RedeemSuccess,
    onDismiss: () -> Unit,
    onNavigateToCoupons: () -> Unit,
) {
    val validUntilText = formatValidUntil(success.validUntil)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(22.dp))
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFECFDF5)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "✅", fontSize = 26.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "쿠폰 발급 완료!",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = Color(0xFF121212),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "쿠폰이 보관함에 저장되었습니다",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color(0xFF9CA3AF),
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFEDF7FF)),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = success.couponName,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF121212),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    if (!success.couponDescription.isNullOrBlank()) {
                        InfoRow(label = "할인 혜택", value = success.couponDescription, valueColor = Primary)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    InfoRow(label = "유효기간", value = validUntilText)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF6092FF),
                                Color(0xFF2563EB),
                                Color(0xFF1551D3),
                            ),
                        ),
                    )
                    .clickable(onClick = onNavigateToCoupons),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "쿠폰 보관함으로 이동",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color.White,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF3F4F6))
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "닫기",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color(0xFF666666),
                )
            }
        }
    }
}

// === 교환 실패 팝업 ===
@Composable
private fun CouponErrorDialog(
    message: String,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(22.dp))
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFEF2F2)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "❌", fontSize = 26.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "교환 실패",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = Color(0xFF121212),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF3F4F6))
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "확인",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color(0xFF666666),
                )
            }
        }
    }
}

// === 정보 행 ===
@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF3D608D),
    valueBold: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = Color(0xFF8F8F8F),
        )
        Text(
            text = value,
            fontFamily = Pretendard,
            fontWeight = if (valueBold) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = 13.sp,
            color = valueColor,
        )
    }
}

// === 날짜 포맷 유틸 ===
private fun formatValidUntil(raw: String): String {
    return try {
        val date = LocalDateTime.parse(raw, DateTimeFormatter.ISO_DATE_TIME)
        date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
    } catch (_: Exception) {
        try {
            val date = java.time.LocalDate.parse(raw.take(10))
            date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
        } catch (_: Exception) {
            raw
        }
    }
}

@Composable
private fun ExchangeCouponCard(
    coupon: CouponItem,
    userStamps: Int,
    onExchangeClick: () -> Unit,
) {
    val canExchange = userStamps >= coupon.requiredStamps
    val validUntilText = formatValidUntil(coupon.validUntil) + "까지"

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
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

            Spacer(modifier = Modifier.width(20.dp))

            Column {
                Text(
                    text = coupon.name,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color(0xFF121212),
                )
                if (!coupon.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = coupon.description,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Primary,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📅", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = validUntilText,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Color(0xFF8F8F8F),
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "필요 스탬프 : ${coupon.requiredStamps}개",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = if (canExchange) Primary else Color(0xFFEA580C),
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(if (canExchange) Color(0xFFEDF7FF) else Color(0xFFFFF7ED))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(
                    if (canExchange) {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF6092FF),
                                Color(0xFF2563EB),
                                Color(0xFF1551D3),
                            ),
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFD0D5DD), Color(0xFFD0D5DD)),
                        )
                    },
                )
                .clickable(enabled = canExchange) { onExchangeClick() },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (canExchange) "교환하기" else "스탬프가 부족해요",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.White,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
