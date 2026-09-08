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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Primary = Color(0xFF02CDF8)
private val Pretendard = FontFamily.SansSerif

private data class WebFirstComeEvent(
    val id: Int,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val startDate: String,
    val endDate: String,
    val participantCount: Int,
    val participantLimit: Int,
    val status: String,
    val isParticipated: Boolean,
    val myRaffleNumber: Int?,
)

@Composable
fun FirstComeEventDetailScreen(
    eventId: Int,
    onBackClick: () -> Unit = {},
) {
    var isLoading by remember { mutableStateOf(false) }
    var isParticipating by remember { mutableStateOf(false) }
    var showMessage by remember { mutableStateOf(false) }
    var participated by remember { mutableStateOf(false) }

    // 실제 API 연결 전 화면 확인용 데이터
    val event = remember(eventId) {
        WebFirstComeEvent(
            id = eventId,
            name = "선착순 사은품 이벤트",
            description = "선착순으로 참여하고 사은품을 받아보세요.",
            imageUrl = null,
            startDate = "2026-09-01",
            endDate = "2026-09-30",
            participantCount = 42,
            participantLimit = 100,
            status = "in_progress",
            isParticipated = false,
            myRaffleNumber = null,
        )
    }

    val hasParticipated =
        participated || event.isParticipated

    val remaining = maxOf(
        event.participantLimit - event.participantCount,
        0,
    )

    val isSoldOut = remaining <= 0
    val isOpen = event.status != "ended"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(
                        horizontal = 20.dp,
                        vertical = 16.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "뒤로",
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
                    modifier = Modifier.width(12.dp)
                )

                Text(
                    text = "선착순 사은품",
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

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = Primary,
                    )
                }
            } else if (hasParticipated) {
                ParticipationCompleteContent(
                    event = event,
                    raffleNumber = event.myRaffleNumber ?: 1001,
                    onBackClick = onBackClick,
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(
                            rememberScrollState()
                        ),
                ) {
                    // 이벤트 이미지
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color(0xFFE8E8E8)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = event.name.take(1),
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Bold,
                            fontSize = 40.sp,
                            color = Color(0xFFB5B5B5),
                        )
                    }

                    Column(
                        modifier = Modifier.padding(
                            horizontal = 20.dp
                        ),
                    ) {
                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        // 유형 + 상태
                        Row(
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFFEDF7FF))
                                    .padding(
                                        horizontal = 10.dp,
                                        vertical = 3.dp,
                                    ),
                            ) {
                                Text(
                                    text = "선착순",
                                    fontFamily = Pretendard,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = Primary,
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(
                                        if (isOpen && !isSoldOut) {
                                            Primary
                                        } else {
                                            Color(0xFF8F8F8F)
                                        }
                                    )
                                    .padding(
                                        horizontal = 10.dp,
                                        vertical = 3.dp,
                                    ),
                            ) {
                                Text(
                                    text =
                                        if (isOpen && !isSoldOut) {
                                            "진행중"
                                        } else {
                                            "마감"
                                        },
                                    fontFamily = Pretendard,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                )
                            }
                        }

                        Spacer(
                            modifier = Modifier.height(10.dp)
                        )

                        Text(
                            text = event.name,
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = Color(0xFF121212),
                        )

                        if (!event.description.isNullOrBlank()) {
                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )

                            Text(
                                text = event.description,
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = Color(0xFF595959),
                            )
                        }

                        // 잔여 수량
                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFAFAFA))
                                .padding(16.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = "잔여 수량",
                                    fontFamily = Pretendard,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp,
                                    color = Color(0xFF595959),
                                )

                                Text(
                                    text = "${remaining}/${event.participantLimit}명",
                                    fontFamily = Pretendard,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color =
                                        if (isSoldOut) {
                                            Color(0xFF8F8F8F)
                                        } else {
                                            Primary
                                        },
                                )
                            }

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            val progress =
                                if (event.participantLimit > 0) {
                                    event.participantCount.toFloat() /
                                            event.participantLimit.toFloat()
                                } else {
                                    0f
                                }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF8F8F8)),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(
                                            progress.coerceIn(
                                                0f,
                                                1f,
                                            )
                                        )
                                        .clip(CircleShape)
                                        .background(
                                            if (isSoldOut) {
                                                Color(0xFF8E8E8E)
                                            } else {
                                                Primary
                                            }
                                        ),
                                )
                            }
                        }

                        // 수령 방법
                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF8F8F8))
                                .padding(16.dp),
                        ) {
                            Text(
                                text = "수령 방법",
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color(0xFF121212),
                            )

                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )

                            ReceiptStep(
                                number = 1,
                                text = "이벤트 부스 스텝이 참여하기 버튼 클릭",
                            )

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            ReceiptStep(
                                number = 2,
                                text = "참여완료 화면 확인",
                            )

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            ReceiptStep(
                                number = 3,
                                text = "사은품 수령",
                            )

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            ReceiptStep(
                                number = 4,
                                text = "재발급 불가",
                            )
                        }

                        Spacer(
                            modifier = Modifier.height(24.dp)
                        )

                        if (isOpen && !isSoldOut) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Primary)
                                    .clickable(
                                        enabled = !isParticipating
                                    ) {
                                        isParticipating = true
                                        participated = true
                                        showMessage = true
                                        isParticipating = false
                                    }
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (isParticipating) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Text(
                                        text = "참여하기",
                                        fontFamily = Pretendard,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        color = Color.White,
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFCCCCCC))
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "마감됨",
                                    fontFamily = Pretendard,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = Color.White,
                                )
                            }
                        }

                        Spacer(
                            modifier = Modifier.height(40.dp)
                        )
                    }
                }
            }
        }

        if (showMessage) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
                    .clip(CircleShape)
                    .background(
                        Color.Black.copy(alpha = 0.8f)
                    )
                    .padding(
                        horizontal = 24.dp,
                        vertical = 12.dp,
                    ),
            ) {
                Text(
                    text = "참여가 완료되었습니다.",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun ParticipationCompleteContent(
    event: WebFirstComeEvent,
    raffleNumber: Int?,
    onBackClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Primary),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "✓",
                fontSize = 32.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = "참여 완료!",
            fontFamily = Pretendard,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = Color(0xFF121212),
        )

        if (raffleNumber != null) {
            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF0F7FF))
                    .padding(
                        horizontal = 24.dp,
                        vertical = 12.dp,
                    ),
            ) {
                Text(
                    text = "참여 번호: #$raffleNumber",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Primary,
                )
            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Text(
            text = event.name,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = Color(0xFF595959),
            textAlign = TextAlign.Center,
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFF5F5))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "⚠ 재발급 불가",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color(0xFFE74C3C),
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "사은품 수령 후 재발급이 불가합니다.\n부스에서 이 화면을 보여주세요.",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = Color(0xFF8F8F8F),
                textAlign = TextAlign.Center,
            )
        }

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Primary)
                .clickable {
                    onBackClick()
                }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "돌아가기",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.White,
            )
        }

        Spacer(
            modifier = Modifier.height(40.dp)
        )
    }
}

@Composable
private fun ReceiptStep(
    number: Int,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Primary),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "$number",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.White,
            )
        }

        Spacer(
            modifier = Modifier.width(10.dp)
        )

        Text(
            text = text,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            color = Color(0xFF595959),
        )
    }
}