package com.alphacity.stamptour.repository

import com.alphacity.stamptour.network.ApiService
import com.alphacity.stamptour.network.dto.ActivityItemDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityHistoryRepository @Inject constructor(
    private val apiService: ApiService,
) {
    suspend fun getActivityHistory(): Result<List<ActivityItemDto>> {
        return try {
            val response = apiService.getActivityHistory()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error?.message ?: "활동 이력 로드 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
