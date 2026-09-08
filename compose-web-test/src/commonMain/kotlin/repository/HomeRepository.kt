package com.alphacity.stamptour.repository

import com.alphacity.stamptour.network.ApiService
import com.alphacity.stamptour.network.dto.FestivalDetail
import com.alphacity.stamptour.network.dto.FestivalItem
import com.alphacity.stamptour.network.dto.MissionItem
import com.alphacity.stamptour.network.dto.ProgramItem
import com.alphacity.stamptour.network.dto.StampItem
import com.alphacity.stamptour.network.dto.UserStampItem

class HomeRepository {

    suspend fun getPrograms(
        festivalId: Int? = null,
        category: String? = null,
    ): Result<List<ProgramItem>> {
        return try {
            val response = ApiService.getPrograms(
                festivalId = festivalId,
                category = category,
            )

            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(
                    Exception(
                        response.error?.message
                            ?: response.message
                            ?: "프로그램 로드 실패"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProgramById(
        programId: Int,
    ): Result<ProgramItem> {
        return try {
            val response = ApiService.getProgramById(programId)

            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(
                    Exception(
                        response.error?.message
                            ?: response.message
                            ?: "프로그램 로드 실패"
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
            val response = ApiService.getProgramMissions(programId)

            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(
                    Exception(
                        response.error?.message
                            ?: response.message
                            ?: "미션 로드 실패"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMissions(
        festivalId: Int? = null,
    ): Result<List<MissionItem>> {
        return try {
            val response = ApiService.getMissions(festivalId)

            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(
                    Exception(
                        response.error?.message
                            ?: response.message
                            ?: "미션 로드 실패"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFestivals(): Result<List<FestivalItem>> {
        return try {
            val response = ApiService.getFestivals()

            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(
                    Exception(
                        response.error?.message
                            ?: response.message
                            ?: "축제 로드 실패"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFestivalDetail(
        festivalId: Int,
    ): Result<FestivalDetail> {
        return try {
            val response = ApiService.getFestivalDetail(festivalId)

            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(
                    Exception(
                        response.error?.message
                            ?: response.message
                            ?: "축제 상세 로드 실패"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 전체 스탬프 조회
     *
     * GET /api/v1/stamps
     */
    suspend fun getStamps(
        festivalId: Int? = null,
    ): Result<List<StampItem>> {
        return try {
            val response = ApiService.getStamps(festivalId)

            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(
                    Exception(
                        response.error?.message
                            ?: response.message
                            ?: "스탬프 목록 로드 실패"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 내가 획득한 스탬프 조회
     *
     * GET /api/v1/stamps/my
     */
    suspend fun getUserStamps(
        festivalId: Int? = null,
    ): Result<List<UserStampItem>> {
        return try {
            val response = ApiService.getUserStamps(festivalId)

            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(
                    Exception(
                        response.error?.message
                            ?: response.message
                            ?: "내 스탬프 로드 실패"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}