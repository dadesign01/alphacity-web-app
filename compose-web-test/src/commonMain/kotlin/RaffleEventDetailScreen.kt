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
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Primary = Color(0xFF02CDF8)
private val Pretendard = FontFamily.SansSerif

private data class DemoRaffleEvent(
    val id: Int,
    val name: String,
    val reward: String,
    val description: String,
    val winnerCount: Int,
    val participantCount: Int,
    val startDate: String,
    val endDate: String,
    val isEnded: Boolean,
)

private val demoRaffleEvents = listOf(
    DemoRaffleEvent(
        id = 1,
        name = "골목상권 추첨 이벤트",
        reward = "골목상권",
        description = "수원페스티벌을 방문하고 스탬프를 모아 특별한 경품에 응모해보세요.",
        winnerCount = 3,
        participantCount = 128,
        startDate = "2026.04.02",
        endDate = "2026.06.05",
        isEnded = false,
    ),
    DemoRaffleEvent(
        id = 2,
        name = "VR 헤드셋 이벤트",
        reward = "VR 헤드셋",
        description = "이벤트에 참여 후 추첨을 통해 VR 헤드셋을 드립니다.",
        winnerCount = 5,
        participantCount = 84,
        startDate = "2026.04.02",
        endDate = "2026.06.05",
        isEnded = false,
    ),
)

