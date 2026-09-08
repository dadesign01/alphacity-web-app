package com.alphacity.stamptour.repository

import com.alphacity.stamptour.network.ApiService
import com.alphacity.stamptour.network.dto.MissionItem
import com.alphacity.stamptour.network.dto.ProgramItem

class ProgramDetailRepository(
    private val apiService: ApiService,
) {

    suspend fun getProgramById(
        programId: Int,
    ): Result<ProgramItem> {
        return try {
            val response = apiService.getProgramById(programId)

            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(
                    Exception(
                        response.error?.message ?: "프로그램 로드 실패"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProgramMissions(
        programId: Int,
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
}