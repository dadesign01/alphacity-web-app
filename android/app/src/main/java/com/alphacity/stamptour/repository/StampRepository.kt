package com.alphacity.stamptour.repository

import com.alphacity.stamptour.network.ApiService
import com.alphacity.stamptour.network.TokenManager
import com.alphacity.stamptour.network.dto.CouponItem
import com.alphacity.stamptour.network.dto.MissionItem
import com.alphacity.stamptour.network.dto.MyCouponItem
import com.alphacity.stamptour.network.dto.StampItem
import com.alphacity.stamptour.network.dto.UserCouponItem
import com.alphacity.stamptour.network.dto.UserProfile
import com.alphacity.stamptour.network.dto.UserStampItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StampRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
) {
    suspend fun getStamps(): Result<List<StampItem>> {
        return try {
            val response = apiService.getStamps()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error?.message ?: "스탬프 로드 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserStamps(): Result<List<UserStampItem>> {
        return try {
            val response = apiService.getUserStamps()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error?.message ?: "유저 스탬프 로드 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(): Result<UserProfile> {
        return try {
            val response = apiService.getUserProfile()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error?.message ?: "프로필 로드 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMissions(): Result<List<MissionItem>> {
        return try {
            val response = apiService.getMissions()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error?.message ?: "미션 로드 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCoupons(): Result<List<CouponItem>> {
        return try {
            val response = apiService.getCoupons()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error?.message ?: "쿠폰 로드 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun redeemCoupon(couponId: Int): Result<UserCouponItem> {
        return try {
            val response = apiService.redeemCoupon(couponId)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error?.message ?: "쿠폰 교환 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyCoupons(): Result<List<MyCouponItem>> {
        return try {
            val response = apiService.getMyCoupons()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error?.message ?: "내 쿠폰 로드 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    val isLoggedIn: Boolean
        get() = tokenManager.isLoggedIn
}