@Composable
fun RaffleEventDetailScreen(
    eventId: Int,
    onBackClick: () -> Unit = {},
) {
    val event =
        demoRaffleEvents.firstOrNull {
            it.id == eventId
        } ?: demoRaffleEvents.first()

    var isParticipating by remember {
        mutableStateOf(false)
    }

    var myRaffleNumber by remember {
        mutableStateOf<Int?>(null)
    }

    var message by remember {
        mutableStateOf<String?>(null)
    }

    val isOpen = !event.isEnded
    val hasParticipated = myRaffleNumber != null

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
                verticalAlignment =
                    Alignment.CenterVertically,
            ) {
                Text(
                    text = "‹",
                    fontFamily = Pretendard,
                    fontSize = 34.sp,
                    color = Color(0xFF121212),
                    modifier = Modifier.clickable {
                        onBackClick()
                    },
                )

                Spacer(
                    modifier = Modifier.width(12.dp)
                )

                Text(
                    text = "추첨 이벤트",
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
                // Event Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color(0xFFE8E8E8)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "🎁",
                        fontSize = 60.sp,
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

                    // 상태 + 제목
                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (isOpen) {
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
                                    if (isOpen) {
                                        "참여 가능"
                                    } else {
                                        "마감"
                                    },
                                fontFamily = Pretendard,
                                fontWeight =
                                    FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = Color.White,
                            )
                        }

                        Spacer(
                            modifier = Modifier.width(10.dp)
                        )

                        Text(
                            text = event.name,
                            fontFamily = Pretendard,
                            fontWeight =
                                FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = Color(0xFF121212),
                        )
                    }

                    // 경품 정보
                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(
                                RoundedCornerShape(12.dp)
                            )
                            .background(
                                Color(0xFFF8F8F8)
                            )
                            .padding(14.dp),
                    ) {
                        Text(
                            text = "경품 정보",
                            fontFamily = Pretendard,
                            fontWeight =
                                FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFF121212),
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "🎁",
                                fontSize = 18.sp,
                            )

                            Spacer(
                                modifier = Modifier.width(5.dp)
                            )

                            Text(
                                text = "상품: ${event.reward}",
                                fontFamily = Pretendard,
                                fontWeight =
                                    FontWeight.Medium,
                                fontSize = 13.sp,
                                color = Primary,
                            )
                        }

                        Spacer(
                            modifier = Modifier.height(5.dp)
                        )

                        Text(
                            text =
                                "당첨자 수: ${event.winnerCount}명",
                            fontFamily = Pretendard,
                            fontWeight =
                                FontWeight.Medium,
                            fontSize = 13.sp,
                            color = Color(0xFF595959),
                        )
                    }

                    // 설명
                    if (event.description.isNotBlank()) {
                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        Text(
                            text = event.description,
                            fontFamily = Pretendard,
                            fontWeight =
                                FontWeight.Normal,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = Color(0xFF595959),
                        )
                    }

                    // 기간
                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(
                        text =
                            "이벤트 기간: ${event.startDate} ~ ${event.endDate}",
                        fontFamily = Pretendard,
                        fontSize = 12.sp,
                        color = Color(0xFF828282),
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text =
                            "${event.participantCount}명 참여",
                        fontFamily = Pretendard,
                        fontSize = 12.sp,
                        color = Color(0xFF828282),
                    )

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )

                    HorizontalDivider(
                        color = Color(0xFFE2E2E2)
                    )

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )

                    // 추첨 번호
                    if (hasParticipated) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(
                                    RoundedCornerShape(16.dp)
                                )
                                .background(
                                    Color(0xFFF0F7FF)
                                )
                                .padding(
                                    vertical = 24.dp
                                ),
                            horizontalAlignment =
                                Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "나의 추첨 번호",
                                fontFamily = Pretendard,
                                fontWeight =
                                    FontWeight.Medium,
                                fontSize = 14.sp,
                                color =
                                    Color(0xFF595959),
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(12.dp)
                            )

                            Text(
                                text =
                                    "#${myRaffleNumber}",
                                fontFamily = Pretendard,
                                fontWeight =
                                    FontWeight.Bold,
                                fontSize = 48.sp,
                                color = Primary,
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(12.dp)
                            )

                            Text(
                                text =
                                    "추첨은 현장에서 진행합니다.",
                                fontFamily = Pretendard,
                                fontSize = 12.sp,
                                color =
                                    Color(0xFF8F8F8F),
                            )
                        }

                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(
                                    RoundedCornerShape(12.dp)
                                )
                                .background(
                                    Color(0xFFCCCCCC)
                                )
                                .padding(
                                    vertical = 16.dp
                                ),
                            contentAlignment =
                                Alignment.Center,
                        ) {
                            Text(
                                text = "번호 발급 완료",
                                fontFamily = Pretendard,
                                fontWeight =
                                    FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = Color.White,
                            )
                        }
                    } else if (isOpen) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(
                                    RoundedCornerShape(12.dp)
                                )
                                .background(
                                    Primary
                                )
                                .clickable(
                                    enabled =
                                        !isParticipating
                                ) {
                                    isParticipating = true

                                    myRaffleNumber =
                                        (event.participantCount + 1)

                                    message =
                                        "추첨 번호가 발급되었습니다."

                                    isParticipating = false
                                }
                                .padding(
                                    vertical = 16.dp
                                ),
                            contentAlignment =
                                Alignment.Center,
                        ) {
                            if (isParticipating) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier =
                                        Modifier.size(
                                            20.dp
                                        ),
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Text(
                                    text = "번호 발급받기",
                                    fontFamily = Pretendard,
                                    fontWeight =
                                        FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = Color.White,
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(
                                    RoundedCornerShape(12.dp)
                                )
                                .background(
                                    Color(0xFFCCCCCC)
                                )
                                .padding(
                                    vertical = 16.dp
                                ),
                            contentAlignment =
                                Alignment.Center,
                        ) {
                            Text(
                                text =
                                    "이벤트가 종료되었습니다.",
                                fontFamily = Pretendard,
                                fontWeight =
                                    FontWeight.SemiBold,
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

        // Message
        message?.let { text ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 30.dp)
                    .clip(CircleShape)
                    .background(
                        Color.Black.copy(
                            alpha = 0.8f
                        )
                    )
                    .padding(
                        horizontal = 24.dp,
                        vertical = 12.dp,
                    ),
            ) {
                Text(
                    text = text,
                    fontFamily = Pretendard,
                    fontWeight =
                        FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color.White,
                    textAlign =
                        TextAlign.Center,
                )
            }
        }
    }
}