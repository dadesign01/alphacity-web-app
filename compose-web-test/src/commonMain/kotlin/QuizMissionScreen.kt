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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.alphacity.stamptour.network.dto.MissionPlace
import com.alphacity.stamptour.repository.StampRepository
import com.alphacity.stamptour.getCurrentBrowserLocation
import kotlin.math.PI

private val Primary = Color(0xFF02CDF8)
private val Pretendard = FontFamily.SansSerif

private const val LOCATION_THRESHOLD_METERS = 25.0

@Composable
fun QuizMissionScreen(
    mission: MissionItem,
    programLat: Double? = null,
    programLng: Double? = null,
    onDismiss: () -> Unit = {},
    onCompleted: () -> Unit = {},
) {
    val repository = remember {
        StampRepository(ApiService)
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    var isLocationChecking by remember {
        mutableStateOf(false)
    }

    var locationVerified by remember {
        mutableStateOf(false)
    }

    var currentDistance by remember {
        mutableStateOf<Int?>(null)
    }

    var isCompleted by remember {
        mutableStateOf(false)
    }

    var showAlert by remember {
        mutableStateOf(false)
    }

    var alertMessage by remember {
        mutableStateOf("")
    }

    var selectedOption by remember {
        mutableStateOf<String?>(null)
    }

    var textAnswer by remember {
        mutableStateOf("")
    }

    var earnedStamp by remember {
        mutableStateOf(false)
    }

    /*
     * 미션 장소 좌표가 있으면 미션 장소를 우선 사용하고,
     * 없으면 프로그램 좌표를 사용한다.
     */
    val targetLat = mission.place?.latitude ?: programLat
    val targetLng = mission.place?.longitude ?: programLng

    val hasTargetLocation =
        targetLat != null && targetLng != null

    /*
     * 미션에 실제 options가 있으면 사용한다.
     * 없으면 주관식으로 처리한다.
     */
    val options = mission.options ?: emptyList()

    val question =
        mission.question?.takeIf { it.isNotBlank() }
            ?: "다음 질문의 정답을 입력해주세요."

    /*
     * 화면 진입 시 상태 초기화
     */
    LaunchedEffect(mission.id) {
        isLoading = false
        isLocationChecking = false
        locationVerified = false
        currentDistance = null
        isCompleted = false
        showAlert = false
        alertMessage = ""
        selectedOption = null
        textAnswer = ""
        earnedStamp = false
    }

    fun showError(message: String) {
        alertMessage = message
        showAlert = true
    }

    suspend fun verifyLocation() {
        if (targetLat == null || targetLng == null) {
            locationVerified = true
            return
        }

        if (isLocationChecking) {
            return
        }

        isLocationChecking = true

        val currentLocation = getCurrentBrowserLocation()

        if (currentLocation == null) {
            showError(
                "현재 위치를 확인할 수 없습니다.\n" +
                        "브라우저의 위치 권한을 허용해주세요."
            )
            isLocationChecking = false
            return
        }

        val distance = calculateDistanceMeters(
            currentLat = currentLocation.first,
            currentLng = currentLocation.second,
            targetLat = targetLat,
            targetLng = targetLng,
        )

        currentDistance = distance.toInt()

        if (distance <= LOCATION_THRESHOLD_METERS) {
            locationVerified = true
        } else {
            showError(
                "현재 거리: ${distance.toInt()}m\n" +
                        "목표 장소까지 ${distance.toInt() - LOCATION_THRESHOLD_METERS.toInt()}m 더 이동해주세요."
            )
        }

        isLocationChecking = false
    }

    fun submitMission() {
        if (isLoading) {
            return
        }

        /*
         * 위치 정보가 존재하는 미션이면
         * 제출 전에 반드시 위치 인증을 거친다.
         */
        if (hasTargetLocation && !locationVerified) {
            showError("먼저 목표 장소에서 위치 인증을 완료해주세요.")
            return
        }

        val answer =
            selectedOption?.takeIf { it.isNotBlank() }
                ?: textAnswer.trim()

        if (answer.isBlank()) {
            showError("정답을 입력하거나 선택해주세요.")
            return
        }

        isLoading = true

        /*
         * Compose callback 안에서 suspend API를 직접 호출할 수 없으므로
         * 아래에서 LaunchedEffect를 이용해 처리한다.
         */
        pendingAnswer = answer
    }

    /*
     * 제출할 답안.
     *
     * 별도의 ViewModel 없이 현재 WASM 화면에서 처리한다.
     */
    var pendingAnswer by remember(mission.id) {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(pendingAnswer) {
        val answer = pendingAnswer ?: return@LaunchedEffect

        pendingAnswer = null

        repository.completeMission(
            missionId = mission.id,
            answer = answer,
        ).onSuccess { result ->

            earnedStamp = result.stamp != null
            isCompleted = true

        }.onFailure { error ->

            val message =
                error.message ?: "미션 완료에 실패했습니다."

            when {
                message.contains("정답") -> {
                    showError("정답이 아닙니다.")
                }

                message.contains("이미 완료") -> {
                    isCompleted = true
                    showError("이미 완료한 미션입니다.")
                }

                else -> {
                    showError(message)
                }
            }
        }

        isLoading = false
    }

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
                modifier = Modifier.width(12.dp)
            )

            Text(
                text = "퀴즈 미션",
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

        // ─────────────────────────────
        // 완료 화면
        // ─────────────────────────────

        if (isCompleted) {

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Spacer(
                    modifier = Modifier.weight(1f)
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
                    modifier = Modifier.height(20.dp)
                )

                Text(
                    text = "퀴즈 미션 완료!",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color(0xFF121212),
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = mission.name,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color(0xFF595959),
                    textAlign = TextAlign.Center,
                )

                if (earnedStamp) {

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(
                        text = "스탬프를 획득했습니다!",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Primary,
                    )
                }

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 20.dp,
                            vertical = 40.dp,
                        )
                        .clip(RoundedCornerShape(12.dp))
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

            return@Column
        }

        // ─────────────────────────────
        // 위치 인증 화면
        // ─────────────────────────────

        if (hasTargetLocation && !locationVerified) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Spacer(
                    modifier = Modifier.height(32.dp)
                )

                Text(
                    text = "위치 인증",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = Color.White,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Primary)
                        .padding(
                            horizontal = 10.dp,
                            vertical = 3.dp,
                        ),
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text = mission.name,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color(0xFF121212),
                    textAlign = TextAlign.Center,
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(
                    text = mission.place?.name
                        ?: "목표 장소",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color(0xFF595959),
                    textAlign = TextAlign.Center,
                )

                Spacer(
                    modifier = Modifier.height(32.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF0F7FF))
                        .padding(24.dp),
                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(),
                    ) {

                        Text(
                            text = "📍",
                            fontSize = 40.sp,
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Text(
                            text = "목표 장소로 이동해주세요.",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            color = Color(0xFF121212),
                            textAlign = TextAlign.Center,
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = "목표 장소 25m 이내에서\n위치 인증이 가능합니다.",
                            fontFamily = Pretendard,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF595959),
                            textAlign = TextAlign.Center,
                        )

                        if (currentDistance != null) {

                            Spacer(
                                modifier = Modifier.height(16.dp)
                            )

                            Text(
                                text = "현재 거리: ${currentDistance}m",
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = Primary,
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(28.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isLocationChecking) {
                                Color(0xFFD1D5DB)
                            } else {
                                Primary
                            }
                        )
                        .clickable(
                            enabled = !isLocationChecking
                        ) {
                            /*
                             * Compose에서 suspend 함수를 호출하기 위해
                             * 별도의 상태를 변경한다.
                             */
                            locationCheckRequested = true
                        }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {

                    if (isLocationChecking) {

                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                        )

                    } else {

                        Text(
                            text = "현재 위치 확인",
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

            LaunchedEffect(locationCheckRequested) {

                if (locationCheckRequested) {

                    locationCheckRequested = false

                    verifyLocation()
                }
            }

        } else {

            // ─────────────────────────────
            // 실제 퀴즈 화면
            // ─────────────────────────────

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(horizontal = 20.dp),
            ) {

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Text(
                    text = "퀴즈",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = Color.White,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Primary)
                        .padding(
                            horizontal = 10.dp,
                            vertical = 3.dp,
                        ),
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(
                    text = mission.name,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = Color(0xFF121212),
                )

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                // 질문 카드
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF0F7FF))
                        .padding(20.dp),
                ) {

                    Text(
                        text = question,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        lineHeight = 26.sp,
                        color = Color(0xFF121212),
                    )
                }

                // ─────────────────────────
                // 객관식
                // ─────────────────────────

                if (options.isNotEmpty()) {

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {

                        options.forEach { option ->

                            OptionRow(
                                option = option,
                                isSelected =
                                    selectedOption == option,
                                onClick = {
                                    selectedOption = option
                                    textAnswer = ""
                                },
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                // ─────────────────────────
                // 주관식
                // ─────────────────────────

                OutlinedTextField(
                    value = textAnswer,
                    onValueChange = {
                        textAnswer = it

                        if (it.isNotBlank()) {
                            selectedOption = null
                        }
                    },
                    placeholder = {
                        Text(
                            text = "직접 입력도 가능합니다.",
                            fontFamily = Pretendard,
                            fontSize = 14.sp,
                            color = Color(0xFF9CA3AF),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedTextColor = Color(0xFF121212),
                        unfocusedTextColor = Color(0xFF121212),
                    ),
                    singleLine = true,
                )

                val canSubmit =
                    selectedOption != null ||
                            textAnswer.isNotBlank()

                Spacer(
                    modifier = Modifier.height(28.dp)
                )

                // ─────────────────────────
                // 제출
                // ─────────────────────────

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (
                                canSubmit &&
                                !isLoading
                            ) {
                                Primary
                            } else {
                                Color(0xFFD1D5DB)
                            }
                        )
                        .clickable(
                            enabled =
                                canSubmit &&
                                        !isLoading
                        ) {
                            submitMission()
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

                        Text(
                            text = "제출하기",
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

/*
 * 위치 확인 요청 상태
 */
private var locationCheckRequested by mutableStateOf(false)

/*
 * 제출 답안 상태
 *
 * 실제 화면 인스턴스별로 관리해야 하므로
 * Composable 내부에서 사용하는 pendingAnswer와 별개로
 * 위치 확인 요청만 외부 상태를 사용하지 않도록 아래처럼
 * Composable-local 상태로 처리하는 것이 최종적으로 더 안전하다.
 */
private var pendingAnswer by mutableStateOf<String?>(null)

@Composable
private fun OptionRow(
    option: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) {
                    Color(0xFFEFF6FF)
                } else {
                    Color.White
                }
            )
            .border(
                width =
                    if (isSelected) {
                        1.5.dp
                    } else {
                        1.dp
                    },
                color =
                    if (isSelected) {
                        Primary
                    } else {
                        Color(0xFFE5E7EB)
                    },
                shape = RoundedCornerShape(12.dp),
            )
            .clickable {
                onClick()
            }
            .padding(
                horizontal = 18.dp,
                vertical = 16.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(14.dp),
    ) {

        Box(
            modifier = Modifier
                .size(22.dp)
                .border(
                    width = 2.dp,
                    color =
                        if (isSelected) {
                            Primary
                        } else {
                            Color(0xFFD1D5DB)
                        },
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {

            if (isSelected) {

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Primary),
                )
            }
        }

        Text(
            text = option,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            color = Color(0xFF121212),
        )
    }
}

/*
 * Browser Geolocation API
 */
/*
 * Haversine 공식으로 두 좌표 사이의 거리 계산
 *
 * 반환값: meter
 */
private fun calculateDistanceMeters(
    currentLat: Double,
    currentLng: Double,
    targetLat: Double,
    targetLng: Double,
): Double {

    val earthRadius = 6_371_000.0

    val lat1 = currentLat * PI / 180.0
    val lat2 = targetLat * PI / 180.0

    val deltaLat = (targetLat - currentLat) * PI / 180.0
    val deltaLng = (targetLng - currentLng) * PI / 180.0

    val a =
        kotlin.math.sin(deltaLat / 2) *
                kotlin.math.sin(deltaLat / 2) +
                kotlin.math.cos(lat1) *
                kotlin.math.cos(lat2) *
                kotlin.math.sin(deltaLng / 2) *
                kotlin.math.sin(deltaLng / 2)

    val c =
        2 * kotlin.math.atan2(
            kotlin.math.sqrt(a),
            kotlin.math.sqrt(1 - a),
        )

    return earthRadius * c
}