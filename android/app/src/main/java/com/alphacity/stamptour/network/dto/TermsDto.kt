package com.alphacity.stamptour.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class TermItem(
    val id: Int,
    val type: String,
    val title: String,
    val content: String,
    val version: String = "1.0",
    val isActive: Boolean = true,
)
