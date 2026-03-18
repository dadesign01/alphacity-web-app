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
    val latitude: Double? = null,
    val longitude: Double? = null,
    val status: String,
    val mission: StoreMissionInfo? = null,
    val storeCoupons: List<StoreCouponDetail>? = null,
    val program: StoreProgramInfo? = null,
)

@Serializable
data class StoreMissionInfo(
    val id: Int,
    val name: String,
    val type: String,
)

@Serializable
data class StoreCouponDetail(
    val coupon: StoreCouponData,
)

@Serializable
data class StoreCouponData(
    val id: Int,
    val name: String,
    val description: String? = null,
    val imageUrl: String? = null,
)

@Serializable
data class StoreProgramInfo(
    val id: Int,
    val name: String,
)
