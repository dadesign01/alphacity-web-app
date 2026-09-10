@file:OptIn(
    androidx.compose.ui.ExperimentalComposeUiApi::class,
)

package com.alphacity.stamptour.ui.screen

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.alphacity.stamptour.network.ApiService
import com.alphacity.stamptour.network.dto.MissionItem
import com.alphacity.stamptour.network.dto.ProgramItem
import com.alphacity.stamptour.repository.ProgramDetailRepository
import composewebtest.generated.resources.Res
import composewebtest.generated.resources.header_left_arrow
import composewebtest.generated.resources.map_icon
import composewebtest.generated.resources.mission_check_icon
import composewebtest.generated.resources.program_stamp_info
import composewebtest.generated.resources.road_navigation
import composewebtest.generated.resources.tts_sound
import kotlinx.browser.document
import org.jetbrains.compose.resources.painterResource
import org.w3c.dom.HTMLAudioElement
import composewebtest.theme.MainGradient

private val Primary = Color(0xFF02CDF8)
private val Pretendard = FontFamily.SansSerif

private const val API_BASE_URL =
    "https://ollymoa-server.vercel.app"

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

@Composable
fun ProgramDetailScreen(
    programId: Int = 1,
    programName: String = "",
    category: String = "",
    onBackClick: () -> Unit = {},
    onNavigateToMap: (
        lat: Double?,
        lng: Double?,
    ) -> Unit = { _, _ -> },
) {
    // ============================================================
    // QR 스캐너 화면
    //
    // 중요:
    // 기존처럼 상세 화면 아래에 QrScannerHost를 붙이지 않는다.
    // showQrScanner가 true이면 이 화면 자체를 QR 스캐너로 교체한다.
    // ============================================================

    var showQrScanner by remember {
        mutableStateOf(false)
    }

    if (showQrScanner) {
        QrScannerFullScreen(
            onQrDetected = { qrText ->
                println(
                    "[ProgramDetailScreen] QR detected: $qrText"
                )

                // 현재는 QR 문자열만 확인한다.
                // 로그인 / 스탬프 / 위치인증은 이후 연결.
            },
            onClose = {
                showQrScanner = false
            },
        )

        return
    }

    val repository = remember {
        ProgramDetailRepository(ApiService)
    }

    var program by remember(programId) {
        mutableStateOf<ProgramItem?>(null)
    }

    var missions by remember(programId) {
        mutableStateOf<List<MissionItem>>(emptyList())
    }

    var isLoading by remember(programId) {
        mutableStateOf(true)
    }

    var errorMessage by remember(programId) {
        mutableStateOf<String?>(null)
    }

    var showMissionSheet by remember {
        mutableStateOf(false)
    }

    var selectedMission by remember {
        mutableStateOf<MissionItem?>(null)
    }

    var showRestrictionAlert by remember {
        mutableStateOf(false)
    }

    var restrictionMessage by remember {
        mutableStateOf("")
    }

    var message by remember {
        mutableStateOf<String?>(null)
    }

    var isSpeaking by remember {
        mutableStateOf(false)
    }

    var ttsAudio by remember {
        mutableStateOf<HTMLAudioElement?>(null)
    }

    // 화면을 벗어나면 TTS 정지
    DisposableEffect(Unit) {
        onDispose {
            ttsAudio?.pause()
            ttsAudio?.currentTime = 0.0
            ttsAudio = null
        }
    }

    // ============================================================
    // 프로그램 조회
    // ============================================================

    LaunchedEffect(programId) {
        isLoading = true
        errorMessage = null

        repository
            .getProgramById(programId)
            .onSuccess {
                program = it
            }
            .onFailure {
                errorMessage =
                    it.message
                        ?: "프로그램을 불러오지 못했습니다."
            }

        repository
            .getProgramMissions(programId)
            .onSuccess {
                missions = it
            }
            .onFailure {
                missions = emptyList()
            }

        isLoading = false
    }

    // ============================================================
    // 화면
    // ============================================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        DetailHeader(
            title = when (program?.category ?: category) {
                "food" -> "맛집 상세"
                "seminar" -> "세미나 상세"
                else -> "프로그램 상세"
            },
            onBackClick = onBackClick,
        )

        HorizontalDivider(
            color = Color(0xFFE2E2E2),
            thickness = 1.dp,
        )

        when {
            // ====================================================
            // Loading
            // ====================================================

            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = Primary,
                    )
                }
            }

            // ====================================================
            // Error
            // ====================================================

            program == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text =
                                errorMessage
                                    ?: "프로그램 정보를 불러올 수 없습니다.",
                            fontFamily = Pretendard,
                            fontSize = 14.sp,
                            color = Color(0xFF595959),
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        TextButton(
                            onClick = {
                                // 현재는 화면 재진입으로 재시도
                            },
                        ) {
                            Text(
                                text = "확인",
                                color = Primary,
                                fontFamily = Pretendard,
                            )
                        }
                    }
                }
            }

            // ====================================================
            // 정상
            // ====================================================

            else -> {
                val currentProgram = program!!

                Box(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(
                                rememberScrollState()
                            ),
                    ) {
                        // ====================================================
                        // 프로그램 이미지
                        // ====================================================

                        val programImage =
                            imageUrl(
                                currentProgram.imageUrl
                            )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 20.dp,
                                    vertical = 20.dp,
                                )
                                .aspectRatio(
                                    390f / 290f
                                )
                                .clip(
                                    RoundedCornerShape(15.dp)
                                )
                                .background(
                                    Color(0xFFF8F8F8)
                                ),
                            contentAlignment =
                                Alignment.Center,
                        ) {
                            if (!programImage.isNullOrBlank()) {
                                AsyncImage(
                                    model = programImage,
                                    contentDescription =
                                        currentProgram.name,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(
                                            RoundedCornerShape(15.dp)
                                        ),
                                    contentScale =
                                        ContentScale.Crop,
                                )
                            } else {
                                Text(
                                    text =
                                        currentProgram.name
                                            .take(1),
                                    fontFamily =
                                        Pretendard,
                                    fontWeight =
                                        FontWeight.Bold,
                                    fontSize = 40.sp,
                                    color =
                                        Color(0xFFB5B5B5),
                                )
                            }
                        }

                        // ====================================================
                        // 카테고리 / 상태
                        // ====================================================

                        val badgeLabel =
                            when (currentProgram.category) {
                                "food" -> "맛집"
                                "seminar" -> "세미나"
                                "exhibition" -> "전시"
                                "experience" -> "체험"
                                else -> "기타"
                            }

                        val isInProgress =
                            currentProgram.status ==
                                    "in_progress"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 20.dp
                                )
                                .padding(
                                    top = 4.dp,
                                    bottom = 10.dp,
                                ),
                            verticalAlignment =
                                Alignment.CenterVertically,
                            horizontalArrangement =
                                Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .aspectRatio(
                                        60f / 28f
                                    )
                                    .clip(
                                        RoundedCornerShape(
                                            15.dp
                                        )
                                    )
                                    .border(
                                        width = 1.dp,
                                        color =
                                            Color(0xFF2563EB),
                                        shape =
                                            RoundedCornerShape(
                                                15.dp
                                            ),
                                    ),
                                contentAlignment =
                                    Alignment.Center,
                            ) {
                                Text(
                                    text = badgeLabel,
                                    fontFamily = Pretendard,
                                    fontWeight =
                                        FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color =
                                        Color(0xFF2563EB),
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .aspectRatio(
                                        60f / 28f
                                    )
                                    .clip(
                                        RoundedCornerShape(
                                            15.dp
                                        )
                                    )
                                    .background(
                                        if (isInProgress) {
                                            Color(0xFF2563EB)
                                        } else {
                                            Color(0xFFEF9E1C)
                                        }
                                    ),
                                contentAlignment =
                                    Alignment.Center,
                            ) {
                                Text(
                                    text =
                                        if (isInProgress) {
                                            "운영중"
                                        } else {
                                            "예정"
                                        },
                                    fontFamily = Pretendard,
                                    fontWeight =
                                        FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                )
                            }
                        }

                        // ====================================================
                        // 프로그램 이름
                        // ====================================================

                        Text(
                            text = currentProgram.name,
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF121212),
                            modifier = Modifier
                                .padding(
                                    horizontal = 20.dp
                                )
                                .padding(
                                    bottom = 10.dp
                                ),
                        )

                        // ====================================================
                        // 위치 / 운영일 / 운영시간 / 스탬프
                        // ====================================================

                        Column(
                            modifier = Modifier
                                .padding(
                                    horizontal = 20.dp
                                ),
                            verticalArrangement =
                                Arrangement.spacedBy(10.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(
                                        350f / 56f
                                    )
                                    .clip(
                                        RoundedCornerShape(
                                            12.dp
                                        )
                                    )
                                    .background(
                                        Color(0xFFF8F8F8)
                                    ),
                                contentAlignment =
                                    Alignment.CenterStart,
                            ) {
                                Text(
                                    text =
                                        currentProgram.location
                                            ?: "",
                                    fontFamily = Pretendard,
                                    fontWeight =
                                        FontWeight.Normal,
                                    fontSize = 14.sp,
                                    color =
                                        Color(0xFF464646),
                                    modifier =
                                        Modifier.padding(
                                            horizontal = 16.dp
                                        ),
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(
                                        350f / 56f
                                    )
                                    .clip(
                                        RoundedCornerShape(
                                            12.dp
                                        )
                                    )
                                    .background(
                                        Color(0xFFF8F8F8)
                                    ),
                                contentAlignment =
                                    Alignment.CenterStart,
                            ) {
                                Text(
                                    text =
                                        currentProgram
                                            .operatingDays
                                            ?: "",
                                    fontFamily = Pretendard,
                                    fontWeight =
                                        FontWeight.Normal,
                                    fontSize = 14.sp,
                                    color =
                                        Color(0xFF464646),
                                    modifier =
                                        Modifier.padding(
                                            horizontal = 16.dp
                                        ),
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(
                                        350f / 56f
                                    )
                                    .clip(
                                        RoundedCornerShape(
                                            12.dp
                                        )
                                    )
                                    .background(
                                        Color(0xFFF8F8F8)
                                    ),
                                contentAlignment =
                                    Alignment.CenterStart,
                            ) {
                                Text(
                                    text =
                                        currentProgram
                                            .operatingHours
                                            ?: "",
                                    fontFamily = Pretendard,
                                    fontWeight =
                                        FontWeight.Normal,
                                    fontSize = 14.sp,
                                    color =
                                        Color(0xFF464646),
                                    modifier =
                                        Modifier.padding(
                                            horizontal = 16.dp
                                        ),
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(
                                        350f / 56f
                                    )
                                    .clip(
                                        RoundedCornerShape(
                                            12.dp
                                        )
                                    )
                                    .background(
                                        Color(0xFFEDF7FF)
                                    ),
                                contentAlignment =
                                    Alignment.CenterStart,
                            ) {
                                Row(
                                    modifier =
                                        Modifier.padding(
                                            horizontal = 16.dp
                                        ),
                                    verticalAlignment =
                                        Alignment.CenterVertically,
                                ) {
                                    Image(
                                        painter =
                                            painterResource(
                                                Res.drawable
                                                    .program_stamp_info
                                            ),
                                        contentDescription =
                                            "스탬프 1개 적립",
                                        modifier =
                                            Modifier
                                                .size(24.dp)
                                                .zIndex(0f),
                                        contentScale =
                                            ContentScale.Fit,
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.width(
                                                15.dp
                                            )
                                    )

                                    Text(
                                        text =
                                            "스탬프 1개 적립",
                                        fontFamily =
                                            Pretendard,
                                        fontWeight =
                                            FontWeight.Normal,
                                        fontSize = 14.sp,
                                        color =
                                            Color(0xFF2563EB),
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            color = Color(0xFFB5B5B5),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(
                                horizontal = 20.dp,
                                vertical = 14.dp,
                            ),
                        )

                        // ====================================================
                        // 소개
                        // ====================================================

                        if (
                            !currentProgram.description
                                .isNullOrBlank()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(
                                        horizontal = 20.dp
                                    )
                                    .padding(top = 4.dp),
                                verticalAlignment =
                                    Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "소개",
                                    fontFamily = Pretendard,
                                    fontWeight =
                                        FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.Black,
                                )

                                if (
                                    currentProgram.category !=
                                    "food"
                                ) {
                                    Spacer(
                                        modifier =
                                            Modifier.width(8.dp)
                                    )

                                    val ttsUrl =
                                        currentProgram.ttsUrl
                                            ?.trim()
                                            ?.takeIf {
                                                it.isNotEmpty()
                                            }
                                            ?.let { url ->
                                                if (
                                                    url.startsWith(
                                                        "http://"
                                                    ) ||
                                                    url.startsWith(
                                                        "https://"
                                                    )
                                                ) {
                                                    url
                                                } else {
                                                    "$API_BASE_URL${
                                                        if (
                                                            url.startsWith(
                                                                "/"
                                                            )
                                                        ) {
                                                            url
                                                        } else {
                                                            "/$url"
                                                        }
                                                    }"
                                                }
                                            }

                                    val hasTts =
                                        !ttsUrl.isNullOrBlank()

                                    Box(
                                        modifier = Modifier
                                            .width(15.dp)
                                            .aspectRatio(
                                                15f / 14f
                                            )
                                            .then(
                                                if (hasTts) {
                                                    Modifier.clickable {
                                                        if (
                                                            isSpeaking
                                                        ) {
                                                            ttsAudio
                                                                ?.pause()

                                                            ttsAudio
                                                                ?.currentTime =
                                                                0.0

                                                            isSpeaking =
                                                                false
                                                        } else {
                                                            val url =
                                                                ttsUrl
                                                                    ?: return@clickable

                                                            ttsAudio
                                                                ?.pause()

                                                            ttsAudio
                                                                ?.currentTime =
                                                                0.0

                                                            val audio =
                                                                document
                                                                    .createElement(
                                                                        "audio"
                                                                    )
                                                                        as HTMLAudioElement

                                                            audio.src =
                                                                url

                                                            audio.preload =
                                                                "auto"

                                                            audio.onended =
                                                                {
                                                                    isSpeaking =
                                                                        false
                                                                }

                                                            audio.onerror =
                                                                { _, _, _, _, _ ->
                                                                    isSpeaking =
                                                                        false
                                                                    null
                                                                }

                                                            ttsAudio =
                                                                audio

                                                            isSpeaking =
                                                                true

                                                            audio.play()
                                                        }
                                                    }
                                                } else {
                                                    Modifier
                                                }
                                            ),
                                        contentAlignment =
                                            Alignment.Center,
                                    ) {
                                        if (isSpeaking) {
                                            TTSSoundBars()
                                        } else {
                                            Image(
                                                painter =
                                                    painterResource(
                                                        Res.drawable
                                                            .tts_sound
                                                    ),
                                                contentDescription =
                                                    if (hasTts) {
                                                        "TTS 재생"
                                                    } else {
                                                        "TTS 없음"
                                                    },
                                                modifier =
                                                    Modifier.fillMaxSize(),
                                                contentScale =
                                                    ContentScale.Fit,
                                                alpha =
                                                    if (hasTts) {
                                                        1f
                                                    } else {
                                                        0.35f
                                                    },
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(
                                modifier =
                                    Modifier.height(8.dp)
                            )

                            Text(
                                text =
                                    currentProgram.description
                                        ?: "",
                                fontFamily =
                                    FontFamily.SansSerif,
                                fontWeight =
                                    FontWeight.Normal,
                                fontSize = 16.sp,
                                lineHeight = 22.sp,
                                color =
                                    Color(0xFF1B1B1B),
                                modifier =
                                    Modifier.padding(
                                        horizontal = 20.dp
                                    ),
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(20.dp)
                            )

                            Text(
                                text = "참여 가능 미션",
                                fontFamily = Pretendard,
                                fontWeight =
                                    FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.Black,
                                modifier =
                                    Modifier.padding(
                                        horizontal = 20.dp
                                    ),
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(10.dp)
                            )

                            val participationMissionText =
                                if (
                                    currentProgram.category ==
                                    "seminar"
                                ) {
                                    missions
                                        .firstOrNull()
                                        ?.name
                                        ?: "QR 코드 스캔 미션"
                                } else {
                                    "QR 코드 스캔 미션"
                                }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = 20.dp
                                    )
                                    .aspectRatio(
                                        350f / 56f
                                    )
                                    .clip(
                                        RoundedCornerShape(
                                            12.dp
                                        )
                                    )
                                    .background(
                                        Color(0xFFF8F8F8)
                                    ),
                                contentAlignment =
                                    Alignment.CenterStart,
                            ) {
                                Row(
                                    modifier =
                                        Modifier.padding(
                                            horizontal = 16.dp
                                        ),
                                    verticalAlignment =
                                        Alignment.CenterVertically,
                                    horizontalArrangement =
                                        Arrangement.spacedBy(
                                            15.dp
                                        ),
                                ) {
                                    Image(
                                        painter =
                                            painterResource(
                                                Res.drawable
                                                    .mission_check_icon
                                            ),
                                        contentDescription = null,
                                        modifier =
                                            Modifier.size(20.dp),
                                    )

                                    Text(
                                        text =
                                            participationMissionText,
                                        fontFamily =
                                            Pretendard,
                                        fontWeight =
                                            FontWeight.Normal,
                                        fontSize = 14.sp,
                                        color =
                                            Color(0xFF464646),
                                    )
                                }
                            }
                        }

                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(
                                    390f / 256f
                                )
                        )
                    }

                    // ========================================================
                    // 하단 고정 영역
                    // ========================================================

                    Box(
                        modifier = Modifier
                            .align(
                                Alignment.BottomCenter
                            )
                            .fillMaxWidth()
                            .aspectRatio(
                                390f / 256f
                            )
                            .zIndex(10f)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(
                                            alpha = 0f
                                        ),
                                        Color(0xFFEDF7FF)
                                            .copy(alpha = 1f),
                                    ),
                                )
                            ),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(
                                    Alignment.BottomCenter
                                )
                                .padding(
                                    horizontal = 20.dp
                                )
                                .padding(
                                    bottom = 20.dp
                                ),
                            verticalArrangement =
                                Arrangement.spacedBy(11.dp),
                        ) {
                            Row(
                                modifier =
                                    Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.spacedBy(
                                        11.dp
                                    ),
                            ) {
                                // ==================================================
                                // 길찾기
                                // ==================================================

                                Box(
                                    modifier = Modifier
                                        .weight(126f)
                                        .aspectRatio(
                                            126f / 56f
                                        )
                                        .clip(
                                            RoundedCornerShape(
                                                12.dp
                                            )
                                        )
                                        .background(
                                            Color.White
                                        )
                                        .border(
                                            width = 1.dp,
                                            color =
                                                Color(0xFF2563EB),
                                            shape =
                                                RoundedCornerShape(
                                                    12.dp
                                                ),
                                        )
                                        .clickable {
                                            val address =
                                                currentProgram
                                                    .location

                                            if (
                                                !address.isNullOrBlank()
                                            ) {
                                                kotlinx.browser
                                                    .window
                                                    .location
                                                    .href =
                                                    "https://map.naver.com/p/search/$address"
                                            } else {
                                                message =
                                                    "주소 정보가 등록되지 않은 프로그램입니다."
                                            }
                                        },
                                    contentAlignment =
                                        Alignment.Center,
                                ) {
                                    Row(
                                        verticalAlignment =
                                            Alignment.CenterVertically,
                                        horizontalArrangement =
                                            Arrangement.spacedBy(
                                                15.dp
                                            ),
                                    ) {
                                        Image(
                                            painter =
                                                painterResource(
                                                    Res.drawable
                                                        .road_navigation
                                                ),
                                            contentDescription =
                                                "길찾기",
                                            modifier =
                                                Modifier.size(
                                                    20.dp
                                                ),
                                        )

                                        Text(
                                            text = "길찾기",
                                            fontFamily =
                                                Pretendard,
                                            fontWeight =
                                                FontWeight.SemiBold,
                                            fontSize = 16.sp,
                                            color =
                                                Color(0xFF2563EB),
                                        )
                                    }
                                }

                                // ==================================================
                                // 지도에서 보기
                                // ==================================================

                                Box(
                                    modifier = Modifier
                                        .weight(213f)
                                        .aspectRatio(
                                            213f / 56f
                                        )
                                        .clip(
                                            RoundedCornerShape(
                                                12.dp
                                            )
                                        )
                                        .background(
                                            Color.White
                                        )
                                        .border(
                                            width = 1.dp,
                                            color =
                                                Color(0xFF2563EB),
                                            shape =
                                                RoundedCornerShape(
                                                    12.dp
                                                ),
                                        )
                                        .clickable {
                                            if (
                                                currentProgram
                                                    .latitude !=
                                                null &&
                                                currentProgram
                                                    .longitude !=
                                                null
                                            ) {
                                                onNavigateToMap(
                                                    currentProgram
                                                        .latitude,
                                                    currentProgram
                                                        .longitude,
                                                )
                                            } else {
                                                message =
                                                    "위치 정보가 등록되지 않은 프로그램입니다."
                                            }
                                        },
                                    contentAlignment =
                                        Alignment.Center,
                                ) {
                                    Row(
                                        verticalAlignment =
                                            Alignment.CenterVertically,
                                        horizontalArrangement =
                                            Arrangement.spacedBy(
                                                15.dp
                                            ),
                                    ) {
                                        Image(
                                            painter =
                                                painterResource(
                                                    Res.drawable
                                                        .map_icon
                                                ),
                                            contentDescription =
                                                "지도에서 보기",
                                            modifier =
                                                Modifier.size(
                                                    20.dp
                                                ),
                                        )

                                        Text(
                                            text =
                                                "지도에서 보기",
                                            fontFamily =
                                                Pretendard,
                                            fontWeight =
                                                FontWeight.Medium,
                                            fontSize = 16.sp,
                                            color =
                                                Color(0xFF2563EB),
                                        )
                                    }
                                }
                            }

                            // ==================================================
                            // 참여하기
                            // ==================================================

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(
                                        350f / 56f
                                    )
                                    .clip(
                                        RoundedCornerShape(
                                            12.dp
                                        )
                                    )
                                    .background(
                                        MainGradient
                                    )
                                    .clickable {
                                        message =
                                            "QR코드를 스캔하여 참여하시겠습니까?"
                                    },
                                contentAlignment =
                                    Alignment.Center,
                            ) {
                                Text(
                                    text = "참여하기",
                                    fontFamily = Pretendard,
                                    fontWeight =
                                        FontWeight.Medium,
                                    fontSize = 16.sp,
                                    color = Color.White,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ================================================================
    // 참여하기 확인창
    // ================================================================

    if (message != null) {
        AlertDialog(
            onDismissRequest = {
                message = null
            },
            title = {
                Text(
                    text = "참여하기",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = message!!,
                    fontFamily = Pretendard,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        message = null

                        // 확인 즉시 QR 스캐너 화면으로 전환
                        showQrScanner = true
                    },
                ) {
                    Text(
                        text = "확인",
                        fontFamily = Pretendard,
                        color = Primary,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        message = null
                    },
                ) {
                    Text(
                        text = "취소",
                        fontFamily = Pretendard,
                        color = Color(0xFF888888),
                    )
                }
            },
        )
    }

    // ================================================================
    // 제한 알림
    // ================================================================

    if (showRestrictionAlert) {
        AlertDialog(
            onDismissRequest = {
                showRestrictionAlert = false
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
                    text = restrictionMessage,
                    fontFamily = Pretendard,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestrictionAlert = false
                    },
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

    // ================================================================
    // 미션 선택
    // ================================================================

    if (
        showMissionSheet &&
        program != null
    ) {
        MissionSelectionBottomSheet(
            missions = missions,
            category = program!!.category ?: "",
            onSelectMission = { mission ->
                showMissionSheet = false

                val currentCategory =
                    program!!.category

                if (
                    currentCategory == "seminar" &&
                    mission.type != "stay_time"
                ) {
                    restrictionMessage =
                        "해당 프로그램은 체류시간 미션만 참여 가능합니다."

                    showRestrictionAlert = true
                    return@MissionSelectionBottomSheet
                }

                if (
                    currentCategory != "seminar" &&
                    mission.type == "stay_time"
                ) {
                    restrictionMessage =
                        "해당 장소는 체류시간 미션 대상이 아닙니다. 다른 미션을 선택해주세요."

                    showRestrictionAlert = true
                    return@MissionSelectionBottomSheet
                }

                if (
                    mission.isCompleted == true
                ) {
                    return@MissionSelectionBottomSheet
                }

                selectedMission = mission
            },
            onDismiss = {
                showMissionSheet = false
            },
        )
    }

    // ================================================================
    // 선택된 미션
    // ================================================================

    selectedMission?.let { mission ->
        val currentProgram =
            program ?: return@let

        when (mission.type) {
            "quiz" -> {
                QuizMissionScreen(
                    mission = mission,
                    programLat =
                        currentProgram.latitude,
                    programLng =
                        currentProgram.longitude,
                    onDismiss = {
                        selectedMission = null
                    },
                    onCompleted = {
                        selectedMission = null
                    },
                )
            }

            "location_auth" -> {
                LocationMissionScreen(
                    mission = mission,
                    programLat =
                        currentProgram.latitude,
                    programLng =
                        currentProgram.longitude,
                    onDismiss = {
                        selectedMission = null
                    },
                    onCompleted = {
                        selectedMission = null
                    },
                )
            }

            "stay_time" -> {
                StayTimeMissionScreen(
                    mission = mission,
                    programLat =
                        currentProgram.latitude,
                    programLng =
                        currentProgram.longitude,
                    onDismiss = {
                        selectedMission = null
                    },
                    onCompleted = {
                        selectedMission = null
                        message = "미션 완료!"
                    },
                )
            }
        }
    }
}

// ============================================================================
// QR Scanner Full Screen
// ============================================================================

@Composable
private fun QrScannerFullScreen(
    onQrDetected: (String) -> Unit,
    onClose: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        // 실제 WASM QR 카메라
        QrScannerHost(
            onQrDetected = onQrDetected,
            onClose = onClose,
        )

        // 닫기 버튼
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    start = 20.dp,
                    top = 20.dp,
                )
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    Color.Black.copy(alpha = 0.55f)
                )
                .clickable {
                    onClose()
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "‹",
                fontFamily = Pretendard,
                fontSize = 34.sp,
                color = Color.White,
            )
        }

        // 안내 문구
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 35.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally,
        ) {
            Text(
                text = "QR코드 스캔",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color.White,
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "카메라로 QR코드를 비춰주세요.",
                fontFamily = Pretendard,
                fontSize = 14.sp,
                color =
                    Color.White.copy(
                        alpha = 0.85f
                    ),
            )
        }
    }
}

