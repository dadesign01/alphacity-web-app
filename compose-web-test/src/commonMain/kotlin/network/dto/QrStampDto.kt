package com.alphacity.stamptour.network.dto
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ValidateStampResponse(
    val success: Boolean,
    val data: ValidateStampData? = null,
    val error: ValidateStampError? = null,
    val message: String? = null,
)
@Serializable
data class ValidateStampData(
    val valid: Boolean,
    val programId: Int,
    val stampId: Int,
    val qrCode: String,
)
@Serializable
data class ValidateStampError(
    val code: String? = null,
    val message: String? = null,
)
@Serializable
data class CollectStampRequest(
    val qrCode: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
)
@Serializable
data class CollectStampResponse(
    val success: Boolean,
    val data: JsonElement? = null,
    val error: CollectStampError? = null,
    val message: String? = null,
)
@Serializable
data class CollectStampError(
    val code: String? = null,
    val message: String? = null,
)