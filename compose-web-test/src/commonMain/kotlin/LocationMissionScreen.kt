package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.alphacity.stamptour.network.ApiService
import com.alphacity.stamptour.network.dto.MissionItem
import com.alphacity.stamptour.repository.StampRepository
import kotlinx.browser.window
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import com.alphacity.stamptour.getCurrentBrowserLocation

private val Primary = Color(0xFF02CDF8)
private val Pretendard = FontFamily.SansSerif

/*
 * 기존 Android 로직과 동일하게 50m 유지
 */
private const val LOCATION_THRESHOLD_METERS = 25.0

@Composable
fun LocationMissionScreen(
    mission: MissionItem,
    programLat: Double? = null,
    programLng: Double? = null,
    onDismiss: () -> Unit = {},
    onCompleted: () -> Unit = {},
) {
    val repository = remember {
        StampRepository(ApiService)
    }

    var isLoading by remember(mission.id) {
        mutableStateOf(false)
    }

    var isCompleted by remember(mission.id) {
        mutableStateOf(false)
    }

    var showAlert by remember(mission.id) {
        mutableStateOf(false)
    }

    var alertMessage by remember(mission.id) {
        mutableStateOf("")
    }

    var currentDistance by remember(mission.id) {
        mutableStateOf<Int?>(null)
    }

    var locationCheckRequested by remember(mission.id) {
        mutableStateOf(false)
    }

    /*
     * 미션 장소 좌표를 우선 사용하고,
     * 없으면 프로그램 좌표를 사용
     */
    val targetLat =
        mission.place?.latitude ?: programLat

    val targetLng =
        mission.place?.longitude ?: programLng

    val placeName =
        mission.place?.name ?: "목표 장소"

    /*
     * 위치 확인 요청
     */
    suspend fun verifyAndComplete() {

        if (isLoading) {
            return
        }

        if (targetLat == null || targetLng == null) {
            alertMessage =
                "미션 장소의 위치 정보가 없습니다."

            showAlert = true
            return
        }

        isLoading = true

        val currentLocation =
            getCurrentBrowserLocation()

        if (currentLocation == null) {

            alertMessage =
                "현재 위치를 확인할 수 없습니다.\n\n" +
                        "브라우저의 위치 권한을 허용하고\n" +
                        "다시 시도해주세요."

            showAlert = true
            isLoading = false

            return
        }

        val distance =
            calculateDistanceMeters(
                currentLat = currentLocation.first,
                currentLng = currentLocation.second,
                targetLat = targetLat,
                targetLng = targetLng,
            )

        currentDistance = distance.toInt()

        /*
         * Android와 동일하게 50m 이내에서만 완료
         */
        if (distance > LOCATION_THRESHOLD_METERS) {

            val remainingDistance =
                distance.toInt() -
                        LOCATION_THRESHOLD_METERS.toInt()

            alertMessage =
                "현재 거리: ${distance.toInt()}m\n" +
                        "목표 장소까지 ${remainingDistance}m 더 이동해주세요."

            showAlert = true
            isLoading = false

            return
        }

        /*
         * 위치 인증 성공 → 실제 미션 완료 API 호출
         */
        repository.completeMission(
            missionId = mission.id,
        ).onSuccess {

            isCompleted = true

        }.onFailure { error ->

            val message =
                error.message
                    ?: "미션 완료에 실패했습니다."

            when {

                message.contains("이미 완료") -> {
                    isCompleted = true
                    alertMessage = "이미 완료한 미션입니다."
                    showAlert = true
                }

                else -> {
                    alertMessage = message
                    showAlert = true
                }
            }
        }

        isLoading = false
    }

    /*
     * 버튼 클릭 → suspend 위치 확인 처리
     */
    LaunchedEffect(locationCheckRequested) {

        if (locationCheckRequested) {

            locationCheckRequested = false

            verifyAndComplete()
        }
    }

    /*
     * 알림
     */
    if (showAlert) {

        AlertDialog(
            onDismissRequest = {
                showAlert = false
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
                    text = alertMessage,
                    fontFamily = Pretendard,
                )
            },
            confirmButton = {

                TextButton(
                    onClick = {
                        showAlert = false
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {

        // ─────────────────────────────
        // Header
        // ─────────────────────────────

        Row(
            modifier = Modifier
                .fillMaxWidth()
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
                        onDismiss()
                    },
            )

            Spacer(
                modifier = Modifier.width(12.dp),
            )

            Text(
                text = "위치 인증 미션",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF121212),
            )
        }

        HorizontalDivider(
            color = Color(0xFFE2E2E2),
        )

        // ─────────────────────────────
        // 완료 화면
        // ─────────────────────────────

        if (isCompleted) {

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Spacer(
                    modifier = Modifier.weight(1f),
                )

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEAF8EF)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "✓",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                        color = Color(0xFF16A34A),
                    )
                }

                Spacer(
                    modifier = Modifier.height(20.dp),
                )

                Text(
                    text = "위치 인증 완료!",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color(0xFF121212),
                )

                Spacer(
                    modifier = Modifier.height(8.dp),
                )

                Text(
                    text = mission.name,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color(0xFF595959),
                    textAlign = TextAlign.Center,
                )

                Spacer(
                    modifier = Modifier.height(4.dp),
                )

                Text(
                    text = placeName,
                    fontFamily = Pretendard,
                    fontSize = 14.sp,
                    color = Color(0xFF828282),
                    textAlign = TextAlign.Center,
                )

                currentDistance?.let { distance ->

                    Spacer(
                        modifier = Modifier.height(8.dp),
                    )

                    Text(
                        text = "현재 거리: ${distance}m",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color(0xFF16A34A),
                    )
                }

                Spacer(
                    modifier = Modifier.weight(1f),
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 20.dp,
                            vertical = 40.dp,
                        )
                        .clip(
                            RoundedCornerShape(12.dp),
                        )
                        .background(Primary)
                        .clickable {

                            onCompleted()
                            onDismiss()
                        }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "확인",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.White,
                    )
                }
            }

        } else {

            // ─────────────────────────────
            // 위치 인증 화면
            // ─────────────────────────────

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Spacer(
                    modifier = Modifier.weight(1f),
                )

                // 위치 아이콘
                Box(
                    contentAlignment = Alignment.Center,
                ) {

                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .border(
                                width = 2.dp,
                                color = Color(0xFFDBEAFE),
                                shape = CircleShape,
                            ),
                    )

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFF6FF)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "📍",
                            fontSize = 48.sp,
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(32.dp),
                )

                // ─────────────────────────
                // 장소 정보
                // ─────────────────────────

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(
                            RoundedCornerShape(14.dp),
                        )
                        .background(Color(0xFFF8FAFC))
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE2E8F0),
                            shape = RoundedCornerShape(14.dp),
                        )
                        .padding(20.dp),
                    horizontalAlignment = Alignment.Start,
                ) {

                    Text(
                        text = placeName,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Color(0xFF121212),
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp),
                    )

                    Text(
                        text =
                            "현재 위치를 인증하면 " +
                                    "스탬프를 획득할 수 있습니다",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF595959),
                    )

                    Spacer(
                        modifier = Modifier.height(14.dp),
                    )

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.spacedBy(6.dp),
                    ) {

                        Text(
                            text = "📍",
                            fontSize = 14.sp,
                        )

                        Text(
                            text = "GPS 인증 사용",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = Primary,
                        )
                    }

                    if (currentDistance != null) {

                        Spacer(
                            modifier = Modifier.height(14.dp),
                        )

                        Text(
                            text =
                                "현재 거리: ${currentDistance}m",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color =
                                if (
                                    currentDistance!! <=
                                    LOCATION_THRESHOLD_METERS
                                ) {
                                    Color(0xFF16A34A)
                                } else {
                                    Color(0xFFEF4444)
                                },
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.weight(1f),
                )

                // ─────────────────────────
                // 인증 버튼
                // ─────────────────────────

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment =
                        Alignment.CenterHorizontally,
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(
                                RoundedCornerShape(12.dp),
                            )
                            .background(
                                if (isLoading) {
                                    Color(0xFFD1D5DB)
                                } else {
                                    Primary
                                }
                            )
                            .clickable(
                                enabled = !isLoading,
                            ) {
                                locationCheckRequested = true
                            }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {

                        if (isLoading) {

                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                            )

                        } else {

                            Row(
                                verticalAlignment =
                                    Alignment.CenterVertically,
                                horizontalArrangement =
                                    Arrangement.spacedBy(8.dp),
                            ) {

                                Text(
                                    text = "📍",
                                    fontSize = 16.sp,
                                )

                                Text(
                                    text = "위치 인증하기",
                                    fontFamily = Pretendard,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = Color.White,
                                )
                            }
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(12.dp),
                    )

                    Text(
                        text =
                            "목표 장소 25m 이내에서 " +
                                    "인증할 수 있습니다",
                        fontFamily = Pretendard,
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(
                    modifier = Modifier.height(40.dp),
                )
            }
        }
    }
}

/*
 * 브라우저 Geolocation API
 */
/*
 * Haversine 공식
 *
 * 두 위도/경도 사이의 실제 지표면 거리를
 * meter 단위로 계산한다.
 */
private fun calculateDistanceMeters(
    currentLat: Double,
    currentLng: Double,
    targetLat: Double,
    targetLng: Double,
): Double {

    val earthRadius = 6_371_000.0

    val lat1 =
        currentLat * PI / 180.0

    val lat2 =
        targetLat * PI / 180.0

    val deltaLat =
        (targetLat - currentLat) * PI / 180.0

    val deltaLng =
        (targetLng - currentLng) * PI / 180.0

    val a =
        sin(deltaLat / 2) *
                sin(deltaLat / 2) +
                cos(lat1) *
                cos(lat2) *
                sin(deltaLng / 2) *
                sin(deltaLng / 2)

    val c =
        2 * atan2(
            sqrt(a),
            sqrt(1 - a),
        )

    return earthRadius * c
}