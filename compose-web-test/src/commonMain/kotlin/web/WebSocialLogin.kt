package com.alphacity.stamptour.web

import kotlin.js.js

private fun jsLoginWithKakao(): Unit =
    js("window.loginWithKakao()")

private fun jsLoginWithNaver(): Unit =
    js("window.loginWithNaver()")

private fun jsGetKakaoAuthorizationCode(): String? =
    js("window.getKakaoAuthorizationCode()")

private fun jsClearSocialLoginData(): Unit =
    js("window.clearSocialLoginData()")

private fun jsGetSocialLoginProvider(): String? =
    js("window.getSocialLoginProvider()")

private fun jsGetSocialLoginToken(): String? =
    js("window.getSocialLoginToken()")

object WebSocialLogin {

    fun loginWithKakao() {
        jsLoginWithKakao()
    }

    fun loginWithNaver() {
        jsLoginWithNaver()
    }

    fun getKakaoAuthorizationCode(): String? {
        return jsGetKakaoAuthorizationCode()
    }

    fun clearSocialLoginData() {
        jsClearSocialLoginData()
    }

    fun getSocialLoginProvider(): String? {
        return jsGetSocialLoginProvider()
    }

    fun getSocialLoginToken(): String? {
        return jsGetSocialLoginToken()
    }
}