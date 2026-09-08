package com.alphacity.stamptour.ui.screen
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Primary = Color(0xFF02CDF8)
private val Pretendard = FontFamily.SansSerif

private data class WebCouponItem(
    val id: Int,
    val name: String,
    val description: String,
    val requiredStamps: Int,
    val validUntil: String,
    val imageLabel: String = "?렅",
)

private val sampleCoupons = listOf(
    WebCouponItem(
        id = 1,
        name = "아메리카노 무료 쿠폰",
        description = "음료 1잔 무료",
        requiredStamps = 5,
        validUntil = "2026-09-30",
    ),
    WebCouponItem(
        id = 2,
        name = "기념일 10% 할인 쿠폰",
        description = "기념일 20% 할인",
        requiredStamps = 10,
        validUntil = "2026-10-15",
    ),
    WebCouponItem(
        id = 3,
        name = "체험 프로그램 할인",
        description = "체험 50% 할인",
        requiredStamps = 15,
        validUntil = "2026-10-31",
    ),
)

private fun formatValidUntil(raw: String): String {
    return if (raw.length >= 10) {
        raw.take(10).replace("-", ".")
    } else {
        raw
    }
}

@Composable
fun StampExchangeScreen(
    onBackClick: () -> Unit = {},
    onNavigateToCoupons: () -> Unit = {},
) {
    var userStampCount by remember {
        mutableStateOf(12)
    }

    var redeemedCouponIds by remember {
        mutableStateOf(setOf<Int>())
    }

    var showConfirmDialog by remember {
        mutableStateOf<WebCouponItem?>(null)
    }

    var showSuccessDialog by remember {
        mutableStateOf<WebCouponItem?>(null)
    }

    var showErrorDialog by remember {
        mutableStateOf<String?>(null)
    }

    var isRedeeming by remember {
        mutableStateOf(false)
    }

    val availableStamps = userStampCount

    val exchangeableCouponCount = sampleCoupons.count {
        availableStamps >= it.requiredStamps &&
                !redeemedCouponIds.contains(it.id)
    }

    showConfirmDialog?.let { coupon ->
        CouponConfirmDialog(
            coupon = coupon,
            isRedeeming = isRedeeming,
            onDismiss = {
                showConfirmDialog = null
            },
            onConfirm = {
                if (availableStamps < coupon.requiredStamps) {
                    showConfirmDialog = null
                    showErrorDialog = "스탬프가 부족합니다."
                } else {
                    isRedeeming = true
                    userStampCount -= coupon.requiredStamps
                    redeemedCouponIds =
                        redeemedCouponIds + coupon.id
                    isRedeeming = false
                    showConfirmDialog = null
                    showSuccessDialog = coupon
                }
            },
        )
    }

    showSuccessDialog?.let { coupon ->
        CouponSuccessDialog(
            coupon = coupon,
            onDismiss = {
                showSuccessDialog = null
            },
            onNavigateToCoupons = {
                showSuccessDialog = null
                onNavigateToCoupons()
            },
        )
    }

    showErrorDialog?.let { message ->
        CouponErrorDialog(
            message = message,
            onDismiss = {
                showErrorDialog = null
            },
        )
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
                .height(50.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "??",
                        fontFamily = Pretendard,
                fontSize = 34.sp,
                color = Color(0xFF121212),
                modifier = Modifier.clickable {
                    onBackClick()
                },
            )

            Spacer(
                modifier = Modifier.width(24.dp)
            )

            Text(
                text = "쿠폰 교환",
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
                ),
        ) {
            // Stamp summary card
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFFEDF7FF)),
            ) {
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
                            .background(
                                Color.White.copy(
                                    alpha = 0.7f
                                )
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "?룇",
                            fontSize = 28.sp,
                        )
                    }

                    Spacer(
                        modifier = Modifier.width(16.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "교환 가능한 스탬프",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = Color(0xFF3D608D),
                        )

                        Text(
                            text = "${availableStamps}개",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = Primary,
                        )
                    }

                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "교환 가능",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = Color(0xFF3D608D),
                        )

                        Spacer(
                            modifier = Modifier.height(2.dp)
                        )

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

            // Notice
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF0F7FF))
                    .padding(
                        horizontal = 16.dp,
                        vertical = 12.dp,
                    ),
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = "?뮕",
                        fontSize = 14.sp,
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text(
                        text = "스탬프를 사용하여 다양한 혜택 쿠폰으로 교환하세요.\n교환 시 스탬프가 차감됩니다.",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = Color(0xFF3D608D),
                        lineHeight = 18.sp,
                    )
                }
            }

            Text(
                text = "교환 가능한 쿠폰",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF121212),
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp),
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Column(
                modifier = Modifier.padding(
                    horizontal = 20.dp
                ),
            ) {
                if (sampleCoupons.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
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
                    sampleCoupons.forEachIndexed {
                            index,
                            coupon ->
                        ExchangeCouponCard(
                            coupon = coupon,
                            userStamps = availableStamps,
                            isRedeemed =
                                redeemedCouponIds.contains(
                                    coupon.id
                                ),
                            onExchangeClick = {
                                showConfirmDialog = coupon
                            },
                        )

                        if (
                            index <
                            sampleCoupons.lastIndex
                        ) {
                            HorizontalDivider(
                                color = Color(0xFFEDEDED),
                                modifier =
                                    Modifier.padding(
                                        horizontal = 23.dp
                                    ),
                            )
                        }
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(40.dp)
            )
        }
    }
}

