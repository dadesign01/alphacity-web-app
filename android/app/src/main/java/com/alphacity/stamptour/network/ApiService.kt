package com.alphacity.stamptour.network

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
import com.alphacity.stamptour.network.dto.MissionItem
import com.alphacity.stamptour.network.dto.ProgramItem
import com.alphacity.stamptour.network.dto.StampItem
import com.alphacity.stamptour.network.dto.StoreData
import com.alphacity.stamptour.network.dto.StoreRegisterRequest
import com.alphacity.stamptour.network.dto.TokenData
import com.alphacity.stamptour.network.dto.UserProfile
import com.alphacity.stamptour.network.dto.UserStampItem
import com.alphacity.stamptour.network.dto.VerifyCodeRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
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

    // 이벤트 참여
    @POST("events/{id}/participate")
    suspend fun participateInEvent(@Path("id") eventId: Int): ApiResponse<Unit>

    // 프로그램 상세
    @GET("programs/{id}")
    suspend fun getProgramById(@Path("id") programId: Int): ApiResponse<ProgramItem>

    // 프로그램 미션
    @GET("programs/{id}/missions")
    suspend fun getProgramMissions(@Path("id") programId: Int): ApiResponse<List<MissionItem>>

    // 상점 등록
    @POST("stores/register")
    suspend fun registerStore(@Body request: StoreRegisterRequest): ApiResponse<StoreData>
}