// ============================================================================
// Header
// ============================================================================

@Composable
private fun DetailHeader(
    title: String,
    onBackClick: () -> Unit,
) {
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
            contentScale = ContentScale.Fit,
        )

        Spacer(
            modifier = Modifier.width(12.dp)
        )

        Text(
            text = title,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color(0xFF121212),
        )
    }
}

// ============================================================================
// Mission Selection Bottom Sheet
// ============================================================================

@Composable
private fun MissionSelectionBottomSheet(
    missions: List<MissionItem>,
    category: String,
    onSelectMission: (MissionItem) -> Unit,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(alpha = 0.45f)
            )
            .clickable {
                onDismiss()
            },
        contentAlignment =
            Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        topStart = 28.dp,
                        topEnd = 28.dp,
                    )
                )
                .background(Color.White)
                .clickable { },
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 14.dp)
                    .width(55.dp)
                    .height(6.dp)
                    .clip(
                        RoundedCornerShape(100.dp)
                    )
                    .background(
                        Color(0xFFD9D9D9)
                    )
                    .align(
                        Alignment.CenterHorizontally
                    ),
            )

            Text(
                text = "미션 선택",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF121212),
                modifier = Modifier.padding(
                    horizontal = 20.dp,
                    vertical = 20.dp,
                ),
            )

            Text(
                text = "참여할 미션을 선택하세요.",
                fontFamily = Pretendard,
                fontSize = 13.sp,
                color = Color(0xFF828282),
                modifier = Modifier.padding(
                    horizontal = 20.dp
                ),
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            HorizontalDivider(
                color = Color(0xFFE2E2E2)
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            if (missions.isEmpty()) {
                Text(
                    text = "등록된 미션이 없습니다.",
                    fontFamily = Pretendard,
                    fontSize = 13.sp,
                    color = Color(0xFF888888),
                    modifier = Modifier.padding(
                        horizontal = 20.dp,
                        vertical = 10.dp,
                    ),
                )
            }

            missions.forEach { mission ->
                val isAvailable =
                    if (category == "seminar") {
                        mission.type == "stay_time"
                    } else {
                        mission.type == "quiz" ||
                                mission.type ==
                                "location_auth"
                    }

                val isCompleted =
                    mission.isCompleted == true

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 20.dp,
                            vertical = 4.dp,
                        )
                        .clip(
                            RoundedCornerShape(12.dp)
                        )
                        .background(
                            if (isCompleted) {
                                Color(0xFFF0FFF4)
                            } else {
                                Color(0xFFF8F9FA)
                            }
                        )
                        .then(
                            if (!isCompleted) {
                                Modifier.clickable {
                                    onSelectMission(
                                        mission
                                    )
                                }
                            } else {
                                Modifier
                            }
                        )
                        .padding(14.dp),
                    verticalAlignment =
                        Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                when (mission.type) {
                                    "quiz" ->
                                        Color(0xFFEFF6FF)

                                    "location_auth" ->
                                        Color(0xFFF0FFF4)

                                    "stay_time" ->
                                        Color(0xFFFFFBEB)

                                    else ->
                                        Color(0xFFF3F4F6)
                                }
                            ),
                        contentAlignment =
                            Alignment.Center,
                    ) {
                        Text(
                            text = when (mission.type) {
                                "quiz" -> "Q"
                                "location_auth" -> "📍"
                                "stay_time" -> "T"
                                else -> "?"
                            },
                            fontFamily = Pretendard,
                            fontWeight =
                                FontWeight.Bold,
                            fontSize = 17.sp,
                            color = when (mission.type) {
                                "quiz" ->
                                    Color(0xFF2563EB)

                                "location_auth" ->
                                    Color(0xFF16A34A)

                                "stay_time" ->
                                    Color(0xFFD97706)

                                else ->
                                    Color(0xFF6B7280)
                            },
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.width(12.dp)
                    )

                    Column(
                        modifier =
                            Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically,
                            horizontalArrangement =
                                Arrangement.spacedBy(
                                    6.dp
                                ),
                        ) {
                            Text(
                                text = mission.name,
                                fontFamily =
                                    Pretendard,
                                fontWeight =
                                    FontWeight.Medium,
                                fontSize = 14.sp,
                                color =
                                    Color(0xFF121212),
                            )

                            Text(
                                text =
                                    when (mission.type) {
                                        "quiz" ->
                                            "퀴즈"

                                        "location_auth" ->
                                            "위치 인증"

                                        "stay_time" ->
                                            "체류시간"

                                        else ->
                                            mission.type
                                    },
                                fontFamily =
                                    Pretendard,
                                fontWeight =
                                    FontWeight.SemiBold,
                                fontSize = 10.sp,
                                color = Color.White,
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            100.dp
                                        )
                                    )
                                    .background(
                                        if (isAvailable) {
                                            when (
                                                mission.type
                                            ) {
                                                "quiz" ->
                                                    Color(
                                                        0xFF2563EB
                                                    )

                                                "location_auth" ->
                                                    Color(
                                                        0xFF16A34A
                                                    )

                                                "stay_time" ->
                                                    Color(
                                                        0xFFD97706
                                                    )

                                                else ->
                                                    Color(
                                                        0xFF6B7280
                                                    )
                                            }
                                        } else {
                                            Color(
                                                0xFFAAAAAA
                                            )
                                        }
                                    )
                                    .padding(
                                        horizontal = 6.dp,
                                        vertical = 1.dp,
                                    ),
                            )
                        }

                        val detail =
                            when (mission.type) {
                                "quiz" ->
                                    mission.question

                                "location_auth" ->
                                    mission.place?.name

                                "stay_time" ->
                                    "${mission.place?.name ?: ""} " +
                                            "${mission.stayMinutes ?: 0}분"

                                else ->
                                    null
                            }

                        if (!detail.isNullOrBlank()) {
                            Text(
                                text = detail,
                                fontFamily =
                                    Pretendard,
                                fontSize = 12.sp,
                                color =
                                    Color(0xFF888888),
                                maxLines = 1,
                            )
                        }
                    }

                    Text(
                        text = when {
                            isCompleted -> "✓"
                            !isAvailable -> "×"
                            else -> "›"
                        },
                        fontFamily = Pretendard,
                        fontWeight =
                            FontWeight.Bold,
                        fontSize = 20.sp,
                        color = when {
                            isCompleted ->
                                Color(0xFF16A34A)

                            !isAvailable ->
                                Color(0xFFCCCCCC)

                            else ->
                                Color(0xFFCCCCCC)
                        },
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(32.dp)
            )
        }
    }
}

