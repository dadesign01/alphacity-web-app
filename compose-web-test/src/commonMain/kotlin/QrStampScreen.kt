package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import com.alphacity.stamptour.network.ApiService
import com.alphacity.stamptour.network.dto.CollectStampRequest
import com.alphacity.stamptour.network.dto.CollectStampResponse
import com.alphacity.stamptour.network.dto.MissionItem
import com.alphacity.stamptour.network.dto.ProgramItem
import com.alphacity.stamptour.network.dto.ValidateStampResponse
import com.alphacity.stamptour.repository.ProgramDetailRepository
import com.alphacity.stamptour.repository.StampRepository
import com.alphacity.stamptour.web.WebTokenManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.browser.window
import web.QrTarget

private const val API_BASE_URL =
    "https://ollymoa-server.vercel.app/api/v1"

/*
 * =============================================================
 * TEMP TEST
 *
 * 오늘 세미나 행사 테스트용
 *
 * 세미나:
 *   QR 위치 인증 X
 *   체류 미션 위치 인증 X
 *
 * 일반 행사:
 *   기존 위치 인증 그대로
 *
 * 테스트 끝나면 이 부분을 원래 로직으로 되돌리면 됨.
 * =============================================================
 */
private const val TEMP_SKIP_SEMINAR_LOCATION = true

private val httpClient = HttpClient {
    install(ContentNegotiation) {
        json()
    }
}

private fun exitQrPage() {
    if (window.history.length > 1) {
        window.history.back()
    } else {
        window.location.replace("about:blank")
    }
}

