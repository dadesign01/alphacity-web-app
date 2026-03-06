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
    val imageUrl: String? = null,
    val operatingHours: String? = null,
    val location: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val speaker: String? = null,
    val startDate: String,
    val endDate: String,
    val status: String,
    val events: List<EventItem>? = null,
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
    val participantLimit: Int = 0,
    val participantCount: Int = 0,
    val status: String,
    val program: EventProgram? = null,
    val isParticipated: Boolean? = null,
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

// === 유저 프로필 ===

@Serializable
data class UserProfile(
    val id: Int,
    val email: String,
    val nickname: String,
    val profileImage: String? = null,
    val stampCount: Int? = null,
    val couponCount: Int? = null,
    val missionCount: Int? = null,
)
