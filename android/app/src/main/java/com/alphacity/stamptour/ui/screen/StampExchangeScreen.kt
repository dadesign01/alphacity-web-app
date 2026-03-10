package com.alphacity.stamptour.ui.screen

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alphacity.stamptour.R
import com.alphacity.stamptour.ui.theme.Pretendard
import com.alphacity.stamptour.ui.theme.Primary

private data class ExchangeCoupon(
    val id: Int,
    val name: String,
    val benefit: String,
    val requiredStamps: Int,
    val validUntil: String,
)

private val mockExchangeCoupons = listOf(
    ExchangeCoupon(1, "메타 카페 음료 할인", "30% 할인", 3, "2026.03.31까지 사용 가능"),
    ExchangeCoupon(2, "푸드 코트 식사 할인권", "5,000원 할인", 6, "2026.03.31까지 사용 가능"),
    ExchangeCoupon(3, "VR 체험 무료 이용권", "1회 무료 이용", 10, "2026.03.31까지 사용 가능"),
    ExchangeCoupon(4, "기념품샵 쇼핑 할인 쿠폰", "10,000원 할인", 10, "2026.03.31까지 사용 가능"),
)

private const val USER_STAMP_COUNT = 5

@Composable
fun StampExchangeScreen(
    onBackClick: () -> Unit,
) {
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
                        text = "나의 보유 스탬프",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Color(0xFF121212),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "스탬프를 사용하여 다양한 혜택 쿠폰으로 교환하세요.\n교환 완료 후, 사용된 스탬프는 보유 수량에서 자동 차감됩니다.",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color(0xFF3D608D),
                    )
                }
                Text(
                    text = "${USER_STAMP_COUNT}개",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Bold,
                    fontSize = 35.sp,
                    color = Primary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp),
                )
            }

            // === Section Title ===
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

            // === Coupon Exchange List ===
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp),
            ) {
                mockExchangeCoupons.forEachIndexed { index, coupon ->
                    ExchangeCouponCard(coupon = coupon, userStamps = USER_STAMP_COUNT)

                    if (index < mockExchangeCoupons.size - 1) {
                        // Dashed divider
                        androidx.compose.foundation.Canvas(
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

            // Footer
            Text(
                text = "© 2026 Alpha Stamp. All rights reserved.",
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

@Composable
private fun ExchangeCouponCard(coupon: ExchangeCoupon, userStamps: Int) {
    val canExchange = userStamps >= coupon.requiredStamps

    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
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
                Image(
                    painter = painterResource(id = R.drawable.icon_coupon),
                    contentDescription = null,
                    modifier = Modifier.size(52.dp),
                    contentScale = ContentScale.Fit,
                )
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
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = coupon.benefit,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = Primary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = coupon.validUntil,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color(0xFF8F8F8F),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "필요 스탬프 : ${coupon.requiredStamps}개",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color(0xFFEDF7FF))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                )
            }
        }

        // Exchange button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(
                    if (canExchange) {
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF6092FF), Color(0xFF2563EB), Color(0xFF1551D3))
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF8F8F8F), Color(0xFF8F8F8F))
                        )
                    }
                )
                .clickable(enabled = canExchange) { },
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
