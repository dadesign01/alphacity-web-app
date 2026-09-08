package com.alphacity.stamptour.network.dto

import kotlinx.serialization.Serializable
@Serializable
data class SendCodeRequest(
    val phone: String,
)
@Serializable
data class SendCodeData(
    val codeSent: Boolean,
)
@Serializable
data class PhoneLoginRequest(
    val phone: String,
    val code: String,
)
@Serializable
data class PhoneLoginData(
    val token: String,
    val refreshToken: String,
    val user: PhoneLoginUser,
)
@Serializable
data class PhoneLoginUser(
    val id: Int,
    val email: String,
    val nickname: String? = null,
    val phone: String? = null,
)