@Composable
private fun CouponConfirmDialog(
    coupon: WebCouponItem,
    isRedeeming: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "?럞  쿠폰 교환 확인",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
            )
        },
        text = {
            Column {
                Text(
                    text = "스탬프를 사용하여 쿠폰을 발급하시겠습니까?",
                    fontFamily = Pretendard,
                    fontSize = 14.sp,
                    color = Color(0xFF9CA3AF),
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text = coupon.name,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = coupon.description,
                    fontFamily = Pretendard,
                    fontSize = 13.sp,
                    color = Primary,
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text = "필요 스탬프: ${coupon.requiredStamps}개",
                    fontFamily = Pretendard,
                    fontSize = 13.sp,
                    color = Color(0xFF595959),
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "유효기간 : ${formatValidUntil(coupon.validUntil)}",
                    fontFamily = Pretendard,
                    fontSize = 13.sp,
                    color = Color(0xFF595959),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isRedeeming,
                onClick = onConfirm,
            ) {
                Text(
                    text = if (isRedeeming) "처리 중..." else "교환하기",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text(
                    text = "취소",
                    fontFamily = Pretendard,
                    color = Color(0xFF8F8F8F),
                )
            }
        },
    )
}

@Composable
private fun CouponSuccessDialog(
    coupon: WebCouponItem,
    onDismiss: () -> Unit,
    onNavigateToCoupons: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "??쿠폰 발급 완료!",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
            )
        },
        text = {
            Column {
                Text(
                    text = "쿠폰은 보관함에 저장되었습니다.",
                    fontFamily = Pretendard,
                    fontSize = 14.sp,
                    color = Color(0xFF9CA3AF),
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                Text(
                    text = coupon.name,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text = coupon.description,
                    fontFamily = Pretendard,
                    fontSize = 14.sp,
                    color = Primary,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onNavigateToCoupons,
            ) {
                Text(
                    text = "쿠폰 보기",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text(
                    text = "?リ린",
                    fontFamily = Pretendard,
                    color = Color(0xFF8F8F8F),
                )
            }
        },
    )
}

@Composable
private fun CouponErrorDialog(
    message: String,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "??교환 실패",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Text(
                text = message,
                fontFamily = Pretendard,
                fontSize = 14.sp,
                color = Color(0xFF8F8F8F),
            )
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
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

@Composable
private fun ExchangeCouponCard(
    coupon: WebCouponItem,
    userStamps: Int,
    isRedeemed: Boolean,
    onExchangeClick: () -> Unit,
) {
    val canExchange =
        !isRedeemed &&
                userStamps >= coupon.requiredStamps

    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
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
                Text(
                    text = coupon.imageLabel,
                    fontSize = 34.sp,
                )
            }

            Spacer(
                modifier = Modifier.width(20.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = coupon.name,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color(0xFF121212),
                )

                Spacer(
                    modifier = Modifier.height(2.dp)
                )

                Text(
                    text = coupon.description,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Primary,
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "?뱟 ${formatValidUntil(coupon.validUntil)}까지",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color(0xFF8F8F8F),
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text = "필요 스탬프: ${coupon.requiredStamps}개",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color =
                        if (canExchange) {
                            Primary
                        } else {
                            Color(0xFFEA580C)
                        },
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(
                            if (canExchange) {
                                Color(0xFFEDF7FF)
                            } else {
                                Color(0xFFFFF7ED)
                            }
                        )
                        .padding(
                            horizontal = 10.dp,
                            vertical = 5.dp,
                        ),
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
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFD0D5DD),
                                Color(0xFFD0D5DD),
                            )
                        )
                    }
                )
                .clickable(
                    enabled = canExchange,
                    onClick = onExchangeClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text =
                    when {
                        isRedeemed -> "이미 교환함"
                        canExchange -> "교환하기"
                        else -> "스탬프가 부족해요"
                    },
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.White,
            )
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )
    }
}
