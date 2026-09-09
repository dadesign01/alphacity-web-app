package com.alphacity.stamptour.repository

import com.alphacity.stamptour.network.ApiService
import com.alphacity.stamptour.network.dto.MissionCompletionResult
import com.alphacity.stamptour.network.dto.MissionItem
import com.alphacity.stamptour.network.dto.StampItem
import com.alphacity.stamptour.network.dto.UserStampItem

class StampRepository(
    private val apiService: ApiService,
) {

    suspend fun getStamps(
        festivalId: Int? = null
    ): Result<List<StampItem>> {
        return try {
            val response = apiService.getStamps(festivalId)

            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(
                    Exception(
                        response.error?.message ?: "스탬프 로드 실패"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMissions(
        festivalId: Int? = null
    ): Result<List<MissionItem>> {
        return try {
            val response = apiService.getMissions(festivalId)

            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(
                    Exception(
                        response.error?.message ?: "미션 로드 실패"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun getProgramMissions(
        programId: Int
    ): Result<List<MissionItem>> {
        return try {
            val response = apiService.getProgramMissions(programId)

            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(
                    Exception(
                        response.error?.message ?: "프로그램 미션 로드 실패"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserStamps(
        festivalId: Int? = null
    ): Result<List<UserStampItem>> {
        return try {
            val response = apiService.getUserStamps(festivalId)

            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(
                    Exception(
                        response.error?.message ?: "유저 스탬프 로드 실패"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completeMission(
        missionId: Int,
        answer: String? = null,
    ): Result<com.alphacity.stamptour.network.dto.MissionCompletionResult> {
        return try {
            val response = apiService.completeMission(
                missionId = missionId,
                answer = answer,
            )

            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(
                    Exception(
                        response.error?.message
                            ?: "미션 완료 실패"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}
