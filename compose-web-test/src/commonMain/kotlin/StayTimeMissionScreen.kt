
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
import com.alphacity.stamptour.getCurrentBrowserLocation
import com.alphacity.stamptour.network.ApiService
import com.alphacity.stamptour.network.dto.MissionItem
import com.alphacity.stamptour.repository.StampRepository
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private val StayOrange = Color(0xFFD97706)
private val Pretendard = FontFamily.SansSerif

/*
 * WASM 웹앱 위치 인증 기준
 *
 * Android / iOS는 기존 50m 유지
 * WASM 웹앱은 25m
 */
private const val LOCATION_THRESHOLD_METERS = 50.0

/*
 * =============================================================
 * TEMP - 오늘 하루 테스트용
 * =============================================================
 *
 * 오늘만 체류시간 미션에서 위치 인증을 완전히 우회한다.
 *
 * 테스트가 끝나면 이 값을 false로 변경하고
 * 아래 TEMP 처리 부분을 원래대로 복구한다.
 *
 * 위치 인증 관련 기존 코드는 삭제하지 않고
 * 주석으로 남겨둔다.
 *
 * =============================================================
 */
private const val TEMP_SKIP_LOCATION_TODAY = true

@Composable
fun StayTimeMissionScreen(
    mission: MissionItem,
    programLat: Double? = null,
    programLng: Double? = null,
    skipLocationVerification: Boolean = false,
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

    /*
     * 실제 체류시간 타이머가 작동 중인지
     */
    var isTimerRunning by remember(mission.id) {
        mutableStateOf(false)
    }

    /*
     * 남은 체류시간
     *
     * 처음에는 전체 체류시간으로 설정
     */
    var remainingSeconds by remember(mission.id) {
        mutableStateOf(
            (mission.stayMinutes ?: 1)
                .coerceAtLeast(1) * 60
        )
    }

    var showAlert by remember(mission.id) {
        mutableStateOf(false)
    }

    var alertMessage by remember(mission.id) {
        mutableStateOf("")
    }

    // 체류시간 완료여부
    var stayTimeCompleted by remember {
        mutableStateOf(false)
    }

    /*
     * =============================================================
     * GPS 위치 인증 완료 여부
     * =============================================================
     *
     * [오늘 임시 테스트]
     *
     * 위치 인증을 아예 사용하지 않는다.
     *
     * 기존 로직:
     *
     * var locationVerified by remember {
     *     mutableStateOf(skipLocationVerification)
     * }
     *
     * 오늘은 무조건 true로 설정해서
     * 위치 인증 화면을 건너뛴다.
     * =============================================================
     */
    var locationVerified by remember(mission.id) {

        mutableStateOf(
            if (TEMP_SKIP_LOCATION_TODAY) {
                true
            } else {
                skipLocationVerification
            }
        )
    }

    /*
     * GPS 인증 당시 목표 장소까지의 거리
     *
     * 오늘 테스트에서는 사용하지 않지만
     * 위치 인증 복구를 위해 유지한다.
     */
    var currentDistance by remember(mission.id) {
        mutableStateOf<Int?>(null)
    }

    var locationCheckRequested by remember(mission.id) {
        mutableStateOf(false)
    }

    /*
     * 실제 API의 체류시간 사용
     *
     * null 또는 0 이하인 경우 최소 1분
     */
    val stayMinutes =
        (mission.stayMinutes ?: 1)
            .coerceAtLeast(1)

    /*
     * 전체 체류시간(초)
     */
    val totalStaySeconds =
        stayMinutes * 60

    /*
     * 현재까지 체류한 시간(초)
     *
     * 체류 시작 전에는 0
     * 타이머가 진행되면 계속 증가
     */
    val elapsedSeconds =
        (
                totalStaySeconds -
                        remainingSeconds
                ).coerceAtLeast(0)

    /*
     * 미션 장소 좌표 우선
     *
     * 장소 좌표가 없으면 프로그램 좌표 사용
     */
    val targetLat =
        mission.place?.latitude ?: programLat

    val targetLng =
        mission.place?.longitude ?: programLng

    val hasLocation =
        targetLat != null &&
                targetLng != null

    val placeName =
        mission.place?.name ?: "지정 장소"

    /*
     * ─────────────────────────────
     * GPS 위치 인증
     * ─────────────────────────────
     *
     * 오늘 테스트에서는 호출되지 않는다.
     *
     * 위치 인증을 다시 사용할 때를 위해
     * 기존 코드를 그대로 유지한다.
     */
    suspend fun verifyLocation() {

        /*
         * TEMP - 오늘 하루 위치 인증 완전 우회
         */
        if (TEMP_SKIP_LOCATION_TODAY) {
            locationVerified = true
            return
        }

        if (isLoading) {
            return
        }

        /*
         * 좌표가 없는 경우
         *
         * 위치 인증 자체를 진행할 수 없으므로
         * 체류시간 화면으로 진입 가능하게 한다.
         */
        if (
            targetLat == null ||
            targetLng == null
        ) {
            locationVerified = true
            return
        }

        isLoading = true

        val currentLocation =
            getCurrentBrowserLocation()

        /*
         * 브라우저에서 현재 위치를 가져오지 못한 경우
         */
        if (currentLocation == null) {

            alertMessage =
                "현재 위치를 확인할 수 없습니다.\n\n" +
                        "브라우저의 위치 권한을 허용하고\n" +
                        "다시 시도해주세요."

            showAlert = true
            isLoading = false

            return
        }

        /*
         * 현재 위치와 목표 장소 사이의 거리 계산
         */
        val distance =
            calculateDistanceMeters(
                currentLat = currentLocation.first,
                currentLng = currentLocation.second,
                targetLat = targetLat,
                targetLng = targetLng,
            )

        currentDistance =
            distance.toInt()

        /*
         * WASM 웹앱은 25m 이내에서 인증
         */
        if (
            distance <=
            LOCATION_THRESHOLD_METERS
        ) {

            /*
             * GPS 인증만 완료
             *
             * 여기서 타이머를 시작하지 않는다.
             *
             * 사용자가 아래의
             * [▶ 체류 시작] 버튼을 눌러야
             * 실제 체류시간 카운트가 시작된다.
             */
            locationVerified = true

        } else {

            val remainingDistance =
                (
                        distance.toInt() -
                                LOCATION_THRESHOLD_METERS.toInt()
                        ).coerceAtLeast(0)

            alertMessage =
                "현재 거리: ${distance.toInt()}m\n" +
                        "목표 장소까지 " +
                        "${remainingDistance}m 더 이동해주세요."

            showAlert = true
        }

        isLoading = false
    }

    /*
     * 위치 확인 버튼 요청 처리
     *
     * 오늘 테스트에서는 화면 자체가 표시되지 않으므로
     * 실행될 일이 없다.
     *
     * 위치 인증 복구 시 기존대로 동작한다.
     */
    LaunchedEffect(locationCheckRequested) {

        if (locationCheckRequested) {

            locationCheckRequested = false

            verifyLocation()
        }
    }

    /*
     * ─────────────────────────────
     * 체류시간 타이머
     * ─────────────────────────────
     *
     * GPS 인증과 별개로
     *
     * [체류 시작]
     *
     * 버튼을 누른 순간부터 카운트한다.
     */
    /*
     * ─────────────────────────────
     * 체류시간 타이머
     * ─────────────────────────────
     */
    LaunchedEffect(
        isTimerRunning,
        mission.id,
    ) {

        if (!isTimerRunning) {
            return@LaunchedEffect
        }

        while (
            isTimerRunning &&
            remainingSeconds > 0
        ) {

            delay(1000)

            if (isTimerRunning) {
                remainingSeconds -= 1
            }
        }

        if (
            isTimerRunning &&
            remainingSeconds <= 0
        ) {
            isTimerRunning = false
            stayTimeCompleted = true
        }
    }


    /*
     * ─────────────────────────────
     * 체류시간 완료 API
     * ─────────────────────────────
     */
    LaunchedEffect(
        stayTimeCompleted,
        mission.id,
    ) {

        if (!stayTimeCompleted) {
            return@LaunchedEffect
        }

        if (isCompleted) {
            return@LaunchedEffect
        }

        isLoading = true

        println("================================")
        println("=== STAY MISSION TIMER COMPLETE ===")
        println("missionId = ${mission.id}")
        println("stayMinutes = $stayMinutes")
        println("→ completeMission() 호출")
        println("================================")

        repository.completeMission(
            missionId = mission.id,
        ).onSuccess {

            println("================================")
            println("=== STAY MISSION COMPLETE API SUCCESS ===")
            println("missionId = ${mission.id}")
            println("================================")

            isCompleted = true

        }.onFailure { error ->

            val message =
                error.message
                    ?: "미션 완료에 실패했습니다."

            println("================================")
            println("=== STAY MISSION COMPLETE API FAILED ===")
            println("missionId = ${mission.id}")
            println("error = $message")
            println("================================")

            when {

                message.contains("이미 완료") -> {

                    isCompleted = true

                    alertMessage =
                        "이미 완료한 미션입니다."

                    showAlert = true
                }

                else -> {

                    alertMessage =
                        message

                    showAlert = true
                }
            }
        }

        isLoading = false
    }

    /*
     * ─────────────────────────────
     * 체류시간 시작
     * ─────────────────────────────
     */
    fun startTimer() {

        /*
         * 이미 처리 중이거나
         * 타이머가 실행 중이면 무시
         */
        if (
            isLoading ||
            isTimerRunning
        ) {
            return
        }

        /*
         * =========================================================
         * TEMP - 오늘 하루 위치 인증 우회
         * =========================================================
         *
         * 오늘은 위치 인증을 하지 않으므로
         * 아래 기존 위치 인증 체크를 사용하지 않는다.
         *
         * 기존 코드:
         *
         * if (
         *     hasLocation &&
         *     !locationVerified
         * ) {
         *
         *     alertMessage =
         *         "먼저 지정 장소에서\n" +
         *         "위치 확인을 완료해주세요."
         *
         *     showAlert = true
         *
         *     return
         * }
         *
         * =========================================================
         */

        /*
         * 위치 인증이 필요한 미션인데
         * 아직 인증하지 않은 경우
         *
         * 오늘 테스트에서는 실행하지 않는다.
         */
        /*
        if (
            hasLocation &&
            !locationVerified
        ) {

            alertMessage =
                "먼저 지정 장소에서\n" +
                        "위치 확인을 완료해주세요."

            showAlert = true

            return
        }
        */

        /*
         * 처음 시작할 때만 전체 체류시간으로 초기화
         *
         * 중지 후 다시 시작하면
         * 기존 진행시간을 유지한다.
         */
        if (remainingSeconds <= 0) {

            remainingSeconds =
                totalStaySeconds
        }

        /*
         * 여기서부터 실제 체류시간 카운트 시작
         */
        isTimerRunning = true

        println("================================")
        println("=== STAY TIMER START ===")
        println("missionId = ${mission.id}")
        println("stayMinutes = $stayMinutes")
        println("TEMP_SKIP_LOCATION_TODAY = $TEMP_SKIP_LOCATION_TODAY")
        println("locationVerified = $locationVerified")
        println("================================")
    }

    /*
     * ─────────────────────────────
     * 타이머 중지
     * ─────────────────────────────
     *
     * 미션을 완료시키지 않고
     * 단순히 카운트만 멈춘다.
     */
    fun stopTimer() {

        isTimerRunning = false

        println(
            "=== STAY TIMER STOP ==="
        )
    }

    /*
     * ─────────────────────────────
     * 알림
     * ─────────────────────────────
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
                        color = StayOrange,
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
            verticalAlignment =
                Alignment.CenterVertically,
        ) {

            Text(
                text = "←",
                fontFamily = Pretendard,
                fontSize = 34.sp,
                color = Color(0xFF121212),
                modifier = Modifier
                    .size(
                        width = 13.dp,
                        height = 26.dp,
                    )
                    .clickable {

                        /*
                         * 화면을 나갈 때
                         * 타이머 중지
                         */
                        if (isTimerRunning) {
                            stopTimer()
                        }

                        onDismiss()
                    },
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Text(
                text = "체류시간 미션",
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
                horizontalAlignment =
                    Alignment.CenterHorizontally,
            ) {

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Color(0xFFFFF7E8)
                        ),
                    contentAlignment =
                        Alignment.Center,
                ) {

                    Text(
                        text = "✓",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                        color = StayOrange,
                    )
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Text(
                    text = "체류시간 미션 완료!",
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

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = placeName,
                    fontFamily = Pretendard,
                    fontSize = 14.sp,
                    color = Color(0xFF828282),
                    textAlign = TextAlign.Center,
                )

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
                        .clip(
                            RoundedCornerShape(12.dp)
                        )
                        .background(StayOrange)
                        .clickable {

                            println(
                                "=== STAY SCREEN CONFIRM ==="
                            )

                            onCompleted()
//                            onDismiss()
                        }
                        .padding(vertical = 16.dp),
                    contentAlignment =
                        Alignment.Center,
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

            /*
             * =========================================================
             * GPS 위치 인증 화면
             * =========================================================
             *
             * 오늘 TEMP_SKIP_LOCATION_TODAY = true 이므로
             * locationVerified가 처음부터 true라서
             * 이 화면은 표시되지 않는다.
             *
             * 위치 인증 복구 시 이 블록은 그대로 사용한다.
             * =========================================================
             */
        } else if (
            hasLocation &&
            !locationVerified &&
            !TEMP_SKIP_LOCATION_TODAY
        ) {

            // ─────────────────────────────
            // GPS 위치 인증 화면
            // ─────────────────────────────

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment =
                    Alignment.CenterHorizontally,
            ) {

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .border(
                            2.dp,
                            Color(0xFFFDE68A),
                            CircleShape,
                        ),
                    contentAlignment =
                        Alignment.Center,
                ) {

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(
                                Color(0xFFFFFBEB)
                            ),
                        contentAlignment =
                            Alignment.Center,
                    ) {

                        Text(
                            text = "📍",
                            fontFamily = Pretendard,
                            fontSize = 46.sp,
                            fontWeight = FontWeight.Bold,
                            color = StayOrange,
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(32.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(
                            RoundedCornerShape(14.dp)
                        )
                        .background(
                            Color(0xFFFFFBEB)
                        )
                        .border(
                            1.dp,
                            Color(0xFFFDE68A),
                            RoundedCornerShape(14.dp),
                        )
                        .padding(20.dp),
                ) {

                    Text(
                        text = "위치 확인 필요",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Color(0xFF121212),
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    Text(
                        text =
                            "지정된 장소에서 위치를 먼저 확인해야\n" +
                                    "체류시간 미션에 참여할 수 있습니다.",
                        fontFamily = Pretendard,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF595959),
                    )

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    Text(
                        text = "📍 25m 이내에서 인증 가능",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = StayOrange,
                    )
                }

                currentDistance?.let {

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Text(
                        text = "현재 거리: ${it}m",
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color =
                            if (
                                it <=
                                LOCATION_THRESHOLD_METERS
                            ) {
                                Color(0xFF16A34A)
                            } else {
                                Color(0xFFEA580C)
                            },
                    )
                }

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(
                            RoundedCornerShape(12.dp)
                        )
                        .background(
                            if (isLoading) {
                                Color(0xFFD1D5DB)
                            } else {
                                StayOrange
                            }
                        )
                        .clickable(
                            enabled = !isLoading
                        ) {

                            locationCheckRequested = true
                        }
                        .padding(vertical = 16.dp),
                    contentAlignment =
                        Alignment.Center,
                ) {

                    if (isLoading) {

                        CircularProgressIndicator(
                            color = Color.White,
                            modifier =
                                Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                        )

                    } else {

                        Text(
                            text = "📍 위치 확인",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.White,
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(
                    text =
                        "위치 서비스가 켜져있는지 확인해주세요",
                    fontFamily = Pretendard,
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                )

                Spacer(
                    modifier = Modifier.height(40.dp)
                )
            }

        } else {

            // ─────────────────────────────
            // 체류시간 메인 화면
            // ─────────────────────────────

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment =
                    Alignment.CenterHorizontally,
            ) {

                /*
                 * GPS 인증 완료 표시
                 *
                 * 오늘 테스트에서는 위치 인증을 하지 않았으므로
                 * 이 표시도 보여주지 않는다.
                 *
                 * 위치 인증 복구 시 기존대로 표시된다.
                 */
                if (
                    hasLocation &&
                    locationVerified &&
                    !TEMP_SKIP_LOCATION_TODAY
                ) {

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(8.dp)
                            )
                            .background(
                                Color(0xFFF0FFF4)
                            )
                            .padding(
                                horizontal = 12.dp,
                                vertical = 6.dp,
                            ),
                    ) {

                        Text(
                            text = "✓",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16A34A),
                        )

                        Text(
                            text =
                                "위치 확인 완료 " +
                                        "(${currentDistance ?: 0}m)",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = Color(0xFF16A34A),
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )
                }

                /*
                 * 오늘 테스트 안내
                 */
                if (TEMP_SKIP_LOCATION_TODAY) {

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(8.dp)
                            )
                            .background(
                                Color(0xFFFFFBEB)
                            )
                            .padding(
                                horizontal = 12.dp,
                                vertical = 6.dp,
                            ),
                    ) {

                        Text(
                            text = "✓",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = StayOrange,
                        )

                        Text(
                            text = "오늘 테스트: 위치 인증 생략",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = StayOrange,
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )
                }

                /*
                 * 미션 타입
                 */
                Text(
                    text = "체류시간",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = Color.White,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(StayOrange)
                        .padding(
                            horizontal = 10.dp,
                            vertical = 3.dp,
                        ),
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                /*
                 * 미션 이름
                 */
                Text(
                    text = mission.name,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = Color(0xFF121212),
                    textAlign = TextAlign.Center,
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                /*
                 * 장소명
                 */
                Text(
                    text = placeName,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color(0xFF595959),
                )

                Spacer(
                    modifier = Modifier.height(30.dp)
                )

                /*
                 * ─────────────────────────
                 * 현재 체류시간
                 * ─────────────────────────
                 */
                Text(
                    text =
                        formatTimer(
                            elapsedSeconds
                        ),
                    fontFamily =
                        FontFamily.Monospace,
                    fontWeight =
                        FontWeight.Bold,
                    fontSize = 56.sp,
                    color =
                        if (isTimerRunning) {
                            StayOrange
                        } else {
                            Color(0xFF121212)
                        },
                )

                Text(
                    text = "현재 체류시간",
                    fontFamily = Pretendard,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF828282),
                )

                Spacer(
                    modifier = Modifier.height(18.dp)
                )

                /*
                 * 필요 체류시간
                 */
                Text(
                    text =
                        "필요 체류시간: ${stayMinutes}분",
                    fontFamily = Pretendard,
                    fontSize = 13.sp,
                    color = Color(0xFF828282),
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text =
                        "※ 페이지를 벗어나면 체류시간이 초기화 되니 주의하시기 바랍니다.",
                    fontFamily = Pretendard,
                    fontSize = 13.sp,
                    color = Color(0xFF828282),
                )

                /*
                 * 타이머 작동 중일 때
                 * 남은 시간 표시
                 */
                if (isTimerRunning) {

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            "남은 시간: " +
                                    formatTimer(
                                        remainingSeconds
                                    ),
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = StayOrange,
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Text(
                        text =
                            "타이머가 작동 중입니다.\n" +
                                    "화면을 닫지 말아주세요.",
                        fontFamily = Pretendard,
                        fontSize = 12.sp,
                        color = StayOrange,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(10.dp)
                            )
                            .background(
                                Color(0xFFFFFBEB)
                            )
                            .padding(12.dp),
                    )
                }

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                /*
                 * 타이머 실행 중
                 */
                if (isTimerRunning) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 20.dp,
                                vertical = 40.dp,
                            )
                            .clip(
                                RoundedCornerShape(12.dp)
                            )
                            .background(
                                Color(0xFFDC2626)
                            )
                            .clickable {

                                stopTimer()
                            }
                            .padding(vertical = 16.dp),
                        contentAlignment =
                            Alignment.Center,
                    ) {

                        Text(
                            text = "타이머 중지",
                            fontFamily = Pretendard,
                            fontWeight =
                                FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.White,
                        )
                    }

                } else {

                    /*
                     * 타이머 시작 전
                     */
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 20.dp,
                                vertical = 40.dp,
                            )
                            .clip(
                                RoundedCornerShape(12.dp)
                            )
                            .background(
                                if (isLoading) {
                                    Color(0xFFD1D5DB)
                                } else {
                                    StayOrange
                                }
                            )
                            .clickable(
                                enabled = !isLoading
                            ) {

                                startTimer()
                            }
                            .padding(vertical = 16.dp),
                        contentAlignment =
                            Alignment.Center,
                    ) {

                        if (isLoading) {

                            CircularProgressIndicator(
                                color = Color.White,
                                modifier =
                                    Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                            )

                        } else {

                            Text(
                                text = "▶ 체류 시작",
                                fontFamily = Pretendard,
                                fontWeight =
                                    FontWeight.SemiBold,
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

/*
 * ─────────────────────────────
 * 타이머 표시
 * ─────────────────────────────
 */
private fun formatTimer(
    totalSeconds: Int,
): String {

    val minutes =
        totalSeconds / 60

    val seconds =
        totalSeconds % 60

    return "${minutes.toString().padStart(2, '0')}:" +
            seconds.toString().padStart(2, '0')
}

/*
 * ─────────────────────────────
 * Haversine 거리 계산
 * ─────────────────────────────
 *
 * 반환값: meter
 *
 * 오늘 테스트에서는 호출되지 않는다.
 * 위치 인증 복구 시 기존대로 사용한다.
 */
private fun calculateDistanceMeters(
    currentLat: Double,
    currentLng: Double,
    targetLat: Double,
    targetLng: Double,
): Double {

    val earthRadius =
        6_371_000.0

    val lat1 =
        currentLat *
                PI /
                180.0

    val lat2 =
        targetLat *
                PI /
                180.0

    val deltaLat =
        (targetLat - currentLat) *
                PI /
                180.0

    val deltaLng =
        (targetLng - currentLng) *
                PI /
                180.0

    val a =
        sin(deltaLat / 2) *
                sin(deltaLat / 2) +
                cos(lat1) *
                cos(lat2) *
                sin(deltaLng / 2) *
                sin(deltaLng / 2)

    val c =
        2 *
                atan2(
                    sqrt(a),
                    sqrt(1 - a),
                )

    return earthRadius * c
}
