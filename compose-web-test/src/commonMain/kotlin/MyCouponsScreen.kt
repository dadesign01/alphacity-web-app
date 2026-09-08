package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alphacity.stamptour.network.dto.MyCouponItem
import com.alphacity.stamptour.viewmodel.MyCouponsViewModel

private val Primary = Color(0xFF02CDF8)
private val Pretendard = FontFamily.SansSerif

private fun formatCouponValidUntil(raw: String): String {
    return if (raw.length >= 10) {
        "${raw.take(10).replace("-", ".")}까지 사용 가능"
    } else {
        raw
    }
}

@Composable
fun MyCouponsScreen(
    onBackClick: () -> Unit = {},
) {
    val viewModel = remember {
        MyCouponsViewModel()
    }
    val myCoupons by viewModel.myCoupons.collectAsState()
    val useSuccess by viewModel.useSuccess.collectAsState()
    val useError by viewModel.useError.collectAsState()

    var selectedCoupon by remember {
        mutableStateOf<MyCouponItem?>(null)
    }

    LaunchedEffect(Unit) {
        viewModel.fetchMyCoupons()
    }

    val availableCoupons = myCoupons.filter {
        it.status == "issued"
    }

    if (useSuccess) {
        AlertDialog(
            onDismissRequest = {
                viewModel.clearUseSuccess()
            },
            title = {
                Text(
                    text = "알림",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            text = {
                Text(
                    text = "쿠폰이 사용되었습니다.",
                    fontFamily = Pretendard,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearUseSuccess()
                    }
                ) {
                    Text(
                        text = "확인",
                        fontFamily = Pretendard,
                        color = Primary,
                    )
                }
            },
        )
    }

    useError?.let { error ->
        AlertDialog(
            onDismissRequest = {
                viewModel.clearUseError()
            },
            title = {
                Text(
                    text = "알림",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            text = {
                Text(
                    text = error,
                    fontFamily = Pretendard,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearUseError()
                    }
                ) {
                    Text(
                        text = "확인",
                        fontFamily = Pretendard,
                        color = Primary,
                    )
                }
            },
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "‹",
                    fontFamily = Pretendard,
                    fontSize = 34.sp,
                    color = Color(0xFF121212),
                    modifier = Modifier
                        .size(
                            width = 13.dp,
                            height = 26.dp,
                        )
                        .clickable {
                            onBackClick()
                        },
                )

                Spacer(
                    modifier = Modifier.width(24.dp)
                )

                Text(
                    text = "쿠폰 보관함",
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

            val couponScrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .then(
                        if (availableCoupons.isEmpty()) {
                            Modifier
                        } else {
                            Modifier.verticalScroll(couponScrollState)
                        }
                    ),
            ) {
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

                        Spacer(
                            modifier = Modifier.height(4.dp)
                        )

                        Text(
                            text = "사용 시 해당 장소에서 고유 코드를 제시해주세요.",
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

                Box(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(8.dp))
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
                            onBackClick()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "추가 쿠폰 받으러 가기",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = Color.White,
                        )

                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )

                        Text(
                            text = ">",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = Color.White,
                        )
                    }
                }

                Text(
                    text = "사용 가능 쿠폰",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color(0xFF121212),
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(top = 24.dp),
                )

                if (availableCoupons.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
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
                            MyCouponCardItem(
                                coupon = coupon,
                                onClick = {
                                    selectedCoupon = coupon
                                },
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(24.dp)
                )
            }

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

        selectedCoupon?.let { coupon ->
            MyCouponDetailBottomSheet(
                coupon = coupon,
                onDismiss = {
                    selectedCoupon = null
                },
                onUseCoupon = {
                    viewModel.useCoupon(coupon.id)
                    selectedCoupon = null
                },
            )
        }
    }
}

@Composable
private fun MyCouponCardItem(
    coupon: MyCouponItem,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(134.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(Color.White)
            .clickable {
                onClick()
            },
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(85.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF8F8F8)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "🎟️",
                    fontSize = 36.sp,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = 18.dp,
                        end = 8.dp,
                    ),
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
                    Spacer(
                        modifier = Modifier.height(2.dp)
                    )

                    Text(
                        text = coupon.description,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = formatCouponValidUntil(coupon.validUntil),
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color(0xFF8F8F8F),
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color(0xFFF8F8F8))
                        .padding(
                            horizontal = 10.dp,
                            vertical = 5.dp,
                        ),
                )
            }

            Box(
                modifier = Modifier
                    .width(82.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF4B8BF5),
                                Primary,
                            )
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "쿠폰",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.61f))
            .clickable {
                onDismiss()
            },
    )
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        topStart = 40.dp,
                        topEnd = 40.dp,
                    )
                )
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 19.dp)
                    .width(65.dp)
                    .height(7.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0xFFD9D9D9)),
            )

            Text(
                text = coupon.storeName ?: coupon.name,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color(0xFF121212),
                modifier = Modifier.padding(top = 24.dp),
            )

            Text(
                text = coupon.description ?: coupon.name,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Color(0xFF121212),
                modifier = Modifier.padding(top = 4.dp),
            )

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

            Text(
                text = "직원에게 쿠폰 사용처 관계없이 보여주세요.",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color(0xFF8F8F8F),
                modifier = Modifier.padding(top = 16.dp),
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp)
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(13.dp))
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
                        onUseCoupon()
                    },
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .background(Color(0xFFF8F8F8))
                    .padding(
                        horizontal = 20.dp,
                        vertical = 16.dp,
                    ),
            ) {
                Text(
                    text = "쿠폰 사용 전 주의해주세요.",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    color = Color(0xFF8F8F8F),
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text =
                        "- 본 쿠폰은 유효기간 내에만 사용 가능합니다.\n" +
                                "- 쿠폰은 1회 사용 시 소진되며, 재사용은 불가능합니다.\n" +
                                "- 현금 교환 및 환불은 불가능합니다.\n" +
                                "- 타 쿠폰과 중복 사용은 불가능할 수 있습니다.\n" +
                                "- 올리모아 축제 고객센터 : 053-123-4567\n" +
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
