package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alphacity.stamptour.getCurrentBrowserLocation
import com.alphacity.stamptour.network.ApiService
import com.alphacity.stamptour.repository.ProgramDetailRepository
import composewebtest.generated.resources.Res
import composewebtest.generated.resources.location_gps
import composewebtest.theme.MainGradient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private val Pretendard = FontFamily.SansSerif

private const val QR_LOCATION_AUTH_RADIUS_METERS = 50.0

private fun calculateQrDistanceMeters(
    latitude1: Double,
    longitude1: Double,
    latitude2: Double,
    longitude2: Double,
): Double {
    val earthRadius = 6_371_000.0

    val lat1 =
        latitude1 * PI / 180.0

    val lat2 =
        latitude2 * PI / 180.0

    val deltaLat =
        (latitude2 - latitude1) *
                PI / 180.0

    val deltaLng =
        (longitude2 - longitude1) *
                PI / 180.0

    val a =
        sin(deltaLat / 2) *
                sin(deltaLat / 2) +
                cos(lat1) *
                cos(lat2) *
                sin(deltaLng / 2) *
                sin(deltaLng / 2)

    val c =
        2.0 * atan2(
            sqrt(a),
            sqrt(1.0 - a),
        )

    return earthRadius * c
}

@Composable
fun QrLocationVerificationScreen(
    programId: Int,
    targetLatitude: Double,
    targetLongitude: Double,
    placeName: String,
    onVerified: (latitude: Double, longitude: Double) -> Unit,
    onExit: () -> Unit,
) {
    // =============================================================
    // 프로그램 정보
    // =============================================================

    var programName by remember {
        mutableStateOf("")
    }

    val programDetailRepository =
        remember {
            ProgramDetailRepository(
                ApiService
            )
        }

    // =============================================================
    // 위치 정보
    // =============================================================

    var distance by remember {
        mutableStateOf<Double?>(null)
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var isTracking by remember {
        mutableStateOf(false)
    }

    var locationError by remember {
        mutableStateOf(false)
    }

    val coroutineScope =
        rememberCoroutineScope()

    // =============================================================
    // 프로그램 조회
    // =============================================================

    LaunchedEffect(programId) {
        println("================================")
        println("=== QR PROGRAM LOAD ===")
        println("programId = $programId")
        println("================================")

        val result =
            programDetailRepository.getProgramById(
                programId
            )

        result.onSuccess { program ->
            programName = program.name

            println("=== QR PROGRAM LOAD SUCCESS ===")
            println("programId = ${program.id}")
            println("programName = ${program.name}")
        }.onFailure { error ->
            println("================================")
            println("=== QR PROGRAM LOAD FAILED ===")
            println("programId = $programId")
            println("error = ${error.message}")
            println("================================")
        }
    }

    // =============================================================
    // 단발성 GPS 확인
    // =============================================================

    suspend fun checkCurrentLocation(): Boolean {
        return try {
            println("================================")
            println("=== QR LOCATION CHECK ===")
            println("programId = $programId")
            println("placeName = $placeName")
            println("target latitude = $targetLatitude")
            println("target longitude = $targetLongitude")
            println("================================")

            val currentLocation =
                getCurrentBrowserLocation()

            if (currentLocation == null) {
                println(
                    "=== QR LOCATION GET FAILED ==="
                )

                locationError = true

                return false
            }

            val currentLatitude =
                currentLocation.first

            val currentLongitude =
                currentLocation.second

            println("=== CURRENT LOCATION ===")
            println("latitude = $currentLatitude")
            println("longitude = $currentLongitude")

            val calculatedDistance =
                calculateQrDistanceMeters(
                    latitude1 = currentLatitude,
                    longitude1 = currentLongitude,
                    latitude2 = targetLatitude,
                    longitude2 = targetLongitude,
                )

            distance =
                calculatedDistance

            locationError = false

            println("=== QR LOCATION DISTANCE ===")
            println(
                "distance = ${calculatedDistance}m"
            )
            println(
                "allowed = ${QR_LOCATION_AUTH_RADIUS_METERS}m"
            )

            // =====================================================
            // 지오펜스 진입
            // =====================================================

            if (
                calculatedDistance <=
                QR_LOCATION_AUTH_RADIUS_METERS
            ) {
                println(
                    "=== QR LOCATION AUTH SUCCESS ==="
                )

                isTracking = false
                isLoading = false

                onVerified(
                    currentLatitude,
                    currentLongitude,
                )

                return true
            }

            println(
                "=== QR LOCATION AUTH WAITING ==="
            )

            false

        } catch (e: Exception) {
            println("================================")
            println("=== QR LOCATION EXCEPTION ===")
            println("exception = $e")
            println("message = ${e.message}")
            println(e.stackTraceToString())
            println("================================")

            locationError = true

            false
        }
    }

    // =============================================================
    // 화면 진입 즉시 GPS 1회 확인
    // =============================================================

    LaunchedEffect(
        targetLatitude,
        targetLongitude,
    ) {
        isLoading = true

        checkCurrentLocation()

        isLoading = false
    }

    // =============================================================
    // 위치 인증 버튼 이후 지속 위치 확인
    //
    // 1초마다 현재 위치를 다시 받아서
    // 목표 장소와의 거리를 계속 갱신한다.
    //
    // 50m 이내 진입 시 자동 인증
    // =============================================================

    LaunchedEffect(isTracking) {
        if (!isTracking) {
            return@LaunchedEffect
        }

        while (isTracking) {
            val verified =
                checkCurrentLocation()

            if (verified) {
                break
            }

            delay(1000)
        }
    }

    // =============================================================
    // 전체 화면
    // =============================================================

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        // =========================================================
        // 배경
        // =========================================================

        Image(
            painter = painterResource(
                Res.drawable.location_gps
            ),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // =========================================================
        // 전체 콘텐츠
        // =========================================================

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 20.dp,
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally,
        ) {
            // =====================================================
            // 상단 60%
            // =====================================================

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f),
                horizontalAlignment =
                    Alignment.CenterHorizontally,
            ) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.15f),
                )

                Text(
                    text =
                        "원활한 미션 진행을 위한\n" +
                                "위치 확인을 해주세요",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    lineHeight = 38.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.85f),
                )
            }

            // =====================================================
            // 하단 40%
            // =====================================================

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f),
                horizontalAlignment =
                    Alignment.CenterHorizontally,
            ) {
                // =================================================
                // 카드
                // =================================================

                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val cardWidth =
                        minOf(
                            maxWidth,
                            350.dp,
                        )

                    Box(
                        modifier = Modifier
                            .width(cardWidth)
                            .aspectRatio(
                                350f / 140f
                            )
                            .clip(
                                RoundedCornerShape(20.dp)
                            )
                            .background(
                                Color.White.copy(
                                    alpha = 0.55f
                                )
                            ),
                        contentAlignment =
                            Alignment.Center,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 12.dp,
                                    vertical = 10.dp,
                                ),
                            horizontalAlignment =
                                Alignment.CenterHorizontally,
                            verticalArrangement =
                                Arrangement.Center,
                        ) {
                            // -------------------------------------
                            // 행사명
                            // -------------------------------------

                            Text(
                                text = programName,
                                fontFamily = Pretendard,
                                fontWeight =
                                    FontWeight.Normal,
                                fontSize = 16.sp,
                                lineHeight = 21.sp,
                                color = Color.Black,
                                textAlign =
                                    TextAlign.Center,
                                modifier =
                                    Modifier.fillMaxWidth(),
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(8.dp)
                            )

                            // -------------------------------------
                            // 거리
                            // -------------------------------------

                            Text(
                                text =
                                    "현재 미션 지정 장소와의 거리 : " +
                                            if (distance != null) {
                                                "${distance!!.toInt()}m"
                                            } else {
                                                "확인 중"
                                            },
                                fontFamily = Pretendard,
                                fontWeight =
                                    FontWeight.Bold,
                                fontSize = 16.sp,
                                lineHeight = 20.sp,
                                color = Color(0xFF2563EB),
                                textAlign =
                                    TextAlign.Center,
                                modifier =
                                    Modifier.fillMaxWidth(),
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(8.dp)
                            )

                            // -------------------------------------
                            // 안내 문구
                            // -------------------------------------

                            Text(
                                text =
                                    "미션 지정 장소에서 위치 인증이 되어야\n" +
                                            "(*100m 이내 인증 가능)",
                                fontFamily = Pretendard,
                                fontWeight =
                                    FontWeight.Normal,
                                fontSize = 16.sp,
                                lineHeight = 21.sp,
                                color = Color(0xFF212121),
                                textAlign =
                                    TextAlign.Center,
                                modifier =
                                    Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                // =================================================
                // 위치 인증하기
                // =================================================

                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val buttonWidth =
                        minOf(
                            maxWidth,
                            350.dp,
                        )

                    Box(
                        modifier = Modifier
                            .width(buttonWidth)
                            .aspectRatio(
                                350f / 56f
                            )
                            .clip(
                                RoundedCornerShape(12.dp)
                            )
                            .background(
                                MainGradient
                            )
                            .clickable(
                                enabled = !isTracking,
                            ) {
                                coroutineScope.launch {
                                    isTracking = true
                                    isLoading = true
                                    locationError = false

                                    checkCurrentLocation()

                                    isLoading = false
                                }
                            },
                        contentAlignment =
                            Alignment.Center,
                    ) {
                        Text(
                            text = "위치 인증하기",
                            fontFamily = Pretendard,
                            fontWeight =
                                FontWeight.Medium,
                            fontSize = 16.sp,
                            color = Color.White,
                            textAlign =
                                TextAlign.Center,
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                // =================================================
                // 위치 서비스 안내
                // =================================================

                Text(
                    text = "위치 서비스가 켜져있는지 확인해주세요.",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    color = Color(0xFF254079),
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier.fillMaxWidth(),
                )

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }

        // =========================================================
        // GPS 오류 안내
        // =========================================================

        if (
            locationError &&
            !isLoading
        ) {
            Text(
                text =
                    "현재 위치를 확인할 수 없습니다.\n" +
                            "위치 권한을 허용한 후 다시 시도해주세요.",
                fontFamily = Pretendard,
                fontWeight =
                    FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = Color(0xFF212121),
                textAlign =
                    TextAlign.Center,
                modifier = Modifier
                    .align(
                        Alignment.BottomCenter
                    )
                    .padding(
                        bottom = 8.dp
                    ),
            )
        }
    }
}