// ============================================================================
// TTS Sound Bars
// ============================================================================

@Composable
private fun TTSSoundBars() {
    val transition =
        rememberInfiniteTransition(
            label = "tts"
        )

    val bar1 =
        transition.animateFloat(
            initialValue = 5f,
            targetValue = 14f,
            animationSpec =
                infiniteRepeatable(
                    tween(400),
                    RepeatMode.Reverse,
                ),
            label = "bar1",
        )

    val bar2 =
        transition.animateFloat(
            initialValue = 4f,
            targetValue = 16f,
            animationSpec =
                infiniteRepeatable(
                    tween(500),
                    RepeatMode.Reverse,
                ),
            label = "bar2",
        )

    val bar3 =
        transition.animateFloat(
            initialValue = 6f,
            targetValue = 12f,
            animationSpec =
                infiniteRepeatable(
                    tween(350),
                    RepeatMode.Reverse,
                ),
            label = "bar3",
        )

    Row(
        horizontalArrangement =
            Arrangement.spacedBy(2.5.dp),
        verticalAlignment =
            Alignment.CenterVertically,
        modifier = Modifier.height(22.dp),
    ) {
        listOf(
            bar1,
            bar2,
            bar3,
        ).forEach { bar ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(
                        bar.value.dp
                    )
                    .clip(
                        RoundedCornerShape(2.dp)
                    )
                    .background(
                        Primary
                    ),
            )
        }
    }
}