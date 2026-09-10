package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.alphacity.stamptour.network.dto.FestivalDetail
import com.alphacity.stamptour.network.dto.FestivalProgramSummary
import com.alphacity.stamptour.repository.HomeRepository
import composewebtest.generated.resources.Res
import composewebtest.generated.resources.header_left_arrow
import org.jetbrains.compose.resources.painterResource

private val Primary = Color(0xFF02CDF8)

private const val API_BASE_URL =
    "https://ollymoa-server.vercel.app"

private val Pretendard =
    FontFamily.SansSerif

private val MainGradient =
    Brush.horizontalGradient(
        colorStops = arrayOf(
            0.02f to Color(0xFF6092FF),
            0.36f to Color(0xFF2563EB),
            1.0f to Color(0xFF1551D3),
        ),
    )

// ============================================
// Image URL
// ============================================

private fun imageUrl(
    path: String?,
): String? {
    if (path.isNullOrBlank()) {
        return null
    }

    return if (
        path.startsWith("http://") ||
        path.startsWith("https://")
    ) {
        path
    } else {
        "$API_BASE_URL${
            if (path.startsWith("/")) {
                path
            } else {
                "/$path"
            }
        }"
    }
}

// ============================================
// Festival Detail Screen
// ============================================

@Composable
fun FestivalDetailScreen(
    festivalId: Int = 1,
    festivalName: String = "",
    festivalDescription: String? = null,
    festivalImageUrl: String? = null,
    festivalBannerUrl: String? = null,
    festivalStartDate: String = "",
    festivalEndDate: String = "",
    festivalAddress: String? = null,
    festivalLatitude: Double? = null,
    festivalLongitude: Double? = null,

    onBackClick: () -> Unit = {},

    // 축제 ID를 넘겨서 해당 축제의 전체 프로그램 목록으로 이동
    onSeeAllPrograms: (Int) -> Unit = {},

    onProgramClick: (Int) -> Unit = {},

    // 부모에서 지도 탭으로 전환 처리
    onNavigateToMap: (Double, Double) -> Unit = { _, _ -> },
) {
    val repository = remember {
        HomeRepository()
    }

    var festival by remember {
        mutableStateOf<FestivalDetail?>(null)
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    // ============================================
    // 축제 상세 조회
    // ============================================

    LaunchedEffect(festivalId) {
        isLoading = true
        errorMessage = null

        repository
            .getFestivalDetail(festivalId)
            .onSuccess {
                festival = it
            }
            .onFailure {
                festival = null

                errorMessage =
                    it.message
                        ?: "축제 정보를 불러오지 못했습니다."

                println(
                    "[FestivalDetailScreen] 축제 조회 실패: ${it.message}"
                )
            }

        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(
                rememberScrollState()
            ),
    ) {

        // ============================================
        // Top Bar
        // ============================================

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 14.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Image(
                painter = painterResource(
                    Res.drawable.header_left_arrow
                ),
                contentDescription = "뒤로가기",
                modifier = Modifier
                    .size(
                        width = 8.dp,
                        height = 15.dp,
                    )
                    .clickable {
                        onBackClick()
                    },
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Text(
                text = "오늘의 축제",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF121212),
            )
        }

        // ============================================
        // Loading
        // ============================================

        if (isLoading) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                contentAlignment = Alignment.Center,
            ) {

                CircularProgressIndicator(
                    color = Primary,
                )
            }

            return@Column
        }

        // ============================================
        // Error
        // ============================================

        if (festival == null) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {

                Text(
                    text =
                        errorMessage
                            ?: "축제 정보를 불러오지 못했습니다.",
                    fontFamily = Pretendard,
                    fontSize = 14.sp,
                    color = Color(0xFF8F8F8F),
                )
            }

            return@Column
        }

        val currentFestival = festival!!
        val programs = currentFestival.programs

        // ============================================
        // Festival Image
        // DB 이미지 → AsyncImage
        // ============================================

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    Color(0xFFE8E8E8)
                ),
            contentAlignment = Alignment.Center,
        ) {

            val festivalImage =
                imageUrl(
                    currentFestival.imageUrl
                )

            if (!festivalImage.isNullOrBlank()) {

                AsyncImage(
                    model = festivalImage,
                    contentDescription =
                        currentFestival.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )

            } else {

                Text(
                    text = currentFestival.name.take(1),
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = Color(0xFFB5B5B5),
                )
            }
        }

        // ============================================
        // Festival Information
        // ============================================

        Column(
            modifier = Modifier.padding(20.dp),
        ) {

            Text(
                text = currentFestival.name,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color(0xFF121212),
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text =
                    "${currentFestival.startDate.take(10)} ~ " +
                            currentFestival.endDate.take(10),
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = Color(0xFF3D608D),
            )

            // ============================================
            // Address
            // ============================================

            if (
                !currentFestival.address
                    .isNullOrBlank()
            ) {

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = currentFestival.address,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = Color(0xFF8F8F8F),
                )
            }

            // ============================================
            // Description
            // ============================================

            if (
                !currentFestival.description
                    .isNullOrBlank()
            ) {

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text = currentFestival.description,
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

            // ============================================
            // 지도보기 + 프로그램 보러가기
            // ============================================

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {

                // ========================================
                // 지도보기
                // ========================================

                if (
                    currentFestival.latitude != null &&
                    currentFestival.longitude != null
                ) {

                    Button(
                        onClick = {

                            onNavigateToMap(
                                currentFestival.latitude,
                                currentFestival.longitude,
                            )
                        },
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    Color(0xFF6366F1),
                                contentColor =
                                    Color.White,
                            ),
                        shape =
                            RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                    ) {

                        Text(
                            text = "지도보기",
                            fontFamily = Pretendard,
                            fontWeight =
                                FontWeight.SemiBold,
                            fontSize = 15.sp,
                        )
                    }
                }

                // ========================================
                // 프로그램 보러가기
                // ========================================

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(
                            RoundedCornerShape(10.dp)
                        )
                        .background(
                            MainGradient
                        )
                        .clickable {

                            // 현재 축제의 고유 ID 전달
                            onSeeAllPrograms(
                                currentFestival.id
                            )
                        },
                    contentAlignment =
                        Alignment.Center,
                ) {

                    Text(
                        text = "프로그램 보러가기",
                        fontFamily = Pretendard,
                        fontWeight =
                            FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color.White,
                    )
                }
            }

            // ============================================
            // 프로그램 미리보기
            // ============================================

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

                    FestivalProgramPreview(
                        program = program,
                        onClick = {
                            onProgramClick(
                                program.id
                            )
                        },
                    )
                }
            }
        }
    }
}

