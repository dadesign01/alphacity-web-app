package com.alphacity.stamptour.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthData(
    val token: String,
    val refreshToken: String,
)