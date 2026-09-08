package com.alphacity.stamptour

import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

external interface BrowserGeolocationCoordinates {
    val latitude: Double
    val longitude: Double
}

external interface BrowserGeolocationPosition {
    val coords: BrowserGeolocationCoordinates
}

external interface BrowserGeolocationError {
    val code: Int
    val message: String
}

external interface BrowserGeolocation {
    fun getCurrentPosition(
        success: (BrowserGeolocationPosition) -> Unit,
        error: (BrowserGeolocationError) -> Unit,
    )
}

external interface BrowserNavigator {
    val geolocation: BrowserGeolocation
}

@JsName("navigator")
external val browserNavigator: BrowserNavigator

suspend fun getCurrentBrowserLocation(): Pair<Double, Double>? {
    return suspendCoroutine { continuation ->
        try {
            browserNavigator.geolocation.getCurrentPosition(
                success = { position ->
                    continuation.resume(
                        position.coords.latitude to
                                position.coords.longitude
                    )
                },
                error = {
                    continuation.resume(null)
                },
            )
        } catch (_: Throwable) {
            continuation.resume(null)
        }
    }
}