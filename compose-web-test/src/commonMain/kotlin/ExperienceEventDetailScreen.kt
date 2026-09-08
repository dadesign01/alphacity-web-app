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

private val ExperienceOrange = Color(0xFFE67E22)
private val Pretendard = FontFamily.SansSerif

private data class WebExperienceEvent(
    val id: Int,
    val name: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val startDate: String = "2026-09-01",
    val endDate: String = "2026-09-30",
    val duration: Int? = 60,
    val capacity: Int? = 20,
    val participantCount: Int = 5,
    val location: String? = "팝파이시티 행사장",
    val price: Int? = 0,
    val status: String = "in_progress",
    val isParticipated: Boolean = false,
)

@Composable
fun ExperienceEventDetailScreen(
    eventId: Int,
    onBackClick: () -> Unit = {},
) {
    var isLoading by remember { mutableStateOf(false) }
    var isParticipating by remember { mutableStateOf(false) }
    var isParticipated by remember { mutableStateOf(false) }
    var showMessage by remember { mutableStateOf(false) }

    val event = remember(eventId) {
        WebExperienceEvent(
            id = eventId,
            name = "팝파이시티 체험 이벤트",
            description = "팝파이시티에서 즐겁게 참여할 수 있는 특별한 체험 프로그램입니다.",
            startDate = "2026-09-01",
            endDate = "2026-09-30",
            duration = 60,
            capacity = 20,
            participantCount = 5,
            location = "팝파이시티 행사장",
            price = 0,
            status = "in_progress",
        )
    }

    val isOpen = event.status != "ended"
    val isFull =
        event.capacity != null &&
                event.participantCount >= event.capacity

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
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
                    modifier = Modifier.width(12.dp)
                )

                Text(
                    text = "체험 이벤트",
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
                        color = ExperienceOrange,
                    )
                }
            } else if (isParticipated || event.isParticipated) {
                ExperienceCompleteContent(
                    event = event,
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

                        Row(
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFF3E0))
                                    .padding(
                                        horizontal = 10.dp,
                                        vertical = 3.dp,
                                    ),
                            ) {
                                Text(
                                    text = "체험",
                                    fontFamily = Pretendard,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = ExperienceOrange,
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(
                                        if (isOpen && !isFull) {
                                            ExperienceOrange
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
                                        if (isOpen && !isFull) {
                                            "신청 가능"
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

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        ExperienceInfoCard(
                            event = event,
                        )

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
                                text = "체험 기간",
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color(0xFF121212),
                            )

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            Text(
                                text = "${formatExperienceDate(event.startDate)} ~ ${formatExperienceDate(event.endDate)}",
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.sp,
                                color = Color(0xFF595959),
                            )
                        }

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
                                text = "참여 안내",
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color(0xFF121212),
                            )

                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )

                            ExperienceGuideStep(
                                number = 1,
                                text = "아래 참여하기 버튼을 눌러 신청",
                            )

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            ExperienceGuideStep(
                                number = 2,
                                text = "체험 장소에서 참여 완료 화면 제시",
                            )

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            ExperienceGuideStep(
                                number = 3,
                                text = "현장에서 체험 진행",
                            )
                        }

                        Spacer(
                            modifier = Modifier.height(24.dp)
                        )

                        if (isOpen && !isFull) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ExperienceOrange)
                                    .clickable(
                                        enabled = !isParticipating
                                    ) {
                                        isParticipating = true
                                        showMessage = false

                                        isParticipating = false
                                        isParticipated = true
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
private fun ExperienceInfoCard(
    event: WebExperienceEvent,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFF8F0))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (event.duration != null) {
            ExperienceInfoRow(
                icon = "⏱",
                label = "체험 시간",
                value = "${event.duration}분",
            )
        }

        if (event.capacity != null) {
            ExperienceInfoRow(
                icon = "👥",
                label = "정원",
                value = "1회 ${event.capacity}명",
            )
        }

        if (!event.location.isNullOrBlank()) {
            ExperienceInfoRow(
                icon = "📍",
                label = "장소",
                value = event.location,
            )
        }

        if (event.price != null) {
            val priceText =
                if (event.price == 0) {
                    "무료"
                } else {
                    "${event.price}원"
                }

            ExperienceInfoRow(
                icon = "💰",
                label = "참가비",
                value = priceText,
            )
        }
    }
}

@Composable
private fun ExperienceInfoRow(
    icon: String,
    label: String,
    value: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = icon,
            fontSize = 16.sp,
            modifier = Modifier.width(24.dp),
        )

        Text(
            text = label,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            color = Color(0xFF595959),
            modifier = Modifier.width(60.dp),
        )

        Text(
            text = value,
            fontFamily = Pretendard,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = Color(0xFF121212),
        )
    }
}

@Composable
private fun ExperienceCompleteContent(
    event: WebExperienceEvent,
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
                .background(ExperienceOrange),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "✓",
                fontFamily = Pretendard,
                fontSize = 32.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = "신청 완료!",
            fontFamily = Pretendard,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = Color(0xFF121212),
        )

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
            modifier = Modifier.height(16.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (event.duration != null) {
                Text(
                    text = "체험 시간: ${event.duration}분",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = Color(0xFF595959),
                )
            }

            if (!event.location.isNullOrBlank()) {
                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "장소: ${event.location}",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = Color(0xFF595959),
                )
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFF8F0))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "ℹ 참여 확인",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = ExperienceOrange,
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "체험 장소에서 이 화면을 보여주세요.",
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
                .background(ExperienceOrange)
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
private fun ExperienceGuideStep(
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
                .background(ExperienceOrange),
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

private fun formatExperienceDate(
    dateStr: String,
): String {
    return dateStr
        .take(10)
        .replace("-", ".")
}