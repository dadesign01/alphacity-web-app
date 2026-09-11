package com.alphacity.stamptour.network.dto
import kotlinx.serialization.Serializable

@Serializable
data class BannerItem(
    val id: Int,
    val title: String,
    val imageUrl: String,
    val linkType: String? = null,
    val linkId: Int? = null,
)
@Serializable
data class FestivalItem(
    val id: Int,
    val name: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val bannerUrl: String? = null,
    val startDate: String,
    val endDate: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null,
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
    val status: String,
)
@Serializable
data class FestivalDetail(
    val id: Int,
    val name: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val bannerUrl: String? = null,
    val startDate: String,
    val endDate: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null,
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
    val status: String,
    val programs: List<FestivalProgramSummary> = emptyList(),
)
@Serializable
data class FestivalProgramSummary(
    val id: Int,
    val name: String,
    val category: String? = null,
    val imageUrl: String? = null,
    val startDate: String,
    val endDate: String,
    val status: String? = null,
    val location: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
)
@Serializable
data class ProgramItem(
    val id: Int,
    val festivalId: Int? = null,
    val name: String,
    val description: String? = null,
    val category: String? = null,
    val subcategory: String? = null,
    val hasCoupon: Boolean? = false,
    val ttsUrl: String? = null,
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
    val ownerName: String? = null,
    val storeCode: String? = null,
    val operatingDays: String? = null,
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
@Serializable
data class StampItem(
    val id: Int,
    val festivalId: Int? = null,
    val programId: Int? = null,
    val name: String,
    val conditionType: String,
    val conditionDetail: String? = null,
    val imageUrl: String? = null,
)
@Serializable
data class UserStampItem(
    val id: Int,
    val stampId: Int,
    val collectedAt: String? = null,
    val stamp: StampItem? = null,
)
@Serializable
data class MissionItem(
    val id: Int,
    val festivalId: Int? = null,
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

@Serializable
data class CouponListResponse(
    val coupons: List<CouponItem>,
    val redeemedCouponIds: List<Int> = emptyList(),
    val availableStamps: Int = 0,
)

@Serializable
data class CouponItem(
    val id: Int,
    val festivalId: Int? = null,
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

@Serializable
data class MyCouponItem(
    val id: Int,
    val couponId: Int,
    val name: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val code: String,
    val status: String,
    val validUntil: String,
    val storeName: String? = null,
    val requestedAt: String? = null,
    val usedAt: String? = null,
    val createdAt: String? = null,
)

@Serializable
data class UserProfile(
    val id: Int,
    val email: String,
    val nickname: String,
    val name: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val addressDetail: String? = null,
    val profileImage: String? = null,
    val provider: String? = null,
    val birthDate: String? = null,
    val gender: String? = null,
    val stampCount: Int? = null,
    val couponCount: Int? = null,
    val missionCount: Int? = null,
)

@Serializable
data class UpdateProfileRequest(
    val nickname: String,
    val name: String? = null,
    val currentPassword: String? = null,
    val newPassword: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val addressDetail: String? = null,
    val birthDate: String? = null,
    val gender: String? = null,
    val profileImage: String? = null,
)

@Serializable
data class ActivityItemDto(
    val type: String,
    val subType: String? = null,
    val title: String,
    val date: String,
    val validUntil: String? = null,
)