package com.alphacity.stamptour.repository

import com.alphacity.stamptour.network.ApiService
import com.alphacity.stamptour.network.TokenManager
import com.alphacity.stamptour.network.dto.BannerItem
import com.alphacity.stamptour.network.dto.EventItem
import com.alphacity.stamptour.network.dto.EventParticipationResult
import com.alphacity.stamptour.network.dto.MissionItem
import com.alphacity.stamptour.network.dto.ProgramItem
import com.alphacity.stamptour.network.dto.StampItem
import com.alphacity.stamptour.network.dto.UserProfile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
) {
    suspend fun getBanners(): Result<List<BannerItem>> {
        return try {
            val response = apiService.getBanners()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error?.message ?: "배너 로드 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPrograms(): Result<List<ProgramItem>> {
        return try {
            val response = apiService.getPrograms()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error?.message ?: "프로그램 로드 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProgramsByCategory(category: String): Result<List<ProgramItem>> {
        return try {
            val response = apiService.getProgramsByCategory(category)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error?.message ?: "프로그램 로드 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEvents(): Result<List<EventItem>> {
        return try {
            val response = apiService.getEvents()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error?.message ?: "이벤트 로드 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

    suspend fun getEventDetail(eventId: Int): Result<EventItem> {
        return try {
            val response = apiService.getEventDetail(eventId)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error?.message ?: "이벤트 로드 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun participateInEvent(eventId: Int): Result<EventParticipationResult> {
        return try {
            val response = apiService.participateInEvent(eventId)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error?.message ?: "참여 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProgramById(programId: Int): Result<ProgramItem> {
        return try {
            val response = apiService.getProgramById(programId)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error?.message ?: "프로그램 로드 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProgramMissions(programId: Int): Result<List<MissionItem>> {
        return try {
            val response = apiService.getProgramMissions(programId)
            if (response.success && response.data != null) Result.success(response.data)
            else Result.failure(Exception(response.error?.message ?: "미션 로드 실패"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    val isLoggedIn: Boolean
        get() = tokenManager.isLoggedIn
}
