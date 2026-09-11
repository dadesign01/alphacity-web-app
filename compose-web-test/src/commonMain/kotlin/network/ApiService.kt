package com.alphacity.stamptour.network

import com.alphacity.stamptour.network.dto.ActivityItemDto
import com.alphacity.stamptour.network.dto.ApiResponse
import com.alphacity.stamptour.network.dto.BannerItem
import com.alphacity.stamptour.network.dto.CouponListResponse
import com.alphacity.stamptour.network.dto.EventItem
import com.alphacity.stamptour.network.dto.EventParticipationResult
import com.alphacity.stamptour.network.dto.FestivalDetail
import com.alphacity.stamptour.network.dto.FestivalItem
import com.alphacity.stamptour.network.dto.MissionCompletionResult
import com.alphacity.stamptour.network.dto.MissionItem
import com.alphacity.stamptour.network.dto.MyCouponItem
import com.alphacity.stamptour.network.dto.ProgramItem
import com.alphacity.stamptour.network.dto.StampItem
import com.alphacity.stamptour.network.dto.StoreData
import com.alphacity.stamptour.network.dto.UpdateProfileRequest
import com.alphacity.stamptour.network.dto.UserCouponItem
import com.alphacity.stamptour.network.dto.UserProfile
import com.alphacity.stamptour.network.dto.UserStampItem
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import com.alphacity.stamptour.network.dto.TermItem
import com.alphacity.stamptour.network.dto.SocialLoginRequest
import com.alphacity.stamptour.network.dto.AuthData
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put


object ApiService {
    private val client = ApiClient.client

// =========================
// 배너
// =========================

    suspend fun getBanners(): ApiResponse<List<BannerItem>> {
        return client.get("banners").body()
    }

// =========================
// 축제
// =========================

    suspend fun getFestivals(): ApiResponse<List<FestivalItem>> {
        return client.get("festivals").body()
    }

    suspend fun getFestivalDetail(
        festivalId: Int
    ): ApiResponse<FestivalDetail> {
        return client.get("festivals/$festivalId").body()
    }

// =========================
// 프로그램
// =========================

    suspend fun getPrograms(
        festivalId: Int? = null,
        category: String? = null,
    ): ApiResponse<List<ProgramItem>> {

        val params = buildList {
            festivalId?.let {
                add("festivalId=$it")
            }

            category?.let {
                add("category=$it")
            }
        }

        val url = if (params.isEmpty()) {
            "programs"
        } else {
            "programs?${params.joinToString("&")}"
        }

        return client.get(url).body()
    }

    suspend fun getProgramsByCategory(
        category: String
    ): ApiResponse<List<ProgramItem>> {
        return client.get("programs?category=$category").body()
    }

    suspend fun getProgramById(
        programId: Int
    ): ApiResponse<ProgramItem> {
        return client.get("programs/$programId").body()
    }


    suspend fun getProgramMissions(
        programId: Int
    ): ApiResponse<List<MissionItem>> {
        return client.get("programs/$programId/missions").body()
    }

    suspend fun getProgramStampMissions(
        stampId: Int
    ): ApiResponse<List<MissionItem>> {
        return client.get("stamps/$stampId/missions").body()
    }

// =========================
// 이벤트
// =========================

    suspend fun getEvents(
        festivalId: Int? = null
    ): ApiResponse<List<EventItem>> {

        val url = if (festivalId != null) {
            "events?festivalId=$festivalId"
        } else {
            "events"
        }

        return client.get(url).body()
    }

    suspend fun getEventDetail(
        eventId: Int
    ): ApiResponse<EventItem> {
        return client.get("events/$eventId").body()
    }

    suspend fun participateInEvent(
        eventId: Int
    ): ApiResponse<EventParticipationResult> {
        return client.post("events/$eventId/participate").body()
    }

// =========================
// 스탬프
// =========================

    suspend fun getStamps(
        festivalId: Int? = null
    ): ApiResponse<List<StampItem>> {

        val url = if (festivalId != null) {
            "stamps?festivalId=$festivalId"
        } else {
            "stamps"
        }

        return client.get(url).body()
    }

    suspend fun getUserStamps(
        festivalId: Int? = null
    ): ApiResponse<List<UserStampItem>> {

        val url = if (festivalId != null) {
            "stamps/my?festivalId=$festivalId"
        } else {
            "stamps/my"
        }

        return client.get(url).body()
    }

// =========================
// 미션
// =========================

    suspend fun getMissions(
        festivalId: Int? = null
    ): ApiResponse<List<MissionItem>> {

        val url = if (festivalId != null) {
            "missions?festivalId=$festivalId"
        } else {
            "missions"
        }

        return client.get(url).body()
    }

    suspend fun completeMission(
        missionId: Int,
        answer: String? = null,
    ): ApiResponse<MissionCompletionResult> {

        return if (answer == null) {
            client.post("missions/$missionId/complete").body()
        } else {
            client.post("missions/$missionId/complete") {
                setBody(
                    buildJsonObject {
                        put("answer", answer)
                    }
                )
            }.body()
        }
    }

// =========================
// 쿠폰
// =========================

    suspend fun getCoupons(
        festivalId: Int? = null
    ): ApiResponse<CouponListResponse> {

        val url = if (festivalId != null) {
            "coupons?festivalId=$festivalId"
        } else {
            "coupons"
        }

        return client.get(url).body()
    }

    suspend fun getMyCoupons(
        festivalId: Int? = null
    ): ApiResponse<List<MyCouponItem>> {

        val params = buildList {
            add("my=true")

            festivalId?.let {
                add("festivalId=$it")
            }
        }

        return client
            .get("coupons?${params.joinToString("&")}")
            .body()
    }

    suspend fun redeemCoupon(
        couponId: Int
    ): ApiResponse<UserCouponItem> {
        return client
            .post("coupons/$couponId/redeem")
            .body()
    }

    suspend fun useCoupon(
        userCouponId: Int
    ): ApiResponse<UserCouponItem> {
        return client.post("coupons/use") {
            setBody(
                mapOf(
                    "userCouponId" to userCouponId
                )
            )
        }.body()
    }

// =========================
// 사용자
// =========================

    // =========================
// 인증
// =========================

    suspend fun socialLogin(
        provider: String,
        accessToken: String? = null,
        code: String? = null,
    ): ApiResponse<AuthData> {
        return client.post("auth/social") {
            setBody(
                SocialLoginRequest(
                    provider = provider,
                    accessToken = accessToken,
                    code = code,
                )
            )
        }.body()
    }

    suspend fun getUserProfile(): ApiResponse<UserProfile> {
        return client.get("users/me").body()
    }

    suspend fun checkFirstComeCouponPopup(): ApiResponse<Boolean> {
        return client.get("users/me/first-come-popup").body()
    }

    suspend fun updateProfile(
        request: UpdateProfileRequest
    ): ApiResponse<UserProfile> {
        return client.put("users/me") {
            setBody(request)
        }.body()
    }

    suspend fun deleteAccount(): ApiResponse<Unit> {
        return client.delete("users/me").body()
    }

// =========================
// 활동 이력
// =========================

    suspend fun getActivityHistory(): ApiResponse<List<ActivityItemDto>> {
        return client.get("users/activity").body()
    }


// 가맹점 목록
    suspend fun getStores(festivalId: Int? = null): ApiResponse<List<StoreData>> {
        val url = if (festivalId != null) {
            "stores?festivalId=$festivalId"
        } else {
            "stores"
        }

        return client.get(url).body()
    }

    suspend fun getTerms(type: String): ApiResponse<List<TermItem>> {
        return client.get("terms?type=$type").body()
    }

}
