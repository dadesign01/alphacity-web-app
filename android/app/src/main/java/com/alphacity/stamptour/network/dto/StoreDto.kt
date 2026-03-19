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

// StoreData → ProgramItem 변환 (ProgramDetailScreen 재사용)
fun StoreData.toProgramItem(): ProgramItem {
    val hours = if (openTime != null && closeTime != null) "$openTime - $closeTime" else null
    val fullAddress = listOfNotNull(address, addressDetail).joinToString(" ")
    return ProgramItem(
        id = -id,
        name = name,
        description = description,
        category = "food",
        imageUrl = imageUrl,
        operatingHours = hours,
        location = fullAddress.ifBlank { null },
        latitude = latitude,
        longitude = longitude,
        startDate = "",
        endDate = "",
        status = "in_progress",
        phone = phone,
        ownerName = ownerName,
        storeCode = storeCode,
        operatingDays = operatingDays,
        storeCoupons = storeCoupons?.map {
            StoreCouponInfo(storeId = id, storeName = name, couponId = it.coupon.id, couponName = it.coupon.name, couponDescription = it.coupon.description)
        },
    )
}
