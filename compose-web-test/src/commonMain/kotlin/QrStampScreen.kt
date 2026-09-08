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
import com.alphacity.stamptour.network.dto.ValidateStampResponse
import com.alphacity.stamptour.repository.ProgramDetailRepository
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
    "http://192.168.0.12:1111/api/v1"

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

    /*
     * QR 유효성 검증 완료 여부
     *
     * false
     * → 아직 QR 검증 전
     *
     * true
     * → QR 검증 성공
     */
    var qrValidated by remember(target) {
        mutableStateOf(false)
    }

    /*
     * 행사 정보
     *
     * QR validate가 성공한 뒤
     * 위치 인증에 사용할 행사 좌표를 가져온다.
     */
    var program by remember(target) {
        mutableStateOf<com.alphacity.stamptour.network.dto.ProgramItem?>(null)
    }

    // 로그인 확인 완료 여부
    var loginVerified by remember(target) {
        mutableStateOf(false)
    }

    /*
     * 위치 인증 완료 여부
     */
    var locationVerified by remember(target) {
        mutableStateOf(false)
    }

    /*
     * 위치 인증 당시의 실제 좌표
     *
     * collect API에 그대로 전달한다.
     */
    var verifiedLatitude by remember(target) {
        mutableStateOf<Double?>(null)
    }

    var verifiedLongitude by remember(target) {
        mutableStateOf<Double?>(null)
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
        //
        // 제일 먼저 실행
        //
        // 실패하면 여기서 종료.
        // GPS / 로그인 / 전화번호 인증 절대 실행하지 않음.
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
                //
                // 여기서 바로 탈락
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
        //
        // 위치 인증에 사용할 program 좌표를 가져온다.
        // =========================================================

        LaunchedEffect(
            target,
            qrValidated,
        ) {

            if (!qrValidated) {
                return@LaunchedEffect
            }

            /*
             * 이미 조회했으면 다시 조회하지 않는다.
             */
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
                println("program location = ${loadedProgram.location}")
                println("program latitude = ${loadedProgram.latitude}")
                println("program longitude = ${loadedProgram.longitude}")

                // =================================================
                // 행사 위치 좌표 확인
                // =================================================

                if (
                    loadedProgram.latitude == null ||
                    loadedProgram.longitude == null
                ) {

                    println("=== PROGRAM LOCATION INVALID ===")

                    message =
                        "행사 위치 정보가 등록되지 않아\n" +
                                "QR 인증을 진행할 수 없습니다."

                    showAlert = true

                    return@LaunchedEffect
                }

                program = loadedProgram

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
        // 3. QR 검증 성공 + 행사 정보 확인 후 로그인 확인
        //
        // 로그인되어 있지 않으면 기존 전화번호 인증 페이지로 이동
        //
        // 로그인되어 있으면 위치 인증으로 바로 진행
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

            /*
             * 이미 위치 인증이 끝났다면
             * 다시 로그인 확인하지 않는다.
             */
            if (locationVerified) {
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
            //
            // 기존 전화번호 인증 페이지로 이동
            // =====================================================

            if (accessToken.isNullOrBlank()) {

                println("=== LOGIN REQUIRED ===")

                onLoginRequired()

                return@LaunchedEffect
            }

            // =====================================================
            // 로그인 상태
            //
            // QrLocationVerificationScreen은
            // 아래 UI 영역에서 표시된다.
            // =====================================================

            println("=== LOGIN VERIFIED ===")
            loginVerified = true
        }

        // =========================================================
        // 4. 위치 인증 성공 후 기존 스탬프 적립 로직
        //
        // 여기부터는 기존 collect 로직 그대로
        // =========================================================

        LaunchedEffect(
            target,
            locationVerified,
            verifiedLatitude,
            verifiedLongitude,
        ) {

            if (!locationVerified) {
                return@LaunchedEffect
            }

            val currentLatitude =
                verifiedLatitude
                    ?: return@LaunchedEffect

            val currentLongitude =
                verifiedLongitude
                    ?: return@LaunchedEffect

            try {

                println("================================")
                println("=== COLLECT STAMP START ===")
                println("qrCode = ${target.qrCode}")
                println("latitude = $currentLatitude")
                println("longitude = $currentLongitude")
                println("================================")

                message =
                    "스탬프를 적립하고 있습니다."

                val accessToken =
                    WebTokenManager.getAccessToken()

                /*
                 * 위치 인증이 끝났는데
                 * 토큰이 없다면 정상적인 흐름이 아니다.
                 *
                 * 기존 로그인 페이지로 돌린다.
                 */
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

                                /*
                                 * 위치 인증 성공 당시의
                                 * 실제 브라우저 GPS 좌표
                                 */
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

    if (
        target != null &&
        qrValidated &&
        program != null &&
        loginVerified &&
        !locationVerified &&
        !showAlert
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
                targetLatitude = targetLatitude,
                targetLongitude = targetLongitude,
                placeName =
                    program!!.location
                        ?: program!!.name,
                onVerified = { latitude, longitude ->

                    println("================================")
                    println("=== QR LOCATION VERIFIED ===")
                    println("latitude = $latitude")
                    println("longitude = $longitude")
                    println("================================")

                    verifiedLatitude = latitude
                    verifiedLongitude = longitude

                    locationVerified = true
                },
                onExit = {
                    exitQrPage()
                },
            )

        } else {

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {

                CircularProgressIndicator()

                Text(
                    text = "행사 위치를 확인하고 있습니다."
                )
            }
        }

    } else if (!showAlert) {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {

            CircularProgressIndicator()

            Text(message)
        }
    }

    // =============================================================
    // AlertDialog
    // =============================================================

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
}