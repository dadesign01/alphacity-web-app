package com.alphacity.stamptour.network

import com.alphacity.stamptour.network.dto.ActivityItemDto
import com.alphacity.stamptour.network.dto.TermItem
import com.alphacity.stamptour.network.dto.ApiResponse
import com.alphacity.stamptour.network.dto.AuthData
import com.alphacity.stamptour.network.dto.LoginRequest
import com.alphacity.stamptour.network.dto.RefreshRequest
import com.alphacity.stamptour.network.dto.RegisterRequest
import com.alphacity.stamptour.network.dto.ResetPasswordRequest
import com.alphacity.stamptour.network.dto.SendCodeRequest
import com.alphacity.stamptour.network.dto.SocialLoginRequest
import com.alphacity.stamptour.network.dto.BannerItem
import com.alphacity.stamptour.network.dto.EventItem
import com.alphacity.stamptour.network.dto.EventParticipationResult
import com.alphacity.stamptour.network.dto.MissionCompletionResult
import com.alphacity.stamptour.network.dto.MissionItem
import com.alphacity.stamptour.network.dto.ProgramItem
import com.alphacity.stamptour.network.dto.CouponItem
import com.alphacity.stamptour.network.dto.MyCouponItem
import com.alphacity.stamptour.network.dto.StampItem
import com.alphacity.stamptour.network.dto.StoreData
import com.alphacity.stamptour.network.dto.StoreRegisterRequest
import com.alphacity.stamptour.network.dto.UserCouponItem
import com.alphacity.stamptour.network.dto.TokenData
import com.alphacity.stamptour.network.dto.UpdateProfileRequest
import com.alphacity.stamptour.network.dto.UserProfile
import com.alphacity.stamptour.network.dto.UserStampItem
import com.alphacity.stamptour.network.dto.VerifyCodeRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // 인증
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<AuthData>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<AuthData>

    @POST("auth/social")
    suspend fun socialLogin(@Body request: SocialLoginRequest): ApiResponse<AuthData>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshRequest): ApiResponse<TokenData>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): ApiResponse<Unit>

    @POST("auth/send-code")
    suspend fun sendCode(@Body request: SendCodeRequest): ApiResponse<Unit>

    @POST("auth/verify-code")
    suspend fun verifyCode(@Body request: VerifyCodeRequest): ApiResponse<Unit>

    @POST("users/verify-password")
    suspend fun verifyPassword(@Body request: Map<String, String>): ApiResponse<Map<String, Boolean>>

    // 배너
    @GET("banners")
    suspend fun getBanners(): ApiResponse<List<BannerItem>>

    // 홈 화면 데이터
    @GET("programs")
    suspend fun getPrograms(): ApiResponse<List<ProgramItem>>

    @GET("programs")
    suspend fun getProgramsByCategory(@Query("category") category: String): ApiResponse<List<ProgramItem>>

    @GET("events")
    suspend fun getEvents(): ApiResponse<List<EventItem>>

    @GET("stamps")
    suspend fun getStamps(): ApiResponse<List<StampItem>>

    @GET("stamps/my")
    suspend fun getUserStamps(): ApiResponse<List<UserStampItem>>

    @GET("users/me")
    suspend fun getUserProfile(): ApiResponse<UserProfile>

    @PUT("users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): ApiResponse<UserProfile>

    @DELETE("users/me")
    suspend fun deleteAccount(): ApiResponse<Unit>

    // 이벤트 상세
    @GET("events/{id}")
    suspend fun getEventDetail(@Path("id") eventId: Int): ApiResponse<EventItem>

    // 이벤트 참여
    @POST("events/{id}/participate")
    suspend fun participateInEvent(@Path("id") eventId: Int): ApiResponse<EventParticipationResult>

    // 프로그램 상세
    @GET("programs/{id}")
    suspend fun getProgramById(@Path("id") programId: Int): ApiResponse<ProgramItem>

    // 프로그램 미션
    @GET("programs/{id}/missions")
    suspend fun getProgramMissions(@Path("id") programId: Int): ApiResponse<List<MissionItem>>

    // 미션 목록
    @GET("missions")
    suspend fun getMissions(): ApiResponse<List<MissionItem>>

    // 미션 완료
    @POST("missions/{id}/complete")
    suspend fun completeMission(@Path("id") missionId: Int, @Body body: Map<String, String> = emptyMap()): ApiResponse<MissionCompletionResult>

    // 쿠폰
    @GET("coupons")
    suspend fun getCoupons(): ApiResponse<List<CouponItem>>

    @GET("coupons")
    suspend fun getMyCoupons(@Query("my") my: Boolean = true): ApiResponse<List<MyCouponItem>>

    @POST("coupons/{id}/redeem")
    suspend fun redeemCoupon(@Path("id") couponId: Int): ApiResponse<UserCouponItem>

    // 상점
    @GET("stores")
    suspend fun getStores(): ApiResponse<List<StoreData>>

    @POST("stores/register")
    suspend fun registerStore(@Body request: StoreRegisterRequest): ApiResponse<StoreData>

    // 활동 이력
    @GET("users/activity")
    suspend fun getActivityHistory(): ApiResponse<List<ActivityItemDto>>

    // 약관
    @GET("terms")
    suspend fun getTerms(@Query("type") type: String): ApiResponse<List<TermItem>>
}
