package com.alphacity.stamptour.repository
import com.alphacity.stamptour.network.ApiService
import com.alphacity.stamptour.network.dto.StoreData

class StoreRepository {

    suspend fun getApprovedStores(): Result<List<StoreData>> {
        return try {
            val response = ApiService.getStores()

            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(
                    Exception(
                        response.error?.message
                            ?: response.message
                            ?: "상점 로드 실패"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}