// ============================================
// Festival Program Preview
// ============================================

@Composable
private fun FestivalProgramPreview(
    program: FestivalProgramSummary,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable {
                onClick()
            },
        verticalAlignment =
            Alignment.CenterVertically,
    ) {

        // ============================================
        // 프로그램 썸네일
        // DB 이미지 → AsyncImage
        // ============================================

        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(
                    RoundedCornerShape(10.dp)
                )
                .background(
                    Color(0xFFF5F5F5)
                ),
            contentAlignment =
                Alignment.Center,
        ) {

            val programImage =
                imageUrl(
                    program.imageUrl
                )

            if (!programImage.isNullOrBlank()) {

                AsyncImage(
                    model = programImage,
                    contentDescription =
                        program.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale =
                        ContentScale.Crop,
                )

            } else {

                // 이미지가 없을 경우 기존 첫 글자
                Text(
                    text = program.name.take(1),
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Primary,
                )
            }
        }

        Spacer(
            modifier = Modifier.width(12.dp)
        )

        // ============================================
        // 프로그램 정보
        // ============================================

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

                    "food" ->
                        "맛집"

                    "exhibition" ->
                        "전시"

                    "seminar" ->
                        "세미나"

                    "experience" ->
                        "체험"

                    "event" ->
                        "이벤트"

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

            if (
                !program.location
                    .isNullOrBlank()
            ) {

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