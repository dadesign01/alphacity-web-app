package com.alphacity.stamptour.network.dto

import kotlinx.serialization.Serializable

// === 배너 ===

@Serializable
data class BannerItem(
    val id: Int,
    val title: String,
    val imageUrl: String,
)

// === 프로그램 ===

@Serializable
data class ProgramItem(
    val id: Int,
    val name: String,
    val description: String? = null,
    val category: String? = null,
    val subcategory: String? = null,
    val hasCoupon: Boolean? = false,
    val imageUrl: String? = null,
    val operatingHours: String? = null,
    val location: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val speaker: String? = null,
    val startDate: String,
    val endDate: String,
    val status: String,
    val phone: String? = null,
    val events: List<EventItem>? = null,
    val storeCoupons: List<StoreCouponInfo>? = null,
    val stores: List<ProgramStoreItem>? = null,
)

@Serializable
data class StoreCouponInfo(
    val storeId: Int,
    val storeName: String,
    val couponId: Int,
    val couponName: String,
    val couponDescription: String? = null,
)

@Serializable
data class ProgramStoreItem(
    val id: Int,
    val name: String,
)

// === 이벤트 ===

@Serializable
data class EventItem(
    val id: Int,
    val name: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val type: String,
    val startDate: String,
    val endDate: String,
    val reward: String? = null,
    val winnerCount: Int? = null,
    val participantLimit: Int = 0,
    val participantCount: Int = 0,
    val status: String,
    val program: EventProgram? = null,
    val isParticipated: Boolean? = null,
    val myRaffleNumber: Int? = null,
    val price: Int? = null,
    val duration: Int? = null,
    val capacity: Int? = null,
    val location: String? = null,
)

@Serializable
data class EventParticipationResult(
    val id: Int,
    val eventId: Int,
    val userId: Int,
    val raffleNumber: Int? = null,
    val joinedAt: String,
)

@Serializable
data class EventProgram(
    val name: String,
)

// === 스탬프 ===

@Serializable
data class StampItem(
    val id: Int,
    val name: String,
    val conditionType: String,
    val conditionDetail: String? = null,
    val imageUrl: String? = null,
)

// === 유저 스탬프 수집 기록 ===

@Serializable
data class UserStampItem(
    val id: Int,
    val stampId: Int,
    val collectedAt: String? = null,
)

// === 미션 ===

@Serializable
data class MissionItem(
    val id: Int,
    val name: String,
    val type: String,
    val placeId: Int? = null,
    val programId: Int? = null,
    val stampId: Int? = null,
    val question: String? = null,
    val answer: String? = null,
    val options: List<String>? = null,
    val stayMinutes: Int? = null,
    val place: MissionPlace? = null,
    val isCompleted: Boolean? = null,
)

@Serializable
data class MissionCompletionResult(
    val id: Int,
    val missionId: Int,
    val userId: Int,
    val completedAt: String? = null,
    val stamp: StampItem? = null,
)

@Serializable
data class MissionPlace(
    val name: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

// === 쿠폰 ===

@Serializable
data class CouponItem(
    val id: Int,
    val name: String,
    val description: String? = null,
    val requiredStamps: Int,
    val validUntil: String,
    val imageUrl: String? = null,
    val programId: Int? = null,
)

@Serializable
data class UserCouponItem(
    val id: Int,
    val userId: Int,
    val couponId: Int,
    val code: String,
    val status: String,
    val storeId: Int? = null,
    val requestedAt: String? = null,
    val usedAt: String? = null,
    val createdAt: String? = null,
)

// === 유저 프로필 ===

@Serializable
data class UserProfile(
    val id: Int,
    val email: String,
    val nickname: String,
    val phone: String? = null,
    val profileImage: String? = null,
    val provider: String? = null,
    val stampCount: Int? = null,
    val couponCount: Int? = null,
    val missionCount: Int? = null,
)

@Serializable
data class UpdateProfileRequest(
    val nickname: String,
    val password: String? = null,
    val phone: String? = null,
)
