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

private const val TEMP_SKIP_SEMINAR_LOCATION = true

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
            startDate
                .take(10)

        val endDateText =
            endDate
                .take(10)

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

    var stayMission by remember(target) {
        mutableStateOf<MissionItem?>(null)
    }

    var missionLoading by remember(target) {
        mutableStateOf(false)
    }

    var collectReady by remember(target) {
        mutableStateOf(false)
    }

    if (target == null) {
        LaunchedEffect(Unit) {
            message =
                "유효하지 않은 QR 코드입니다."

            showAlert = true
        }
    } else {

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
                println("program startDate = ${loadedProgram.startDate}")
                println("program endDate = ${loadedProgram.endDate}")
                println(
                    "program operatingHours = " +
                            loadedProgram.operatingHours
                )

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

                val isSeminar =
                    loadedProgram.category == "seminar"

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

            } catch (e: CancellationException) {
                println("================================")
                println("=== PROGRAM LOAD CANCELLED ===")
                println("message = ${e.message}")
                println("================================")

                throw e

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

            if (accessToken.isNullOrBlank()) {
                println("=== LOGIN REQUIRED ===")

                onLoginRequired()

                return@LaunchedEffect
            }

            println("=== LOGIN VERIFIED ===")

            loginVerified = true
        }

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

            val isSeminar =
                loadedProgram.category == "seminar"

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
                                "mission id = ${mission.id}"
                            )

                            println(
                                "stayMinutes = ${mission.stayMinutes}"
                            )

                            showAlert = false

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

                } catch (e: CancellationException) {
                    println("================================")
                    println("=== MISSION LOAD CANCELLED ===")
                    println("message = ${e.message}")
                    println("================================")

                    throw e

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
                println("=== NORMAL PROGRAM ===")
                println("→ 바로 스탬프 적립")

                collectReady = true
            }
        }

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

            val isSeminar =
                loadedProgram.category == "seminar"

            val currentLatitude: Double
            val currentLongitude: Double

            if (
                TEMP_SKIP_SEMINAR_LOCATION &&
                isSeminar
            ) {
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
                println(
                    "=== LOCATION VERIFICATION SKIPPED ==="
                )
                println(
                    "program latitude = " +
                            currentLatitude
                )
                println(
                    "program longitude = " +
                            currentLongitude
                )
                println("================================")

            } else {
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
                println("success = ${result.success}")
                println("data = ${result.data}")
                println("message = ${result.message}")
                println("error = ${result.error}")
                println("======================")

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

                if (result.success) {
                    println("=== STAMP EARNED ===")
                    println("→ onEarned() 호출")

                    onEarned()

                    println("→ onEarned() 완료")

                    return@LaunchedEffect
                }

                message =
                    result.error?.message
                        ?: result.message
                                ?: "스탬프 수집에 실패했습니다."

                println("=== COLLECT FAILED ===")
                println("message = $message")

                showAlert = true

            } catch (e: CancellationException) {
                println("================================")
                println("=== COLLECT CANCELLED ===")
                println("message = ${e.message}")
                println("================================")

                throw e

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
    } else if (stayMission != null) {
        StayTimeMissionScreen(
            mission = stayMission!!,

            programLat =
                program?.latitude,

            programLng =
                program?.longitude,

            skipLocationVerification = true,

            onCompleted = {
                println("================================")
                println("=== STAY MISSION COMPLETED ===")
                println("=== TEMP LOCATION SKIPPED ===")
                println("=== STAMP COLLECT READY ===")
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
    } else if (
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