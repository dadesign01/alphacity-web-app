@file:OptIn(ExperimentalTime::class)

package com.alphacity.stamptour.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import web.QrTarget
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private const val API_BASE_URL =
    "https://ollymoa-server.vercel.app/api/v1"

private val httpClient = HttpClient {
    install(ContentNegotiation) {
        json()
    }
}

private val KOREA_TIME_ZONE =
    TimeZone.of("UTC+09:00")

private fun isWithinProgramPeriod(
    startDate: String,
    endDate: String,
): Boolean {
    return try {
        val today =
            Clock.System.now()
                .toLocalDateTime(
                    KOREA_TIME_ZONE
                )
                .date

        val startDateText =
            startDate.take(10)

        val endDateText =
            endDate.take(10)

        val start =
            LocalDate.parse(startDateText)

        val end =
            LocalDate.parse(endDateText)

        val result =
            today >= start &&
                    today <= end

        println("================================")
        println("=== PROGRAM PERIOD CHECK ===")
        println("today = $today")
        println("start = $start")
        println("end = $end")
        println("withinProgramPeriod = $result")
        println("================================")

        result
    } catch (e: Exception) {
        println("================================")
        println("=== PROGRAM DATE CHECK FAILED ===")
        println("startDate = $startDate")
        println("endDate = $endDate")
        println("exception = $e")
        println("================================")

        false
    }
}

private fun isWithinOperatingHours(
    operatingHours: String?,
): Boolean {
    if (operatingHours.isNullOrBlank()) {
        println("=== OPERATING HOURS NOT SET ===")
        println("→ 운영시간 제한 없음")
        return true
    }

    return try {
        val parts =
            operatingHours.split("~")

        if (parts.size != 2) {
            println("================================")
            println("=== OPERATING HOURS INVALID ===")
            println("operatingHours = $operatingHours")
            println("================================")

            return false
        }

        val startText =
            parts[0].trim()

        val endText =
            parts[1].trim()

        val startTime =
            LocalTime.parse(startText)

        val endTime =
            LocalTime.parse(endText)

        val now =
            Clock.System.now()
                .toLocalDateTime(
                    KOREA_TIME_ZONE
                )
                .time

        val result =
            if (startTime <= endTime) {
                now >= startTime &&
                        now <= endTime
            } else {
                now >= startTime ||
                        now <= endTime
            }

        println("================================")
        println("=== OPERATING HOURS CHECK ===")
        println("operatingHours = $operatingHours")
        println("startTime = $startTime")
        println("endTime = $endTime")
        println("now(KST) = $now")
        println("withinOperatingHours = $result")
        println("================================")

        result
    } catch (e: Exception) {
        println("================================")
        println("=== OPERATING HOURS CHECK FAILED ===")
        println("operatingHours = $operatingHours")
        println("exception = $e")
        println("================================")

        false
    }
}

