package com.alphacity.stamptour.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class SocialLoginRequest(
    val provider: String,
    val accessToken: String? = null,
    val code: String? = null,
)