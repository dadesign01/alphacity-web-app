package com.alphacity.stamptour.web

import kotlinx.browser.window

enum class DevicePlatform {
    IOS,
    ANDROID,
    OTHER,
}

object DevicePlatformDetector {

    fun current(): DevicePlatform {
        val userAgent = window.navigator.userAgent.lowercase()

        return when {
            userAgent.contains("iphone") ||
                    userAgent.contains("ipad") ||
                    userAgent.contains("ipod") -> {
                DevicePlatform.IOS
            }

            userAgent.contains("android") -> {
                DevicePlatform.ANDROID
            }

            else -> {
                DevicePlatform.OTHER
            }
        }
    }

    fun isIos(): Boolean {
        return current() == DevicePlatform.IOS
    }
}