private fun exitQrPage() {
    window.location.replace("about:blank")
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

    var qrValidated by remember(target) {
        mutableStateOf(false)
    }

    var stampStatusChecked by remember(target) {
        mutableStateOf(false)
    }

    var program by remember(target) {
        mutableStateOf<ProgramItem?>(null)
    }

    var loginVerified by remember(target) {
        mutableStateOf(false)
    }

    /*
     * QR 행사 위치 인증 완료 여부
     */
    var locationVerified by remember(target) {
        mutableStateOf(false)
    }

    /*
     * QR 위치 인증에서 실제로 확인된 사용자 좌표
     *
     * 최종 스탬프 적립에서도 이 좌표를 그대로 사용한다.
     */
    var verifiedLatitude by remember(target) {
        mutableStateOf<Double?>(null)
    }

    var verifiedLongitude by remember(target) {
        mutableStateOf<Double?>(null)
    }

    /*
     * seminar 프로그램의 체류시간 미션
     */
    var stayMission by remember(target) {
        mutableStateOf<MissionItem?>(null)
    }

    var missionLoading by remember(target) {
        mutableStateOf(false)
    }

    /*
     * 최종 스탬프 적립 준비 여부
     */
    var collectReady by remember(target) {
        mutableStateOf(false)
    }

    /*
     * ============================================================
     * QR Target 자체가 없는 경우
     * ============================================================
     */
    if (target == null) {

        LaunchedEffect(Unit) {
            message =
                "유효하지 않은 QR 코드입니다."

            showAlert = true
        }

    } else {

        /*
         * ============================================================
         * 1. QR 유효성 검증
         *
         * 서버에서:
         *
         * programId
         * stampId
         * qrCode
         *
         * 세 값을 함께 검증한다.
         *
         * 다른 행사 QR이면 여기서 즉시 차단한다.
         * ============================================================
         */
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

                val validateResult:
                        ValidateStampResponse =
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

                println(
                    "validate error = " +
                            validateResult.error
                )

                /*
                 * ----------------------------------------------------
                 * 서버에서 잘못된 행사 QR이라고 판단한 경우
                 * ----------------------------------------------------
                 */
                if (
                    validateResult.error?.code ==
                    "INVALID_PROGRAM"
                ) {

                    println(
                        "=== WRONG EVENT QR ==="
                    )

                    message =
                        "행사가 다른 QR입니다."

                    showAlert = true

                    return@LaunchedEffect
                }

                /*
                 * ----------------------------------------------------
                 * 그 외 QR 검증 실패
                 * ----------------------------------------------------
                 */
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

                /*
                 * ----------------------------------------------------
                 * 서버 검증 결과와
                 * 현재 QR Target을 한 번 더 비교
                 *
                 * 서버가 valid라고 하더라도
                 * 실제 반환 데이터가 현재 QR Target과
                 * 다르면 진행하지 않는다.
                 * ----------------------------------------------------
                 */
                val validatedData =
                    validateResult.data

                if (validatedData == null) {

                    println(
                        "=== QR VALIDATION DATA NULL ==="
                    )

                    message =
                        "유효하지 않은 QR 코드입니다."

                    showAlert = true

                    return@LaunchedEffect
                }

                val sameProgram =
                    validatedData.programId ==
                            target.programId

                val sameStamp =
                    validatedData.stampId ==
                            target.stampId

                val sameQrCode =
                    validatedData.qrCode ==
                            target.qrCode

                println("================================")
                println("=== QR TARGET COMPARISON ===")
                println(
                    "target.programId = " +
                            target.programId
                )
                println(
                    "validated.programId = " +
                            validatedData.programId
                )
                println(
                    "sameProgram = $sameProgram"
                )
                println(
                    "target.stampId = " +
                            target.stampId
                )
                println(
                    "validated.stampId = " +
                            validatedData.stampId
                )
                println(
                    "sameStamp = $sameStamp"
                )
                println(
                    "target.qrCode = " +
                            target.qrCode
                )
                println(
                    "validated.qrCode = " +
                            validatedData.qrCode
                )
                println(
                    "sameQrCode = $sameQrCode"
                )
                println("================================")

                if (
                    !sameProgram ||
                    !sameStamp ||
                    !sameQrCode
                ) {

                    println(
                        "=== QR TARGET MISMATCH ==="
                    )

                    message =
                        if (!sameProgram) {
                            "행사가 다른 QR입니다."
                        } else {
                            "유효하지 않은 QR 코드입니다."
                        }

                    showAlert = true

                    return@LaunchedEffect
                }

                println("=== QR VALID ===")

                qrValidated = true

            } catch (e: CancellationException) {

                println("================================")
                println("=== QR VALIDATION CANCELLED ===")
                println("message = ${e.message}")
                println("================================")

                throw e

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

        /*
         * ============================================================
         * 2. 이미 해당 스탬프를 적립했는지 확인
         *
         * 이미 적립되어 있다면
         * 프로그램 조회 / 위치 인증 / 체류 미션으로
         * 넘어가지 않는다.
         * ============================================================
         */
        LaunchedEffect(
            target,
            qrValidated,
        ) {

            if (!qrValidated) {
                return@LaunchedEffect
            }

            if (stampStatusChecked) {
                return@LaunchedEffect
            }

            try {

                println("================================")
                println("=== STAMP STATUS CHECK START ===")
                println("stampId = ${target.stampId}")
                println("================================")

                message =
                    "스탬프 적립 여부를 확인하고 있습니다."

                val accessToken =
                    WebTokenManager.getAccessToken()

                if (accessToken.isNullOrBlank()) {

                    println(
                        "=== LOGIN REQUIRED FOR STAMP CHECK ==="
                    )

                    onLoginRequired()

                    return@LaunchedEffect
                }

                val repository =
                    StampRepository(
                        ApiService
                    )

                val userStampResult =
                    repository.getUserStamps()

                userStampResult
                    .onSuccess { userStamps ->

                        println("================================")
                        println(
                            "=== STAMP STATUS CHECK RESULT ==="
                        )
                        println(
                            "userStamp count = " +
                                    userStamps.size
                        )
                        println(
                            "target stampId = " +
                                    target.stampId
                        )
                        println("================================")

                        val alreadyCollected =
                            userStamps.any {
                                it.stampId ==
                                        target.stampId
                            }

                        println(
                            "alreadyCollected = " +
                                    alreadyCollected
                        )

                        if (alreadyCollected) {

                            println("================================")
                            println("=== ALREADY COLLECTED ===")
                            println(
                                "→ 바로 STAMP_NOT_EARNED 이동"
                            )
                            println("================================")

                            onAlreadyCollected()

                            return@onSuccess
                        }

                        println("================================")
                        println("=== STAMP NOT COLLECTED ===")
                        println("→ 다음 단계 진행")
                        println("================================")

                        stampStatusChecked = true
                    }
                    .onFailure { error ->

                        println("================================")
                        println(
                            "=== STAMP STATUS CHECK FAILED ==="
                        )
                        println(
                            "error = " +
                                    error.message
                        )
                        println("================================")

                        message =
                            "스탬프 적립 여부를 확인할 수 없습니다."

                        showAlert = true
                    }

            } catch (e: CancellationException) {

                println("================================")
                println(
                    "=== STAMP STATUS CHECK CANCELLED ==="
                )
                println("message = ${e.message}")
                println("================================")

                throw e

            } catch (e: Exception) {

                println("================================")
                println(
                    "=== STAMP STATUS CHECK EXCEPTION ==="
                )
                println(
                    "exception = $e"
                )
                println(
                    "message = $message"
                )
                println(e.stackTraceToString())
                println("================================")

                message =
                    "스탬프 적립 여부를 확인하는 중 오류가 발생했습니다."

                showAlert = true
            }
        }

        /*
         * ============================================================
         * 3. 프로그램 조회 및 기본 조건 확인
         *
         * seminar 포함 모든 프로그램을 동일하게 처리한다.
         *
         * 여기서는:
         *
         * - 프로그램 존재 여부
         * - 프로그램 기간
         * - 프로그램 운영시간
         * - 프로그램 위치
         *
         * 를 확인한다.
         * ============================================================
         */
        LaunchedEffect(
            target,
            qrValidated,
            stampStatusChecked,
        ) {

            if (!qrValidated) {
                return@LaunchedEffect
            }

            if (!stampStatusChecked) {
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

                        println(
                            "=== PROGRAM LOAD FAILED ==="
                        )

                        println(
                            "error = ${error.message}"
                        )

                        message =
                            "행사 정보를 확인할 수 없습니다."

                        showAlert = true

                        return@LaunchedEffect
                    }

                println(
                    "=== PROGRAM LOAD SUCCESS ==="
                )

                println(
                    "program id = " +
                            loadedProgram.id
                )

                println(
                    "program name = " +
                            loadedProgram.name
                )

                println(
                    "program category = " +
                            loadedProgram.category
                )

                println(
                    "program location = " +
                            loadedProgram.location
                )

                println(
                    "program latitude = " +
                            loadedProgram.latitude
                )

                println(
                    "program longitude = " +
                            loadedProgram.longitude
                )

                println(
                    "program startDate = " +
                            loadedProgram.startDate
                )

                println(
                    "program endDate = " +
                            loadedProgram.endDate
                )

                println(
                    "program operatingHours = " +
                            loadedProgram.operatingHours
                )

                /*
                 * 프로그램 기간 확인
                 */
                val withinProgramPeriod =
                    isWithinProgramPeriod(
                        startDate =
                            loadedProgram.startDate,
                        endDate =
                            loadedProgram.endDate,
                    )

                if (!withinProgramPeriod) {

                    println(
                        "=== PROGRAM NOT IN ACTIVE PERIOD ==="
                    )

                    message =
                        "현재는 프로그램 참여 기간이 아닙니다.\n" +
                                "프로그램 기간: " +
                                "${loadedProgram.startDate.take(10)} ~ " +
                                loadedProgram.endDate.take(10)

                    showAlert = true

                    return@LaunchedEffect
                }

                /*
                 * 프로그램 운영시간 확인
                 */
                val withinOperatingHours =
                    isWithinOperatingHours(
                        loadedProgram.operatingHours
                    )

                if (!withinOperatingHours) {

                    println(
                        "=== PROGRAM OUTSIDE OPERATING HOURS ==="
                    )

                    message =
                        "현재는 프로그램 운영시간이 아닙니다.\n" +
                                "운영시간: " +
                                (
                                        loadedProgram.operatingHours
                                            ?: "정보 없음"
                                        )

                    showAlert = true

                    return@LaunchedEffect
                }

                /*
                 * seminar 포함 모든 프로그램은
                 * 위치 정보가 반드시 있어야 한다.
                 */
                if (
                    loadedProgram.latitude == null ||
                    loadedProgram.longitude == null
                ) {

                    println(
                        "=== PROGRAM LOCATION INVALID ==="
                    )

                    message =
                        "행사 위치 정보가 등록되지 않아\n" +
                                "QR 인증을 진행할 수 없습니다."

                    showAlert = true

                    return@LaunchedEffect
                }

                program =
                    loadedProgram

            } catch (e: CancellationException) {

                println("================================")
                println(
                    "=== PROGRAM LOAD CANCELLED ==="
                )
                println("message = ${e.message}")
                println("================================")

                throw e

            } catch (e: Exception) {

                println("================================")
                println(
                    "=== PROGRAM LOAD EXCEPTION ==="
                )
                println(
                    "exception = $e"
                )
                println(
                    "message = $message"
                )
                println(e.stackTraceToString())
                println("================================")

                message =
                    "행사 정보를 확인하는 중 오류가 발생했습니다."

                showAlert = true
            }
        }

        /*
         * ============================================================
         * 4. 로그인 상태 확인
         * ============================================================
         */
        LaunchedEffect(
            target,
            qrValidated,
            program,
            stampStatusChecked,
        ) {

            if (!qrValidated) {
                return@LaunchedEffect
            }

            if (!stampStatusChecked) {
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

            if (accessToken.isNullOrBlank()) {

                println("=== LOGIN REQUIRED ===")

                onLoginRequired()

                return@LaunchedEffect
            }

            println("=== LOGIN VERIFIED ===")

            loginVerified = true
        }

        /*
         * ============================================================
         * 5. 위치 인증 완료 후 다음 단계
         *
         * 모든 프로그램은 반드시
         * QrLocationVerificationScreen을 거친다.
         *
         * 위치 인증 완료 후:
         *
         * 일반 프로그램
         *     → 바로 스탬프 적립
         *
         * seminar
         *     → 체류시간 미션
         * ============================================================
         */
        LaunchedEffect(
            target,
            locationVerified,
            program,
            loginVerified,
            stampStatusChecked,
        ) {

            val loadedProgram =
                program
                    ?: return@LaunchedEffect

            if (!stampStatusChecked) {
                return@LaunchedEffect
            }

            if (!loginVerified) {
                return@LaunchedEffect
            }

            /*
             * 위치 인증 전에는 아무것도 진행하지 않는다.
             */
            if (!locationVerified) {
                return@LaunchedEffect
            }

            if (
                stayMission != null ||
                collectReady ||
                missionLoading
            ) {
                return@LaunchedEffect
            }

            val isSeminar =
                loadedProgram.category == "seminar"

            /*
             * --------------------------------------------------------
             * seminar
             *
             * 위치 인증이 이미 완료된 상태에서
             * 체류시간 미션을 불러온다.
             * --------------------------------------------------------
             */
            if (isSeminar) {

                println("================================")
                println("=== SEMINAR DETECTED ===")
                println("=== LOCATION VERIFIED ===")
                println(
                    "programId = " +
                            loadedProgram.id
                )
                println("================================")

                missionLoading = true

                showAlert = false

                message =
                    "체류 미션을 확인하고 있습니다."

                try {

                    val repository =
                        StampRepository(
                            ApiService
                        )

                    val missionResult =
                        repository.getProgramStampMissions(
                            stampId =
                                target.stampId
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

                            val mission =
                                missions.firstOrNull {
                                    it.type == "stay_time"
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
                                "mission id = " +
                                        mission.id
                            )

                            println(
                                "stayMinutes = " +
                                        mission.stayMinutes
                            )

                            showAlert = false

                            stayMission =
                                mission
                        }
                        .onFailure { error ->

                            println(
                                "=== PROGRAM MISSION LOAD FAILED ==="
                            )

                            println(
                                "error = " +
                                        error.message
                            )

                            message =
                                "체류 미션을 불러오지 못했습니다."

                            showAlert = true
                        }

                } catch (e: CancellationException) {

                    println("================================")
                    println(
                        "=== MISSION LOAD CANCELLED ==="
                    )
                    println("message = ${e.message}")
                    println("================================")

                    throw e

                } catch (e: Exception) {

                    println("================================")
                    println(
                        "=== MISSION LOAD EXCEPTION ==="
                    )
                    println(
                        "exception = $e"
                    )
                    println(
                        "message = $message"
                    )
                    println(e.stackTraceToString())
                    println("================================")

                    message =
                        "체류 미션을 확인하는 중 오류가 발생했습니다."

                    showAlert = true

                } finally {

                    missionLoading = false
                }

            } else {

                /*
                 * ----------------------------------------------------
                 * 일반 프로그램
                 *
                 * 위치 인증이 끝났으므로 바로 적립 단계로 간다.
                 * ----------------------------------------------------
                 */
                println("================================")
                println("=== NORMAL PROGRAM ===")
                println("=== LOCATION VERIFIED ===")
                println("→ 바로 스탬프 적립")
                println("================================")

                collectReady = true
            }
        }

        /*
         * ============================================================
         * 6. 스탬프 적립
         *
         * 일반 프로그램 / seminar 모두 동일하다.
         *
         * seminar의 경우에도
         * QR 위치 인증에서 실제로 확인된 좌표를 사용한다.
         *
         * seminar 체류 미션 완료 후
         * collectReady = true가 되면서 실행된다.
         * ============================================================
         */
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

            program
                ?: return@LaunchedEffect

            /*
             * QR 위치 인증에서 실제로 받은 좌표가 없으면
             * 절대로 임의의 프로그램 좌표를 사용하지 않는다.
             */
            val currentLatitude =
                verifiedLatitude
                    ?: return@LaunchedEffect

            val currentLongitude =
                verifiedLongitude
                    ?: return@LaunchedEffect

            try {

                println("================================")
                println("=== COLLECT STAMP START ===")
                println(
                    "qrCode = " +
                            target.qrCode
                )
                println(
                    "latitude = " +
                            currentLatitude
                )
                println(
                    "longitude = " +
                            currentLongitude
                )
                println(
                    "programId = " +
                            program?.id
                )
                println(
                    "================================"
                )

                message =
                    "스탬프를 적립하고 있습니다."

                val accessToken =
                    WebTokenManager.getAccessToken()

                if (accessToken.isNullOrBlank()) {

                    println(
                        "=== ACCESS TOKEN MISSING ==="
                    )

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
                                qrCode =
                                    target.qrCode,

                                latitude =
                                    currentLatitude,

                                longitude =
                                    currentLongitude,
                            )
                        )
                    }

                println(
                    "collect status = " +
                            response.status
                )

                val result:
                        CollectStampResponse =
                    response.body()

                println("=== COLLECT RESULT ===")

                println(
                    "success = " +
                            result.success
                )

                println(
                    "data = " +
                            result.data
                )

                println(
                    "message = " +
                            result.message
                )

                println(
                    "error = " +
                            result.error
                )

                println("======================")

                /*
                 * 이미 적립된 경우
                 */
                if (
                    !result.success &&
                    result.error?.code ==
                    "ALREADY_COLLECTED"
                ) {

                    println(
                        "=== ALREADY COLLECTED ==="
                    )

                    onAlreadyCollected()

                    return@LaunchedEffect
                }

                /*
                 * 적립 성공
                 */
                if (result.success) {

                    println(
                        "=== STAMP EARNED ==="
                    )

                    println(
                        "→ onEarned() 호출"
                    )

                    onEarned()

                    println(
                        "→ onEarned() 완료"
                    )

                    return@LaunchedEffect
                }

                /*
                 * 적립 실패
                 */
                message =
                    result.error?.message
                        ?: result.message
                                ?: "스탬프 수집에 실패했습니다."

                println(
                    "=== COLLECT FAILED ==="
                )

                println(
                    "message = $message"
                )

                showAlert = true

            } catch (e: CancellationException) {

                println("================================")
                println(
                    "=== COLLECT CANCELLED ==="
                )
                println(
                    "message = ${e.message}"
                )
                println("================================")

                throw e

            } catch (e: Exception) {

                println("================================")
                println(
                    "=== COLLECT STAMP EXCEPTION ==="
                )
                println(
                    "exception = $e"
                )
                println(
                    "message = $message"
                )
                println(e.stackTraceToString())
                println("================================")

                message =
                    "스탬프 적립 중 오류가 발생했습니다."

                showAlert = true
            }
        }
    }

    /*
     * ============================================================
     * 화면 표시
     * ============================================================
     */

    /*
     * 오류 / 차단 메시지
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

        /*
         * ============================================================
         * seminar 체류시간 미션
         *
         * 위치 인증은 이미 QrLocationVerificationScreen에서
         * 완료된 상태다.
         *
         * 따라서 여기서는 GPS 인증을 다시 하지 않는다.
         * ============================================================
         */
    } else if (stayMission != null) {

        StayTimeMissionScreen(
            mission =
                stayMission!!,

            onCompleted = {

                println("================================")
                println(
                    "=== STAY MISSION COMPLETED ==="
                )
                println(
                    "=== LOCATION ALREADY VERIFIED ==="
                )
                println(
                    "=== STAMP COLLECT READY ==="
                )
                println("================================")

                stayMission = null

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

        /*
         * ============================================================
         * QR 행사 위치 인증
         *
         * seminar 포함 모든 프로그램이
         * 반드시 이 화면을 거친다.
         * ============================================================
         */
    } else if (
        target != null &&
        qrValidated &&
        stampStatusChecked &&
        program != null &&
        loginVerified &&
        !locationVerified
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

                programId =
                    target.programId,

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
                    println(
                        "=== QR LOCATION VERIFIED ==="
                    )
                    println(
                        "latitude = $latitude"
                    )
                    println(
                        "longitude = $longitude"
                    )
                    println(
                        "category = " +
                                program?.category
                    )
                    println("================================")

                    /*
                     * QR 위치 인증에서 확인한
                     * 실제 사용자 좌표 저장
                     */
                    verifiedLatitude =
                        latitude

                    verifiedLongitude =
                        longitude

                    /*
                     * 위치 인증 완료
                     */
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

                Spacer(
                    modifier =
                        Modifier.height(15.dp)
                )

                Text(
                    text =
                        "행사 위치를 확인하고 있습니다."
                )
            }
        }

        /*
         * ============================================================
         * 최종 적립 진행 중
         * ============================================================
         */
    } else if (
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

            Spacer(
                modifier =
                    Modifier.height(15.dp)
            )

            Text(message)
        }

        /*
         * ============================================================
         * 그 외 초기 로딩
         * ============================================================
         */
    } else if (!showAlert) {

        Column(
            modifier =
                Modifier.fillMaxSize(),

            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.Center,
        ) {

            CircularProgressIndicator()

            Spacer(
                modifier =
                    Modifier.height(15.dp)
            )

            Text(message)
        }
    }
}