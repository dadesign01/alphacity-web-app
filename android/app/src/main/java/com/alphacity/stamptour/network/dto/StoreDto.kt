package com.alphacity.stamptour.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class StoreRegisterRequest(
    val name: String,
    val category: String,
    val ownerName: String,
    val phone: String,
    val address: String? = null,
    val addressDetail: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val storeCode: String? = null,
    val operatingDays: String? = null,
    val openTime: String? = null,
    val closeTime: String? = null,
)

@Serializable
data class StoreData(
    val id: Int,
    val name: String,
    val category: String,
    val ownerName: String,
    val phone: String,
    val address: String? = null,
    val addressDetail: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val storeCode: String? = null,
    val operatingDays: String? = null,
    val openTime: String? = null,
    val closeTime: String? = null,
    val status: String,
)
