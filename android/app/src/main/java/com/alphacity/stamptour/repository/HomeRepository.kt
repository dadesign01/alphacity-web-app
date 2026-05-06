package com.alphacity.stamptour.repository

import com.alphacity.stamptour.network.ApiService
import com.alphacity.stamptour.network.TokenManager
import com.alphacity.stamptour.network.dto.BannerItem
import com.alphacity.stamptour.network.dto.EventItem
import com.alphacity.stamptour.network.dto.FestivalDetail
import com.alphacity.stamptour.network.dto.FestivalItem
import com.alphacity.stamptour.network.dto.EventParticipationResult
import com.alphacity.stamptour.network.dto.MissionCompletionResult
import com.alphacity.stamptour.network.dto.MissionItem
import com.alphacity.stamptour.network.dto.ProgramItem
import com.alphacity.stamptour.network.dto.StampItem
import com.alphacity.stamptour.network.dto.TermItem
import com.alphacity.stamptour.network.dto.UpdateProfileRequest
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

    suspend fun getFestivals(): Result<List<FestivalItem>> {
        return try {
            val response = apiService.getFestivals()
            if (response.success && response.data != null) Result.success(response.data)
            else Result.failure(Exception(response.error?.message ?: "축제 로드 실패"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFestivalDetail(festivalId: Int): Result<FestivalDetail> {
        return try {
            val response = apiService.getFestivalDetail(festivalId)
            if (response.success && response.data != null) Result.success(response.data)
            else Result.failure(Exception(response.error?.message ?: "축제 상세 로드 실패"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPrograms(festivalId: Int? = null, category: String? = null): Result<List<ProgramItem>> {
        return try {
            val response = apiService.getPrograms(festivalId, category)
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

    suspend fun getEvents(festivalId: Int? = null): Result<List<EventItem>> {
        return try {
            val response = apiService.getEvents(festivalId)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error?.message ?: "이벤트 로드 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStamps(festivalId: Int? = null): Result<List<StampItem>> {
        return try {
            val response = apiService.getStamps(festivalId)
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

    suspend fun updateProfile(nickname: String, currentPassword: String?, newPassword: String?, phone: String? = null, name: String? = null, address: String? = null, addressDetail: String? = null, birthDate: String? = null, gender: String? = null): Result<UserProfile> {
        return try {
            val request = UpdateProfileRequest(nickname = nickname, name = name, currentPassword = currentPassword?.ifBlank { null }, newPassword = newPassword?.ifBlank { null }, phone = phone, address = address, addressDetail = addressDetail, birthDate = birthDate, gender = gender)
            val response = apiService.updateProfile(request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error?.message ?: "프로필 저장 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val response = apiService.deleteAccount()
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.error?.message ?: "회원 탈퇴 실패"))
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

    suspend fun completeMission(missionId: Int, answer: String? = null): Result<MissionCompletionResult> {
        return try {
            val body = if (answer != null) mapOf("answer" to answer) else emptyMap()
            val response = apiService.completeMission(missionId, body)
            if (response.success && response.data != null) Result.success(response.data)
            else Result.failure(Exception(response.error?.message ?: "미션 완료 실패"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTerms(type: String): Result<List<TermItem>> {
        return try {
            val response = apiService.getTerms(type)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error?.message ?: "약관 로드 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    val isLoggedIn: Boolean
        get() = tokenManager.isLoggedIn
}
