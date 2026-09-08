package com.alphacity.stamptour.web

import kotlinx.browser.window

object WebTokenManager {

    private const val ACCESS_TOKEN_KEY = "access_token"
    private const val REFRESH_TOKEN_KEY = "refresh_token"

    fun saveTokens(
        accessToken: String,
        refreshToken: String,
    ) {
        window.localStorage.setItem(
            ACCESS_TOKEN_KEY,
            accessToken,
        )

        window.localStorage.setItem(
            REFRESH_TOKEN_KEY,
            refreshToken,
        )
    }

    fun getAccessToken(): String? {
        return window.localStorage.getItem(
            ACCESS_TOKEN_KEY
        )
    }

    fun getRefreshToken(): String? {
        return window.localStorage.getItem(
            REFRESH_TOKEN_KEY
        )
    }

    fun isLoggedIn(): Boolean {
        return !getAccessToken().isNullOrBlank()
    }

    fun clear() {
        window.localStorage.removeItem(
            ACCESS_TOKEN_KEY
        )

        window.localStorage.removeItem(
            REFRESH_TOKEN_KEY
        )
    }
}