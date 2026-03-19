package com.alphacity.stamptour.network.dto

import kotlinx.serialization.Serializable

// === 요청 ===

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val nickname: String,
    val phone: String? = null,
    val name: String? = null,
)

@Serializable
data class SocialLoginRequest(
    val provider: String,
    val accessToken: String,
)

@Serializable
data class RefreshRequest(
    val refreshToken: String,
)

@Serializable
data class ResetPasswordRequest(
    val email: String,
    val newPassword: String,
)

@Serializable
data class SendCodeRequest(
    val phone: String,
)

@Serializable
data class VerifyCodeRequest(
    val phone: String,
    val code: String,
)

// === 응답 ===

@Serializable
data class AuthData(
    val token: String,
    val refreshToken: String,
    val user: UserData,
)

@Serializable
data class UserData(
    val id: Int,
    val email: String,
    val nickname: String,
    val profileImage: String? = null,
)

@Serializable
data class TokenData(
    val token: String,
    val refreshToken: String,
)
