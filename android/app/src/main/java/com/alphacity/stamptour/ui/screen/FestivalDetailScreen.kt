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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

private data class WebFestivalProgram(
    val id: Int,
    val name: String,
    val category: String?,
    val imageUrl: String? = null,
    val startDate: String = "2026-09-01",
    val endDate: String = "2026-09-30",
    val status: String = "in_progress",
    val location: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

@Composable
fun FestivalDetailScreen(
    festivalId: Int = 1,
    festivalName: String = "페스티벌 하이라이트",
    festivalDescription: String? = "다양한 프로그램과 이벤트가 함께하는 페스티벌 축제입니다.",
    festivalImageUrl: String? = null,
    festivalBannerUrl: String? = null,
    festivalStartDate: String = "2026-09-01",
    festivalEndDate: String = "2026-09-30",
    festivalAddress: String? = "대구광역시 축제 페스티벌",
    festivalLatitude: Double? = 35.8379,
    festivalLongitude: Double? = 128.6812,
    onBackClick: () -> Unit = {},
    onSeeAllPrograms: () -> Unit = {},
    onProgramClick: (Int) -> Unit = {},
    onNavigateToMap: (Double, Double) -> Unit = { _, _ -> },
) {
    val programs = remember {
        listOf(
            WebFestivalProgram(
                id = 1,
                name = "AI 기술 전시",
                category = "exhibition",
                location = "전시관 A",
                latitude = festivalLatitude,
                longitude = festivalLongitude,
            ),
            WebFestivalProgram(
                id = 2,
                name = "AI 세미나",
                category = "seminar",
                location = "세미나홀",
                latitude = festivalLatitude,
                longitude = festivalLongitude,
            ),
            WebFestivalProgram(
                id = 3,
                name = "체험 프로그램",
                category = "experience",
                location = "체험관",
                latitude = festivalLatitude,
                longitude = festivalLongitude,
            ),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState()),
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 14.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "뒤로",
                fontFamily = Pretendard,
                fontSize = 34.sp,
                color = Color(0xFF121212),
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        onBackClick()
                    },
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Text(
                text = "축제 상세",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF121212),
            )
        }

        // Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Color(0xFFE8E8E8)),
            contentAlignment = Alignment.Center,
        ) {
            val hasBanner =
                !festivalBannerUrl.isNullOrBlank() ||
                        !festivalImageUrl.isNullOrBlank()

            Text(
                text = if (hasBanner) {
                    "축제 이미지"
                } else {
                    festivalName.take(1)
                },
                fontFamily = Pretendard,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = Color(0xFFB5B5B5),
            )
        }

        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Text(
                text = festivalName,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color(0xFF121212),
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "${festivalStartDate.take(10)} ~ ${festivalEndDate.take(10)}",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = Color(0xFF3D608D),
            )

            if (!festivalAddress.isNullOrBlank()) {
                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = festivalAddress,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = Color(0xFF8F8F8F),
                )
            }

            if (!festivalDescription.isNullOrBlank()) {
                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text = festivalDescription,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color(0xFF121212),
                    lineHeight = 22.sp,
                )
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            // 지도보기 + 보러가기
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (
                    festivalLatitude != null &&
                    festivalLongitude != null
                ) {
                    Button(
                        onClick = {
                            onNavigateToMap(
                                festivalLatitude,
                                festivalLongitude,
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1),
                            contentColor = Color.White,
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                    ) {
                        Text(
                            text = "지도보기",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                        )
                    }
                }

                Button(
                    onClick = onSeeAllPrograms,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = Color.White,
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                ) {
                    Text(
                        text = "프로그램 보러가기",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                    )
                }
            }

            // 프로그램 미리보기
            if (programs.isNotEmpty()) {
                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Text(
                    text = "이 축제의 프로그램",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color(0xFF121212),
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                programs.forEach { program ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable {
                                onProgramClick(program.id)
                            },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when (program.category) {
                                        "exhibition" ->
                                            Color(0xFFE8F9FF)

                                        "seminar" ->
                                            Color(0xFFF0E9FF)

                                        "experience" ->
                                            Color(0xFFFFF0E6)

                                        else ->
                                            Color(0xFFF5F5F5)
                                    }
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = program.name.take(1),
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = Primary,
                            )
                        }

                        Spacer(
                            modifier = Modifier.width(12.dp)
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = program.name,
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color(0xFF121212),
                            )

                            Spacer(
                                modifier = Modifier.height(2.dp)
                            )

                            val categoryText =
                                when (program.category) {
                                    "food" -> "맛집"
                                    "exhibition" -> "전시"
                                    "seminar" -> "세미나"
                                    "experience" -> "체험"
                                    "event" -> "이벤트"
                                    else ->
                                        program.category ?: ""
                                }

                            if (categoryText.isNotEmpty()) {
                                Text(
                                    text = categoryText,
                                    fontFamily = Pretendard,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = Color(0xFF8F8F8F),
                                )
                            }

                            if (!program.location.isNullOrBlank()) {
                                Spacer(
                                    modifier = Modifier.height(2.dp)
                                )

                                Text(
                                    text = program.location,
                                    fontFamily = Pretendard,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 11.sp,
                                    color = Color(0xFFB0B0B0),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}