@Composable
fun QrStampScreen(
    target: QrTarget?,
    onLoginRequired: () -> Unit,
    onAlreadyCollected: () -> Unit,
    onEarned: () -> Unit,
) {

    var message by remember {
        mutableStateOf("QR 코드를 확인하고 있습니다.")
    }

    var showAlert by remember {
        mutableStateOf(false)
    }

    // =============================================================
    // 상태
    // =============================================================

    var qrValidated by remember(target) {
        mutableStateOf(false)
    }

    var program by remember(target) {
        mutableStateOf<ProgramItem?>(null)
    }

    var loginVerified by remember(target) {
        mutableStateOf(false)
    }

    var locationVerified by remember(target) {
        mutableStateOf(false)
    }

    var verifiedLatitude by remember(target) {
        mutableStateOf<Double?>(null)
    }

    var verifiedLongitude by remember(target) {
        mutableStateOf<Double?>(null)
    }

    /*
     * 세미나 체류 미션
     */
    var stayMission by remember(target) {
        mutableStateOf<MissionItem?>(null)
    }

    /*
     * 미션 조회 중 여부
     */
    var missionLoading by remember(target) {
        mutableStateOf(false)
    }

    /*
     * 스탬프 적립 준비 완료 여부
     */
    var collectReady by remember(target) {
        mutableStateOf(false)
    }

    // =============================================================
    // 0. QR URL 자체가 잘못된 경우
    // =============================================================

    if (target == null) {

        LaunchedEffect(Unit) {

            message =
                "유효하지 않은 QR 코드입니다."

            showAlert = true
        }

    } else {

        // =========================================================
        // 1. QR 유효성 검증
        // =========================================================

        LaunchedEffect(target) {

            try {

                println("================================")
                println("=== QR VALIDATION START ===")
                println("programId = ${target.programId}")
                println("stampId = ${target.stampId}")
                println("qrCode = ${target.qrCode}")
                println("================================")

                message =
                    "QR 코드를 확인하고 있습니다."

                val validateUrl =
                    "$API_BASE_URL/stamps/validate" +
                            "?programId=${target.programId}" +
                            "&stampId=${target.stampId}" +
                            "&qrCode=${target.qrCode}"

                println("=== QR VALIDATE ===")
                println("url = $validateUrl")

                val validateResponse =
                    httpClient.get(validateUrl)

                println(
                    "validate status = " +
                            validateResponse.status
                )

                val validateResult: ValidateStampResponse =
                    validateResponse.body()

                println(
                    "validate success = " +
                            validateResult.success
                )

                println(
                    "validate data = " +
                            validateResult.data
                )

                println(
                    "validate message = " +
                            validateResult.message
                )

                // =================================================
                // QR 검증 실패
                // =================================================

                if (
                    !validateResult.success ||
                    validateResult.data?.valid != true
                ) {

                    println("=== QR INVALID ===")

                    message =
                        validateResult.error?.message
                            ?: validateResult.message
                                    ?: "유효하지 않은 QR 코드입니다."

                    showAlert = true

                    return@LaunchedEffect
                }

                // =================================================
                // QR 검증 성공
                // =================================================

                println("=== QR VALID ===")

                qrValidated = true

            } catch (e: Exception) {

                println("================================")
                println("=== QR VALIDATION EXCEPTION ===")
                println("exception = $e")
                println("message = ${e.message}")
                println(e.stackTraceToString())
                println("================================")

                message =
                    "QR 코드 확인 중 오류가 발생했습니다."

                showAlert = true
            }
        }

        // =========================================================
        // 2. QR 검증 성공 후 행사 정보 조회
        // =========================================================

        LaunchedEffect(
            target,
            qrValidated,
        ) {

            if (!qrValidated) {
                return@LaunchedEffect
            }

            if (program != null) {
                return@LaunchedEffect
            }

            try {

                println("================================")
                println("=== PROGRAM LOAD START ===")
                println("programId = ${target.programId}")
                println("================================")

                message =
                    "행사 정보를 확인하고 있습니다."

                val programRepository =
                    ProgramDetailRepository(
                        ApiService
                    )

                val programResult =
                    programRepository.getProgramById(
                        target.programId
                    )

                val loadedProgram =
                    programResult.getOrElse { error ->

                        println("=== PROGRAM LOAD FAILED ===")
                        println("error = ${error.message}")

                        message =
                            "행사 정보를 확인할 수 없습니다."

                        showAlert = true

                        return@LaunchedEffect
                    }

                println("=== PROGRAM LOAD SUCCESS ===")
                println("program id = ${loadedProgram.id}")
                println("program name = ${loadedProgram.name}")
                println("program category = ${loadedProgram.category}")
                println("program location = ${loadedProgram.location}")
                println("program latitude = ${loadedProgram.latitude}")
                println("program longitude = ${loadedProgram.longitude}")

                /*
                 * =================================================
                 * 세미나 여부
                 *
                 * DB에 혹시 "세미나 "처럼 공백이 들어가 있어도
                 * 정상적으로 세미나로 인식하도록 trim().
                 * =================================================
                 */

                val isSeminar = loadedProgram.category == "seminar"

                // =================================================
                // 일반 행사만 위치 좌표 필수
                //
                // 세미나는 오늘 임시 테스트이므로
                // 프로그램 좌표가 없어도 QR 흐름을 막지 않는다.
                // =================================================

                if (
                    !isSeminar &&
                    (
                            loadedProgram.latitude == null ||
                                    loadedProgram.longitude == null
                            )
                ) {

                    println("=== PROGRAM LOCATION INVALID ===")

                    message =
                        "행사 위치 정보가 등록되지 않아\n" +
                                "QR 인증을 진행할 수 없습니다."

                    showAlert = true

                    return@LaunchedEffect
                }

                program = loadedProgram

                // =================================================
                // TEMP
                //
                // 세미나는 위치 인증 자체를 완료 처리한다.
                //
                // 실제 위치를 확인한 것은 아니고,
                // 이후 일반 행사 위치 인증 화면으로 들어가지
                // 않도록 상태만 true로 만든다.
                // =================================================

                if (
                    TEMP_SKIP_SEMINAR_LOCATION &&
                    isSeminar
                ) {

                    println("================================")
                    println("=== TEMP SEMINAR LOCATION SKIP ===")
                    println("locationVerified = true")
                    println("================================")

                    locationVerified = true
                }

            } catch (e: Exception) {

                println("================================")
                println("=== PROGRAM LOAD EXCEPTION ===")
                println("exception = $e")
                println("message = ${e.message}")
                println(e.stackTraceToString())
                println("================================")

                message =
                    "행사 정보를 확인하는 중 오류가 발생했습니다."

                showAlert = true
            }
        }

        // =========================================================
        // 3. QR 검증 + 행사 정보 확인 후 로그인 확인
        // =========================================================

        LaunchedEffect(
            target,
            qrValidated,
            program,
        ) {

            if (!qrValidated) {
                return@LaunchedEffect
            }

            if (program == null) {
                return@LaunchedEffect
            }

            if (showAlert) {
                return@LaunchedEffect
            }

            if (loginVerified) {
                return@LaunchedEffect
            }

            val accessToken =
                WebTokenManager.getAccessToken()

            println(
                "accessToken exists = " +
                        !accessToken.isNullOrBlank()
            )

            // =====================================================
            // 비로그인
            // =====================================================

            if (accessToken.isNullOrBlank()) {

                println("=== LOGIN REQUIRED ===")

                onLoginRequired()

                return@LaunchedEffect
            }

            // =====================================================
            // 로그인 상태
            // =====================================================

            println("=== LOGIN VERIFIED ===")

            loginVerified = true
        }

        // =========================================================
        // 4. 위치 인증 완료 후 처리
        //
        // 일반 행사:
        //   위치 인증 완료
        //   → collectReady = true
        //
        // 세미나:
        //   위치 인증 완전 생략
        //   → 체류 미션 조회
        //   → stayMission 표시
        // =========================================================

        LaunchedEffect(
            target,
            locationVerified,
            program,
            loginVerified,
        ) {

            val loadedProgram =
                program
                    ?: return@LaunchedEffect

            if (!loginVerified) {
                return@LaunchedEffect
            }

            val isSeminar = loadedProgram.category == "seminar"

            // =====================================================
            // 일반 행사
            //
            // 기존대로 위치 인증 완료 후 진행
            // =====================================================

            if (
                !isSeminar &&
                !locationVerified
            ) {

                return@LaunchedEffect
            }

            if (
                stayMission != null ||
                collectReady ||
                missionLoading
            ) {

                return@LaunchedEffect
            }

            // =====================================================
            // 세미나
            //
            // TEMP:
            // 위치 인증 없이 바로 체류 미션 조회
            // =====================================================

            if (
                TEMP_SKIP_SEMINAR_LOCATION &&
                isSeminar
            ) {

                println("================================")
                println("=== SEMINAR DETECTED ===")
                println("=== TEMP: LOCATION SKIP ===")
                println("programId = ${loadedProgram.id}")
                println("================================")

                missionLoading = true

                message =
                    "체류 미션을 확인하고 있습니다."

                try {

                    val repository =
                        StampRepository(
                            ApiService
                        )

                    val missionResult =
                        repository.getProgramStampMissions(
                            stampId = target.stampId
                        )

                    missionResult
                        .onSuccess { missions ->

                            println(
                                "=== PROGRAM MISSIONS ==="
                            )

                            println(
                                "count = ${missions.size}"
                            )

                            missions.forEach { mission ->

                                println(
                                    "mission id = ${mission.id}, " +
                                            "type = ${mission.type}, " +
                                            "stayMinutes = ${mission.stayMinutes}, " +
                                            "programId = ${mission.programId}"
                                )
                            }

                            /*
                             * 체류시간 미션은
                             * stayMinutes가 존재하는 미션으로 판단
                             */

                            val mission =
                                missions.firstOrNull {
                                    it.type != "stay_time"
                                }

                            if (mission == null) {

                                println(
                                    "=== STAY MISSION NOT FOUND ==="
                                )

                                message =
                                    "체류시간 미션을 찾을 수 없습니다."

                                showAlert = true

                                return@onSuccess
                            }

                            println(
                                "=== STAY MISSION FOUND ==="
                            )

                            println(
                                "mission id = ${mission.id}"
                            )

                            println(
                                "stayMinutes = ${mission.stayMinutes}"
                            )

                            stayMission = mission
                        }
                        .onFailure { error ->

                            println(
                                "=== PROGRAM MISSION LOAD FAILED ==="
                            )

                            println(
                                "error = ${error.message}"
                            )

                            message =
                                "체류 미션을 불러오지 못했습니다."

                            showAlert = true
                        }

                } catch (e: Exception) {

                    println("================================")
                    println("=== MISSION LOAD EXCEPTION ===")
                    println("exception = $e")
                    println("message = ${e.message}")
                    println(e.stackTraceToString())
                    println("================================")

                    message =
                        "체류 미션을 확인하는 중 오류가 발생했습니다."

                    showAlert = true

                } finally {

                    missionLoading = false
                }

            } else {

                // =================================================
                // 일반 행사
                // =================================================

                println("=== NORMAL PROGRAM ===")
                println("→ 바로 스탬프 적립")

                collectReady = true
            }
        }

        // =========================================================
        // 5. 스탬프 적립
        //
        // 일반 행사:
        //   실제 위치 인증 좌표 사용
        //
        // 세미나:
        //   TEMP
        //   프로그램에 등록된 행사 좌표 사용
        // =========================================================

        LaunchedEffect(
            target,
            collectReady,
            verifiedLatitude,
            verifiedLongitude,
            program,
        ) {

            if (!collectReady) {
                return@LaunchedEffect
            }

            val loadedProgram =
                program
                    ?: return@LaunchedEffect

            val isSeminar = loadedProgram.category == "seminar"
            // =====================================================
            // 좌표 결정
            // =====================================================

            val currentLatitude: Double
            val currentLongitude: Double

            if (
                TEMP_SKIP_SEMINAR_LOCATION &&
                isSeminar
            ) {

                // =================================================
                // TEMP
                //
                // 세미나는 실제 위치 인증을 하지 않았으므로
                // 행사에 등록된 좌표를 사용한다.
                // =================================================

                currentLatitude =
                    loadedProgram.latitude
                        ?: run {

                            message =
                                "행사 위치 정보가 없습니다."

                            showAlert = true

                            return@LaunchedEffect
                        }

                currentLongitude =
                    loadedProgram.longitude
                        ?: run {

                            message =
                                "행사 위치 정보가 없습니다."

                            showAlert = true

                            return@LaunchedEffect
                        }

                println("================================")
                println("=== SEMINAR TEMP COLLECT ===")
                println("=== LOCATION VERIFICATION SKIPPED ===")
                println("program latitude = $currentLatitude")
                println("program longitude = $currentLongitude")
                println("================================")

            } else {

                // =================================================
                // 일반 행사
                //
                // 실제 위치 인증 좌표가 반드시 있어야 한다.
                // =================================================

                currentLatitude =
                    verifiedLatitude
                        ?: return@LaunchedEffect

                currentLongitude =
                    verifiedLongitude
                        ?: return@LaunchedEffect
            }

            try {

                println("================================")
                println("=== COLLECT STAMP START ===")
                println("qrCode = ${target.qrCode}")
                println("latitude = $currentLatitude")
                println("longitude = $currentLongitude")
                println("seminar = $isSeminar")
                println("================================")

                message =
                    "스탬프를 적립하고 있습니다."

                val accessToken =
                    WebTokenManager.getAccessToken()

                if (accessToken.isNullOrBlank()) {

                    println("=== ACCESS TOKEN MISSING ===")

                    onLoginRequired()

                    return@LaunchedEffect
                }

                val response =
                    httpClient.post(
                        "$API_BASE_URL/stamps/collect"
                    ) {

                        contentType(
                            ContentType.Application.Json
                        )

                        header(
                            "Authorization",
                            "Bearer $accessToken",
                        )

                        setBody(
                            CollectStampRequest(
                                qrCode = target.qrCode,
                                latitude = currentLatitude,
                                longitude = currentLongitude,
                            )
                        )
                    }

                println(
                    "collect status = " +
                            response.status
                )

                val result: CollectStampResponse =
                    response.body()

                println("=== COLLECT RESULT ===")
                println("success = ${result.success}")
                println("data = ${result.data}")
                println("message = ${result.message}")
                println("error = ${result.error}")
                println("======================")

                // =================================================
                // 이미 적립한 스탬프
                // =================================================

                if (
                    !result.success &&
                    result.error?.code == "ALREADY_COLLECTED"
                ) {

                    println("=== ALREADY COLLECTED ===")

                    onAlreadyCollected()

                    return@LaunchedEffect
                }

                // =================================================
                // 정상 적립
                // =================================================

                if (result.success) {

                    println("=== STAMP EARNED ===")
                    println("→ onEarned() 호출")

                    onEarned()

                    println("→ onEarned() 완료")

                    return@LaunchedEffect
                }

                // =================================================
                // 기타 실패
                // =================================================

                message =
                    result.error?.message
                        ?: result.message
                                ?: "스탬프 수집에 실패했습니다."

                println("=== COLLECT FAILED ===")
                println("message = $message")

                showAlert = true

            } catch (e: Exception) {

                println("================================")
                println("=== COLLECT EXCEPTION ===")
                println("exception = $e")
                println("message = ${e.message}")
                println(e.stackTraceToString())
                println("================================")

                message =
                    "스탬프 적립 중 오류가 발생했습니다."

                showAlert = true
            }
        }
    }

    // =============================================================
    // 화면
    // =============================================================

    /*
     * 1. Alert
     */

    if (showAlert) {

        AlertDialog(
            onDismissRequest = {
                exitQrPage()
            },

            title = {
                Text("알림")
            },

            text = {
                Text(message)
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        exitQrPage()
                    }
                ) {

                    Text("확인")
                }
            },
        )
    }

    /*
     * 2. 세미나 체류 미션
     *
     * TEMP:
     * skipLocationVerification = true
     *
     * 따라서 체류 미션 내부에서도
     * 위치 인증을 하지 않는다.
     */

    else if (stayMission != null) {

        StayTimeMissionScreen(
            mission = stayMission!!,

            programLat =
                program?.latitude,

            programLng =
                program?.longitude,

            /*
             * TEMP
             *
             * 체류 미션 내부 위치 인증 완전 생략
             */
            skipLocationVerification = true,

            onCompleted = {

                println("================================")
                println("=== STAY MISSION COMPLETED ===")
                println("=== TEMP LOCATION SKIPPED ===")
                println("=== STAMP COLLECT READY ===")
                println("================================")

                /*
                 * 체류 미션 화면 종료
                 */
                stayMission = null

                /*
                 * 바로 스탬프 적립 시작
                 */
                collectReady = true
            },

            onDismiss = {

                println(
                    "=== STAY MISSION DISMISSED ==="
                )

                stayMission = null

                exitQrPage()
            },
        )
    }

    /*
     * 3. 일반 행사 위치 인증
     *
     * 세미나는 절대 여기로 들어오지 않는다.
     */



    else if (

        target != null &&
        qrValidated &&
        program != null &&
        loginVerified &&
        !locationVerified &&
        program!!.category != "seminar"

    ) {

        val targetLatitude =
            program!!.latitude

        val targetLongitude =
            program!!.longitude

        if (
            targetLatitude != null &&
            targetLongitude != null
        ) {

            QrLocationVerificationScreen(
                targetLatitude =
                    targetLatitude,

                targetLongitude =
                    targetLongitude,

                placeName =
                    program!!.location
                        ?: program!!.name,

                onVerified = {
                        latitude,
                        longitude ->

                    println("================================")
                    println("=== QR LOCATION VERIFIED ===")
                    println("latitude = $latitude")
                    println("longitude = $longitude")
                    println(
                        "category = " +
                                program?.category
                    )
                    println("================================")

                    verifiedLatitude =
                        latitude

                    verifiedLongitude =
                        longitude

                    locationVerified =
                        true
                },

                onExit = {
                    exitQrPage()
                },
            )

        } else {

            Column(
                modifier =
                    Modifier.fillMaxSize(),

                horizontalAlignment =
                    Alignment.CenterHorizontally,

                verticalArrangement =
                    Arrangement.Center,
            ) {

                CircularProgressIndicator()

                Text(
                    text =
                        "행사 위치를 확인하고 있습니다."
                )
            }
        }
    }

    /*
     * 4. 스탬프 적립 중
     */

    else if (
        collectReady &&
        !showAlert
    ) {

        Column(
            modifier =
                Modifier.fillMaxSize(),

            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.Center,
        ) {

            CircularProgressIndicator()

            Text(message)
        }
    }

    /*
     * 5. 기타 로딩
     */

    else if (!showAlert) {

        Column(
            modifier =
                Modifier.fillMaxSize(),

            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.Center,
        ) {

            CircularProgressIndicator()

            Text(message)
        }
    }
}